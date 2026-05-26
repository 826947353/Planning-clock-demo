<template>
  <div class="ai-chat">
    <Transition name="chat-panel">
      <div v-if="isOpen" class="chat-panel glass-card" ref="panelRef">
        <div class="chat-header">
          <div class="header-left">
            <span class="ai-avatar">🌿</span>
            <div class="header-info">
              <h3>Man AI</h3>
              <span class="ai-status online">本地运行中</span>
            </div>
          </div>
          <button class="close-btn" @click="toggleChat">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <line x1="18" y1="6" x2="6" y2="18"/>
              <line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
        </div>

        <div class="chat-messages" ref="messagesRef">
          <div class="welcome-msg" v-if="messages.length === 0 && !isThinking">
            <div class="welcome-icon">✨</div>
            <p class="welcome-text">你好，我是 Man AI</p>
            <p class="welcome-sub">教会人重新回归人本身 🌿</p>
            <p class="welcome-hint">我可以帮你选择专注方案、直接开始计时<br/>或者只是陪你聊聊天</p>
          </div>

          <TransitionGroup name="msg-list" tag="div" class="messages-container">
            <div
              v-for="(msg, index) in messages"
              :key="index"
              class="message-row"
              :class="{ 'user-msg': msg.role === 'user', 'ai-msg': msg.role === 'assistant' }"
            >
              <div class="message-bubble">
                <span class="msg-role-icon" v-if="msg.role === 'assistant'">🌿</span>
                <div class="msg-content" v-html="formatContent(msg.content)"></div>
                <span class="msg-time">{{ msg.time }}</span>
              </div>

              <div class="action-cards" v-if="msg.actions && msg.actions.length > 0">
                <button
                  v-for="(action, ai) in msg.actions"
                  :key="ai"
                  class="action-card"
                  :class="{ primary: action.primary }"
                  @click="executeAction(action)"
                >
                  <span class="action-icon">{{ action.icon }}</span>
                  <span class="action-text">{{ action.label }}</span>
                  <span class="action-sub">{{ action.sub }}</span>
                </button>
              </div>
            </div>
          </TransitionGroup>

          <div class="typing-indicator" v-if="isThinking">
            <span class="typing-dot"></span>
            <span class="typing-dot"></span>
            <span class="typing-dot"></span>
          </div>
        </div>

        <div class="quick-replies" v-if="messages.length === 0 && !isThinking">
          <button
            v-for="(q, i) in quickReplies"
            :key="i"
            class="quick-btn"
            @click="sendQuickReply(q)"
          >{{ q }}</button>
        </div>

        <div class="chat-input-area">
          <div class="input-wrapper">
            <textarea
              ref="inputRef"
              v-model="inputText"
              @keydown.enter.exact.prevent="sendMessage"
              placeholder="说点什么..."
              rows="1"
              :disabled="isThinking"
              @input="autoResize"
            ></textarea>
            <button
              class="send-btn"
              @click="sendMessage"
              :disabled="!inputText.trim() || isThinking"
              :class="{ active: inputText.trim() }"
            >
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <line x1="22" y1="2" x2="11" y2="13"/>
                <polygon points="22 2 15 22 11 13 2 9 22 2"/>
              </svg>
            </button>
          </div>
          <p class="input-hint">Man AI · 教会人回归人 · 本地运行</p>
        </div>
      </div>
    </Transition>

    <button class="chat-fab" @click="toggleChat" :class="{ open: isOpen }">
      <Transition name="fab-icon" mode="out-in">
        <svg v-if="!isOpen" key="open" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
        </svg>
        <svg v-else key="close" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <line x1="18" y1="6" x2="6" y2="18"/>
          <line x1="6" y1="6" x2="18" y2="18"/>
        </svg>
      </Transition>
    </button>
  </div>
</template>

<script setup>
import { ref, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useTimerStore } from '../stores/timer'

const router = useRouter()
const timerStore = useTimerStore()

const isOpen = ref(false)
const isThinking = ref(false)
const inputText = ref('')
const messages = ref([])
const messagesRef = ref(null)
const inputRef = ref(null)
const panelRef = ref(null)

const quickReplies = [
  '今天感觉很累',
  '我想看书',
  '不知道做什么好',
  '帮我选个方案'
]

watch(isOpen, async (val) => {
  if (val) {
    await nextTick()
    scrollToBottom()
    if (inputRef.value) inputRef.value.focus()
  }
})

function toggleChat() {
  isOpen.value = !isOpen.value
}

async function sendMessage() {
  const text = inputText.value.trim()
  if (!text || isThinking.value) return

  const timeStr = formatTime()
  messages.value.push({ role: 'user', content: text, time: timeStr })
  inputText.value = ''
  isThinking.value = true

  await nextTick()
  scrollToBottom()

  await simulateThinking(600 + Math.random() * 800)

  const response = generateResponse(text)
  const replyTime = formatTime()
  messages.value.push({
    role: 'assistant',
    content: response.text,
    time: replyTime,
    actions: response.actions || []
  })

  isThinking.value = false
  await nextTick()
  scrollToBottom()
}

async function sendQuickReply(text) {
  inputText.value = text
  await nextTick()
  sendMessage()
}

function executeAction(action) {
  if (action.type === 'start-focus') {
    const rec = action.data
    if (rec) {
      timerStore.startTimer(rec.durationMinutes, rec.title, rec)
      router.push('/')
      addSystemMessage(`已为你启动「${rec.title}」专注模式，${rec.durationMinutes}分钟倒计时已开始 ✨`)
    }
  } else if (action.type === 'show-options') {
    const opts = action.data || []
    const cards = opts.map(o => ({
      type: 'start-focus',
      icon: o.icon || '📖',
      label: o.title,
      sub: `${o.durationMinutes}分钟 · ${o.description}`,
      data: o,
      primary: true
    }))
    const replyTime = formatTime()
    messages.value.push({
      role: 'assistant',
      content: '这些是你现在可以做的，点击任意一个即可开始：',
      time: replyTime,
      actions: cards
    })
  }
}

function addSystemMessage(text) {
  const timeStr = formatTime()
  messages.value.push({ role: 'assistant', content: text, time: timeStr })
}

function formatTime() {
  const now = new Date()
  return `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`
}

function formatContent(content) {
  return content.replace(/\n/g, '<br/>')
}

function autoResize(e) {
  const el = e.target
  el.style.height = 'auto'
  el.style.height = Math.min(el.scrollHeight, 120) + 'px'
}

function scrollToBottom() {
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}

function simulateThinking(ms) {
  return new Promise(resolve => setTimeout(resolve, ms))
}

const PLANS = [
  { id: 1, title: '听20分钟钢琴', durationMinutes: 20, category: 'MUSIC', description: '让舒缓的钢琴曲带走疲惫', icon: '🎹' },
  { id: 2, title: '花25分钟写一些想法', durationMinutes: 25, category: 'WRITING', description: '把心中所想记录下来', icon: '✍️' },
  { id: 3, title: '发呆10分钟', durationMinutes: 10, category: 'RELAX', description: '什么都不做就是最好的充电', icon: '☁️' },
  { id: 4, title: '做5分钟拉伸', durationMinutes: 5, category: 'SPORT', description: '舒展身体唤醒肌肉', icon: '🤸' },
  { id: 5, title: '阅读30分钟', durationMinutes: 30, category: 'READING', description: '沉浸在书的世界里', icon: '📖' },
  { id: 6, title: '冥想15分钟', durationMinutes: 15, category: 'MEDITATION', description: '专注于一呼一吸', icon: '🧘' }
]

const TIRED_RESPONSES = [
  '累了就停下来吧，这不是偷懒，是身体在提醒你该休息了 🌙',
  '疲惫的时候，最勇敢的选择就是允许自己什么都不做',
  '你不需要每时每刻都保持高效，慢下来也是一种能力'
]

const ANXIOUS_RESPONSES = [
  '焦虑往往来自"我应该"，但也许你可以问问自己"我想要什么"',
  '不需要追赶任何人，你的节奏就是正确的节奏',
  '把注意力从"别人在做什么"收回到"我现在感觉如何"'
]

const LOST_RESPONSES = [
  '迷茫说明你在思考，这本身就是一种前进',
  '不知道做什么的时候，不如先做一件最小的事',
  '人生没有标准答案，此刻的空白也是旅程的一部分'
]

function detectMood(text) {
  const t = text.toLowerCase()
  if (/累|疲惫| tired|困|没精神|撑不住|扛不住了|不想动/.test(t)) return 'tired'
  if (/焦虑|慌|烦|急|赶不上|害怕|担心|压力|stress|anxious|不安/.test(t)) return 'anxious'
  if (/迷茫|不知道|空虚|无聊|没意思|没方向|lost|不知道做什/.test(t)) return 'lost'
  if (/开心|高兴|不错|挺好|happy|舒服|轻松| calm/.test(t)) return 'happy'
  return null
}

function matchPlan(text) {
  const t = text.toLowerCase()
  if (/书|阅读|read|看书|学习|study/.test(t)) return PLANS[4]
  if (/钢琴|音乐|听歌|music|钢琴曲|旋律/.test(t)) return PLANS[0]
  if (/写|记录|想法|日记|write|写作|写字/.test(t)) return PLANS[1]
  if (/发呆|休息|放空|relax|呆着|什么都不做/.test(t)) return PLANS[2]
  if (/拉伸|运动|运动|stretch|活动/.test(t)) return PLANS[3]
  if (/冥想|呼吸|meditation|静坐|打坐/.test(t)) return PLANS[5]
  return null
}

function pickRandom(arr) {
  return arr[Math.floor(Math.random() * arr.length)]
}

function generateResponse(userInput) {
  const mood = detectMood(userInput)
  const matchedPlan = matchPlan(userInput)
  const t = userInput.toLowerCase()

  if (/你好|hi|hello|嗨|在吗|hey/.test(t) && userInput.length < 8) {
    return {
      text: '你好呀 🌿\n\n我是 Man AI，在这里陪你慢下来。\n\n你想聊聊感受，还是让我帮你选一个专注方案？',
      actions: [{ type: 'show-options', label: '看看推荐方案', sub: '帮你挑选适合现在的', icon: '✨', data: PLANS }]
    }
  }

  if (/推荐|方案|建议|选一选|帮我选|做什么|what.*do|recommend/.test(t)) {
    const topPlans = [...PLANS].sort(() => Math.random() - 0.5).slice(0, 3)
    return {
      text: '根据你现在的情况，我推荐这几个：\n\n不用纠结，选一个让你感到"不排斥"的就好。没有对错之分。',
      actions: topPlans.map(p => ({
        type: 'start-focus',
        icon: p.icon,
        label: p.title,
        sub: `${p.durationMinutes}分钟 · ${p.description}`,
        data: p,
        primary: true
      }))
    }
  }

  if (/开始|启动|计时|focus|专注|进入|go|start/.test(t) && matchedPlan) {
    return {
      text: `好的，准备开始「${matchedPlan.title}」🌿\n\n${matchedPlan.description}\n\n点击下方按钮立即开始：`,
      actions: [{
        type: 'start-focus',
        icon: matchedPlan.icon,
        label: `开始${matchedPlan.title}`,
        sub: `${matchedPlan.durationMinutes}分钟`,
        data: matchedPlan,
        primary: true
      }]
    }
  }

  if (mood === 'tired') {
    const plan = PLANS[2]
    return {
      text: `${pickRandom(TIRED_RESPONSES)}\n\n要不要先发呆10分钟？什么都不做，只是存在。`,
      actions: [{
        type: 'start-focus',
        icon: plan.icon,
        label: plan.title,
        sub: plan.description,
        data: plan,
        primary: true
      }, {
        type: 'show-options',
        icon: '📋',
        label: '看看其他方案',
        sub: '如果不想休息的话',
        data: PLANS
      }]
    }
  }

  if (mood === 'anxious') {
    const plan = PLANS[5]
    return {
      text: `${pickRandom(ANXIOUS_RESPONSES)}\n\n试试冥想？15分钟的呼吸练习，能帮你的思绪慢慢落地。`,
      actions: [{
        type: 'start-focus',
        icon: plan.icon,
        label: plan.title,
        sub: plan.description,
        data: plan,
        primary: true
      }]
    }
  }

  if (mood === 'lost') {
    return {
      text: `${pickRandom(LOST_RESPONSES)}\n\n与其想太多，不如先做一件小事。这里有几个选项：`,
      actions: [...PLANS.slice(0, 4)].map(p => ({
        type: 'start-focus',
        icon: p.icon,
        label: p.title,
        sub: `${p.durationMinutes}分钟`,
        data: p,
        primary: true
      }))
    }
  }

  if (matchedPlan) {
    return {
      text: `听起来不错，「${matchedPlan.title}」很适合现在。\n\n${matchedPlan.description}\n\n准备好了吗？`,
      actions: [{
        type: 'start-focus',
        icon: matchedPlan.icon,
        label: `开始${matchedPlan.title}`,
        sub: `${matchedPlan.durationMinutes}分钟`,
        data: matchedPlan,
        primary: true
      }]
    }
  }

  const genericResponses = [
    '我听到了 🌿\n\n每个人的节奏都不一样，你想聊聊具体的感觉吗？或者我帮你挑一个适合现在的方案。',
    '嗯，我在听。\n\n有时候把想法说出来本身就是一种释放。要不要试试一个专注方案，让大脑换个频道？',
    '谢谢你愿意分享 ✨\n\n无论是什么状态都是被允许的。需要我帮你安排一段专注时间吗？'
  ]
  return {
    text: pickRandom(genericResponses),
    actions: [{ type: 'show-options', icon: '🌿', label: '查看推荐方案', sub: '一键开始专注', data: PLANS }]
  }
}
</script>

<style scoped>
.ai-chat {
  position: fixed;
  z-index: 60;
}

.chat-fab {
  position: fixed;
  bottom: 160px;
  right: 20px;
  width: 54px;
  height: 54px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--sage), #7ab070);
  border: none;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 20px rgba(168, 192, 160, 0.3);
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
  z-index: 61;
}

.chat-fab:hover {
  transform: scale(1.1);
  box-shadow: 0 8px 30px rgba(168, 192, 160, 0.4);
}

.chat-fab svg {
  width: 24px;
  height: 24px;
}

.chat-panel {
  position: fixed;
  bottom: 220px;
  right: 20px;
  width: calc(100vw - 40px);
  max-width: 380px;
  height: 520px;
  max-height: calc(100vh - 240px);
  display: flex;
  flex-direction: column;
  z-index: 62;
  overflow: hidden;
  animation: slide-up-enter 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 18px;
  border-bottom: 1px solid var(--border);
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ai-avatar {
  width: 38px;
  height: 38px;
  background: linear-gradient(135deg, rgba(168, 192, 160, 0.2), rgba(232, 168, 124, 0.15));
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.header-info h3 {
  font-size: 15px;
  font-weight: 700;
  color: var(--text);
  margin-bottom: 2px;
}

.ai-status {
  font-size: 11px;
  color: var(--text-muted);
}

.ai-status.online {
  color: var(--sage);
}

.close-btn {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: transparent;
  border: none;
  color: var(--text-dim);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.05);
  color: var(--text);
}

.close-btn svg {
  width: 16px;
  height: 16px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  scrollbar-width: thin;
  scrollbar-color: rgba(255, 255, 255, 0.05) transparent;
}

.chat-messages::-webkit-scrollbar {
  width: 4px;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 4px;
}

.welcome-msg {
  text-align: center;
  padding: 28px 16px;
}

.welcome-icon {
  font-size: 40px;
  margin-bottom: 14px;
  animation: float 3s ease-in-out infinite;
}

.welcome-text {
  font-size: 18px;
  font-weight: 700;
  color: var(--text);
  margin-bottom: 6px;
}

.welcome-sub {
  font-size: 13px;
  color: var(--warm);
  margin-bottom: 14px;
  font-weight: 500;
}

.welcome-hint {
  font-size: 13px;
  color: var(--text-dim);
  line-height: 1.7;
}

.messages-container {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.message-row {
  display: flex;
  flex-direction: column;
}

.message-row.user-msg {
  align-items: flex-end;
}

.message-row.ai-msg {
  align-items: flex-start;
}

.message-bubble {
  max-width: 85%;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.msg-role-icon {
  font-size: 16px;
  margin-bottom: 2px;
}

.msg-content {
  padding: 12px 16px;
  border-radius: 18px;
  font-size: 14px;
  line-height: 1.7;
  word-break: break-word;
}

.user-msg .msg-content {
  background: linear-gradient(135deg, var(--warm-deep), var(--warm));
  color: white;
  border-bottom-right-radius: 6px;
}

.ai-msg .msg-content {
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid var(--border);
  color: var(--text);
  border-bottom-left-radius: 6px;
}

.msg-time {
  font-size: 10px;
  color: var(--text-muted);
  padding: 0 6px;
}

.action-cards {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 8px;
  width: 100%;
  max-width: 340px;
}

.action-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid var(--border);
  color: var(--text-dim);
  cursor: pointer;
  transition: all 0.35s cubic-bezier(0.16, 1, 0.3, 1);
  text-align: left;
  width: 100%;
}

.action-card:hover {
  border-color: rgba(232, 168, 124, 0.25);
  background: rgba(232, 168, 124, 0.06);
  color: var(--text);
  transform: translateX(4px);
}

.action-card.primary:hover {
  border-color: rgba(168, 192, 160, 0.3);
  background: rgba(168, 192, 160, 0.08);
  box-shadow: 0 4px 16px rgba(168, 192, 160, 0.1);
}

.action-icon {
  font-size: 26px;
  flex-shrink: 0;
}

.action-text {
  font-size: 14px;
  font-weight: 600;
  color: inherit;
}

.action-sub {
  font-size: 11px;
  color: var(--text-muted);
  margin-top: 2px;
}

.typing-indicator {
  display: flex;
  gap: 5px;
  padding: 14px 18px;
  align-self: flex-start;
}

.typing-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--sage);
  opacity: 0.4;
  animation: typing-bounce 1.4s ease-in-out infinite;
}

.typing-dot:nth-child(2) { animation-delay: 0.2s; }
.typing-dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing-bounce {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.4; }
  30% { transform: translateY(-6px); opacity: 1; }
}

.quick-replies {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 0 16px 10px;
}

.quick-btn {
  padding: 8px 14px;
  border-radius: 20px;
  background: rgba(168, 192, 160, 0.08);
  border: 1px solid rgba(168, 192, 160, 0.2);
  color: var(--sage);
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.quick-btn:hover {
  background: rgba(168, 192, 160, 0.15);
  transform: translateY(-1px);
}

.chat-input-area {
  padding: 12px 16px;
  border-top: 1px solid var(--border);
  flex-shrink: 0;
}

.input-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid var(--border);
  border-radius: 24px;
  padding: 8px 12px;
  transition: border-color 0.3s ease;
}

.input-wrapper:focus-within {
  border-color: rgba(168, 192, 160, 0.3);
}

.input-wrapper textarea {
  flex: 1;
  background: transparent;
  border: none;
  outline: none;
  color: var(--text);
  font-size: 14px;
  resize: none;
  max-height: 120px;
  line-height: 1.5;
  font-family: inherit;
}

.input-wrapper textarea::placeholder {
  color: var(--text-muted);
}

.send-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: transparent;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.3s ease;
}

.send-btn.active {
  background: linear-gradient(135deg, var(--sage), #7ab070);
  color: white;
}

.send-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.send-btn svg {
  width: 16px;
  height: 16px;
}

.input-hint {
  text-align: center;
  font-size: 10px;
  color: var(--text-muted);
  margin-top: 6px;
  letter-spacing: 1px;
}

/* ========== Transitions ========== */
.chat-panel-enter-active,
.chat-panel-leave-active {
  transition: all 0.35s cubic-bezier(0.16, 1, 0.3, 1);
}
.chat-panel-enter-from,
.chat-panel-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}

.fab-icon-enter-active,
.fab-icon-leave-active {
  transition: all 0.25s ease;
}
.fab-icon-enter-from,
.fab-icon-leave-to {
  opacity: 0;
  transform: rotate(-90deg) scale(0.5);
}

.msg-list-enter-active {
  transition: all 0.35s ease;
}
.msg-list-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

/* ========== PC端 ========== */
@media (min-width: 768px) {
  .chat-fab {
    bottom: 172px;
    right: 36px;
    width: 58px;
    height: 58px;
  }

  .chat-panel {
    bottom: 236px;
    right: 36px;
    max-width: 420px;
    height: 560px;
  }

  .welcome-icon {
    font-size: 48px;
  }

  .welcome-text {
    font-size: 20px;
  }

  .msg-content {
    font-size: 15px;
    padding: 14px 18px;
  }

  .action-card {
    padding: 16px 20px;
  }

  .action-icon {
    font-size: 30px;
  }

  .action-text {
    font-size: 15px;
  }
}
</style>
