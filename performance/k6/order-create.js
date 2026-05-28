import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 5 },
    { duration: '1m', target: 15 },
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    http_req_failed: ['rate<0.02'],
    http_req_duration: ['p(95)<800', 'p(99)<1500'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const PRODUCT_ID = Number(__ENV.PRODUCT_ID || '1');
const SKU_ID = Number(__ENV.SKU_ID || '1');
const QUANTITY = Number(__ENV.QUANTITY || '1');
const DEBUG_RESPONSE = __ENV.DEBUG_RESPONSE === 'true';

export default function () {
  const unique = `${__VU}-${__ITER}-${Date.now()}`;
  const payload = JSON.stringify({
    items: [
      {
        productId: PRODUCT_ID,
        skuId: SKU_ID,
        quantity: QUANTITY,
      },
    ],
    delivery: {
      receiverName: 'k6-test-user',
      receiverPhone: '010-0000-0000',
      zipCode: '12345',
      addressLine1: '서울시 강남구 테스트로 1',
      addressLine2: '101호',
      memo: 'k6 load test',
    },
    guestEmail: `k6-${unique}@example.com`,
    guestPhone: '010-9999-8888',
  });

  const res = http.post(`${BASE_URL}/api/orders/guest`, payload, {
    headers: {
      'Content-Type': 'application/json',
    },
  });

  const passed = check(res, {
    'order create status is 201': (r) => r.status === 201,
    'order create response is success': (r) => {
      const body = safeJson(r);
      return body && body.status === 'success';
    },
  });

  if (!passed && DEBUG_RESPONSE) {
    console.error(
      `order-create failed: status=${res.status}, body=${truncate(res.body, 500)}`
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
