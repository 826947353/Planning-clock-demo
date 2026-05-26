<template>
  <div class="star-bg-wrapper">
    <canvas ref="starCanvas" class="star-canvas"></canvas>
    <canvas ref="trailCanvas" class="trail-canvas"></canvas>
    <div class="ambient-glow"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const starCanvas = ref(null)
const trailCanvas = ref(null)

const trailTexts = [
  '来专注5分钟？', '深呼吸...', '慢慢来', '此刻很安静',
  '你做得很好', '休息一下', '保持专注', '享受当下',
  '再坚持一下', '很棒！', '放轻松', '用心感受',
  '时间还很多', '别急躁', '慢慢来', '专注此刻',
  '心静自然凉', '享受过程', '慢慢来', '很好',
  '深呼吸', '慢慢来', '专注', '放松',
  '很好', '继续', '加油', '平静'
]

let starAnimId = null
let trailAnimId = null

onMounted(() => {
  const starC = starCanvas.value
  const trailC = trailCanvas.value
  const starCtx = starC.getContext('2d')
  const trailCtx = trailC.getContext('2d')

  function resize() {
    const w = window.innerWidth
    const h = window.innerHeight
    starC.width = w
    starC.height = h
    trailC.width = w
    trailC.height = h
  }
  resize()
  window.addEventListener('resize', resize)

  // ===== 星空 =====
  const stars = Array.from({ length: 300 }, () => ({
    x: Math.random() * starC.width,
    y: Math.random() * starC.height,
    r: Math.random() * 1.5 + 0.3,
    alpha: Math.random(),
    speed: Math.random() * 0.008 + 0.002,
    twinkle: Math.random() * Math.PI * 2
  }))

  function drawStars() {
    starCtx.clearRect(0, 0, starC.width, starC.height)

    stars.forEach(star => {
      star.twinkle += star.speed
      const a = 0.15 + Math.sin(star.twinkle) * 0.25 + star.alpha * 0.35
      starCtx.beginPath()
      starCtx.arc(star.x, star.y, star.r, 0, Math.PI * 2)
      starCtx.fillStyle = `rgba(220, 230, 255, ${a})`
      starCtx.fill()
    })

    starAnimId = requestAnimationFrame(drawStars)
  }
  drawStars()

  // ===== 文字拖尾 =====
  const particles = []
  let lastX = 0, lastY = 0
  let textIndex = 0
  let frameCount = 0

  function getSpeed(x, y) {
    const dx = x - lastX
    const dy = y - lastY
    return Math.sqrt(dx * dx + dy * dy)
  }

  function onMouseMove(e) {
    const speed = getSpeed(e.clientX, e.clientY)
    lastX = e.clientX
    lastY = e.clientY

    if (speed < 1) return

    frameCount++
    if (frameCount % 4 !== 0) return

    const text = trailTexts[textIndex % trailTexts.length]
    textIndex++

    particles.push({
      x: e.clientX,
      y: e.clientY,
      text,
      speed,
      life: 1,
      maxLife: 300 + Math.random() * 200,
      driftX: (Math.random() - 0.5) * 0.08,
      driftY: -0.06 - Math.random() * 0.1,
      fontSize: 11 + Math.min(speed * 0.1, 10),
      hue: 20 + Math.random() * 40
    })
  }

  function drawTrail() {
    trailCtx.clearRect(0, 0, trailC.width, trailC.height)

    for (let i = particles.length - 1; i >= 0; i--) {
      const p = particles[i]
      p.life -= 1 / p.maxLife
      p.x += p.driftX + (p.speed * 0.005)
      p.y += p.driftY

      if (p.life <= 0) {
        particles.splice(i, 1)
        continue
      }

      const alpha = Math.max(0, p.life * 0.5)
      trailCtx.font = `300 ${p.fontSize}px "Segoe UI", "PingFang SC", sans-serif`
      trailCtx.fillStyle = `hsla(${p.hue}, 60%, 78%, ${alpha})`
      trailCtx.textAlign = 'center'
      trailCtx.shadowColor = `hsla(${p.hue}, 60%, 78%, ${alpha * 0.5})`
      trailCtx.shadowBlur = 8
      trailCtx.fillText(p.text, p.x, p.y)
      trailCtx.shadowBlur = 0
    }

    trailAnimId = requestAnimationFrame(drawTrail)
  }

  window.addEventListener('mousemove', onMouseMove)
  drawTrail()

  onUnmounted(() => {
    window.removeEventListener('resize', resize)
    window.removeEventListener('mousemove', onMouseMove)
    if (starAnimId) cancelAnimationFrame(starAnimId)
    if (trailAnimId) cancelAnimationFrame(trailAnimId)
  })
})
</script>

<style scoped>
.star-bg-wrapper {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
}

.star-canvas {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}

.trail-canvas {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
}

.ambient-glow {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse at 20% 80%, rgba(232, 168, 124, 0.04) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 20%, rgba(184, 168, 208, 0.04) 0%, transparent 50%),
    radial-gradient(ellipse at 50% 50%, rgba(168, 192, 160, 0.02) 0%, transparent 60%);
  animation: breathe 8s ease-in-out infinite;
}
</style>