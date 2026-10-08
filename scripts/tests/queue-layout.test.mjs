// VIVA GUIDE: Explicit regression/fixture tool: inspect assertions and cleanup; database checks use their configured database. Standalone test or diagnostic program. Read fixture setup, assertions and cleanup; do not assume Maven package executes this file.
import assert from "node:assert/strict";
import { queueLayout } from "../../QueueSystemFrontend/src/queue-layout.js";

for (const count of [0, 1, 3, 4, 5, 25, 26, 100, 1000, 10000]) {
  const layout = queueLayout(count);
  assert.equal(layout.people.length + layout.hidden, count, "Every person ahead is represented individually or in the group");
  assert.ok(layout.people.every((x) => x > layout.you && x < 411), "People remain between YOU and the desk");
  assert.ok(layout.you >= 0 && layout.you < 411, "Student remains in the illustration bounds");
  if (layout.grouped) {
    assert.ok(layout.you + 36 < 111 && Math.min(...layout.people) - 23 > 186, "Group badge fits between the student and visible people");
  }
  if (count > 0) assert.ok(queueLayout(count - 1).you >= layout.you, "Advancing the queue never moves the student away from the desk");
}
assert.equal(queueLayout(25).hidden, 22);
assert.equal(queueLayout(25).people.length, 3);
console.log("Queue representation and direction verified for 0–10,000 people ahead.");
