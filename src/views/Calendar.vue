<template>
  <div class="calendar-page">
    <header class="page-header">
      <h1 class="page-title">📅 专注日历</h1>
      <p class="page-subtitle">回顾每一次专注的脚印</p>
    </header>

    <div class="calendar-header">
      <button class="nav-btn" @click="prevMonth">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <polyline points="15 18 9 12 15 6"/>
        </svg>
      </button>
      <h2 class="current-month">{{ currentYear }}年{{ currentMonth + 1 }}月</h2>
      <button class="nav-btn" @click="nextMonth">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <polyline points="9 18 15 12 9 6"/>
        </svg>
      </button>
    </div>

    <div class="calendar-grid glass-card">
      <div class="week-header">
        <div class="week-cell" v-for="d in weekNames" :key="d">{{ d }}</div>
      </div>
      <div class="days-grid">
        <div
          v-for="cell in calendarDays"
          :key="cell.date"
          class="day-cell"
          :class="{
            other: cell.isOtherMonth,
            today: cell.isToday,
            hasFocus: cell.focusCount > 0,
            selected: selectedDate === cell.date,
            highFocus: cell.focusMinutes >= 60,
          }"
          @click="selectDay(cell.date)"
        >
          <span class="day-number">{{ cell.day }}</span>
          <span v-if="cell.focusCount > 0" class="day-dot" :style="{ background: cell.focusMinutes >= 60 ? 'var(--warm)' : cell.focusMinutes >= 30 ? 'var(--sky)' : 'var(--sage)' }"></span>
        </div>
      </div>
    </div>

    <div v-if="selectedSessionList.length > 0" class="selected-day-info">
      <h3>{{ formatSelectedDate }} (共 {{ selectedSessionList.length }} 次专注)</h3>
      <div class="session-list">
        <div
          v-for="session in selectedSessionList"
          :key="session.id"
          class="session-item glass-card"
          @click="toggleDetail(session)"
        >
          <div class="session-header">
            <span class="session-title">{{ session.title }}</span>
            <span class="session-duration">{{ session.actualMinutes || session.plannedMinutes }}分钟</span>
          </div>
          <div class="session-meta">
            <span class="session-time">{{ formatTime(session.startTime) }}</span>
            <span class="session-status" :class="session.status">{{ session.status === 'COMPLETED' ? '已完成' : session.status === 'ABANDONED' ? '已终止' : '进行中' }}</span>
          </div>

          <div v-show="expandedSessionId === session.id && session.aiSummary" class="session-summary">
            <h4>AI 总结</h4>
            <p>{{ session.aiSummary }}</p>
          </div>

          <div v-if="session.moodBefore || session.moodAfter" class="session-moods">
            <span v-if="session.moodBefore">开始: {{ session.moodBefore }}</span>
            <span v-if="session.moodAfter"> → {{ session.moodAfter }}</span>
          </div>
        </div>
      </div>
    </div>

    <div v-else-if="selectedDate" class="empty-day">
      <p>这天还没有专注记录哦</p>
    </div>

    <div class="month-stats">
      <div class="stat-item">
        <span class="stat-value">{{ monthTotalSessions }}</span>
        <span class="stat-label">本月专注次数</span>
      </div>
      <div class="stat-item">
        <span class="stat-value">{{ monthTotalMinutes }}</span>
        <span class="stat-label">本月分钟数</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { focusApi } from '../api'

const weekNames = ['日', '一', '二', '三', '四', '五', '六']
const today = new Date()
const currentYear = ref(today.getFullYear())
const currentMonth = ref(today.getMonth())
const selectedDate = ref(null)
const expandedSessionId = ref(null)
const allSessions = ref([])
const dayFocusMap = ref({})

const calendarDays = computed(() => {
  const year = currentYear.value
  const month = currentMonth.value
  const firstDay = new Date(year, month, 1)
  const lastDay = new Date(year, month + 1, 0)
  const startDayOfWeek = firstDay.getDay()
  const prevLastDay = new Date(year, month, 0).getDate()

  const result = []

  for (let i = 0; i < startDayOfWeek; i++) {
    const day = prevLastDay - i
    const dateStr = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`
    result.push({
      date: dateStr,
      day,
      isOtherMonth: true,
      focusCount: dayFocusMap.value[dateStr]?.count || 0,
      focusMinutes: dayFocusMap.value[dateStr]?.minutes || 0,
      isToday: isSameDay(year, month + 1, day)
    })
  }

  result.reverse()

  for (let day = 1; day <= lastDay.getDate(); day++) {
    const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`
    result.push({
      date: dateStr,
      day,
      isOtherMonth: false,
      focusCount: dayFocusMap.value[dateStr]?.count || 0,
      focusMinutes: dayFocusMap.value[dateStr]?.minutes || 0,
      isToday: isSameDay(year, month + 1, day)
    })
  }

  const remaining = 42 - result.length
  let nextMonth = month + 1
  let nextYear = year
  if (nextMonth > 11) {
    nextMonth = 0
    nextYear = nextYear + 1
  }
  for (let day = 1; day <= remaining; day++) {
    const dateStr = `${nextYear}-${String(nextMonth + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`
    result.push({
      date: dateStr,
      day,
      isOtherMonth: true,
      focusCount: dayFocusMap.value[dateStr]?.count || 0,
      focusMinutes: dayFocusMap.value[dateStr]?.minutes || 0,
      isToday: isSameDay(nextYear, nextMonth + 1, day)
    })
  }

  return result
})

function isSameDay(y, m, d) {
  return today.getFullYear() === y && today.getMonth() + 1 === m && today.getDate() === d
}

function prevMonth() {
  if (currentMonth.value === 0) {
    currentMonth.value = 11
    currentYear.value--
  } else {
    currentMonth.value--
  }
  loadMonthData()
}

function nextMonth() {
  if (currentMonth.value === 11) {
    currentMonth.value = 0
    currentYear.value++
  } else {
    currentMonth.value++
  }
  loadMonthData()
}

function selectDay(date) {
  selectedDate.value = date
  expandedSessionId.value = null
}

function toggleDetail(session) {
  if (expandedSessionId.value === session.id) {
    expandedSessionId.value = null
  } else {
    expandedSessionId.value = session.id
  }
}

function formatTime(timeStr) {
  if (!timeStr) return ''
  const d = new Date(timeStr)
  return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

const formatSelectedDate = computed(() => {
  if (!selectedDate.value) return ''
  const [y, m, d] = selectedDate.value.split('-')
  return `${y}年${parseInt(m)}月${parseInt(d)}日`
})

const selectedSessionList = computed(() => {
  if (!selectedDate.value) return []
  const [y, m, d] = selectedDate.value.split('-')
  const start = new Date(+y, +m - 1, +d, 0, 0, 0)
  const end = new Date(+y, +m - 1, +d, 23, 59, 59)
  return allSessions.value.filter(s => {
    const st = new Date(s.startTime)
    return st >= start && st <= end
  }).sort((a, b) => new Date(b.startTime) - new Date(a.startTime))
})

const monthTotalSessions = computed(() => {
  return Object.values(dayFocusMap.value).reduce((sum, d) => sum + (d.count || 0), 0)
})

const monthTotalMinutes = computed(() => {
  return Object.values(dayFocusMap.value).reduce((sum, d) => sum + (d.minutes || 0), 0)
})

function loadMonthData() {
  const startOfMonth = new Date(currentYear.value, currentMonth.value, 1)
  const endOfMonth = new Date(currentYear.value, currentMonth.value + 1, 0, 23, 59, 59)
  focusApi.getSessionsByRange(1, startOfMonth.toISOString(), endOfMonth.toISOString())
    .then(res => {
      allSessions.value = res.data
      buildDayMap(res.data)
    })
    .catch(() => {
      const mock = [
        { id: 1, title: '听20分钟钢琴', plannedMinutes: 20, actualMinutes: 20, status: 'COMPLETED', startTime: new Date(currentYear.value, currentMonth.value, today.getDate() - 2, 14, 30).toISOString(), aiSummary: '今天的冥想让我内心更加平静，收获了很多。', moodBefore: '疲惫', moodAfter: '平静' },
        { id: 2, title: '冥想15分钟', plannedMinutes: 15, actualMinutes: 15, status: 'COMPLETED', startTime: new Date(currentYear.value, currentMonth.value, today.getDate() - 1, 10, 0).toISOString(), aiSummary: null, moodBefore: '焦躁', moodAfter: '平静' },
        { id: 3, title: '阅读30分钟', plannedMinutes: 30, actualMinutes: 25, status: 'COMPLETED', startTime: new Date(currentYear.value, currentMonth.value, today.getDate(), 9, 0).toISOString(), aiSummary: '在阅读中获得了启发，这段专注很有价值。', moodBefore: '平静', moodAfter: '满足' }
      ]
      allSessions.value = mock
      buildDayMap(mock)
    })
}

function buildDayMap(sessions) {
  const map = {}
  sessions.forEach(s => {
    const d = new Date(s.startTime)
    const key = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
    if (!map[key]) {
      map[key] = { count: 0, minutes: 0 }
    }
    map[key].count++
    map[key].minutes += s.actualMinutes || s.plannedMinutes
  })
  dayFocusMap.value = map
}

onMounted(() => {
  loadMonthData()
})
</script>

<style scoped>
.calendar-page {
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
  border-radius: var(--radius-sm);
}

.calendar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 4px 12px;
}

.calendar-header .nav-btn {
  width: 36px;
  height: 36px;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-dim);
  transition: all 0.3s ease;
}

.calendar-header .nav-btn:hover {
  border-color: var(--warm);
  color: var(--warm);
}

.calendar-header .nav-btn svg {
  width: 18px;
  height: 18px;
}

.current-month {
  font-size: 18px;
  font-weight: 600;
  color: var(--text);
}

.calendar-grid {
  padding: 16px;
  margin-bottom: 16px;
}

.week-header {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
  margin-bottom: 12px;
}

.week-cell {
  text-align: center;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-muted);
  padding: 4px 0;
}

.days-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
}

.day-cell {
  aspect-ratio: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  cursor: pointer;
  position: relative;
  transition: all 0.3s ease;
}

.day-cell.other {
  opacity: 0.2;
}

.day-cell.today {
  background: rgba(232, 168, 124, 0.1);
  border: 1px solid var(--warm);
}

.day-cell:hover {
  background: rgba(255, 255, 255, 0.05);
}

.day-cell.selected {
  background: rgba(184, 168, 208, 0.15);
  border: 1px solid var(--lavender);
}

.day-cell.highFocus {
  background: rgba(232, 168, 124, 0.1);
}

.day-number {
  font-size: 14px;
  font-weight: 500;
  color: var(--text);
}

.day-dot {
  position: absolute;
  bottom: 6px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.selected-day-info {
  margin-top: 16px;
}

.selected-day-info h3 {
  font-size: 16px;
  color: var(--text);
  margin-bottom: 12px;
}

.session-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.session-item {
  padding: 14px 16px;
  cursor: pointer;
}

.session-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.session-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text);
}

.session-duration {
  font-size: 13px;
  color: var(--sage);
  font-weight: 600;
}

.session-meta {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  font-size: 11px;
  color: var(--text-muted);
}

.session-status {
  padding: 2px 8px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.04);
}

.session-status.COMPLETED {
  color: var(--sage);
}

.session-status.ABANDONED {
  color: var(--text-dim);
}

.session-summary {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid var(--border);
}

.session-summary h4 {
  font-size: 12px;
  color: var(--text-muted);
  margin-bottom: 4px;
}

.session-summary p {
  font-size: 13px;
  color: var(--text-dim);
  line-height: 1.6;
}

.session-moods {
  margin-top: 6px;
  font-size: 12px;
  color: var(--text-dim);
}

.empty-day {
  text-align: center;
  padding: 32px;
  color: var(--text-dim);
  background: var(--bg-card);
  border-radius: var(--radius);
  border: 1px dashed var(--border);
}

.month-stats {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}

.month-stats .stat-item {
  flex: 1;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  padding: 16px;
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 24px;
  font-weight: 700;
  color: var(--sky);
}

.stat-label {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 4px;
}

/* ========== PC端自适应 ========== */
@media (min-width: 768px) {
  .calendar-page {
    max-width: 640px;
    padding: 32px 32px 100px;
  }

  .calendar-header {
    padding: 0 8px 16px;
  }

  .calendar-header .nav-btn {
    width: 44px;
    height: 44px;
    border-radius: 12px;
  }

  .calendar-header .nav-btn svg {
    width: 22px;
    height: 22px;
  }

  .current-month {
    font-size: 24px;
  }

  .calendar-grid {
    padding: 24px;
    margin-bottom: 24px;
  }

  .week-cell {
    font-size: 14px;
    padding: 8px 0;
  }

  .day-cell {
    border-radius: 14px;
  }

  .day-number {
    font-size: 16px;
  }

  .day-dot {
    width: 8px;
    height: 8px;
    bottom: 8px;
  }

  .selected-day-info {
    margin-top: 24px;
  }

  .selected-day-info h3 {
    font-size: 20px;
    margin-bottom: 16px;
  }

  .session-item {
    padding: 18px 24px;
  }

  .session-title {
    font-size: 17px;
  }

  .session-duration {
    font-size: 15px;
  }

  .session-meta {
    font-size: 13px;
  }

  .session-summary h4 {
    font-size: 14px;
  }

  .session-summary p {
    font-size: 15px;
  }

  .month-stats {
    gap: 16px;
    margin-top: 24px;
  }

  .month-stats .stat-item {
    padding: 24px;
  }

  .stat-value {
    font-size: 32px;
  }

  .stat-label {
    font-size: 14px;
  }
}

@media (min-width: 1024px) {
  .calendar-page {
    max-width: 720px;
    padding: 40px 40px 100px;
  }

  .calendar-grid {
    padding: 28px;
  }

  .day-number {
    font-size: 18px;
  }
}
</style>