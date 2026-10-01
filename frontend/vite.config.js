import { fileURLToPath, URL } from "node:url";
import { defineConfig, loadEnv } from "vite";
import vue from "@vitejs/plugin-vue";
import vueDevTools from "vite-plugin-vue-devtools";

/**
 * [Vite 설정]
 * 개발 서버에서 /api, /ws 요청을 백엔드(VITE_PROXY_TARGET)로 프록시합니다.
 */
export default defineConfig(({ command, mode }) => {
  const env = loadEnv(mode, process.cwd(), "");
  const proxyTarget = env.VITE_PROXY_TARGET || "http://localhost:8080";

  return {
    plugins: [vue(), vueDevTools()],

    base: "./",
    resolve: {
      alias: {
        "@": fileURLToPath(new URL("./src", import.meta.url)),
      },
    },
    define: {
      // SockJS 호환성을 위한 global 객체 polyfill
      global: "globalThis",
    },
    // 운영 빌드에서는 디버그 로그 제거 (console.warn / console.error는 유지)
    esbuild: command === "build" ? { pure: ["console.log", "console.info", "console.debug"] } : {},
    server: {
      proxy: {
        "/api": {
          target: proxyTarget,
          changeOrigin: true,
          secure: false, // 자체 서명 인증서 허용 (개발 환경용)
        },
        "/ws": {
          target: proxyTarget,
          changeOrigin: true,
          ws: true,
        },
      },
    },
  };
});
