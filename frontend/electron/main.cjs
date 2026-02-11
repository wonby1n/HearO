// frontend/electron/main.cjs
const { app, BrowserWindow } = require("electron");
const path = require("path");
const { spawn, execFile } = require("child_process");

let splashWin = null;
let mainWin = null;
let aiProc = null;
let proxy = null;

const isDev = !app.isPackaged;

// ====== AI Server 설정 ======
const AI_PORT = 8000;
const AI_HEALTH_URL = `http://127.0.0.1:${AI_PORT}/health`;

// ====== 종료 중복 방지 플래그 ======
let isQuitting = false;
let isStoppingAi = false;

// ====== 경로 유틸 ======
function getRootDir() {
  return path.resolve(__dirname, "..", "..");
}

function getAiExePath() {
  if (isDev) {
    return path.join(getRootDir(), "ai", "dist", "ai-server.exe");
  }
  return path.join(process.resourcesPath, "ai", "ai-server.exe");
}

function getIconPath() {
  return path.join(__dirname, "assets", "icon.png");
}

// ====== 창 생성 ======
function createSplash() {
  splashWin = new BrowserWindow({
    width: 420,
    height: 280,
    frame: false,
    resizable: false,
    movable: true,
    alwaysOnTop: true,
    show: true,
    icon: getIconPath(),
    webPreferences: {
      contextIsolation: true,
    },
  });

  splashWin.loadFile(path.join(__dirname, "splash.html"));
  splashWin.on("closed", () => (splashWin = null));
}

function createMain() {
  mainWin = new BrowserWindow({
    width: 1200,
    height: 800,
    show: false,
    icon: getIconPath(),
    webPreferences: {
      contextIsolation: true,
    },
  });

  // ✅ 콘솔(DevTools) 이제 안 열리게: 아래 라인 제거/주석
  // mainWin.webContents.openDevTools({ mode: "detach" });

  mainWin.webContents.on("did-fail-load", (_e, errorCode, errorDesc, validatedURL) => {
    console.error("[Renderer] did-fail-load:", { errorCode, errorDesc, validatedURL });
  });

  mainWin.webContents.on("render-process-gone", (_e, details) => {
    console.error("[Renderer] render-process-gone:", details);
  });

  if (isDev) {
    mainWin.loadURL("http://localhost:5173");
    // 개발할 때만 devtools 열고 싶으면 여기만 켜도 됨
    // mainWin.webContents.openDevTools({ mode: "detach" });
  } else {
    mainWin.loadFile(path.join(__dirname, "../dist/index.html"));
  }

  mainWin.once("ready-to-show", () => {
    mainWin.show();
    splashWin?.close();
    splashWin = null;
  });

  mainWin.on("closed", () => (mainWin = null));
}

// ====== AI 서버 실행/종료 ======
function startAiServer() {
  const exePath = getAiExePath();
  console.log("[AI] exePath =", exePath);

  // ✅ (중요) mac/linux에서 그룹 kill(-pid) 가능하게 detached
  // Windows에서는 영향 거의 없고, 종료 로직 안정성만 올라감
  aiProc = spawn(exePath, [], {
    cwd: path.dirname(exePath),
    windowsHide: true,
    detached: process.platform !== "win32",
    stdio: ["ignore", "pipe", "pipe"],
  });

  aiProc.stdout?.on("data", (d) => console.log("[AI]", d.toString().trim()));
  aiProc.stderr?.on("data", (d) => console.error("[AI ERR]", d.toString().trim()));
  aiProc.on("exit", (code) => {
    console.log("[AI] exited:", code);
    aiProc = null;
  });
  aiProc.on("error", (err) => console.error("[AI] spawn error:", err));
}

function stopAiServer() {
  // ✅ Promise로 만들어서 before-quit에서 await 가능하게
  return new Promise((resolve) => {
    if (!aiProc) return resolve();
    if (isStoppingAi) return resolve();
    isStoppingAi = true;

    const pid = aiProc.pid;
    console.log("[AI] stopping, pid =", pid);

    try {
      if (process.platform === "win32") {
        // ✅ 프로세스 트리 전체 종료
        execFile("taskkill", ["/PID", String(pid), "/T", "/F"], (err, stdout, stderr) => {
          if (err) console.error("[AI] taskkill error:", err, stderr?.toString?.());
          else console.log("[AI] taskkill ok:", stdout?.trim());

          aiProc = null;
          isStoppingAi = false;
          resolve();
        });
      } else {
        // ✅ mac/linux: 프로세스 그룹 종료 (-pid)
        try {
          process.kill(-pid, "SIGTERM");
        } catch (e) {
          // fallback
          try { process.kill(pid, "SIGTERM"); } catch {}
        }

        // 약간 기다렸다가 정리
        setTimeout(() => {
          aiProc = null;
          isStoppingAi = false;
          resolve();
        }, 300);
      }
    } catch (e) {
      console.error("[AI] stop error:", e);
      aiProc = null;
      isStoppingAi = false;
      resolve();
    }
  });
}

// ====== /health 폴링 ======
async function waitForHealth(timeoutMs = 45000, intervalMs = 500) {
  const start = Date.now();

  while (Date.now() - start < timeoutMs) {
    try {
      const res = await fetch(AI_HEALTH_URL, { method: "GET" });
      if (res.ok) return true;
    } catch {}
    await new Promise((r) => setTimeout(r, intervalMs));
  }
  return false;
}

// ====== 메인 ======
process.on("uncaughtException", (err) => console.error("[Main] uncaughtException:", err));
process.on("unhandledRejection", (reason) => console.error("[Main] unhandledRejection:", reason));

app.whenReady().then(async () => {
  app.setAppUserModelId("com.ssafy.HearO");

  createSplash();
  startAiServer();

  const ok = await waitForHealth(60000, 500);
  if (!ok) console.error("[AI] health check timeout:", AI_HEALTH_URL);

  createMain();
});

// ✅ window-all-closed에서는 “정리만 하고 quit” (stop은 before-quit에서 단일 처리)
app.on("window-all-closed", () => {
  try { proxy?.server?.close(); } catch {}
  if (process.platform !== "darwin") app.quit();
});

// ✅ 핵심: quit을 가로채고 AI 서버를 확실히 끈 뒤 종료
app.on("before-quit", async (e) => {
  if (isQuitting) return;
  isQuitting = true;

  e.preventDefault(); // quit 잠깐 멈춤
  try { proxy?.server?.close(); } catch {}
  await stopAiServer();

  app.exit(0); // 확실 종료
});
