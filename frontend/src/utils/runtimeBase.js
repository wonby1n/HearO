// src/utils/runtimeBase.js

export function getApiBase() {
  const url = new URL(window.location.href);
  const q = url.searchParams.get("apiBase");
  if (q) return q.replace(/\/$/, "");

  const env = import.meta.env.VITE_API_BASE_URL;
  if (env) return String(env).replace(/\/$/, "");

  return ""; // dev에서는 vite proxy가 있으니 상대경로 그대로
}

export function getWsBase() {
  const url = new URL(window.location.href);
  const q = url.searchParams.get("wsBase");
  if (q) return q.replace(/\/$/, "");

  const env = import.meta.env.VITE_WS_BASE_URL;
  if (env) return String(env).replace(/\/$/, "");

  if (window.location.host) {
    return `${window.location.protocol}//${window.location.host}`.replace(/\/$/, "");
  }
  return "";
}

export function toWsUrl(httpBase) {
  if (!httpBase) return "";
  if (httpBase.startsWith("ws://") || httpBase.startsWith("wss://")) return httpBase;
  if (httpBase.startsWith("https://")) return "wss://" + httpBase.slice(8);
  if (httpBase.startsWith("http://")) return "ws://" + httpBase.slice(7);
  return httpBase;
}

export function getWsUrl(path) {
  const apiBase =
    new URL(window.location.href).searchParams.get("apiBase") ||
    import.meta.env.VITE_API_BASE_URL ||
    "";

  if (!apiBase) {
    throw new Error("[WS] apiBase is empty");
  }

  let wsBase = apiBase;

  // http → ws 변환
  if (wsBase.startsWith("https://")) {
    wsBase = "wss://" + wsBase.slice(8);
  } else if (wsBase.startsWith("http://")) {
    wsBase = "ws://" + wsBase.slice(7);
  }

  return `${wsBase}${path.startsWith("/") ? path : "/" + path}`;
}
export function getSockJsUrl(path) {
  const apiBase =
    new URL(window.location.href).searchParams.get("apiBase") ||
    import.meta.env.VITE_API_BASE_URL ||
    "";

  if (!apiBase) throw new Error("[SockJS] apiBase is empty");

  // SockJS는 http/https만 허용
  return `${apiBase.replace(/\/$/, "")}${path.startsWith("/") ? path : "/" + path}`;
}
export function getNativeWsUrl(path) {
  const apiBase =
    new URL(window.location.href).searchParams.get("apiBase") ||
    import.meta.env.VITE_API_BASE_URL ||
    "";

  if (!apiBase) throw new Error("[WS] apiBase is empty");

  let wsBase = apiBase.replace(/\/$/, "");
  if (wsBase.startsWith("https://")) wsBase = "wss://" + wsBase.slice(8);
  else if (wsBase.startsWith("http://")) wsBase = "ws://" + wsBase.slice(7);

  return `${wsBase}${path.startsWith("/") ? path : "/" + path}`;
}
