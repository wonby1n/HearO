import os

from fastapi import FastAPI, UploadFile, File
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
whisper = WhisperService()


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
    return whisper.transcribe_bytes(audio_bytes, filename=file.filename)
