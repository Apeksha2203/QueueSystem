import { writeFileSync } from 'node:fs';

const backend = new URL(process.env.QUEUE_BACKEND_URL || '');
if (backend.protocol !== 'https:' || backend.username || backend.password || backend.pathname !== '/') {
  throw new Error('Set QUEUE_BACKEND_URL to the HTTPS origin of the Render backend, without credentials or a path.');
}
writeFileSync('dist/_redirects', `/api/* ${backend.origin}/api/:splat 200!\n/* /index.html 200\n`);
console.log('Netlify API proxy and app routing configured.');
