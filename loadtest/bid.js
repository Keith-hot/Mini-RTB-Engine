import http from "k6/http";
import { check } from "k6";

export const options = {
  vus: 20,
  duration: "30s",
  thresholds: {
    http_req_failed: ["rate<0.01"],
    http_req_duration: ["p(95)<100"]
  }
};

export default function () {
  const body = JSON.stringify({
    requestId: `k6-${Date.now()}-${Math.random()}`,
    userId: `user-${Math.floor(Math.random() * 1000)}`,
    placementId: "slot-home",
    device: "mobile",
    country: "HK",
    userSegments: ["saas", "ecommerce"]
  });
  const response = http.post("http://localhost:8080/api/bid", body, {
    headers: { "Content-Type": "application/json" }
  });
  check(response, {
    "status is 200": r => r.status === 200
  });
}
