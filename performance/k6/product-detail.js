import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 10 },
    { duration: '1m', target: 30 },
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<500', 'p(99)<1000'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const PRODUCT_IDS = (__ENV.PRODUCT_IDS || __ENV.PRODUCT_ID || '1')
  .split(',')
  .map((id) => id.trim())
  .filter(Boolean);
const DEBUG_RESPONSE = __ENV.DEBUG_RESPONSE === 'true';

export default function () {
  const productId = PRODUCT_IDS[(__VU + __ITER) % PRODUCT_IDS.length];
  const res = http.get(`${BASE_URL}/api/products/${productId}`);

  const passed = check(res, {
    'product detail status is 200': (r) => r.status === 200,
    'product detail response is success': (r) => {
      const body = safeJson(r);
      return body && body.status === 'success';
    },
  });

  if (!passed && DEBUG_RESPONSE) {
    console.error(
      `product-detail failed: productId=${productId}, status=${res.status}, body=${truncate(res.body, 500)}`
    );
  }

  sleep(1);
}

function safeJson(response) {
  try {
    return response.json();
  } catch (e) {
    return null;
  }
}

function truncate(value, maxLength) {
  if (!value) {
    return '';
  }
  return value.length > maxLength ? `${value.substring(0, maxLength)}...` : value;
}
