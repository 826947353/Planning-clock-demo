<template>
  <div class="stars-page">
    <header class="page-header">
      <h1 class="page-title">⭐ 星空</h1>
      <p class="page-subtitle">你的每一次专注，都点亮一颗星</p>
    </header>

    <div class="stars-info glass-card">
      <div class="stars-count">
        <span class="stars-number">{{ starCount }}</span>
        <span class="stars-unit">颗星</span>
      </div>
      <div class="stars-desc">
        连续专注 <strong>{{ consecutiveDays }}</strong> 天
      </div>
    </div>

    <canvas ref="starCanvas" class="star-canvas"></canvas>

    <div class="constellations">
      <h3>我的星座</h3>
      <div class="constellation-grid">
        <div
          v-for="c in constellations"
          :key="c.name"
          class="constellation-card glass-card"
          :class="{ unlocked: c.unlocked }"
        >
          <div class="constellation-icon">{{ c.icon }}</div>
          <div class="constellation-name">{{ c.name }}</div>
          <div class="constellation-desc">{{ c.desc }}</div>
          <div class="constellation-progress" v-if="!c.unlocked">
            <div class="progress-bar" :style="{ width: c.progress + '%' }"></div>
          </div>
          <div class="constellation-unlocked" v-else>已解锁</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const starCanvas = ref(null)
const starCount = ref(42)
const consecutiveDays = ref(7)
let animationId = null

const constellations = ref([
  { name: '初学者', icon: '🌱', desc: '完成首次专注', unlocked: true, progress: 100 },
  { name: '坚持者', icon: '🕯️', desc: '连续专注7天', unlocked: true, progress: 100 },
  { name: '时间旅人', icon: '⏳', desc: '累计专注1000分钟', unlocked: false, progress: 62 },
  { name: '专注大师', icon: '🏆', desc: '完成50次专注', unlocked: false, progress: 38 },
  { name: '星辰收集者', icon: '🌟', desc: '解锁全部星座', unlocked: false, progress: 24 }
])

onMounted(() => {
  const canvas = starCanvas.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')

  const dpr = window.devicePixelRatio || 1
  const rect = canvas.getBoundingClientRect()
  canvas.width = rect.width * dpr
  canvas.height = rect.height * dpr
  ctx.scale(dpr, dpr)

  const stars = Array.from({ length: 200 }, () => ({
    x: Math.random() * rect.width,
    y: Math.random() * rect.height,
    r: Math.random() * 2 + 0.5,
    twinkle: Math.random() * Math.PI * 2,
    speed: Math.random() * 0.03 + 0.01,
    brightness: Math.random()
  }))

  const brightStarCount = starCount.value

  function draw() {
    ctx.clearRect(0, 0, rect.width, rect.height)

    const gradient = ctx.createRadialGradient(rect.width / 2, rect.height / 2, 0, rect.width / 2, rect.height / 2, rect.width / 1.5)
    gradient.addColorStop(0, 'rgba(184, 168, 208, 0.06)')
    gradient.addColorStop(1, 'transparent')
    ctx.fillStyle = gradient
    ctx.fillRect(0, 0, rect.width, rect.height)

    stars.forEach((star, i) => {
      star.twinkle += star.speed
      const alpha = 0.3 + Math.sin(star.twinkle) * 0.35 + star.brightness * 0.35

      if (i < brightStarCount) {
        ctx.fillStyle = `rgba(255, 255, 255, ${alpha * 1.4})`
        ctx.shadowColor = 'rgba(255, 255, 200, 0.5)'
        ctx.shadowBlur = 6
      } else {
        ctx.fillStyle = i % 3 === 0
          ? `rgba(200, 200, 255, ${alpha * 0.6})`
          : `rgba(255, 255, 255, ${alpha * 0.5})`
        ctx.shadowBlur = 0
      }

      ctx.beginPath()
      ctx.arc(star.x, star.y, star.r, 0, Math.PI * 2)
      ctx.fill()
    })

    ctx.shadowBlur = 0
    animationId = requestAnimationFrame(draw)
  }

  draw()
})

onUnmounted(() => {
  if (animationId) cancelAnimationFrame(animationId)
})
</script>

<style scoped>
.stars-page {
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

.stars-info {
  text-align: center;
}

.stars-number {
  font-size: 48px;
  font-weight: 300;
  color: var(--warm);
}

.stars-unit {
  font-size: 16px;
  color: var(--text-dim);
  margin-left: 6px;
}

.stars-desc {
  font-size: 14px;
  color: var(--text-dim);
  margin-top: 4px;
}

.stars-desc strong {
  color: var(--warm);
}

.star-canvas {
  width: 100%;
  height: 260px;
  border-radius: var(--radius);
  background: var(--bg-card);
  border: 1px solid var(--border);
  margin-bottom: 16px;
}

.constellations h3 {
  font-size: 16px;
  color: var(--text);
  margin-bottom: 12px;
}

.constellation-grid {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.constellation-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  opacity: 0.5;
}

.constellation-card.unlocked {
  opacity: 1;
}

.constellation-icon {
  font-size: 28px;
}

.constellation-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text);
  min-width: 80px;
}

.constellation-desc {
  flex: 1;
  font-size: 12px;
  color: var(--text-dim);
}

.constellation-progress {
  width: 60px;
  height: 5px;
  background: var(--border);
  border-radius: 3px;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  background: linear-gradient(90deg, var(--lavender), var(--warm));
  border-radius: 3px;
}

.constellation-unlocked {
  font-size: 11px;
  color: var(--sage);
  font-weight: 600;
}

/* ========== PC端自适应 ========== */
@media (min-width: 768px) {
  .stars-page {
    max-width: 640px;
    padding: 32px 32px 100px;
  }

  .stars-number {
    font-size: 64px;
  }

  .stars-unit {
    font-size: 20px;
  }

  .stars-desc {
    font-size: 16px;
  }

  .star-canvas {
    height: 360px;
  }

  .constellations h3 {
    font-size: 20px;
    margin-bottom: 16px;
  }

  .constellation-card {
    padding: 18px 24px;
  }

  .constellation-icon {
    font-size: 36px;
  }

  .constellation-name {
    font-size: 17px;
    min-width: 100px;
  }

  .constellation-desc {
    font-size: 14px;
  }

  .constellation-progress {
    width: 80px;
    height: 6px;
  }
}

@media (min-width: 1024px) {
  .stars-page {
    max-width: 720px;
    padding: 40px 40px 100px;
  }

  .star-canvas {
    height: 400px;
  }
}
</style>