// VIVA GUIDE: Local staff preview utility; inspect its configuration and caller before treating it as the cloud deployment entry point.
import http from 'node:http';

// Serve the teammate's JSP UI through Tomcat, with its assets and API on one origin.
const port = Number(process.env.STAFF_PORT || 5175);
// Validate the configured HTTPS backend origin before generating API rewrites; credentials/path must not be embedded.
const backend = new URL(process.env.QUEUE_BACKEND_URL || 'http://127.0.0.1:8081');
if (backend.protocol !== 'http:') throw new Error('Local backend must use HTTP.');

http.createServer((request, response) => {
  if (request.url === '/') {
    response.writeHead(302, { Location: '/staff/login.jsp' });
    response.end();
    return;
  }
  const upstream = http.request({
    hostname: backend.hostname,
    port: backend.port || 80,
    path: request.url,
    method: request.method,
    headers: { ...request.headers, host: backend.host },
  }, result => {
    const headers = { ...result.headers };
    if (headers.location?.startsWith(backend.origin)) {
      headers.location = headers.location.slice(backend.origin.length) || '/';
    }
    response.writeHead(result.statusCode, headers);
    result.pipe(response);
  });
  upstream.on('error', () => {
    if (!response.headersSent) {
      response.writeHead(502, { 'Content-Type': 'text/plain; charset=utf-8' });
      response.end('Tomcat is unavailable. Run scripts/start-backend.ps1 first.');
    } else response.destroy();
  });
  request.on('aborted', () => upstream.destroy());
  request.pipe(upstream);
}).listen(port, '127.0.0.1', () => {
  console.log(`Staff portal: http://localhost:${port}/ (backend ${backend.origin})`);
}).on('error', error => {
  console.error(error.message);
  process.exitCode = 1;
});
