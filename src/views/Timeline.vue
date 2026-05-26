<template>
  <div class="timeline-page">
    <header class="page-header">
      <h1 class="page-title">📈 成长时间轴</h1>
      <p class="page-subtitle">见证你的每一步成长</p>
    </header>

    <div class="summary-cards">
      <div class="summary-card glass-card">
        <span class="summary-value">{{ totalSessions }}</span>
        <span class="summary-label">专注次数</span>
      </div>
      <div class="summary-card glass-card">
        <span class="summary-value">{{ totalMinutes }}</span>
        <span class="summary-label">总分钟数</span>
      </div>
      <div class="summary-card glass-card">
        <span class="summary-value">{{ totalLeaves }}</span>
        <span class="summary-label">收集树叶</span>
      </div>
    </div>

    <div class="timeline">
      <div class="timeline-line"></div>
      <div
        v-for="(event, i) in timelineEvents"
        :key="event.id"
        class="timeline-item"
        :class="{ left: i % 2 === 0, right: i % 2 === 1 }"
      >
        <div class="timeline-dot" :style="{ background: event.color }">
          <span>{{ event.icon }}</span>
        </div>
        <div class="timeline-card glass-card">
          <div class="card-header">
            <span class="card-type">{{ event.eventType }}</span>
            <span class="card-time">{{ formatTime(event.eventTime) }}</span>
          </div>
          <h4 class="card-title">{{ event.title }}</h4>
          <p class="card-desc">{{ event.description }}</p>
          <div class="card-footer" v-if="event.leavesEarned">
            <span class="leaf-badge">🍃 +{{ event.leavesEarned }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="empty-state" v-if="timelineEvents.length === 0">
      <span class="empty-icon">🌱</span>
      <p>还没有成长记录，开始你的第一次专注吧！</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { growthApi } from '../api'

const totalSessions = ref(0)
const totalMinutes = ref(0)
const totalLeaves = ref(0)
const timelineEvents = ref([])

const colorMap = {
  'FOCUS_COMPLETED': '#a8c0a0',
  'STREAK': '#d0c0a0',
  'MILESTONE': '#b8a8d0',
  'ACHIEVEMENT': '#90b8d0'
}

function formatTime(timeStr) {
  if (!timeStr) return ''
  return new Date(timeStr).toLocaleString('zh-CN')
}

onMounted(async () => {
  try {
    const { data } = await growthApi.getUserGrowth(1)
    timelineEvents.value = data.map(e => ({
      ...e,
      color: colorMap[e.eventType] || '#888',
      icon: e.eventType === 'FOCUS_COMPLETED' ? '🍅' :
            e.eventType === 'MILESTONE' ? '🏆' :
            e.eventType === 'ACHIEVEMENT' ? '⭐' : '🌟'
    }))
    totalSessions.value = data.length
    totalLeaves.value = data.reduce((sum, r) => sum + (r.leavesEarned || 0), 0)
  } catch {
    timelineEvents.value = [
      { id: 1, eventType: 'FOCUS_COMPLETED', title: '听20分钟钢琴', description: '完成了第一次慢节奏专注', leavesEarned: 1, eventTime: new Date(Date.now() - 86400000 * 2).toISOString(), color: '#a8c0a0', icon: '🍅' },
      { id: 2, eventType: 'FOCUS_COMPLETED', title: '阅读30分钟', description: '沉浸在《百年孤独》中', leavesEarned: 2, eventTime: new Date(Date.now() - 86400000).toISOString(), color: '#a8c0a0', icon: '🍅' },
      { id: 3, eventType: 'MILESTONE', title: '累计专注100分钟', description: '你已经养成了专注的好习惯', leavesEarned: 5, eventTime: new Date(Date.now() - 43200000).toISOString(), color: '#b8a8d0', icon: '🏆' },
      { id: 4, eventType: 'FOCUS_COMPLETED', title: '冥想15分钟', description: '内心平静如水', leavesEarned: 1, eventTime: new Date().toISOString(), color: '#a8c0a0', icon: '🍅' }
    ]
    totalSessions.value = 4
    totalMinutes.value = 85
    totalLeaves.value = 9
  }
})
</script>

<style scoped>
.timeline-page {
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

.summary-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin-bottom: 32px;
}

.summary-card {
  text-align: center;
  padding: 16px 10px;
}

.glass-card {
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
}

.summary-value {
  display: block;
  font-size: 26px;
  font-weight: 700;
  color: var(--warm);
}

.summary-label {
  font-size: 11px;
  color: var(--text-muted);
  margin-top: 4px;
}

.timeline {
  position: relative;
  padding: 0 0 0 24px;
}

.timeline-line {
  position: absolute;
  left: 19px;
  top: 0;
  bottom: 0;
  width: 2px;
  background: linear-gradient(to bottom, var(--sage), var(--sky), var(--lavender));
  border-radius: 1px;
}

.timeline-item {
  position: relative;
  margin-bottom: 24px;
  padding-left: 28px;
}

.timeline-dot {
  position: absolute;
  left: -17px;
  top: 12px;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  border: 3px solid var(--bg);
  z-index: 2;
}

.timeline-card {
  padding: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.card-type {
  font-size: 11px;
  padding: 2px 10px;
  border-radius: 10px;
  background: rgba(255,255,255,0.04);
  color: var(--text-dim);
}

.card-time {
  font-size: 11px;
  color: var(--text-muted);
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text);
  margin-bottom: 4px;
}

.card-desc {
  font-size: 13px;
  color: var(--text-dim);
  line-height: 1.5;
}

.card-footer {
  margin-top: 8px;
}

.leaf-badge {
  font-size: 13px;
  font-weight: 600;
  color: var(--sage);
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 48px;
  display: block;
  margin-bottom: 16px;
}

.empty-state p {
  font-size: 15px;
  color: var(--text-dim);
}

/* ========== PC端自适应 ========== */
@media (min-width: 768px) {
  .timeline-page {
    max-width: 640px;
    padding: 32px 32px 100px;
  }

  .summary-cards {
    gap: 16px;
    margin-bottom: 40px;
  }

  .summary-card {
    padding: 24px 16px;
  }

  .summary-value {
    font-size: 36px;
  }

  .summary-label {
    font-size: 14px;
  }

  .timeline {
    padding: 0 0 0 32px;
  }

  .timeline-line {
    left: 27px;
  }

  .timeline-item {
    margin-bottom: 32px;
    padding-left: 40px;
  }

  .timeline-dot {
    left: -19px;
    top: 16px;
    width: 44px;
    height: 44px;
    font-size: 20px;
  }

  .timeline-card {
    padding: 20px;
  }

  .card-type {
    font-size: 13px;
    padding: 4px 14px;
  }

  .card-time {
    font-size: 13px;
  }

  .card-title {
    font-size: 17px;
  }

  .card-desc {
    font-size: 15px;
  }

  .leaf-badge {
    font-size: 15px;
  }
}

@media (min-width: 1024px) {
  .timeline-page {
    max-width: 720px;
    padding: 40px 40px 100px;
  }

  .summary-value {
    font-size: 42px;
  }
}
</style>