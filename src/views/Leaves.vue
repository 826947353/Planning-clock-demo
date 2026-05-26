<template>
  <div class="leaves-page">
    <header class="page-header">
      <h1 class="page-title">🍃 树叶集</h1>
      <p class="page-subtitle">每片叶子都是你专注的见证</p>
    </header>

    <div class="stats-row">
      <div class="stat-card glass-card">
        <span class="stat-number">{{ totalLeaves }}</span>
        <span class="stat-label">收集的树叶</span>
      </div>
      <div class="stat-card glass-card">
        <span class="stat-number">{{ totalMinutes }}</span>
        <span class="stat-label">专注分钟</span>
      </div>
    </div>

    <div class="leaves-garden">
      <div class="garden-tree">
        <div class="tree-trunk"></div>
        <div class="tree-branches">
          <div
            v-for="i in 30"
            :key="i"
            class="leaf-wrapper"
            :style="leafStyle(i)"
          >
            <svg viewBox="0 0 40 50" class="leaf-svg">
              <path
                d="M20 5 C30 10, 38 20, 35 32 C32 42, 20 48, 20 48 C20 48, 8 42, 5 32 C2 20, 10 10, 20 5Z"
                :fill="leafColor(i)"
                :opacity="i <= totalLeaves ? 1 : 0.15"
              />
              <line x1="20" y1="48" x2="20" y2="40" stroke="#5a8a5a" stroke-width="1" />
            </svg>
            <span class="leaf-label" v-if="i <= totalLeaves">+1</span>
          </div>
        </div>
      </div>
    </div>

    <div class="recent-leaves" v-if="recentLeaves.length > 0">
      <h3>最近收获</h3>
      <div class="leaf-list">
        <div v-for="leaf in recentLeaves" :key="leaf.id" class="leaf-item glass-card">
          <span class="leaf-icon">🍃</span>
          <div class="leaf-info">
            <span class="leaf-name">{{ leaf.title }}</span>
            <span class="leaf-time">{{ formatTime(leaf.eventTime) }}</span>
          </div>
          <span class="leaf-count">+{{ leaf.leavesEarned }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { growthApi } from '../api'

const totalLeaves = ref(0)
const totalMinutes = ref(0)
const recentLeaves = ref([])

function leafColor(i) {
  const colors = [
    '#a8c0a0', '#b8c8a8', '#c0d0b0', '#c8d8b8',
    '#d0c0a0', '#d8c8a8', '#e0d0b0', '#e8d8b8'
  ]
  return colors[i % colors.length]
}

function leafStyle(i) {
  const angle = (i / 30) * Math.PI * 2
  const radius = 100 + Math.sin(i * 0.7) * 30
  const x = Math.cos(angle) * radius
  const y = Math.sin(angle) * radius - 20
  const rotation = Math.random() * 60 - 30
  return {
    left: `calc(50% + ${x}px)`,
    top: `calc(50% + ${y}px)`,
    transform: `rotate(${rotation}deg)`
  }
}

function formatTime(timeStr) {
  if (!timeStr) return ''
  return new Date(timeStr).toLocaleString('zh-CN')
}

onMounted(async () => {
  try {
    const { data } = await growthApi.getUserGrowth(1)
    recentLeaves.value = data.slice(0, 8)
    totalLeaves.value = data.reduce((sum, r) => sum + (r.leavesEarned || 0), 0)
  } catch {
    totalLeaves.value = 8
    totalMinutes.value = 240
    recentLeaves.value = [
      { id: 1, title: '听20分钟钢琴', leavesEarned: 1, eventTime: new Date().toISOString() },
      { id: 2, title: '阅读30分钟', leavesEarned: 2, eventTime: new Date(Date.now() - 86400000).toISOString() }
    ]
  }
})
</script>

<style scoped>
.leaves-page {
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

.stats-row {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.stat-card {
  flex: 1;
  text-align: center;
}

.stat-number {
  display: block;
  font-size: 32px;
  font-weight: 700;
  color: var(--sage);
}

.stat-label {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 4px;
}

.leaves-garden {
  height: 400px;
  position: relative;
  overflow: hidden;
  background: radial-gradient(ellipse at center bottom, rgba(168, 192, 160, 0.05) 0%, transparent 60%);
  border-radius: var(--radius);
}

.garden-tree {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
}

.tree-trunk {
  width: 8px;
  height: 60px;
  background: linear-gradient(to top, #5a3a1a, #8B6914);
  margin: 0 auto;
  border-radius: 4px;
}

.tree-branches {
  position: absolute;
  bottom: 50px;
  left: 50%;
  width: 0;
  height: 0;
}

.leaf-wrapper {
  position: absolute;
  width: 36px;
  height: 44px;
  transition: all 0.5s ease;
}

.leaf-svg {
  width: 100%;
  height: 100%;
}

.leaf-label {
  position: absolute;
  bottom: -14px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 10px;
  color: var(--sage);
  font-weight: 700;
  white-space: nowrap;
}

.recent-leaves {
  margin-top: 24px;
}

.recent-leaves h3 {
  font-size: 16px;
  color: var(--text);
  margin-bottom: 12px;
}

.leaf-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.leaf-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
}

.leaf-icon {
  font-size: 24px;
}

.leaf-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.leaf-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text);
}

.leaf-time {
  font-size: 11px;
  color: var(--text-muted);
}

.leaf-count {
  font-size: 16px;
  font-weight: 700;
  color: var(--sage);
}

/* ========== PC端自适应 ========== */
@media (min-width: 768px) {
  .leaves-page {
    max-width: 640px;
    padding: 32px 32px 100px;
  }

  .stats-row {
    gap: 16px;
    margin-bottom: 32px;
  }

  .stat-number {
    font-size: 40px;
  }

  .stat-label {
    font-size: 14px;
  }

  .leaves-garden {
    height: 480px;
  }

  .leaf-wrapper {
    width: 44px;
    height: 54px;
  }

  .tree-trunk {
    width: 10px;
    height: 80px;
  }

  .recent-leaves {
    margin-top: 32px;
  }

  .recent-leaves h3 {
    font-size: 20px;
    margin-bottom: 16px;
  }

  .leaf-item {
    padding: 16px 24px;
  }

  .leaf-icon {
    font-size: 28px;
  }

  .leaf-name {
    font-size: 16px;
  }

  .leaf-time {
    font-size: 13px;
  }

  .leaf-count {
    font-size: 20px;
  }
}

@media (min-width: 1024px) {
  .leaves-page {
    max-width: 720px;
    padding: 40px 40px 100px;
  }

  .leaves-garden {
    height: 520px;
  }
}
</style>