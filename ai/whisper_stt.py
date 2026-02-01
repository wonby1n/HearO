from __future__ import annotations
from typing import Any, Dict, Optional, List

import io
import os

from faster_whisper import WhisperModel

# (선택) pydub로 어떤 포맷이든 wav 변환하려면 ffmpeg 필요
# ffmpeg 없으면 wav만 제대로 들어오도록 프론트에서 맞추는 게 제일 간단함
try:
    from pydub import AudioSegment
    _HAS_PYDUB = True
except Exception:
    _HAS_PYDUB = False


class WhisperService:
    def __init__(
        self,
        model_size: str = None,
        device: str = None,
        compute_type: str = None,
    ):
        # env로 제어 가능
        self.model_size = model_size or os.getenv("WHISPER_MODEL", "base")
        self.device = device or os.getenv("WHISPER_DEVICE", "cpu")
        self.compute_type = compute_type or os.getenv("WHISPER_COMPUTE_TYPE", "int8")

        # faster-whisper
        self.model = WhisperModel(
            self.model_size,
            device=self.device,
            compute_type=self.compute_type,
        )

    def _to_wav_16k_mono(self, audio_bytes: bytes, filename: str) -> bytes:
        """
        입력이 wav가 아닐 수 있을 때 pydub로 16k mono wav로 변환.
        ffmpeg가 없으면 여기서 실패할 수 있음.
        """
        if not _HAS_PYDUB:
            # pydub 미설치면 원본 그대로(=wav 들어온다는 가정)
            return audio_bytes

        # 확장자 힌트
        ext = (filename.split(".")[-1] or "").lower()
        fmt = ext if ext else None

        seg = AudioSegment.from_file(io.BytesIO(audio_bytes), format=fmt)
        seg = seg.set_frame_rate(16000).set_channels(1)
        out = io.BytesIO()
        seg.export(out, format="wav")
        return out.getvalue()

    def transcribe_bytes(
        self,
        audio_bytes: bytes,
        filename: str = "audio",
        language: str = "ko",
        vad_filter: bool = True,
    ) -> Dict[str, Any]:
        """
        ✅ 환각 줄이기 포인트:
        - vad_filter 켜기
        - temperature=0
        - condition_on_previous_text=False
        - no_speech_threshold / log_prob_threshold 등 보수적으로
        """
        wav_bytes = self._to_wav_16k_mono(audio_bytes, filename)

        # faster-whisper는 파일 경로 or file-like를 받음
        audio_file = io.BytesIO(wav_bytes)

        segments, info = self.model.transcribe(
            audio_file,
            language=language,
            vad_filter=vad_filter,
            # 환각 억제 성향
            temperature=0.0,
            condition_on_previous_text=False,
            no_speech_threshold=0.6,
            log_prob_threshold=-1.0,
            compression_ratio_threshold=2.4,
            beam_size=1,  # beam 키우면 "그럴듯한" 문장 만들며 환각 증가하는 경우 있음
        )

        seg_list: List[Dict[str, Any]] = []
        texts: List[str] = []
        for s in segments:
            t = (s.text or "").strip()
            if t:
                texts.append(t)
            seg_list.append(
                {
                    "start": float(s.start),
                    "end": float(s.end),
                    "text": t,
                    # faster-whisper segment는 avg_logprob/no_speech_prob 등을 갖는 버전도 있음
                    "avg_logprob": getattr(s, "avg_logprob", None),
                    "no_speech_prob": getattr(s, "no_speech_prob", None),
                }
            )

        return {
            "text": " ".join(texts).strip(),
            "segments": seg_list,
            "language": getattr(info, "language", language),
            "duration": getattr(info, "duration", None),
        }
