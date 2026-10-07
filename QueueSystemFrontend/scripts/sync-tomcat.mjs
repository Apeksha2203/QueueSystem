import {
  cpSync,
  existsSync,
  mkdirSync,
  readFileSync,
  writeFileSync,
  statSync,
} from "node:fs";
import { fileURLToPath } from "node:url";
import path from "node:path";
const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const dist = process.argv[2] ? path.resolve(process.argv[2]) : path.join(root, "dist");
const project = existsSync(path.resolve(root, "../pom.xml"))
  ? ".."
  : "../QueueSystem";
const webapp = path.resolve(root, project, "src/main/webapp");
mkdirSync(path.join(webapp, "ui"), { recursive: true });
cpSync(dist, path.join(webapp, "ui"), {
  recursive: true,
  // Leave identical assets alone, including fonts held open by the preview.
  filter: (source, destination) =>
    !existsSync(destination) || statSync(source).isDirectory() ||
    !readFileSync(source).equals(readFileSync(destination)),
});
const template = readFileSync(
  path.join(dist, "index.html"),
  "utf8",
).replaceAll('"./', '"../ui/');
for (const page of ["dashboard", "queue", "bookings", "settings", "login"]) {
  const boot = `<script>if(!location.hash){location.replace(location.pathname+location.search+'#/${page}'+location.search)}</script>`;
  writeFileSync(
    path.join(webapp, "student", `${page}.html`),
    template.replace("<head>", `<head>${boot}`),
  );
}
console.log("Synced shared frontend to Tomcat ui/ and student entry pages.");
