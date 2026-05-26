<template>
  <div class="noise-panel glass-card" v-if="timerStore.isRunning">
    <!-- 白噪音 audio 元素 -->
    <audio
      ref="noiseEl"
      :src="currentNoiseSrc"
      loop
      preload="auto"
      @play="isPlaying = true"
      @pause="isPlaying = false"
      @error="onAudioError"
    ></audio>

    <div class="noise-header">
      <span class="noise-title">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M12 1a3 3 0 0 0-3 3v8a3 3 0 0 0 6 0V4a3 3 0 0 0-3-3z"/>
          <path d="M19 10v2a7 7 0 0 1-14 0v-2"/>
          <line x1="12" y1="19" x2="12" y2="23"/>
          <line x1="8" y1="23" x2="16" y2="23"/>
        </svg>
        白噪音
      </span>
      <span class="noise-status" v-if="isPlaying && currentNoise">
        {{ currentNoise.icon }} {{ currentNoise.name }}
      </span>
    </div>

    <div class="noise-options">
      <button
        v-for="noise in noiseOptions"
        :key="noise.id"
        class="noise-btn"
        :class="{ active: currentNoise?.id === noise.id && isPlaying }"
        @click="toggleNoise(noise)"
      >
        <span class="noise-icon">{{ noise.icon }}</span>
        <span class="noise-name">{{ noise.name }}</span>
        <span class="noise-indicator" v-if="currentNoise?.id === noise.id && isPlaying">
          <span class="wave"></span>
          <span class="wave"></span>
          <span class="wave"></span>
        </span>
      </button>
    </div>

    <div class="noise-volume" v-if="isPlaying">
      <span class="vol-icon">🔉</span>
      <input
        type="range"
        min="0"
        max="1"
        step="0.05"
        :value="volume"
        @input="onVolumeChange"
        class="vol-slider"
      />
      <span class="vol-icon">🔊</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useTimerStore } from '../stores/timer'

const timerStore = useTimerStore()

const noiseEl = ref(null)
const isPlaying = ref(false)
const currentNoise = ref(null)
const volume = ref(0.5)
const audioError = ref('')

const currentNoiseSrc = computed(() => currentNoise.value?.file || '')

const noiseOptions = [
  { id: 'rain', name: '雨声', icon: '🌧', file: '/audio/white-noise/Sacred%20Play%20Secret%20Place%20(%E6%B2%BB%E6%84%88%E7%BA%AF%E9%9F%B3%E4%B9%90)-StellarWhisper.mp3' },
  { id: 'forest', name: '森林', icon: '🌲', file: '/audio/white-noise/Sacred%20Play%20Secret%20Place%20(%E6%B2%BB%E6%84%88%E7%BA%AF%E9%9F%B3%E4%B9%90)-StellarWhisper.mp3' },
  { id: 'waves', name: '海浪', icon: '🌊', file: '/audio/white-noise/Sacred%20Play%20Secret%20Place%20(%E6%B2%BB%E6%84%88%E7%BA%AF%E9%9F%B3%E4%B9%90)-StellarWhisper.mp3' },
  { id: 'fire', name: '篝火', icon: '🔥', file: '/audio/white-noise/Sacred%20Play%20Secret%20Place%20(%E6%B2%BB%E6%84%88%E7%BA%AF%E9%9F%B3%E4%B9%90)-StellarWhisper.mp3' },
  { id: 'cafe', name: '咖啡馆', icon: '☕', file: '/audio/white-noise/Sacred%20Play%20Secret%20Place%20(%E6%B2%BB%E6%84%88%E7%BA%AF%E9%9F%B3%E4%B9%90)-StellarWhisper.mp3' },
  { id: 'wind', name: '微风', icon: '🍃', file: '/audio/white-noise/Sacred%20Play%20Secret%20Place%20(%E6%B2%BB%E6%84%88%E7%BA%AF%E9%9F%B3%E4%B9%90)-StellarWhisper.mp3' }
]

function onAudioError(e) {
  audioError.value = '音频加载失败'
  console.error('White noise error:', e)
}

async function toggleNoise(noise) {
  if (currentNoise.value?.id === noise.id && isPlaying.value) {
    pauseNoise()
    return
  }

  currentNoise.value = noise

  await nextTick()

  if (!noiseEl.value) return

  noiseEl.value.volume = volume.value
  try {
    await noiseEl.value.play()
  } catch (e) {
    console.warn('White noise play failed:', e)
    audioError.value = '需要用户交互后才能播放'
  }
}

function pauseNoise() {
  if (noiseEl.value) {
    noiseEl.value.pause()
  }
}

function onVolumeChange(e) {
  const vol = e.target.valueAsNumber
  volume.value = vol
  if (noiseEl.value) {
    noiseEl.value.volume = vol
  }
}

import { nextTick } from 'vue'
</script>

<style scoped>
.noise-panel {
  position: fixed;
  bottom: 92px;
  left: 20px;
  right: 84px;
  z-index: 50;
  padding: 16px;
  animation: slide-up-enter 0.5s cubic-bezier(0.16, 1, 0.3, 1);
}

.noise-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.noise-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text);
}

.noise-title svg {
  width: 18px;
  height: 18px;
}

.noise-status {
  font-size: 12px;
  color: var(--warm);
  font-weight: 500;
}

.noise-options {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding-bottom: 4px;
  scrollbar-width: none;
}

.noise-options::-webkit-scrollbar {
  display: none;
}

.noise-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 10px 14px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid var(--border);
  color: var(--text-dim);
  font-size: 12px;
  transition: all 0.3s ease;
  flex-shrink: 0;
  min-width: 64px;
  position: relative;
}

.noise-btn:hover {
  border-color: rgba(255, 255, 255, 0.1);
  transform: translateY(-2px);
}

.noise-btn.active {
  border-color: rgba(232, 168, 124, 0.4);
  background: rgba(232, 168, 124, 0.06);
  color: var(--warm);
}

.noise-icon {
  font-size: 22px;
}

.noise-name {
  font-size: 11px;
  white-space: nowrap;
}

.noise-indicator {
  display: flex;
  gap: 2px;
  position: absolute;
  top: 6px;
  right: 6px;
}

.wave {
  width: 3px;
  height: 8px;
  background: var(--warm);
  border-radius: 2px;
  animation: sound-wave 1s ease-in-out infinite;
}

.wave:nth-child(2) {
  animation-delay: 0.15s;
}

.wave:nth-child(3) {
  animation-delay: 0.3s;
}

@keyframes sound-wave {
  0%, 100% { transform: scaleY(0.5); opacity: 0.5; }
  50% { transform: scaleY(1.2); opacity: 1; }
}

.noise-volume {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--border);
}

.vol-icon {
  font-size: 14px;
}

.vol-slider {
  flex: 1;
  height: 3px;
  accent-color: var(--warm);
  cursor: pointer;
}

@media (min-width: 768px) {
  .noise-panel {
    bottom: 104px;
    left: 32px;
    right: 108px;
    padding: 20px;
  }

  .noise-title {
    font-size: 16px;
  }

  .noise-title svg {
    width: 20px;
    height: 20px;
  }

  .noise-btn {
    padding: 12px 18px;
    min-width: 72px;
  }

  .noise-icon {
    font-size: 26px;
  }

  .noise-name {
    font-size: 12px;
  }
}

@media (min-width: 1024px) {
  .noise-panel {
    left: 40px;
    right: 120px;
  }
}
</style>