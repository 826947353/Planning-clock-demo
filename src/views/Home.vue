<template>
  <div class="home-page">
    <header class="home-header">
      <div class="date-display">{{ formattedDate }}</div>
    </header>

    <section class="mood-status">
      <div class="status-card glass-card">
        <div class="status-label">今日状态</div>
        <div class="status-mood">
          <span class="mood-emoji">{{ currentMood.emoji }}</span>
          <span class="mood-text">{{ currentMood.text }}</span>
        </div>
        <div class="status-divider"></div>
        <div class="status-location">
          <span class="location-label">现在</span>
          <span class="location-text">{{ weather.city }} · {{ weather.condition }}</span>
        </div>
      </div>
    </section>

    <section class="question-section" v-if="!timerStore.isRunning && !showSummary">
      <h2 class="question-text">想做点什么？</h2>
      <div class="recommendations">
        <div
          v-for="(rec, index) in recommendations"
          :key="rec.id"
          class="rec-card glass-card"
          :class="{ selected: selectedRec?.id === rec.id }"
          :style="{ animationDelay: index * 0.1 + 's' }"
          @click="selectRecommendation(rec)"
        >
          <div class="rec-left">
            <span class="rec-checkbox">
              <svg v-if="selectedRec?.id === rec.id" viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
              </svg>
              <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <rect x="3" y="3" width="18" height="18" rx="4" ry="4"/>
              </svg>
            </span>
            <div class="rec-info">
              <span class="rec-title">{{ rec.title }}</span>
              <span class="rec-desc">{{ rec.description }}</span>
            </div>
          </div>
          <span class="rec-duration">{{ rec.durationMinutes }}分钟</span>
        </div>
      </div>

      <button
        class="start-btn"
        :disabled="!selectedRec"
        @click="startFocus"
      >
        <span class="btn-glow"></span>
        <svg viewBox="0 0 24 24" fill="currentColor">
          <polygon points="5 3 19 12 5 21 5 3"/>
        </svg>
        开始专注
      </button>
    </section>

    <section class="timer-section" v-if="timerStore.isRunning">
      <div class="timer-circle-wrapper">
        <svg class="timer-ring" viewBox="0 0 200 200">
          <circle
            cx="100" cy="100" r="90"
            fill="none"
            stroke="rgba(255,255,255,0.05)"
            stroke-width="4"
          />
          <circle
            cx="100" cy="100" r="90"
            fill="none"
            :stroke="ringColor"
            stroke-width="4"
            stroke-linecap="round"
            :stroke-dasharray="2 * Math.PI * 90"
            :stroke-dashoffset="2 * Math.PI * 90 * (1 - timerStore.progress)"
            transform="rotate(-90 100 100)"
            class="timer-progress-ring"
          />
        </svg>
        <div class="timer-center">
          <span class="timer-text">{{ timerStore.displayTime }}</span>
          <span class="timer-title">{{ timerStore.sessionTitle }}</span>
        </div>
      </div>

      <button class="abandon-btn" @click="abandonFocus">
        <span class="btn-text">结束专注</span>
      </button>
    </section>

    <section class="summary-section" v-if="showSummary">
      <div class="summary-card glass-card">
        <div class="summary-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
          </svg>
        </div>
        <h2 class="summary-title">专注完成</h2>
        <p class="summary-detail">
          你专注了 <strong>{{ completedMinutes }} 分钟</strong>
        </p>
        <p class="summary-task">{{ completedTitle }}</p>

        <div class="summary-check">
          <label class="check-label">
            <input type="checkbox" v-model="taskCompleted" />
            <span class="check-custom"></span>
            <span>已完成该方案</span>
          </label>
        </div>

        <div class="summary-mood">
          <span class="mood-question">结束后的心情？</span>
          <div class="mood-options">
            <button
              v-for="m in afterMoods"
              :key="m.text"
              class="mood-btn"
              :class="{ picked: selectedAfterMood === m.text }"
              @click="selectedAfterMood = m.text"
            >
              {{ m.emoji }} {{ m.text }}
            </button>
          </div>
        </div>

        <div class="summary-actions">
          <button class="action-btn primary" @click="completeFocus" :disabled="completing">
            <span v-if="!completing">完成并生成总结</span>
            <span v-else>生成中...</span>
          </button>
          <button class="action-btn secondary" @click="resetAll">
            返回首页
          </button>
        </div>

        <div class="ai-summary-box" v-if="aiSummary">
          <h4>
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M12 2L2 7l10 5 10-5-10-5z"/>
              <path d="M2 17l10 5 10-5"/>
              <path d="M2 12l10 5 10-5"/>
            </svg>
            AI 专注总结
          </h4>
          <p>{{ aiSummary }}</p>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useTimerStore } from '../stores/timer'
import { recommendationApi, focusApi, aiApi } from '../api'

const timerStore = useTimerStore()
const recommendations = ref([])
const selectedRec = ref(null)
const showSummary = ref(false)
const completedMinutes = ref(0)
const completedTitle = ref('')
const taskCompleted = ref(true)
const selectedAfterMood = ref('平静')
const aiSummary = ref('')
const completing = ref(false)
const currentSessionId = ref(null)

const weather = ref({
  city: '北京',
  condition: '小雨',
  temp: '18°C',
  icon: '🌧'
})

const currentMood = ref({
  emoji: '😴',
  text: '微疲惫'
})

const afterMoods = [
  { emoji: '😌', text: '平静' },
  { emoji: '😊', text: '满足' },
  { emoji: '💪', text: '振奋' },
  { emoji: '😐', text: '一般' },
  { emoji: '😤', text: '焦躁' }
]

const ringColor = computed(() => {
  if (timerStore.progress < 0.3) return 'var(--sage)'
  if (timerStore.progress < 0.7) return 'var(--warm)'
  return 'var(--rose)'
})

const formattedDate = computed(() => {
  const now = new Date()
  const days = ['日', '一', '二', '三', '四', '五', '六']
  return `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日 星期${days[now.getDay()]}`
})

function selectRecommendation(rec) {
  selectedRec.value = rec
}

function startFocus() {
  if (!selectedRec.value) return
  const rec = selectedRec.value
  completedTitle.value = rec.title
  completedMinutes.value = rec.durationMinutes
  timerStore.startTimer(rec.durationMinutes, rec.title, rec)

  const startInterval = setInterval(() => {
    if (!timerStore.isRunning) {
      clearInterval(startInterval)
      showSummary.value = true
    }
  }, 500)
}

function abandonFocus() {
  completedMinutes.value = Math.floor((timerStore.totalSeconds - timerStore.remainingSeconds) / 60)
  timerStore.stopTimer()
  showSummary.value = true
}

async function completeFocus() {
  if (completing.value) return
  completing.value = true

  const sessionData = {
    userId: 1,
    title: completedTitle.value,
    plannedMinutes: selectedRec.value?.durationMinutes || completedMinutes.value,
    sessionType: 'SLOW_RHYTHM',
    moodBefore: currentMood.value.text
  }

  try {
    const { data } = await focusApi.startSession(sessionData)
    currentSessionId.value = data.id

    await focusApi.completeSession(
      data.id,
      completedMinutes.value,
      selectedAfterMood.value
    )

    try {
      const summaryRes = await aiApi.generateSummary(data.id)
      aiSummary.value = summaryRes.data.aiSummary || summaryRes.data
    } catch {
      aiSummary.value = 'AI 总结服务接口已预留，接入后端 API 后可自动生成专注总结报告。'
    }

  } catch {
    aiSummary.value = '专注记录已本地保存。连接后端服务后可同步到数据库。'
  }

  completing.value = false
}

function resetAll() {
  showSummary.value = false
  selectedRec.value = null
  aiSummary.value = ''
  completing.value = false
  timerStore.resetTimer()
}

onMounted(async () => {
  try {
    const { data } = await recommendationApi.getRecommendations(currentMood.value.text)
    recommendations.value = data
  } catch {
    recommendations.value = [
      { id: 1, title: '听20分钟钢琴', durationMinutes: 20, category: 'MUSIC', description: '让舒缓的钢琴曲带走疲惫', icon: 'piano' },
      { id: 2, title: '花25分钟写一些想法', durationMinutes: 25, category: 'WRITING', description: '把心中所想记录下来', icon: 'pen' },
      { id: 3, title: '发呆10分钟', durationMinutes: 10, category: 'RELAX', description: '什么都不做就是最好的充电', icon: 'cloud' },
      { id: 4, title: '做5分钟拉伸', durationMinutes: 5, category: 'SPORT', description: '舒展身体唤醒肌肉', icon: 'stretch' },
      { id: 5, title: '阅读30分钟', durationMinutes: 30, category: 'READING', description: '沉浸在书的世界里', icon: 'book' },
      { id: 6, title: '冥想15分钟', durationMinutes: 15, category: 'MEDITATION', description: '专注于一呼一吸', icon: 'meditation' }
    ]
  }
})
</script>

<style scoped>
.home-page {
  max-width: 480px;
  margin: 0 auto;
  padding: 20px 20px 40px;
}

.home-header {
  padding: 10px 0 20px;
}

.date-display {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-dim);
}

.status-card {
  display: flex;
  flex-direction: column;
  gap: 14px;
  animation: float 6s ease-in-out infinite;
}

.status-label {
  font-size: 13px;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 3px;
}

.status-mood {
  display: flex;
  align-items: center;
  gap: 14px;
}

.mood-emoji {
  font-size: 40px;
  animation: float 4s ease-in-out infinite;
}

.mood-text {
  font-size: 26px;
  font-weight: 700;
  color: var(--text);
}

.status-divider {
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--border), transparent);
}

.status-location {
  display: flex;
  align-items: center;
  gap: 12px;
}

.location-label {
  font-size: 13px;
  color: var(--text-muted);
}

.location-text {
  font-size: 16px;
  color: var(--sky);
  font-weight: 500;
}

.question-section {
  margin-top: 32px;
}

.question-text {
  font-size: 22px;
  font-weight: 700;
  color: var(--text);
  margin-bottom: 20px;
}

.recommendations {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.rec-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  padding: 16px 18px;
  animation: slide-up-enter 0.6s cubic-bezier(0.16, 1, 0.3, 1) both;
}

.rec-card.selected {
  border-color: rgba(232, 168, 124, 0.3);
  background: rgba(232, 168, 124, 0.06);
  box-shadow: var(--shadow-warm);
}

.rec-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.rec-checkbox {
  width: 24px;
  height: 24px;
  flex-shrink: 0;
  color: var(--warm);
  transition: all 0.3s ease;
}

.rec-checkbox svg {
  width: 100%;
  height: 100%;
}

.rec-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.rec-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text);
}

.rec-desc {
  font-size: 12px;
  color: var(--text-dim);
}

.rec-duration {
  font-size: 13px;
  color: var(--warm);
  font-weight: 600;
  white-space: nowrap;
  padding: 4px 12px;
  background: rgba(232, 168, 124, 0.08);
  border-radius: 20px;
}

.start-btn {
  width: 100%;
  margin-top: 28px;
  padding: 18px;
  border-radius: var(--radius);
  background: linear-gradient(135deg, var(--warm-deep), var(--warm));
  color: white;
  font-size: 17px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
  position: relative;
  overflow: hidden;
}

.start-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.start-btn:not(:disabled):hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 40px rgba(232, 168, 124, 0.3);
}

.start-btn svg {
  width: 20px;
  height: 20px;
}

.btn-glow {
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.1), transparent);
  background-size: 200% 100%;
  animation: shimmer 3s ease-in-out infinite;
}

.timer-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 40px;
  gap: 30px;
}

.timer-circle-wrapper {
  position: relative;
  width: 220px;
  height: 220px;
}

.timer-ring {
  width: 100%;
  height: 100%;
  filter: drop-shadow(0 0 20px rgba(232, 168, 124, 0.2));
}

.timer-progress-ring {
  transition: stroke-dashoffset 1s linear, stroke 1s ease;
}

.timer-center {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.timer-text {
  font-size: 44px;
  font-weight: 300;
  font-variant-numeric: tabular-nums;
  color: var(--text);
  letter-spacing: 4px;
}

.timer-title {
  font-size: 14px;
  color: var(--text-dim);
}

.abandon-btn {
  padding: 14px 40px;
  border-radius: 28px;
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: var(--text-dim);
  font-size: 15px;
  transition: all 0.4s ease;
  position: relative;
  overflow: hidden;
}

.abandon-btn:hover {
  border-color: var(--rose);
  color: var(--rose);
  box-shadow: 0 0 20px rgba(224, 160, 160, 0.1);
}

.summary-section {
  margin-top: 32px;
}

.summary-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 18px;
  text-align: center;
}

.summary-icon {
  width: 56px;
  height: 56px;
  color: var(--rose);
  animation: float 4s ease-in-out infinite;
}

.summary-icon svg {
  width: 100%;
  height: 100%;
}

.summary-title {
  font-size: 26px;
  font-weight: 700;
  color: var(--sage);
}

.summary-detail {
  font-size: 16px;
  color: var(--text-dim);
}

.summary-detail strong {
  color: var(--sage);
  font-size: 20px;
}

.summary-task {
  font-size: 14px;
  color: var(--text-muted);
}

.summary-check {
  width: 100%;
}

.check-label {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  font-size: 15px;
  color: var(--text);
  cursor: pointer;
}

.check-label input[type="checkbox"] {
  display: none;
}

.check-custom {
  width: 22px;
  height: 22px;
  border: 2px solid var(--border);
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  position: relative;
}

.check-label input[type="checkbox"]:checked + .check-custom {
  background: var(--sage);
  border-color: var(--sage);
}

.check-label input[type="checkbox"]:checked + .check-custom::after {
  content: '';
  width: 6px;
  height: 10px;
  border: solid white;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
}

.summary-mood {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.mood-question {
  font-size: 14px;
  color: var(--text-dim);
}

.mood-options {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: center;
}

.mood-btn {
  padding: 10px 16px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid var(--border);
  color: var(--text-dim);
  font-size: 13px;
  transition: all 0.3s ease;
}

.mood-btn.picked {
  border-color: var(--warm);
  background: rgba(232, 168, 124, 0.1);
  color: var(--warm-light);
  box-shadow: 0 0 20px rgba(232, 168, 124, 0.1);
}

.mood-btn:hover {
  border-color: rgba(255, 255, 255, 0.15);
  transform: translateY(-2px);
}

.summary-actions {
  display: flex;
  flex-direction: column;
  width: 100%;
  gap: 10px;
  margin-top: 8px;
}

.action-btn {
  width: 100%;
  padding: 16px;
  border-radius: var(--radius-sm);
  font-size: 15px;
  font-weight: 600;
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

.action-btn.primary {
  background: linear-gradient(135deg, var(--sage), #7ab070);
  color: white;
}

.action-btn.primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(168, 192, 160, 0.3);
}

.action-btn.primary:disabled {
  opacity: 0.6;
}

.action-btn.secondary {
  background: transparent;
  border: 1px solid var(--border);
  color: var(--text-dim);
}

.action-btn.secondary:hover {
  border-color: rgba(255, 255, 255, 0.15);
  color: var(--text);
}

.ai-summary-box {
  width: 100%;
  margin-top: 8px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.02);
  border-radius: var(--radius-sm);
  border: 1px solid var(--border);
  text-align: left;
}

.ai-summary-box h4 {
  font-size: 14px;
  color: var(--lavender);
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.ai-summary-box h4 svg {
  width: 18px;
  height: 18px;
}

.ai-summary-box p {
  font-size: 13px;
  color: var(--text-dim);
  line-height: 1.8;
}

/* ========== PC端自适应 ========== */
@media (min-width: 768px) {
  .home-page {
    max-width: 640px;
    padding: 32px 32px 60px;
  }

  .home-header {
    padding: 20px 0 32px;
  }

  .date-display {
    font-size: 22px;
  }

  .status-card {
    gap: 18px;
  }

  .mood-emoji {
    font-size: 52px;
  }

  .mood-text {
    font-size: 32px;
  }

  .location-text {
    font-size: 20px;
  }

  .question-section {
    margin-top: 48px;
  }

  .question-text {
    font-size: 28px;
    margin-bottom: 28px;
  }

  .recommendations {
    gap: 14px;
  }

  .rec-card {
    padding: 20px 24px;
  }

  .rec-checkbox {
    width: 28px;
    height: 28px;
  }

  .rec-title {
    font-size: 17px;
  }

  .rec-desc {
    font-size: 14px;
  }

  .rec-duration {
    font-size: 15px;
  }

  .start-btn {
    padding: 22px;
    font-size: 20px;
    margin-top: 32px;
  }

  .timer-circle-wrapper {
    width: 280px;
    height: 280px;
  }

  .timer-text {
    font-size: 56px;
  }

  .timer-title {
    font-size: 16px;
  }

  .abandon-btn {
    padding: 16px 52px;
    font-size: 17px;
  }

  .summary-card {
    gap: 22px;
  }

  .summary-icon {
    width: 64px;
    height: 64px;
  }

  .summary-title {
    font-size: 30px;
  }

  .summary-detail {
    font-size: 18px;
  }

  .mood-btn {
    padding: 12px 20px;
    font-size: 15px;
  }

  .action-btn {
    padding: 18px;
    font-size: 17px;
  }
}

@media (min-width: 1024px) {
  .home-page {
    max-width: 720px;
    padding: 40px 40px 80px;
  }

  .question-text {
    font-size: 32px;
  }

  .timer-circle-wrapper {
    width: 320px;
    height: 320px;
  }

  .timer-text {
    font-size: 64px;
  }
}
</style>