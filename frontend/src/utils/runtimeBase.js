// src/utils/runtimeBase.js

// 백엔드 주소: URL 쿼리(?apiBase=) > VITE_API_BASE_URL > 빈 값(dev에서는 vite proxy가 있으니 상대경로 그대로)
export function getApiBase() {
  const q = new URL(window.location.href).searchParams.get("apiBase");
  if (q) return q.replace(/\/$/, "");

  const env = import.meta.env.VITE_API_BASE_URL;
  if (env) return String(env).replace(/\/$/, "");

  return "";
}

// SockJS는 http/https 주소를 받는다
export function getSockJsUrl(path) {
  const apiBase = getApiBase();
  if (!apiBase) throw new Error("[SockJS] apiBase is empty");

  return `${apiBase}${path.startsWith("/") ? path : "/" + path}`;
}
