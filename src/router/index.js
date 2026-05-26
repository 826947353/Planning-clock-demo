import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue')
  },
  {
    path: '/leaves',
    name: 'Leaves',
    component: () => import('../views/Leaves.vue')
  },
  {
    path: '/mood',
    name: 'MoodCloud',
    component: () => import('../views/MoodCloud.vue')
  },
  {
    path: '/stars',
    name: 'StarSky',
    component: () => import('../views/StarSky.vue')
  },
  {
    path: '/timeline',
    name: 'Timeline',
    component: () => import('../views/Timeline.vue')
  },
  {
    path: '/calendar',
    name: 'Calendar',
    component: () => import('../views/Calendar.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router