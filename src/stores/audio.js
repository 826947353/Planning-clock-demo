import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAudioStore = defineStore('audio', () => {
  const bgmAudio = ref(null)
  const whiteNoiseAudio = ref(null)
  const isBgmPlaying = ref(false)
  const isWhiteNoisePlaying = ref(false)
  const currentWhiteNoise = ref(null)
  const bgmVolume = ref(0.3)
  const whiteNoiseVolume = ref(0.5)
  const audioContextAllowed = ref(false)

  const whiteNoiseOptions = [
    { id: 'rain', name: '雨声', icon: '🌧', file: '/audio/white-noise/rain.mp3' },
    { id: 'forest', name: '森林', icon: '🌲', file: '/audio/white-noise/forest.mp3' },
    { id: 'waves', name: '海浪', icon: '🌊', file: '/audio/white-noise/waves.mp3' },
    { id: 'fire', name: '篝火', icon: '🔥', file: '/audio/white-noise/fire.mp3' },
    { id: 'cafe', name: '咖啡馆', icon: '☕', file: '/audio/white-noise/cafe.mp3' },
    { id: 'wind', name: '微风', icon: '🍃', file: '/audio/white-noise/wind.mp3' }
  ]

  function initBgm() {
    if (bgmAudio.value) return
    bgmAudio.value = new Audio('/audio/bgm/background.mp3')
    bgmAudio.value.loop = true
    bgmAudio.value.volume = bgmVolume.value
  }

  async function playBgm() {
    initBgm()
    try {
      await bgmAudio.value.play()
      isBgmPlaying.value = true
      audioContextAllowed.value = true
    } catch (e) {
      console.warn('BGM play failed:', e)
      isBgmPlaying.value = false
    }
  }

  function pauseBgm() {
    if (bgmAudio.value) {
      bgmAudio.value.pause()
      isBgmPlaying.value = false
    }
  }

  function toggleBgm() {
    if (isBgmPlaying.value) {
      pauseBgm()
    } else {
      playBgm()
    }
  }

  function setBgmVolume(vol) {
    bgmVolume.value = vol
    if (bgmAudio.value) bgmAudio.value.volume = vol
  }

  async function playWhiteNoise(noiseId) {
    const noise = whiteNoiseOptions.find(n => n.id === noiseId)
    if (!noise) return

    if (whiteNoiseAudio.value) {
      whiteNoiseAudio.value.pause()
      whiteNoiseAudio.value = null
    }

    whiteNoiseAudio.value = new Audio(noise.file)
    whiteNoiseAudio.value.loop = true
    whiteNoiseAudio.value.volume = whiteNoiseVolume.value

    try {
      await whiteNoiseAudio.value.play()
      currentWhiteNoise.value = noise
      isWhiteNoisePlaying.value = true
      audioContextAllowed.value = true
    } catch (e) {
      console.warn('White noise play failed:', e)
      isWhiteNoisePlaying.value = false
    }
  }

  function pauseWhiteNoise() {
    if (whiteNoiseAudio.value) {
      whiteNoiseAudio.value.pause()
      isWhiteNoisePlaying.value = false
    }
  }

  function toggleWhiteNoise(noiseId) {
    if (currentWhiteNoise.value?.id === noiseId && isWhiteNoisePlaying.value) {
      pauseWhiteNoise()
    } else {
      playWhiteNoise(noiseId)
    }
  }

  function setWhiteNoiseVolume(vol) {
    whiteNoiseVolume.value = vol
    if (whiteNoiseAudio.value) whiteNoiseAudio.value.volume = vol
  }

  function stopAll() {
    pauseBgm()
    pauseWhiteNoise()
  }

  return {
    isBgmPlaying,
    isWhiteNoisePlaying,
    currentWhiteNoise,
    bgmVolume,
    whiteNoiseVolume,
    audioContextAllowed,
    whiteNoiseOptions,
    playBgm,
    pauseBgm,
    toggleBgm,
    setBgmVolume,
    playWhiteNoise,
    pauseWhiteNoise,
    toggleWhiteNoise,
    setWhiteNoiseVolume,
    stopAll
  }
})