// k6 run k6/mcp-load-test.js   (server on http://localhost:8080)
// Simulates bursty agent traffic: initialize -> tools/call in a loop.
import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE = __ENV.MCP_URL || 'http://localhost:8080/mcp';
const HEADERS = {
  'Content-Type': 'application/json',
  Accept: 'application/json, text/event-stream',
};

export const options = {
  scenarios: {
    agents: {
      executor: 'ramping-arrival-rate',
      startRate: 10,
      timeUnit: '1s',
      preAllocatedVUs: 50,
      stages: [
        { duration: '30s', target: 50 },
        { duration: '1m', target: 200 }, // burst: agents chaining calls
        { duration: '30s', target: 0 },
      ],
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    'http_req_duration{tool:findOrder}': ['p(95)<150'],
  },
};

function rpc(method, params, id, session) {
  const headers = session ? { ...HEADERS, 'Mcp-Session-Id': session } : HEADERS;
  const body = JSON.stringify({ jsonrpc: '2.0', id, method, params });
  return http.post(BASE, body, { headers, tags: { tool: params?.name || method } });
}

export function setup() {
  const init = rpc('initialize', {
    protocolVersion: '2025-06-18',
    capabilities: {},
    clientInfo: { name: 'k6-agent', version: '1.0.0' },
  }, 1);
  check(init, { 'initialize ok': (r) => r.status === 200 });
  const session = init.headers['Mcp-Session-Id'];
  http.post(BASE, JSON.stringify({ jsonrpc: '2.0', method: 'notifications/initialized' }),
    { headers: session ? { ...HEADERS, 'Mcp-Session-Id': session } : HEADERS });
  return { session };
}

export default function ({ session }) {
  const orderId = `ORD-${1000 + Math.floor(Math.random() * 60)}`;
  const res = rpc('tools/call', { name: 'findOrder', arguments: { orderId } }, 2, session);
  check(res, {
    'status 200': (r) => r.status === 200,
    'has result': (r) => r.body.includes(orderId),
  });
  sleep(0.1);
}
