import os
import threading

from fastapi import FastAPI, UploadFile, File
from fastapi.concurrency import run_in_threadpool
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel

from unsmile import UnsmileService
from whisper_stt import WhisperService

app = FastAPI(title="AI Server", version="0.1.0")
app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        o.strip() for o in os.getenv("CORS_ALLOW_ORIGINS", "http://localhost:5173,http://127.0.0.1:5173").split(",")
        if o.strip()
    ],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

unsmile = UnsmileService()

# Whisper 모델은 무거우므로 /stt 첫 요청 시에만 로드 (현재 프론트는 브라우저 STT를 사용)
_whisper = None
_whisper_lock = threading.Lock()


def get_whisper() -> WhisperService:
    global _whisper
    if _whisper is None:
        with _whisper_lock:
            if _whisper is None:
                _whisper = WhisperService()
    return _whisper


class ModerateReq(BaseModel):
    text: str


@app.get("/health")
def health():
    return {"ok": True}


@app.post("/unsmile")
def moderate_text(req: ModerateReq):
    return unsmile.moderate(req.text)


@app.post("/stt")
async def stt_whisper(file: UploadFile = File(...)):
    audio_bytes = await file.read()
    # 모델 로드/추론은 블로킹 작업이므로 이벤트 루프(/unsmile 등)를 막지 않도록 스레드풀에서 실행
    return await run_in_threadpool(lambda: get_whisper().transcribe_bytes(audio_bytes, filename=file.filename))
