# 고객 상담 신청 플로우 API 명세서

## 개요
고객이 상담을 신청하는 3단계 플로우에 필요한 API 명세입니다.

---

## 1. 고객 로그인 API

### Endpoint
```
POST /api/v1/auth/customer/login
```

### 설명
고객이 이름과 휴대폰 번호를 입력하고 인증 버튼을 누르면 호출됩니다.
- 기존 고객이면 조회, 신규 고객이면 자동 생성
- JWT 액세스 토큰 발급

### Request Body
```json
{
  "name": "홍길동",
  "phone": "01012345678"
}
```

#### 필드 설명
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| name | String | O | 고객 이름 (최대 20자) |
| phone | String | O | 휴대폰 번호 (11자리 숫자만, 하이픈 없음) |

#### Validation
- `name`: NotBlank
- `phone`: NotBlank, Pattern: `^\d{11}$`

### Response (200 OK)
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "customerId": 123,
  "name": "홍길동",
  "phone": "01012345678"
}
```

#### 필드 설명
| 필드 | 타입 | 설명 |
|------|------|------|
| accessToken | String | JWT 액세스 토큰 (대기열 등록 시 사용) |
| customerId | Integer | 고객 ID |
| name | String | 고객 이름 |
| phone | String | 휴대폰 번호 |

### Error Responses

#### 400 Bad Request - Validation 실패
```json
{
  "error": "Bad Request",
  "message": "휴대폰 번호는 11자리 숫자만 입력 가능합니다"
}
```

---

## 2. 대기열 등록 API (수정 필요)

### Endpoint
```
POST /api/v1/queue/register
```

### 설명
고객이 3단계(약관 동의)에서 "접수" 버튼을 누르면 호출됩니다.
- 1단계에서 입력한 증상, 에러코드, 모델코드와 함께 전송
- JWT 토큰으로 고객 인증

### Request Headers
```
Authorization: Bearer {accessToken}
```

### Request Body
```json
{
  "symptom": "화면이 켜지지 않습니다. 어제부터 갑자기 검은 화면만 나옵니다.",
  "errorCode": "ERR-001",
  "modelCode": "SM-S928N"
}
```

#### 필드 설명
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| symptom | String | O | 고객이 입력한 증상 (최대 500자) |
| errorCode | String | X | 에러코드 (선택, 최대 100자) |
| modelCode | String | O | 제품 모델 코드 |

#### Validation
- `symptom`: NotBlank
- `modelCode`: NotBlank

### Response (200 OK)
```json
{
  "isSuccess": true,
  "message": "대기열 등록이 완료되었습니다",
  "data": {
    "registrationId": 456,
    "customerId": 123,
    "rank": 5,
    "queueType": "NORMAL",
    "estimatedWaitTime": 15
  }
}
```

#### 필드 설명
| 필드 | 타입 | 설명 |
|------|------|------|
| registrationId | Integer | 등록 ID |
| customerId | Integer | 고객 ID |
| rank | Integer | 대기 순번 |
| queueType | String | 대기열 타입 (NORMAL/VIP) |
| estimatedWaitTime | Integer | 예상 대기 시간(분) |

### Error Responses

#### 401 Unauthorized - 토큰 없음/만료
```json
{
  "error": "Unauthorized",
  "message": "유효하지 않은 토큰입니다"
}
```

#### 400 Bad Request - Validation 실패
```json
{
  "error": "Bad Request",
  "message": "증상 설명은 필수입니다"
}
```

---

## 플로우 다이어그램

```
[1단계: 제품 정보 입력]
    ↓ (localStorage에 임시 저장)
    - symptom
    - errorCode
    - modelCode

[2단계: 본인 인증]
    ↓ POST /api/v1/auth/customer/login
    - name
    - phone
    ↓ (응답)
    - accessToken 받아서 localStorage 저장

[3단계: 약관 동의]
    ↓ (접수 버튼 클릭)
    ↓ POST /api/v1/queue/register
    - Authorization: Bearer {accessToken}
    - Body: symptom, errorCode, modelCode
    ↓ (성공)
    → 대기 페이지로 이동
```

---

## 구현 참고사항

### Customer 엔티티
```java
@Entity
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String phone;
}
```

### JWT 토큰 생성
- `subject`: customerId
- `claim`: name, phone
- 만료 시간: 1시간 (상담 신청용이므로 짧게 설정)

### 보안 설정
- `/api/v1/auth/customer/login`: permitAll
- `/api/v1/queue/register`: authenticated (JWT 필요)
