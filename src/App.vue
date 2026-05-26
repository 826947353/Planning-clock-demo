<template>
  <div class="app-container">
    <StarBg />
    <main class="main-content">
      <router-view v-slot="{ Component, route }">
        <transition :name="route.meta.transition || 'fade'" mode="out-in">
          <component :is="Component" :key="route.path" />
        </transition>
      </router-view>
    </main>

    <nav class="bottom-nav">
      <router-link to="/" class="nav-item" exact-active-class="active">
        <span class="nav-glow"></span>
        <span class="nav-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <circle cx="12" cy="12" r="10"/>
            <polyline points="12 6 12 12 16 14"/>
          </svg>
        </span>
        <span class="nav-label">慢空间</span>
      </router-link>

      <router-link to="/leaves" class="nav-item" active-class="active">
        <span class="nav-glow"></span>
        <span class="nav-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M17 8C8 10 5.9 16.17 3.82 21.34l1.89.66.95-2.3c.48.17.98.3 1.34.3C19 20 22 3 22 3c-1 2-8 2.25-13 3.25S2 11.5 2 13.5s1.75 3.75 1.75 3.75"/>
          </svg>
        </span>
        <span class="nav-label">树叶</span>
      </router-link>

      <router-link to="/mood" class="nav-item" active-class="active">
        <span class="nav-glow"></span>
        <span class="nav-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M20.24 12.24a6 6 0 0 0-8.49-8.49L5 10.5V19h8.5z"/>
            <line x1="16" y1="8" x2="2" y2="22"/>
            <line x1="17.5" y1="15" x2="9" y2="15"/>
          </svg>
        </span>
        <span class="nav-label">情绪云</span>
      </router-link>

      <router-link to="/stars" class="nav-item" active-class="active">
        <span class="nav-glow"></span>
        <span class="nav-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
          </svg>
        </span>
        <span class="nav-label">星空</span>
      </router-link>

      <router-link to="/timeline" class="nav-item" active-class="active">
        <span class="nav-glow"></span>
        <span class="nav-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <line x1="12" y1="2" x2="12" y2="22"/>
            <polyline points="17 8 12 2 7 8"/>
            <polyline points="17 16 12 22 7 16"/>
          </svg>
        </span>
        <span class="nav-label">时间轴</span>
      </router-link>
    </nav>

    <div class="calendar-fab" @click="$router.push('/calendar')">
      <span class="fab-glow"></span>
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
        <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
        <line x1="16" y1="2" x2="16" y2="6"/>
        <line x1="8" y1="2" x2="8" y2="6"/>
        <line x1="3" y1="10" x2="21" y2="10"/>
      </svg>
    </div>

    <BgmPlayer />
    <WhiteNoisePanel />
    <AiChat />
  </div>
</template>

<script setup>
import StarBg from './components/StarBg.vue'
import BgmPlayer from './components/BgmPlayer.vue'
import WhiteNoisePanel from './components/WhiteNoisePanel.vue'
import AiChat from './components/AiChat.vue'
</script>

<style scoped>
.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  position: relative;
}

.main-content {
  flex: 1;
  padding-bottom: 100px;
  position: relative;
  z-index: 1;
}

.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 72px;
  background: rgba(12, 10, 24, 0.8);
  backdrop-filter: blur(30px) saturate(1.2);
  -webkit-backdrop-filter: blur(30px) saturate(1.2);
  border-top: 1px solid rgba(255, 255, 255, 0.04);
  display: flex;
  align-items: center;
  justify-content: space-around;
  z-index: 100;
  padding-bottom: env(safe-area-inset-bottom, 0);
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
  color: var(--text-muted);
  text-decoration: none;
  padding: 8px 14px;
  border-radius: 16px;
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
  position: relative;
}

.nav-item.active {
  color: var(--warm-light);
}

.nav-glow {
  position: absolute;
  inset: 0;
  border-radius: 16px;
  opacity: 0;
  transition: opacity 0.4s ease;
  background: radial-gradient(ellipse at center, rgba(232, 168, 124, 0.15) 0%, transparent 70%);
}

.nav-item.active .nav-glow {
  opacity: 1;
}

.nav-icon {
  width: 22px;
  height: 22px;
  transition: transform 0.3s ease;
}

.nav-item:hover .nav-icon {
  transform: scale(1.15);
}

.nav-icon svg {
  width: 100%;
  height: 100%;
}

.nav-label {
  font-size: 11px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.calendar-fab {
  position: fixed;
  right: 20px;
  bottom: 92px;
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--warm-deep), var(--warm));
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
  z-index: 99;
  animation: pulse-glow 3s ease-in-out infinite;
}

.calendar-fab:hover {
  transform: scale(1.12) rotate(5deg);
  box-shadow: 0 8px 30px rgba(232, 168, 124, 0.3);
}

.calendar-fab svg {
  width: 22px;
  height: 22px;
  color: white;
}

.fab-glow {
  position: absolute;
  inset: -4px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(232, 168, 124, 0.2) 0%, transparent 70%);
  animation: breathe 3s ease-in-out infinite;
}

/* ========== PC端导航优化 ========== */
@media (min-width: 768px) {
  .bottom-nav {
    height: 80px;
    justify-content: center;
    gap: 48px;
  }

  .nav-item {
    padding: 10px 28px;
    gap: 6px;
  }

  .nav-icon {
    width: 26px;
    height: 26px;
  }

  .nav-label {
    font-size: 13px;
  }

  .calendar-fab {
    right: 36px;
    bottom: 104px;
    width: 60px;
    height: 60px;
  }

  .calendar-fab svg {
    width: 26px;
    height: 26px;
  }
}

@media (min-width: 1024px) {
  .bottom-nav {
    gap: 72px;
  }

  .nav-item {
    padding: 12px 36px;
  }

  .nav-icon {
    width: 28px;
    height: 28px;
  }

  .nav-label {
    font-size: 14px;
  }
}
</style>