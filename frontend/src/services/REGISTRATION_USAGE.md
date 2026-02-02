# 대기열 등록 API 사용 가이드

## API 명세

### POST /api/v1/registrations

**요청 (Request Body)**
```json
{
  "symptom": "냉장고가 안 시원해요",
  "productId": 1,
  "errorCode": "E001",
  "manufacturedAt": "2025-01-01",
  "warrantyEndsAt": "2027-01-01"
}
```

**응답 (Response Body)**
```json
{
  "isSuccess": true,
  "code": 200,
  "message": "요청에 성공하였습니다.",
  "data": {
    "registrationId": 0,
    "customerId": 2,
    "waitingRank": 2,
    "queueType": "NORMAL",
    "estimatedWaitMinutes": 10,
    "websocketTopic": "/topic/queue-rank/2"
  }
}
```

## 사용 예시

### 1. 대기열 등록

```javascript
import { registerQueue } from '@/services/customerService'
import { useCallStore } from '@/stores/call'

const callStore = useCallStore()

// 대기열 등록
const handleRegisterQueue = async (formData) => {
  try {
    const result = await registerQueue({
      symptom: formData.symptom,
      productId: formData.productId,
      errorCode: formData.errorCode,
      manufacturedAt: formData.manufacturedAt,
      warrantyEndsAt: formData.warrantyEndsAt
    })

    // ✅ 응답 데이터 구조
    console.log('registrationId:', result.registrationId)  // 접수 ID
    console.log('customerId:', result.customerId)          // 고객 ID
    console.log('waitingRank:', result.waitingRank)        // 대기 순번
    console.log('queueType:', result.queueType)            // NORMAL | PRIORITY
    console.log('estimatedWaitMinutes:', result.estimatedWaitMinutes) // 예상 대기 시간
    console.log('websocketTopic:', result.websocketTopic)  // WebSocket 구독 토픽

    // ✅ Call Store에 저장 (통화 시작 준비)
    callStore.initiateCall({
      registrationId: result.registrationId,
      customerId: result.customerId,
      // roomToken과 serverUrl은 매칭 완료 후 받음
      roomToken: null,
      serverUrl: null
    })

    // ✅ 대기 화면으로 이동 (WebSocket 연결)
    router.push({
      name: 'client-waiting',
      params: {
        registrationId: result.registrationId
      },
      query: {
        waitingRank: result.waitingRank,
        estimatedWaitMinutes: result.estimatedWaitMinutes,
        websocketTopic: result.websocketTopic
      }
    })
  } catch (error) {
    console.error('대기열 등록 실패:', error)
    // 에러 처리
  }
}
```

### 2. 고객 정보 조회 (상담사용)

상담사가 통화 화면에서 고객 정보를 불러올 때:

```javascript
import { fetchCustomerData } from '@/services/customerService'
import { useCallStore } from '@/stores/call'

const callStore = useCallStore()

const loadCustomerData = async () => {
  try {
    // callStore에 저장된 registrationId 사용
    const registrationId = callStore.currentCall.registrationId

    // GET /api/v1/registrations/{registrationId} 호출
    const customerData = await fetchCustomerData(registrationId)

    console.log('고객 정보:', customerData)
    // customerData = { id, customerId, customerName, customerPhone, productName, ... }
  } catch (error) {
    console.error('고객 정보 로드 실패:', error)
  }
}
```

## 주의사항

1. **registrationId 저장**: POST 응답에서 받은 `registrationId`는 반드시 `callStore`에 저장해야 합니다.
2. **WebSocket 연결**: `websocketTopic`을 사용해 대기 순번 업데이트를 구독해야 합니다.
3. **매칭 완료 처리**: WebSocket으로 매칭 완료 알림을 받으면 `roomToken`과 `serverUrl`을 callStore에 업데이트합니다.
4. **에러 처리**: API 호출 실패 시 사용자에게 명확한 에러 메시지를 표시해야 합니다.

## 데이터 흐름

```
1. 고객: 접수 폼 작성
   ↓
2. POST /api/v1/registrations
   ↓
3. 응답: { registrationId, customerId, waitingRank, websocketTopic, ... }
   ↓
4. callStore.initiateCall({ registrationId, customerId })
   ↓
5. 대기 화면으로 이동 + WebSocket 연결
   ↓
6. 매칭 완료 알림 수신
   ↓
7. callStore 업데이트 (roomToken, serverUrl)
   ↓
8. 통화 화면으로 이동
   ↓
9. 상담사: GET /api/v1/registrations/{registrationId} 호출
   ↓
10. 고객 정보 표시
```
