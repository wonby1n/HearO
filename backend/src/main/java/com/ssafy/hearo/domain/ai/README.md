
## 1. 시스템 개요

### 역할 분리

| 구성요소                  | 역할                           |
| --------------------- | ---------------------------- |
| Backend (Spring Boot) | RAG 요청 API 제공, AI 서버 연동      |
| AI Server (Python)    | 문서 검색(Vector DB) + LLM 응답 생성 |
| Vector DB (Chroma 등)  | 문서 임베딩 및 유사도 검색              |
| Client (Web/Electron) | 질문 입력 및 답변 표시                |

---

## 2. 전체 흐름

```
Client
  └─ POST /api/v1/rag/ask (question)
        ↓
Backend (Spring Boot)
  └─ 질문을 AI Server로 전달
        ↓
AI Server
  ├─ Vector DB에서 관련 문서 검색
  └─ 검색 문서 + 질문으로 LLM 응답 생성
        ↓
Backend
  └─ 응답(JSON) 파싱 및 정제
        ↓
Client
  └─ answer + retrievedDocs 표시
```

---

## 3. API 명세

### 3.1 질문 요청 API

**URL**

```
POST /api/v1/rag/ask
```

---

### Request Body

```json
{
  "question": "에러코드 E101의 원인"
}
```

| 필드       | 설명     |
| -------- | ------ |
| question | 사용자 질문 |

---

### Response (200 OK)

```json
{
    "answer": "{\n  \"answer\": \"에러코드 E101의 원인은 스위치 불량이나 문 미닫힘입니다.\",\n  \"evidence\": [\n    {\n      \"source\": \"rag.pdf\",\n      \"quote\": \"E101 오류 문 열림 감지 지속 스위치 불량/문 미닫힘\"\n    }\n  ],\n  \"need_more_info\": []\n}",
    "contexts": [
        {
            "id": "e270266e69407b34046a00d27d0463ba7cc16f7a",
            "text": "드는 RAG 학습/테스트 목적으로 임의 구성된 예시입니다. 코드는 'E(오류) / W(경고) /\nN(알림)'으로 구분했습니다.\n코드\n구분\n증상\n가능 원인\n사용자 조치\n서비스 권장\nE101\n오류\n문 열림 감지 지속\n스위치 불량/문\n미닫힘\n문 재정렬, 패킹 이물 제거\n반복 시 점검\nE102\n오류\n냉장실 온도 상승\n지속\n통풍 부족/과다 적재\n벽 간격 확보, 적재량 줄임\n12시간 이상\n개선 없음\nE103\n오류\n냉동실 제빙 불가\nIce Maker OFF/물\n부족\nON 설정, 물 채움, 얼음통 정리\n반복 또는\n누수 동반\nE201\n오류\n팬 소음 과다/진동\n수평 불량/간섭\n수평 조절, 주변 접촉 제거\n금속 마찰음\n지속\nE202\n오류\n해동수 넘침/바닥 물\n배수구 막힘/성에\n과다\n전원 분리 후 이물 확인(분해 금지)\n누수 지속\nE301\n오류\nWi-Fi 연결 실패\n반복\n거리/2.4 GHz/인증\n공유기 가까이, 2.4 GHz, 비번 재입력\n등록 불가\n지속\nE401\n오류\n표시부 버튼 미동작\n잠금/수분\n잠금 해제, 물기 제거 후 재시도\n영구 미동작\nW110\n경고\n주변 온도 범위 이탈\n설치 온도 이탈\n환경 개선(난방/환기/차광)\n지속 시 권장\nW120\n경고\n통풍 간격 부족 추정\n벽 거리 부족/적재물\n뒷면/측면 5 cm 확보\n외벽\n이슬/열감\n과다\nW130\n경고\n전원 품질 불안정\n멀티탭/접촉 불량\n단독 콘센트 사용, 점검\n플러그 발열\n시 즉시\nN010\n알림\n초기 냄새 안내\n초기 소재 냄새\n환기 후 사용, 테이프 제거\n해당 없음\nN020\n알림\n초기 소음 안내\n초기 고속 운전\n2~3일 관찰\n3일 이상\n악화\nN030\n알림\n문 장시간 개방\n사용",
            "score": 0.47261655,
            "source": "rag.pdf"
        },
        {
            "id": "7a1de1a8cd8a5f33cd4a1e17dbc00b82bbe7875e",
            "text": "0분이 지나면 꺼질 수 있음(청각장애인 알림 목적).\n• 정전 시에는 문을 되도록 열지 말 것(2~3시간은 보관 가능, 24시간 이상이면 식품 손상 가능).\n5. Wi-Fi / SmartThings 연결 절차(요약)\n• SmartThings 앱 설치(구글 플레이/앱스토어/GALAXY Apps).\n• 삼성 계정으로 로그인.\n• 앱에서 기기 추가 → 냉장고 선택 → 공유기 정보 입력.\n• 냉장고 표시부의 냉장실 버튼을 5초 이상 눌러 'AP' 표시 확인.\n• 등록 완료 후 Wi-Fi 아이콘 점등.\n삼성 BESPOKE 냉장고 RF85T92N1AP - RAG 학습용 요약\n3\n6. 고장 신고 전 빠른 점검(요약)\n증상\n우선 확인\n권장 조치\n냉각이 약함/안 됨\n플러그 연결, 설정 온도, 통풍\n간격\n플러그 확인 → 온도 낮춤 → 벽과 5 cm 이상 확보\n성에/이슬\n문 장시간 개방, 습도, 개방 보관\n문 밀폐 확인 → 밀폐용기 사용 → 개방 시간\n최소화\n소음이 심함\n바닥 수평/단단함, 벽 간격, 접촉\n물건\n수평 조절 → 5 cm 간격 확보 → 주변 물건 제거\n문이 잘 안 열림\n앞쪽 높이/수평, 문 좌우 정렬\n수평 조절 → 1~2회 천천히 열닫 → 정면에서\n부드럽게 개폐\n얼음 냄새\n반찬류(김치/생선) 밀폐 여부\n뚜껑/랩/비닐 포장 → 통 외부 오염 닦기 → 물\n교체(정수 권장)\n7. 가상 에러코드(학습용)\n아래 코드는 RAG 학습/테스트 목적으로 임의 구성된 예시입니다. 코드는 'E(오류) / W(경고) /\nN(알림)'으로 구분했습니다.\n코드\n구분\n증상\n가능 원인\n사용자 조치\n서비스 권장\nE101\n오류\n문 열림 감지 지속\n스",
            "score": 0.41987719999999995,
            "source": "rag.pdf"
        },
        {
            "id": "ed45cd8cbbd98afa2fa673e6b266eba00c9af35e",
            "text": "발열\n시 즉시\nN010\n알림\n초기 냄새 안내\n초기 소재 냄새\n환기 후 사용, 테이프 제거\n해당 없음\nN020\n알림\n초기 소음 안내\n초기 고속 운전\n2~3일 관찰\n3일 이상\n악화\nN030\n알림\n문 장시간 개방\n사용 중 문 열림\n문 닫기, 식품 정리\n스위치 이상\n의심\n문의/안전 안내\n• 제품에서 타는 냄새, 연기, 과열, 침수, 가스 누출 의심 상황이 있으면 즉시 전원을 차단하고\n서비스센터에 문의하세요.\n• 임의 분해·개조·변압기 사용·멀티탭 사용은 감전/화재 위험을 높입니다.\n삼성 BESPOKE 냉장고 RF85T92N1AP - RAG 학습용 요약\n2\n8. 가상 환불 규정 (학습용)\n구분\n조건\n처리 기준(예시)\n구입 후 7일 이내\n초기 불량, 정상 설치 상태\n전액 환불 또는 동일 모델 교환\n구입 후 30일 이내\n동일 하자 2회 이상 발생\n제품 교환 또는 환불 중 선택\n구입 후 1년 이내\n중대한 기능 불량으로 수리 불가\n감가상각 후 환불 또는 상위 모델 교환\n소비자 과실\n파손·침수·임의 개조\n환불 불가\n9. 가상 무상 AS 규정 (학습용)\n항목\n무상 기준(예시)\n보증 기간\n제품 1년 / 핵심부품(압축기) 3년 / 인버터 압축기 10년\n무상 수리 대상\n정상 사용 중 발생한 성능·기능 고장\n무상 제외\n소모품, 외관 손상, 전압 오류, 멀티탭 사용\n출장 AS\n보증기간 내 연 2회 무상 출장\n데이터/설정\nWi-Fi, 앱 설정은 사용자 책임\n※ 본 환불·AS 규정은 RAG 시스템의 정책 질의응답 테스트를 위한 예시 데이터입니다. 실제 서비스 정책 판단에\n사용해서는 안 됩니다.",
            "score": 0.32881629999999995,
            "source": "rag.pdf"
        },
        {
            "id": "f46fbaee109a660b349c07440cb6d0ec67b1ed6a",
            "text": "삼성 BESPOKE 냉장고 RF85T92N1AP - RAG 학습용 요약\n1\nRAG 학습용 정리본 + 가상 에러코드 목록\n대상 제품: 삼성 BESPOKE 냉장고 4도어 프리스탠딩 849L (모델 RF85T92N1AP)\n주의: 본 문서의 가상 에러코드는 RAG/QA 학습 및 테스트 목적의 예시이며, 실제 삼성전자 제품의 진단 코드와\n일치하지 않을 수 있습니다. 실제 문제 발생 시 제품 사용설명서 및 서비스센터 안내를 따르세요.\n항목\n내용\n전원\nAC 220 V / 60 Hz 전용\n권장 콘센트\n정격 15 A 이상, 제품 단독 사용(멀티탭/병행 사용 금지)\n설치\n프리스탠딩(가구 내 빌트인 사용 금지), 전문 기사 의뢰 권장\n설치 환경\n주위 온도 5 ℃ 이상 43 ℃ 이하, 직사광선/열기구/습기/가스 누출 위험 장소 피함\n통풍 간격\n뒷면/측면 벽과 5 cm 이상 확보\n연결 기능\nSmartThings 앱을 통한 Wi-Fi 연결(냉장실 버튼 5초 → AP 표시)\n보증(요약)\n제품 1년, 핵심부품(압축기) 3년, 인버터 압축기(해당 시) 10년 무상수리\n1. 전원 및 전기 안전(핵심)\n• 멀티탭 사용 금지, 제품 단독 콘센트 사용\n• 변압기 사용 금지\n• 접지 필수(가스관/수도관/전화선 등에 접지 금지)\n• 전원코드 연장·임의 변경 금지\n• 플러그는 끝까지 확실히, 코드는 아래 방향으로 연결\n• 젖은 손으로 플러그 접촉 금지, 이상 소음/탄 냄새/연기 시 즉시 전원 차단 후 서비스센터 문의\n2. 설치(핵심)\n• 바닥이 단단하고 수평인 곳에 설치, 조절 다리가 바닥에 완전히 닿도록 조절\n• 뒷면/측면 5 cm 이상 간격 확보(",
            "score": 0.30972504999999995,
            "source": "rag.pdf"
        },
        {
            "id": "56afe8671fe3e1045aa7d4abef5bd7ffe1421491",
            "text": "-Fi, 앱 설정은 사용자 책임\n※ 본 환불·AS 규정은 RAG 시스템의 정책 질의응답 테스트를 위한 예시 데이터입니다. 실제 서비스 정책 판단에\n사용해서는 안 됩니다.",
            "score": 0.29399747,
            "source": "rag.pdf"
        }
    ]
}
```

| 필드                    | 설명             |
| --------------------- | -------------- |
| answer                | LLM이 생성한 최종 답변 |
| retrievedDocs         | 검색된 문서 목록      |
| retrievedDocs.content | 문서 텍스트         |
| retrievedDocs.score   | 질문과의 유사도       |
| retrievedDocs.source  | 문서 출처          |

---

## 4. RAG 처리 로직

### 핵심 개념

* **Retrieval**
  → Vector DB에서 질문과 유사한 문서 검색
* **Augmentation**
  → 검색된 문서를 질문과 함께 LLM에 전달
* **Generation**
  → 문서를 참고한 답변 생성

---

### 처리 순서

1. Client에서 질문 수신
2. AI Server에 HTTP 요청
3. AI Server에서:

   * 문서 임베딩 검색
   * 관련 문서 추출
   * LLM 답변 생성
4. 응답 JSON을 Backend에서 파싱
5. `answer`, `retrievedDocs` 형태로 Client에 반환

---

### 예시 코드 (Service)

```java
public AskResponse ask(String question) {
    // AI 서버로 요청
    String response = webClient.post()
        .uri("/ask")
        .bodyValue(Map.of("question", question))
        .retrieve()
        .bodyToMono(String.class)
        .block();

    // JSON 파싱 및 응답 가공
    return parseResponse(response);
}
```

---

## 5. 로컬 테스트 방법 (Postman)

### 요청

* Method: `POST`
* URL:

```
http://localhost:8080/api/v1/rag/ask
```

* Headers:

```
Content-Type: application/json
```

* Body:

```json
{
  "question": "에러코드 E01의 원인이 뭐야?"
}
```

---

### 기대 결과

* Status: `200 OK`
* Response에 다음 필드 포함

  * `answer`
  * `retrievedDocs`

---

## 6. 프로젝트 구조

```
ai
 ├─ controller
 │   └─ RagController.java
 ├─ dto
 │   ├─ AskRequest.java
 │   └─ AskResponse.java
 └─ service
     ├─ RagService.java
     └─ RagServiceImpl.java
```
---

## 7. AI 상담 요약 기능 (STT 기반)

### 7.1 개요

HearO는 상담 중 수집된 **STT(Speech-To-Text) 상담 로그**를 기반으로
AI(GMS, OpenAI 호환)를 활용한 **상담 요약 기능**을 제공합니다.

> 본 기능은 **RAG 시스템과 독립적으로 동작**하며,
> 상담 종료 전/후에 요약을 생성하여 프론트엔드에 반환합니다.

---

### 7.2 STT 데이터 성격

* STT는 **로그인 로그가 아닌 상담 로그(업무 로그)** 입니다.
* 상담 세션(Consultation) 단위로 생성됩니다.
* AI 요약 생성 시 DB에 즉시 저장되지 않습니다.

STT 예시:

```text
[00:01] 고객: 냉장고가 갑자기 작동하지 않습니다.
[00:10] 상담원: 전원 플러그를 확인해보셨을까요?
[00:20] 고객: 확인했는데도 동일합니다.
```
---

### 7.3 전체 흐름 (AI 요약)

```
Client (상담 화면)
  └─ STT 로그 수집
        ↓
Client
  └─ POST /api/ai/consultations/summary
        ↓
Backend (Spring Boot)
  └─ GMS(OpenAI 호환) 서버 호출
        ↓
GMS
  └─ STT 기반 요약 생성
        ↓
Backend
  └─ 요약 결과 반환
        ↓
Client
  └─ 상담 종료 모달 / 미리보기 표시
```

---

### 7.4 AI 요약 생성 API

#### URL

```http
POST /api/ai/consultations/summary
```

---

#### Request Body

```json
{
  "fullTranscript": "고객: 냉장고가 작동하지 않습니다.\n상담원: 전원 플러그를 확인해보셨을까요?"
}
```

| 필드             | 설명            |
| -------------- | ------------- |
| fullTranscript | 상담 STT 전체 텍스트 |

---

#### Response (200 OK)

```json
{
  "aiSummary": "- 이슈 요약: 냉장고 작동 불량\n- 고객 요청: 원인 확인 요청\n- 상담원 안내: 전원 상태 점검 안내\n- 후속 조치: 필요 시 AS 접수 안내"
}
```

| 필드        | 설명            |
| --------- | ------------- |
| aiSummary | AI가 생성한 상담 요약 |

---

### 7.5 요약 생성 정책

* STT에 포함되지 않은 정보는 **추측하지 않음**
* 시간 정보(타임스탬프)는 요약 결과에 포함하지 않음
* 한국어로 응답
* 다음 구조를 유지하여 요약

```text
- 이슈 요약
- 고객 요청
- 상담원 안내
- 후속 조치
```

---

### 7.6 저장 시점

* AI 요약 생성 API는 **DB를 수정하지 않음**
* 상담 종료 시 다음 데이터와 함께 저장됨:

| 항목                | 설명        |
| ----------------- | --------- |
| fullTranscript    | 상담 STT 전체 |
| aiSummary         | AI 요약     |
| userMemo          | 상담원 메모    |
| profanityCount    | 욕설 감지 횟수  |
| terminationReason | 상담 종료 사유  |
| durationSeconds   | 상담 시간     |

---

### 7.7 관련 엔티티

* `Consultation`

  * `fullTranscript`
  * `aiSummary`
  * `userMemo`
  * `profanityCount`
  * `terminationReason`
  * `durationSeconds`

---

### 7.8 프로젝트 구조 (AI 요약)

```
ai
 ├─ controller
 │   └─ ConsultationAiController.java
 ├─ dto
 │   ├─ ConsultationSummaryRequest.java
 │   └─ ConsultationSummaryResponse.java
 ├─ client
 │   └─ GmsClient.java
 └─ service
     └─ ConsultationSummaryService.java
```

---


