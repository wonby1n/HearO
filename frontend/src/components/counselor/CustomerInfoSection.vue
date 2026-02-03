<template>
  <CustomerInfoPanel
    :customerInfo="customerInfo || initialPlaceholder"
    :isLoading="isLoadingCustomerInfo"
    :error="customerInfoError"
  />
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useDashboardStore } from '@/stores/dashboard'
import CustomerInfoPanel from '@/components/counselor/CustomerInfoPanel.vue'
import { fetchCustomerData } from '@/services/customerService'
import { mockCustomerInfo } from '@/mocks/counselor'

const dashboardStore = useDashboardStore()

const initialPlaceholder = {
  customerName: '로딩 중...',
  phone: '-',
  productName: '-',
  warrantyStatus: { status: '-', isExpired: false, endDate: '-' },
  symptoms: []
}

const customerInfo = ref(null)
const isLoadingCustomerInfo = ref(false)
const customerInfoError = ref(null)

// 날짜 포맷팅 헬퍼
const formatDate = (dateString) => {
  if (!dateString) return '-';
  try {
    const date = new Date(dateString);
    return isNaN(date) ? '-' : `${date.getFullYear()}.${String(date.getMonth() + 1).padStart(2, '0')}.${String(date.getDate()).padStart(2, '0')}`;
  } catch { return '-'; }
}

// 보증 상태 계산 헬퍼
const getWarrantyStatus = (warrantyEndsAt) => {
  if (!warrantyEndsAt) return { status: '정보 없음', isExpired: null, endDate: null };
  const endDate = new Date(warrantyEndsAt);
  const today = new Date();
  const isExpired = endDate <= today;
  return { status: isExpired ? '만료' : '이내', isExpired, endDate: formatDate(warrantyEndsAt) };
}

// 고객 정보 로딩
const loadCustomerData = async () => {
  try {
    isLoadingCustomerInfo.value = true;
    customerInfoError.value = null;

    // 매칭 데이터에서 registrationId 가져오기
    console.log('[CustomerInfoSection] dashboardStore.matchedData:', dashboardStore.matchedData)
    const registrationId = dashboardStore.matchedData?.registrationId;
    console.log('[CustomerInfoSection] registrationId:', registrationId)

    if (!registrationId) {
      console.warn('[CustomerInfoSection] registrationId를 찾을 수 없습니다. 목 데이터 사용');
      console.warn('[CustomerInfoSection] matchedData 전체:', JSON.stringify(dashboardStore.matchedData))
      customerInfo.value = mockCustomerInfo;
      return;
    }

    console.log('[CustomerInfoSection] Registration ID:', registrationId);

    const response = await fetchCustomerData(registrationId);
    customerInfo.value = {
      id: response.id,
      customerId: response.customerId,
      customerName: response.customerName || '고객 정보 없음',
      phone: response.customerPhone || response.phone || '정보 없음',
      productName: response.productName || '정보 없음',
      productCategory: response.productCategory || '정보 없음',
      modelCode: response.modelCode || '정보 없음',
      purchaseDate: formatDate(response.manufacturedAt || response.purchaseDate),
      warrantyStatus: getWarrantyStatus(response.warrantyEndsAt),
      productImage: response.productImageUrl || '/images/default-product.png',
      errorCode: response.errorCode || '정보 없음',
      symptoms: response.symptom ? [response.symptom] : (response.symptoms || ['정보 없음']),
      consultationHistory: response.consultationHistory || []
    };
  } catch (error) {
    console.error('[CustomerInfoSection] 고객 로드 실패:', error);
    customerInfoError.value = '데이터를 불러올 수 없습니다.';
    customerInfo.value = mockCustomerInfo;
  } finally {
    isLoadingCustomerInfo.value = false;
  }
};

// 컴포넌트 마운트 시 고객 정보 로드
onMounted(() => {
  loadCustomerData()
})
</script>
