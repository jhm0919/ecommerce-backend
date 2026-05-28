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
const PAGE_SIZE = __ENV.PAGE_SIZE || '20';
const CATEGORY_ID = __ENV.CATEGORY_ID || '';
const KEYWORD = __ENV.KEYWORD || '';

export default function () {
  const params = [
    ['page', '0'],
    ['size', PAGE_SIZE],
    ['sort', 'createdAt,desc'],
  ];

  if (CATEGORY_ID) {
    params.push(['categoryId', CATEGORY_ID]);
  }
  if (KEYWORD) {
    params.push(['keyword', KEYWORD]);
  }

  const query = params
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
    .join('&');

  const res = http.get(`${BASE_URL}/api/products?${query}`);

  check(res, {
    'product list status is 200': (r) => r.status === 200,
    'product list response is success': (r) => {
      const body = safeJson(r);
      return body && body.status === 'success';
    },
  });

  sleep(1);
}

function safeJson(response) {
  try {
    return response.json();
  } catch (e) {
    return null;
  }
}
