import { fileURLToPath, URL } from "node:url";
import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import vueDevTools from "vite-plugin-vue-devtools";

/**
 * [Vite 설정]
 * 프록시 설정을 통해 프론트엔드에서 /api로 보내는 요청을
 * 실제 백엔드 서버로 자동 전달합니다.
 */
export default defineConfig({
  plugins: [vue(), vueDevTools()],
  
  base: './',resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url)),
    },
  },
  define: {
    // SockJS 호환성을 위한 global 객체 polyfill
    global: "globalThis",
  },
  server: {
    proxy: {
      // 🔹 '/api'로 시작하는 모든 요청을 가로챕니다.
      "/api": {
        // 🔧 로컬 개발: localhost:8080 (백엔드 Spring Boot 기본 포트)
        // 🔧 운영 배포: https://i14e106.p.ssafy.io
        // target: 'http://localhost:8080',
        target: "https://i14e106.p.ssafy.io",
        changeOrigin: true, // 서버가 출처(Origin)를 검사할 때 백엔드 주소로 속여줍니다.
        secure: false, // SSL 인증서 관련 경고 무시 (개발 환경용)

        /**
         * 만약 서버 API 구조가 '도메인/api/v1'이 아니라 '도메인/v1' 형태라면
         * 아래 주석을 해제하여 요청 경로에서 '/api'라는 글자를 지우고 보낼 수 있습니다.
         */
        // rewrite: (path) => path.replace(/^\/api/, ''),
      },
      // 🔹 WebSocket 프록시 추가
      "/ws": {
        // 🔧 로컬 개발: localhost:8080
        // target: 'http://localhost:8080',
        target: "https://i14e106.p.ssafy.io",
        changeOrigin: true,
        ws: true, // WebSocket 지원 활성화
      },
    },
  },
});
