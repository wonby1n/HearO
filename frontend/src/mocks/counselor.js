/**
 * 상담원 통화 화면 더미 데이터
 * 실제 API 연동 시 이 파일을 제거하고 API 응답으로 대체
 */

export const mockCustomerInfo = {
  phone: '010-1234-5678',
  productName: '삼성',
  modelNumber: 'SSAFY-E106',
  purchaseDate: '2026.01.12',
  warrantyStatus: '이내',
  productImage: null,
  symptoms: [
    '냉장실이 덜 시원함',
    '찬물이 안 얼음',
  ],
  consultationHistory: [
    {
      date: '2026.01.08',
      agent: '담당자: 나루토',
      summary: '냉장고 문제'
    }
  ]
}

export const mockSttMessages = [
  {
    speaker: 'customer',
    text: '안녕하십니까.',
    timestamp: '12:05:47',
    hasProfanity: false
  },
  {
    speaker: 'agent',
    text: '안녕하십니다. 무엇을 도와드릴까요?',
    timestamp: '12:05:50',
    hasProfanity: false
  },
  {
    speaker: 'customer',
    text: '명령하지 마라',
    timestamp: '12:05:55',
    hasProfanity: false
  },
  {
    speaker: 'customer',
    text: '미친놈이',
    timestamp: '12:05:58',
    hasProfanity: true,
    maskedText: '####',
    showOriginal: false
  }
]
