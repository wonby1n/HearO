const express = require("express");
const { createProxyMiddleware } = require("http-proxy-middleware");

function startProxyServer({
  port = 0, // 0이면 OS가 빈 포트 자동 할당
  targetHttp = "https://i14e106.p.ssafy.io",
  targetWs = "https://i14e106.p.ssafy.io",
} = {}) {
  const app = express();

  // 헬스 체크
  app.get("/healthz", (_req, res) => res.status(200).send("ok"));

  // /api 프록시 (HTTP)
  app.use(
    "/api",
    createProxyMiddleware({
      target: targetHttp,
      changeOrigin: true,
      secure: false, // 사내/개발 인증서 이슈 있으면 false
      logLevel: "warn",
      // 필요하면 rewrite:
      // pathRewrite: { "^/api": "" },
    })
  );

  // /ws 프록시 (WebSocket)
  app.use(
    "/ws",
    createProxyMiddleware({
      target: targetWs,
      changeOrigin: true,
      ws: true,
      secure: false,
      logLevel: "warn",
    })
  );

  // 서버 시작
  const server = app.listen(port, "127.0.0.1");

  return new Promise((resolve, reject) => {
    server.on("listening", () => {
      const address = server.address();
      resolve({
        app,
        server,
        port: address.port,
        baseURL: `http://127.0.0.1:${address.port}`,
      });
    });
    server.on("error", reject);
  });
}

module.exports = { startProxyServer };
