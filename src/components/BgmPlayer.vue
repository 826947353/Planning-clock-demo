<template>
  <div class="bgm-player">
    <!-- 隐藏的 audio 元素 -->
    <audio
      ref="bgmEl"
      src="/audio/bgm/Bgm1.mp3"
      loop
      preload="auto"
      @play="isBgmPlaying = true"
      @pause="isBgmPlaying = false"
      @error="onAudioError"
    ></audio>

    <!-- 首次进入时的音乐引导弹窗 -->
    <Transition name="fade">
      <div v-if="showIntro" class="bgm-intro-overlay" @click="dismissIntro">
        <div class="bgm-intro-card" @click.stop>
          <div class="intro-icon">🎵</div>
          <h3>开启音乐之旅</h3>
          <p>让轻柔的旋律陪伴你的专注时光</p>
          <div class="intro-actions">
            <button class="intro-btn primary" @click="enableBgm">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <polygon points="5 3 19 12 5 21 5 3"/>
              </svg>
              开启音乐
            </button>
            <button class="intro-btn secondary" @click="dismissIntro">
              稍后再说
            </button>
          </div>
        </div>
      </div>
    </Transition>

    <div class="bgm-error-toast" v-if="audioError">
      {{ audioError }}
    </div>

    <button class="bgm-toggle" @click="toggleBgm" :class="{ playing: isBgmPlaying }">
      <span class="bgm-icon">
        <svg v-if="isBgmPlaying" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5"/>
          <path d="M19.07 4.93a10 10 0 0 1 0 14.14"/>
          <path d="M15.54 8.46a5 5 0 0 1 0 7.07"/>
        </svg>
        <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5"/>
          <line x1="23" y1="9" x2="17" y2="15"/>
          <line x1="17" y1="9" x2="23" y2="15"/>
        </svg>
      </span>
      <span class="bgm-label">{{ isBgmPlaying ? '音乐中' : '音乐' }}</span>
    </button>

    <div class="bgm-volume" v-show="isBgmPlaying">
      <input
        type="range"
        min="0"
        max="1"
        step="0.05"
        :value="bgmVolume"
        @input="onVolumeChange"
        class="volume-slider"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const bgmEl = ref(null)
const isBgmPlaying = ref(false)
const bgmVolume = ref(0.3)
const showIntro = ref(false)
const audioError = ref('')

onMounted(() => {
  const hasDismissed = localStorage.getItem('bgm-intro-dismissed')
  if (!hasDismissed) {
    showIntro.value = true
  }
})

function onAudioError(e) {
  const el = bgmEl.value
  const err = el?.error
  let msg = '音频加载失败'
  if (err) {
    switch (err.code) {
      case 1: msg = '音频加载被中断'; break
      case 2: msg = '网络错误，无法加载音频'; break
      case 3: msg = '音频解码错误，文件可能损坏'; break
      case 4: msg = '音频格式不支持或文件不存在 (404)'; break
    }
  }
  audioError.value = msg
  console.error('Audio error:', err, e)
  setTimeout(() => { audioError.value = '' }, 5000)
}

async function enableBgm() {
  await playBgm()
  dismissIntro()
}

function dismissIntro() {
  showIntro.value = false
  localStorage.setItem('bgm-intro-dismissed', 'true')
}

async function playBgm() {
  if (!bgmEl.value) return
  bgmEl.value.volume = bgmVolume.value
  try {
    await bgmEl.value.play()
  } catch (e) {
    console.warn('BGM play failed:', e)
    audioError.value = '需要用户交互后才能播放音频'
  }
}

function pauseBgm() {
  if (bgmEl.value) {
    bgmEl.value.pause()
  }
}

function toggleBgm() {
  if (isBgmPlaying.value) {
    pauseBgm()
  } else {
    playBgm()
  }
}

function onVolumeChange(e) {
  const vol = e.target.valueAsNumber
  bgmVolume.value = vol
  if (bgmEl.value) {
    bgmEl.value.volume = vol
  }
}
</script>

<style scoped>
.bgm-player {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 50;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.bgm-intro-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 200;
}

.bgm-intro-card {
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: 40px 32px;
  text-align: center;
  max-width: 320px;
  width: 90%;
  animation: slide-up-enter 0.5s cubic-bezier(0.16, 1, 0.3, 1);
}

.intro-icon {
  font-size: 48px;
  margin-bottom: 16px;
  animation: float 3s ease-in-out infinite;
}

.bgm-intro-card h3 {
  font-size: 22px;
  font-weight: 700;
  color: var(--text);
  margin-bottom: 8px;
}

.bgm-intro-card p {
  font-size: 14px;
  color: var(--text-dim);
  margin-bottom: 24px;
  line-height: 1.6;
}

.intro-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.intro-btn {
  width: 100%;
  padding: 14px;
  border-radius: var(--radius-sm);
  font-size: 15px;
  font-weight: 600;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.intro-btn.primary {
  background: linear-gradient(135deg, var(--warm-deep), var(--warm));
  color: white;
}

.intro-btn.primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(232, 168, 124, 0.3);
}

.intro-btn.primary svg {
  width: 18px;
  height: 18px;
}

.intro-btn.secondary {
  background: transparent;
  border: 1px solid var(--border);
  color: var(--text-dim);
}

.intro-btn.secondary:hover {
  border-color: rgba(255, 255, 255, 0.15);
  color: var(--text);
}

.bgm-toggle {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  border-radius: 24px;
  background: var(--bg-card);
  border: 1px solid var(--border);
  color: var(--text-dim);
  font-size: 13px;
  font-weight: 500;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  transition: all 0.4s ease;
  cursor: pointer;
}

.bgm-toggle:hover {
  border-color: rgba(255, 255, 255, 0.1);
  transform: translateY(-2px);
}

.bgm-toggle.playing {
  border-color: rgba(232, 168, 124, 0.3);
  color: var(--warm);
  box-shadow: 0 0 20px rgba(232, 168, 124, 0.1);
  animation: pulse-glow 3s ease-in-out infinite;
}

.bgm-icon {
  width: 18px;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.bgm-icon svg {
  width: 100%;
  height: 100%;
}

.bgm-volume {
  padding: 8px 14px;
  border-radius: 16px;
  background: var(--bg-card);
  border: 1px solid var(--border);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
}

.volume-slider {
  width: 80px;
  height: 3px;
  accent-color: var(--warm);
  cursor: pointer;
}

.bgm-error-toast {
  position: fixed;
  top: 80px;
  right: 20px;
  background: rgba(220, 80, 80, 0.9);
  color: white;
  padding: 10px 16px;
  border-radius: 12px;
  font-size: 13px;
  z-index: 100;
  animation: slide-in 0.3s ease;
}

@keyframes slide-in {
  from { transform: translateX(100%); opacity: 0; }
  to { transform: translateX(0); opacity: 1; }
}

@media (min-width: 768px) {
  .bgm-player {
    top: 28px;
    right: 28px;
  }

  .bgm-toggle {
    padding: 12px 22px;
    font-size: 14px;
  }

  .bgm-icon {
    width: 20px;
    height: 20px;
  }

  .volume-slider {
    width: 100px;
  }

  .bgm-intro-card {
    padding: 48px 40px;
    max-width: 380px;
  }

  .bgm-intro-card h3 {
    font-size: 26px;
  }

  .bgm-intro-card p {
    font-size: 16px;
  }
}
</style>