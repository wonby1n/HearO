
---

# AI Server (FastAPI) - Whisper STT + Unsmile

FastAPI 기반의 AI 서버입니다. 음성 인식(STT)과 텍스트 유해성 분류(Moderation) 기능을 제공합니다.

### 주요 기능

* **`/stt`**: `faster-whisper`를 이용한 한국어 음성 텍스트 변환
* **`/unsmile`**: `smilegate-ai/kor_unsmile` 모델을 이용한 문장 유해성 검사
* **`/health`**: 서버 상태 체크

### 프로젝트 구조

```text
ai-server/
├── app.py              # FastAPI 엔드포인트 및 서비스 로직
├── whisper_stt.py      # faster-whisper 기반 STT 클래스
├── unsmile.py          # kor_unsmile 기반 유해성 분류 클래스
└── requirements.txt    # 의존성 패키지 목록

```

---

## 1. 사전 준비

* **Python 3.10 이상** 권장
* **FFmpeg 설치** (권장)
* 다양한 오디오 포맷(mp3, m4a 등)을 처리하기 위해 필요합니다.
* 설치되어 있지 않다면 클라이언트에서 `wav(16kHz/mono)` 포맷으로 업로드해야 합니다.



---

## 2. 가상환경 설정 및 설치

### Windows (PowerShell)

```powershell
# 가상환경 생성 및 활성화
python -m venv venv
.\venv\Scripts\Activate.ps1

# (선택) 권한 오류 발생 시 실행
# Set-ExecutionPolicy -Scope CurrentUser RemoteSigned

```

### macOS / Linux

```bash
# 가상환경 생성 및 활성화
python3 -m venv venv
source venv/bin/activate

```

### 의존성 설치

```bash
python -m pip install --upgrade pip
pip install -r requirements.txt

```

> **참고**: CUDA(GPU) 가속을 사용하려면 사용 중인 그래픽 카드 드라이버 버전에 맞는 PyTorch 별도 설치가 필요할 수 있습니다.

---

## 3. 서버 실행

```bash
# uvicorn을 이용한 실행 (Hot-reload 활성화)
uvicorn app:app --host 0.0.0.0 --port 8000 --reload

```

* **API 문서(Swagger):** [http://127.0.0.1:8000/docs](http://127.0.0.1:8000/docs)
* **헬스 체크:** [http://127.0.0.1:8000/health](http://127.0.0.1:8000/health)

---

## 4. API 사용법

### 4-1. 텍스트 유해성 분류 (Unsmile)

```bash
curl -X POST "http://127.0.0.1:8000/unsmile" \
     -H "Content-Type: application/json" \
     -d "{\"text\":\"테스트 문장입니다.\"}"

```

### 4-2. 음성 인식 (STT)

```bash
curl -X POST "http://127.0.0.1:8000/stt" \
     -F "file=@./sample.wav"

```

---

## 5. 환경 변수 설정 (선택 사항)

필요에 따라 `.env` 파일 또는 시스템 환경 변수를 통해 설정을 변경할 수 있습니다.

| 변수명 | 설명 | 기본값 |
| --- | --- | --- |
| `CORS_ALLOW_ORIGINS` | CORS 허용 도메인 | `http://localhost:5173` |
| `WHISPER_MODEL` | Whisper 모델 크기 (`base`, `small` 등) | `base` |
| `WHISPER_DEVICE` | 연산 장치 (`cpu`, `cuda`) | `cpu` |
| `UNSMILE_THRESHOLD` | 유해성 판정 임계치 | `0.75` |

---

## 6. 트러블슈팅

1. **모델 다운로드 지연**: 첫 실행 시 HuggingFace에서 수 GB의 모델 데이터를 다운로드하므로 시간이 걸릴 수 있습니다.
2. **FFmpeg 관련 경고**: `RuntimeWarning: Couldn't find ffmpeg` ffmpeg가 없이도 잘 되도록 되어있습니다. 
---
