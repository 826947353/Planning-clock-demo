import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useTimerStore = defineStore('timer', () => {
  const isRunning = ref(false)
  const totalSeconds = ref(0)
  const remainingSeconds = ref(0)
  const sessionId = ref(null)
  const sessionTitle = ref('')
  const selectedRecommendation = ref(null)
  let timerInterval = null

  const minutes = computed(() => Math.floor(remainingSeconds.value / 60))
  const seconds = computed(() => remainingSeconds.value % 60)
  const progress = computed(() => {
    if (totalSeconds.value === 0) return 0
    return 1 - remainingSeconds.value / totalSeconds.value
  })
  const displayTime = computed(() => {
    const m = String(minutes.value).padStart(2, '0')
    const s = String(seconds.value).padStart(2, '0')
    return `${m}:${s}`
  })

  function startTimer(durationMinutes, title, rec) {
    totalSeconds.value = durationMinutes * 60
    remainingSeconds.value = totalSeconds.value
    sessionTitle.value = title
    selectedRecommendation.value = rec
    isRunning.value = true

    timerInterval = setInterval(() => {
      if (remainingSeconds.value > 0) {
        remainingSeconds.value--
      } else {
        stopTimer()
      }
    }, 1000)
  }

  function stopTimer() {
    isRunning.value = false
    if (timerInterval) {
      clearInterval(timerInterval)
      timerInterval = null
    }
  }

  function resetTimer() {
    stopTimer()
    remainingSeconds.value = 0
    totalSeconds.value = 0
    sessionId.value = null
    sessionTitle.value = ''
    selectedRecommendation.value = null
  }

  return {
    isRunning,
    totalSeconds,
    remainingSeconds,
    sessionId,
    sessionTitle,
    selectedRecommendation,
    minutes,
    seconds,
    progress,
    displayTime,
    startTimer,
    stopTimer,
    resetTimer
  }
})