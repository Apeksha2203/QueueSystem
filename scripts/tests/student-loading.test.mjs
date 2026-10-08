// Regression: a slow/failed personal endpoint must not hide a successfully loaded service catalogue.
import assert from 'node:assert/strict';
import fs from 'node:fs';
import vm from 'node:vm';
import { createRequire } from 'node:module';
const require = createRequire(new URL('../../QueueSystemFrontend/package.json', import.meta.url));
const { transformSync } = require('esbuild');
const source = fs.readFileSync(new URL('../../QueueSystemFrontend/src/student-api.js', import.meta.url), 'utf8');
const code = transformSync(source, { format: 'cjs', loader: 'js' }).code;
const states = [];
const pending = new Map();
let calls = 0;
const react = {
  useState(initial) { const index = states.length; states.push(initial); return [initial, value => { states[index] = value; }]; },
  useRef(initial) { return { current: initial }; },
  useCallback(callback) { return callback; },
  useEffect() {}, // Exercise refresh directly, without scheduling browser polling.
};
const context = {
  module: { exports: {} }, require: name => { assert.equal(name, 'react'); return react; },
  AbortSignal, URLSearchParams,
  fetch(url, options) {
    calls++;
    assert.equal(options.credentials, 'include');
    return new Promise(resolve => pending.set(url, (data, status = 200) => resolve({
      ok: status < 400, status, json: async () => status < 400 ? { success: true, data } : { success: false, message: data },
    })));
  },
};
vm.runInNewContext(code, context);
const backend = context.module.exports.useStudentBackend();
const first = backend.refresh();
const second = backend.refresh();
assert.equal(calls, 3, 'Overlapping refreshes must share in-flight requests');
const services = [{ serviceId: 1, serviceName: 'General Inquiries' }];
pending.get('/api/services')(services);
await new Promise(resolve => setImmediate(resolve));
assert.equal(states[2], services, 'Cards load before personal endpoints finish');
assert.equal(states[7], true, 'Successful catalogue is marked loaded');
pending.get('/api/student/overview')({ services, tickets: [] });
pending.get('/api/student/bookings')('History temporarily unavailable', 503);
const results = await Promise.allSettled([first, second]);
assert.ok(results.every(result => result.status === 'rejected'));
assert.equal(states[2], services, 'A history failure cannot erase service cards');
assert.equal(states[5], 'History temporarily unavailable', 'Personal endpoint failures remain visible');
console.log('PASS catalogue loads independently, in-flight refreshes are deduplicated, and history failures preserve services.');
const saving = backend.action('/bookings/create', { serviceId: '1' });
const savedTicket = { queueId: 123, token: '123' };
pending.get('/api/student/bookings/create')(savedTicket);
await new Promise(resolve => setImmediate(resolve));
pending.get('/api/services')(services);
pending.get('/api/student/overview')('Overview temporarily unavailable', 503);
pending.get('/api/student/bookings')([]);
assert.equal(await saving, savedTicket, 'A saved booking must not become a failed write because its refresh failed');
assert.equal(states[5], 'Overview temporarily unavailable', 'Refresh failure remains visible without misreporting the write');
console.log('PASS saved bookings remain successful when a follow-up read fails.');
