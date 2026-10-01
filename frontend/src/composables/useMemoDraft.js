import { ref, computed, watch, onBeforeUnmount } from 'vue'

/**
 * useMemoDraft — 통화 메모를 localStorage에 임시 저장/복구하는 composable
 *
 * - memo            : callStore.callMemo와 연결된 v-model용 computed
 * - memoSaveLabel   : "임시 저장됨 · 14:03" 표시 문구
 * - clearMemoDraft(): 서버 저장 성공 후 드래프트 삭제 (unmount 시 재저장도 막음)
 */
export function useMemoDraft(callStore) {
  const memo = computed({
    get: () => callStore.callMemo,
    set: (val) => callStore.updateMemo(val)
  })

  const memoLastSavedAt = ref(null)
  const memoDraftKey = ref('')
  let memoSaveTimeout = null
  let skipDraftSaveOnUnmount = false

  const memoSaveLabel = computed(() => {
    if (!memoLastSavedAt.value) return memo.value?.trim().length ? '임시 저장 전' : ''
    const timeLabel = new Date(memoLastSavedAt.value).toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })
    return `임시 저장됨 · ${timeLabel}`
  })

  const getSessionDraftKey = () => {
    let storedKey = localStorage.getItem('callMemoDraftKey')
    if (!storedKey) {
      storedKey = `callMemoDraft:${Date.now()}`
      localStorage.setItem('callMemoDraftKey', storedKey)
    }
    return storedKey
  }

  const resolveMemoDraftKey = (callId) => {
    return callId ? `callMemoDraft:${callId}` : getSessionDraftKey()
  }

  const loadMemoDraft = () => {
    if (!memoDraftKey.value) return
    try {
      const raw = localStorage.getItem(memoDraftKey.value)
      if (!raw) return
      const parsed = JSON.parse(raw)
      const draftText = typeof parsed === 'string' ? parsed : parsed?.memo
      if (draftText && memo.value.trim().length === 0) {
        callStore.updateMemo(draftText)
      }
      if (parsed?.savedAt) memoLastSavedAt.value = parsed.savedAt
    } catch (error) {
      console.warn('메모 드래프트 로드 실패:', error)
    }
  }

  const saveMemoDraft = (value) => {
    if (!memoDraftKey.value) return
    if (!value || value.trim().length === 0) {
      localStorage.removeItem(memoDraftKey.value)
      memoLastSavedAt.value = null
      return
    }
    const payload = { memo: value, savedAt: Date.now() }
    localStorage.setItem(memoDraftKey.value, JSON.stringify(payload))
    memoLastSavedAt.value = payload.savedAt
  }

  const removeDraft = (key) => {
    if (memoSaveTimeout) { clearTimeout(memoSaveTimeout); memoSaveTimeout = null }
    if (key) localStorage.removeItem(key)
    memoLastSavedAt.value = null
  }

  const clearMemoDraft = () => {
    removeDraft(memoDraftKey.value)
    skipDraftSaveOnUnmount = true
  }

  // 메모 변경 시 500ms 디바운스 자동 저장
  watch(memo, (newValue) => {
    if (memoSaveTimeout) clearTimeout(memoSaveTimeout)
    memoSaveTimeout = setTimeout(() => saveMemoDraft(newValue), 500)
  })

  // 콜 변경 시 드래프트 키 갱신
  watch(() => callStore.currentCall?.id, (newId) => {
    const nextKey = resolveMemoDraftKey(newId)
    const previousKey = memoDraftKey.value
    if (previousKey && previousKey !== nextKey) removeDraft(previousKey)
    memoDraftKey.value = nextKey
    skipDraftSaveOnUnmount = false
    loadMemoDraft()
  }, { immediate: true })

  onBeforeUnmount(() => {
    if (memoSaveTimeout) clearTimeout(memoSaveTimeout)
    if (!skipDraftSaveOnUnmount && memo.value?.trim().length) saveMemoDraft(memo.value)
  })

  return { memo, memoSaveLabel, clearMemoDraft }
}
