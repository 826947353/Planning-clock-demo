<template>
  <div class="mood-page">
    <header class="page-header">
      <h1 class="page-title">☁️ 情绪云</h1>
      <p class="page-subtitle">记录你的心绪变化</p>
    </header>

    <div class="mood-input-card glass-card">
      <div class="mood-prompt">此刻感觉如何？</div>
      <div class="mood-emojis">
        <button
          v-for="m in moodOptions"
          :key="m.text"
          class="mood-emoji-btn"
          :class="{ active: newMood === m.text }"
          @click="newMood = m.text"
        >
          <span class="emoji">{{ m.emoji }}</span>
          <span class="emoji-label">{{ m.text }}</span>
        </button>
      </div>
      <div class="intensity-row">
        <span class="intensity-label">强度</span>
        <input
          type="range"
          min="1"
          max="10"
          v-model.number="intensity"
          class="intensity-slider"
        />
        <span class="intensity-value">{{ intensity }}</span>
      </div>
      <button class="record-btn" @click="recordMood" :disabled="!newMood">
        记录此刻
      </button>
    </div>

    <div class="mood-chart glass-card">
      <h3>情绪波动</h3>
      <div class="chart-area">
        <svg viewBox="0 0 400 120" class="chart-svg">
          <defs>
            <linearGradient id="lineGrad" x1="0" y1="0" x2="1" y2="0">
              <stop offset="0%" stop-color="var(--sky)" />
              <stop offset="50%" stop-color="var(--lavender)" />
              <stop offset="100%" stop-color="var(--rose)" />
            </linearGradient>
          </defs>

          <polyline
            :points="chartPoints"
            fill="none"
            stroke="url(#lineGrad)"
            stroke-width="2.5"
            stroke-linecap="round"
            stroke-linejoin="round"
          />

          <circle
            v-for="(point, i) in chartData"
            :key="i"
            :cx="point.x"
            :cy="point.y"
            r="4"
            :fill="point.color"
            stroke="var(--bg-card)"
            stroke-width="1.5"
          />
        </svg>
      </div>
    </div>

    <div class="mood-history">
      <h3>历史记录</h3>
      <div class="history-list">
        <div
          v-for="record in history"
          :key="record.id"
          class="history-item glass-card"
        >
          <span class="history-emoji">{{ getEmoji(record.mood) }}</span>
          <div class="history-info">
            <span class="history-mood">{{ record.mood }}</span>
            <span class="history-time">{{ formatTime(record.recordedAt) }}</span>
          </div>
          <div class="history-intensity">
            <div class="intensity-bar" :style="{ width: (record.intensity || 5) * 10 + '%' }"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { moodApi } from '../api'

const newMood = ref('')
const intensity = ref(5)
const history = ref([])

const moodOptions = [
  { emoji: '😄', text: '开心' },
  { emoji: '😌', text: '平静' },
  { emoji: '😴', text: '疲惫' },
  { emoji: '😤', text: '焦躁' },
  { emoji: '😢', text: '难过' },
  { emoji: '💪', text: '振奋' },
  { emoji: '🤔', text: '思考' },
  { emoji: '😎', text: '满足' }
]

const emotionColorMap = {
  '开心': '#d0c0a0',
  '平静': '#90b8d0',
  '疲惫': '#a0a0a0',
  '焦躁': '#e0a0a0',
  '难过': '#9090a0',
  '振奋': '#a8c0a0',
  '思考': '#b8a8d0',
  '满足': '#c0b8a0'
}

function getEmoji(mood) {
  return moodOptions.find(m => m.text === mood)?.emoji || '😶'
}

const chartData = computed(() => {
  if (history.value.length === 0) return []
  return history.value.slice(0, 12).reverse().map((record, i) => {
    const x = (i / Math.max(history.value.slice(0, 12).length - 1, 1)) * 380 + 10
    const y = 100 - (record.intensity || 5) * 9
    return {
      x, y,
      color: emotionColorMap[record.mood] || '#888'
    }
  })
})

const chartPoints = computed(() => {
  return chartData.value.map(p => `${p.x},${p.y}`).join(' ')
})

function formatTime(timeStr) {
  if (!timeStr) return ''
  return new Date(timeStr).toLocaleString('zh-CN')
}

async function recordMood() {
  if (!newMood.value) return
  try {
    const { data } = await moodApi.recordMood({
      userId: 1,
      mood: newMood.value,
      intensity: intensity.value
    })
    history.value.unshift(data)
  } catch {
    history.value.unshift({
      id: Date.now(),
      mood: newMood.value,
      intensity: intensity.value,
      recordedAt: new Date().toISOString()
    })
  }
  newMood.value = ''
  intensity.value = 5
}

onMounted(async () => {
  try {
    const { data } = await moodApi.getUserMoods(1)
    history.value = data
  } catch {
    history.value = [
      { id: 1, mood: '疲惫', intensity: 4, recordedAt: new Date().toISOString() },
      { id: 2, mood: '开心', intensity: 8, recordedAt: new Date(Date.now() - 3600000).toISOString() },
      { id: 3, mood: '平静', intensity: 6, recordedAt: new Date(Date.now() - 7200000).toISOString() }
    ]
  }
})
</script>

<style scoped>
.mood-page {
  max-width: 480px;
  margin: 0 auto;
  padding: 20px 20px 100px;
}

.page-header {
  text-align: center;
  padding: 20px 0;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text);
}

.page-subtitle {
  font-size: 14px;
  color: var(--text-dim);
  margin-top: 6px;
}

.glass-card {
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: 20px;
  margin-bottom: 16px;
}

.mood-input-card {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.mood-prompt {
  font-size: 16px;
  font-weight: 600;
  color: var(--text);
}

.mood-emojis {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
}

.mood-emoji-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 10px 6px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid var(--border);
  color: var(--text-dim);
  transition: all 0.3s ease;
}

.mood-emoji-btn.active {
  border-color: var(--warm);
  background: rgba(232, 168, 124, 0.08);
  color: var(--warm-light);
  box-shadow: 0 0 20px rgba(232, 168, 124, 0.1);
}

.mood-emoji-btn:hover {
  transform: translateY(-3px);
  border-color: rgba(255, 255, 255, 0.1);
}

.emoji {
  font-size: 28px;
}

.emoji-label {
  font-size: 11px;
}

.intensity-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.intensity-label {
  font-size: 13px;
  color: var(--text-dim);
  width: 32px;
}

.intensity-slider {
  flex: 1;
  height: 4px;
  accent-color: var(--warm);
  cursor: pointer;
}

.intensity-value {
  font-size: 16px;
  font-weight: 700;
  color: var(--warm);
  width: 24px;
  text-align: center;
}

.record-btn {
  width: 100%;
  padding: 14px;
  border-radius: var(--radius-sm);
  background: linear-gradient(135deg, var(--lavender), var(--rose));
  color: white;
  font-size: 15px;
  font-weight: 600;
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

.record-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.record-btn:not(:disabled):hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(184, 168, 208, 0.3);
}

.mood-chart h3,
.mood-history h3 {
  font-size: 16px;
  color: var(--text);
  margin-bottom: 12px;
}

.chart-area {
  width: 100%;
  overflow: hidden;
}

.chart-svg {
  width: 100%;
  height: 120px;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
}

.history-emoji {
  font-size: 24px;
}

.history-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.history-mood {
  font-size: 14px;
  font-weight: 500;
  color: var(--text);
}

.history-time {
  font-size: 11px;
  color: var(--text-muted);
}

.history-intensity {
  width: 60px;
  height: 6px;
  background: var(--border);
  border-radius: 3px;
  overflow: hidden;
}

.intensity-bar {
  height: 100%;
  background: linear-gradient(90deg, var(--sky), var(--lavender));
  border-radius: 3px;
  transition: width 0.5s ease;
}

/* ========== PC端自适应 ========== */
@media (min-width: 768px) {
  .mood-page {
    max-width: 640px;
    padding: 32px 32px 100px;
  }

  .mood-emojis {
    gap: 12px;
  }

  .mood-emoji-btn {
    padding: 14px 8px;
    border-radius: 20px;
  }

  .emoji {
    font-size: 36px;
  }

  .emoji-label {
    font-size: 13px;
  }

  .mood-prompt {
    font-size: 20px;
  }

  .record-btn {
    padding: 16px;
    font-size: 17px;
  }

  .chart-svg {
    height: 160px;
  }

  .mood-chart h3,
  .mood-history h3 {
    font-size: 20px;
    margin-bottom: 16px;
  }

  .history-item {
    padding: 16px 24px;
  }

  .history-emoji {
    font-size: 28px;
  }

  .history-mood {
    font-size: 16px;
  }

  .history-time {
    font-size: 13px;
  }

  .history-intensity {
    width: 80px;
    height: 8px;
  }
}

@media (min-width: 1024px) {
  .mood-page {
    max-width: 720px;
    padding: 40px 40px 100px;
  }

  .mood-emojis {
    gap: 14px;
  }

  .emoji {
    font-size: 40px;
  }
}
</style>