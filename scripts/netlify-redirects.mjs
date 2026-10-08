// VIVA GUIDE: Build-time HTTPS backend-origin validation and API-first rewrite generation, followed by SPA route fallback.
import { writeFileSync } from 'node:fs';

// Validate the configured HTTPS backend origin before generating API rewrites; credentials/path must not be embedded.
const backend = new URL(process.env.QUEUE_BACKEND_URL || '');
if (backend.protocol !== 'https:' || backend.username || backend.password || backend.pathname !== '/') {
  throw new Error('Set QUEUE_BACKEND_URL to the HTTPS origin of the Render backend, without credentials or a path.');
}
// Write the API proxy before the SPA fallback so API requests never resolve to index.html.
writeFileSync('dist/_redirects', `/api/* ${backend.origin}/api/:splat 200!\n/* /index.html 200\n`);
console.log('Netlify API proxy and app routing configured.');
