// src/services/clientReviewService.js
export async function submitClientReview(consultationId, reviewData) {
  const response = await fetch(`/api/v1/consultations/${consultationId}/review`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      processRating: reviewData.processRating,
      solutionRating: reviewData.solutionRating,
      kindnessRating: reviewData.kindnessRating,
      feedback: reviewData.additionalComment,
    }),
  });

  if (!response.ok) {
    throw new Error('리뷰 제출 실패');
  }

  return response.json(); // 필요 없으면 생략 가능
}
