import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' }
})

export const focusApi = {
  getUserSessions(userId) {
    return api.get(`/focus/user/${userId}`)
  },
  getSessionsByRange(userId, start, end) {
    return api.get(`/focus/user/${userId}/range`, { params: { start, end } })
  },
  startSession(data) {
    return api.post('/focus/start', data)
  },
  completeSession(sessionId, actualMinutes, moodAfter) {
    return api.put(`/focus/${sessionId}/complete`, null, {
      params: { actualMinutes, moodAfter }
    })
  },
  abandonSession(sessionId) {
    return api.put(`/focus/${sessionId}/abandon`)
  }
}

export const moodApi = {
  getUserMoods(userId) {
    return api.get(`/mood/user/${userId}`)
  },
  getLatestMood(userId) {
    return api.get(`/mood/user/${userId}/latest`)
  },
  getMoodsByRange(userId, start, end) {
    return api.get(`/mood/user/${userId}/range`, { params: { start, end } })
  },
  recordMood(data) {
    return api.post('/mood', data)
  }
}

export const growthApi = {
  getUserGrowth(userId) {
    return api.get(`/growth/user/${userId}`)
  }
}

export const recommendationApi = {
  getRecommendations(mood) {
    return api.get('/recommendation', { params: { mood } })
  }
}

export const aiApi = {
  generateSummary(sessionId) {
    return api.post(`/ai/summary/${sessionId}`)
  }
}

export const chatApi = {
  send(messages) {
    return api.post('/ai/chat', { messages })
  },
  status() {
    return api.get('/ai/chat/status')
  }
}

export default api