
---

# 상담 종료 저장 및 AI 요약 / 블랙리스트 기능 README

## 1. 기능 개요

HearO 백엔드는 **상담 종료 시점**에 상담 데이터를 저장하고,
**STT 기반 전체 상담 로그(fullTranscript)**를 활용하여
AI 요약을 생성·저장하는 기능을 제공합니다.

또한 상담 종료 사유에 따라 **상담원 기준 고객 블랙리스트**를 자동으로 관리하며,
상담 종료 이후 **녹음 파일 및 평점(PATCH)**을 추가로 저장할 수 있습니다.

---

## 2. 핵심 특징 요약

| 기능          | 설명                                                |
| ----------- | ------------------------------------------------- |
| 상담 종료 저장    | 상담 종료 시 상담 데이터 일괄 저장                              |
| AI 상담 요약    | fullTranscript 기반 title / subtitle / aiSummary 생성 |
| 블랙리스트 자동 등록 | 욕설/공격성 종료 시 상담원 → 고객 밴                            |
| PATCH 업데이트  | 상담 종료 후 녹음 파일 / 평점 추가                             |
| 원본-파생 분리    | fullTranscript만 원본, 요약은 파생 데이터                    |

---

## 3. 전체 흐름

```
Client (상담 종료)
  └─ POST /api/v1/consultations
        ↓
Backend (Spring Boot)
  ├─ Consultation 저장
  ├─ 종료 사유 기반 블랙리스트 처리
  ├─ AI 요약 생성 (LLM)
        ↓
DB
  └─ Consultation / Blacklist 저장
```

```
Client (상담 종료 이후)
  └─ PATCH /api/v1/consultations/{id}
        ↓
Backend
  └─ VoiceRecording / Rating upsert
```

---

## 4. 상담 종료 저장 API

### 4.1 URL

```http
POST /api/v1/consultations
```

---

### 4.2 Request Body

```json
{
  "customerId": 1,
  "registrationId": 1,
  "fullTranscript": "고객: 세탁기가 물이 안빠져요 .\n상담원: 기사님을 불러드릴게요. 언제가 편하시죠?",
  "userMemo": "세탁기 배수 문제",
  "profanityCount": 0,
  "avgAggressionScore": 0.12,
  "maxAggressionScore": 0.33,
  "terminationReason": "NORMAL",
  "durationSeconds": 620
}
```

| 필드                 | 설명                 |
| ------------------ | ------------------ |
| customerId         | 고객 ID              |
| registrationId     | 접수 ID              |
| fullTranscript     | 상담 STT 전체 텍스트 (원본) |
| userMemo           | 상담원 메모             |
| profanityCount     | 욕설 감지 횟수           |
| avgAggressionScore | 평균 공격성 점수          |
| maxAggressionScore | 최대 공격성 점수          |
| terminationReason  | 상담 종료 사유           |
| durationSeconds    | 상담 시간(초)           |

---

### 4.3 Response (200 OK)

```json
{
  "consultationId": 10,
  "title": "세탁기 고장",
  "subtitle": "배수가 안됨",
  "aiSummary": "- 이슈 요약: 세탁기가 고장남\n- 고객 요청: 원인 상담\n- 상담원 안내: AS 기사 부름\n- 후속 조치: 필요 시 재상담"
}
```

---

## 5. AI 요약 생성 정책

### 요약 입력

* **fullTranscript만 사용**
* DB에 저장된 요약은 항상 fullTranscript로부터 생성됨

### 요약 규칙

* STT에 없는 내용은 **추측하지 않음**
* 한국어로 응답
* 시간 정보(타임스탬프) 제외
* JSON 구조로 생성 후 서버에서 파싱

### 생성 항목

* `title` (30자 제한)
* `subtitle` (30자 제한)
* `aiSummary` (Bullet 요약)

---

## 6. 블랙리스트 자동 등록 정책

### 적용 조건

상담 종료 사유가 아래 중 하나일 경우:

```text
PROFANITY_LIMIT
AGGRESSION_LIMIT
```

### 동작 방식

* **상담원(User) 기준 → 고객(Customer) 밴**
* `(user_id, customer_id)` 조합으로 unique 관리
* 이미 존재 시 **중복 생성 없이 reason 갱신**

```text
Blacklist (user_id, customer_id) UNIQUE
```

---

## 7. 상담 종료 후 PATCH API

### 7.1 URL

```http
PATCH /api/v1/consultations/{consultationId}
```

---

### 7.2 Request Body 예시 (녹음 파일)

```json
{
  "voiceRecording": {
    "fileUrl": "https://cdn.example.com/recording.wav",
    "fileSize": 1234567,
    "durationSeconds": 620
  }
}
```

---

### 7.3 Request Body 예시 (평점)

```json
{
  "rating": {
    "processRating": 4.5,
    "solutionRating": 4.0,
    "kindnessRating": 5.0,
    "feedback": "설명이 친절했어요."
  }
}
```

---

### 7.4 동작 특징

* VoiceRecording: **upsert**
* Rating: **upsert**
* 상담 종료 이후 비동기적으로 호출 가능

---

## 8. 데이터 설계 핵심 철학

### 원본 / 파생 데이터 분리

| 구분                  | 데이터                        |
| ------------------- | -------------------------- |
| 원본(Source of Truth) | fullTranscript             |
| 파생 데이터              | title, subtitle, aiSummary |

→ AI 정책 변경 시 **fullTranscript 기준 재요약 가능**

---

## 9. 관련 엔티티

### Consultation

* fullTranscript
* title
* subtitle
* aiSummary
* userMemo
* profanityCount
* avgAggressionScore
* maxAggressionScore
* terminationReason
* durationSeconds

### Blacklist

* user
* customer
* reason

### VoiceRecording

* fileUrl
* fileSize
* durationSeconds

### ConsultationRating

* processRating
* solutionRating
* kindnessRating
* feedback

---

## 10. 프로젝트 구조

```
consultation
 ├─ controller
 │   └─ ConsultationController.java
 ├─ dto
 │   ├─ ConsultationEndRequest.java
 │   ├─ ConsultationEndResponse.java
 │   ├─ ConsultationPatchRequest.java
 │   └─ ConsultationSummaryResponse.java
 ├─ entity
 │   ├─ Consultation.java
 │   ├─ VoiceRecording.java
 │   └─ ConsultationRating.java
 └─ service
     └─ ConsultationServiceImpl.java

ai
 ├─ controller
 │   └─ ConsultationAiController.java
 └─ service
     └─ ConsultationSummaryService.java

customer
 └─ entity
     └─ Blacklist.java
```

---
