<template>
  <div class="chat-page">
    <div class="chat-toolbar">
      <RouterLink to="/" class="back">← 返回</RouterLink>
      <div class="toolbar-meta">
        <span class="title">{{ title }}</span>
        <span v-if="chatId" class="chip" title="当前会话 ID">{{ shortId }}</span>
      </div>
    </div>

    <div ref="scrollRef" class="chat-scroll">
      <div v-if="!messages.length" class="empty">发一句甜甜的话，开始聊天吧</div>

      <template v-for="(m, i) in messages" :key="i">
        <div class="row" :class="m.role === 'user' ? 'row-user' : 'row-ai'">
          <div v-if="m.role === 'assistant'" class="avatar-slot">
            <div v-if="showAiAvatarForIndex(i)" class="avatar avatar-ai" aria-hidden="true">
              <svg class="avatar-svg" viewBox="0 0 40 40">
                <defs>
                  <linearGradient id="aiFace" x1="0" y1="0" x2="1" y2="1">
                    <stop offset="0%" stop-color="#ffdff0" />
                    <stop offset="100%" stop-color="#ffcde6" />
                  </linearGradient>
                </defs>
                <circle cx="20" cy="20" r="18" fill="url(#aiFace)" stroke="#fff" stroke-width="2" />
                <path d="M9 12 C12 8, 16 8, 19 12" fill="none" stroke="#ff9bc8" stroke-width="2" stroke-linecap="round" />
                <path d="M21 12 C24 8, 28 8, 31 12" fill="none" stroke="#ff9bc8" stroke-width="2" stroke-linecap="round" />
                <ellipse cx="14.5" cy="17.5" rx="2.4" ry="2.8" fill="#8f5b74" />
                <ellipse cx="25.5" cy="17.5" rx="2.4" ry="2.8" fill="#8f5b74" />
                <circle cx="15.2" cy="16.7" r="0.7" fill="#fff" />
                <circle cx="26.2" cy="16.7" r="0.7" fill="#fff" />
                <ellipse cx="12" cy="23" rx="2.3" ry="1.5" fill="#ffb3d5" opacity="0.85" />
                <ellipse cx="28" cy="23" rx="2.3" ry="1.5" fill="#ffb3d5" opacity="0.85" />
                <path
                  d="M13 24 Q20 30 27 24"
                  fill="none"
                  stroke="#d65a8b"
                  stroke-width="2.2"
                  stroke-linecap="round"
                />
              </svg>
            </div>
          </div>

          <div
            class="bubble"
            :class="[
              m.role === 'user' ? 'bubble-user tail-user' : 'bubble-ai tail-ai',
              m.collapsed ? 'bubble-collapsed' : '',
              canToggleCollapse(m) ? 'bubble-clickable' : '',
            ]"
            @click="toggleBubbleCollapse(m)"
          >
            <div class="bubble-text">{{ m.content }}</div>
            <span v-if="m.streaming" class="cursor">▌</span>
          </div>

          <div v-if="m.role === 'user'" class="avatar-slot">
            <div v-if="showUserAvatarForIndex(i)" class="avatar avatar-user" aria-hidden="true">
              <svg class="avatar-svg" viewBox="0 0 40 40">
                <defs>
                  <linearGradient id="userFace" x1="0" y1="0" x2="1" y2="1">
                    <stop offset="0%" stop-color="#ffe9d9" />
                    <stop offset="100%" stop-color="#ffd7bf" />
                  </linearGradient>
                </defs>
                <circle cx="20" cy="20" r="18" fill="url(#userFace)" stroke="#fff" stroke-width="2" />
                <path d="M10 13 C12 8, 18 7, 22 10 C27 13, 31 12, 32 16" fill="#d6875f" opacity="0.85" />
                <ellipse cx="14.5" cy="18" rx="2.2" ry="2.6" fill="#7f4d3f" />
                <ellipse cx="25.5" cy="18" rx="2.2" ry="2.6" fill="#7f4d3f" />
                <circle cx="15.1" cy="17.2" r="0.65" fill="#fff" />
                <circle cx="26.1" cy="17.2" r="0.65" fill="#fff" />
                <ellipse cx="12" cy="23.2" rx="2.2" ry="1.4" fill="#f5b39e" opacity="0.8" />
                <ellipse cx="28" cy="23.2" rx="2.2" ry="1.4" fill="#f5b39e" opacity="0.8" />
                <path
                  d="M14 26 Q20 30 26 26"
                  fill="none"
                  stroke="#c0705a"
                  stroke-width="2"
                  stroke-linecap="round"
                />
              </svg>
            </div>
          </div>
        </div>
      </template>

      <div v-if="error" class="error-banner">{{ error }}</div>
    </div>

    <form class="composer" @submit.prevent="onSubmit">
      <textarea
        v-model="draft"
        class="input"
        rows="2"
        placeholder="想说点什么…（Enter 发送，Shift+Enter 换行）"
        :disabled="loading"
        @keydown="onKeydown"
      />
      <button type="submit" class="send" :disabled="loading || !draft.trim()">
        {{ loading ? '正在想…' : '发送 ❤' }}
      </button>
    </form>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'
import { streamSseGet } from '@/api/sseStream'

const props = defineProps({
  title: { type: String, required: true },
  ssePath: { type: String, required: true },
  withChatId: { type: Boolean, default: false },
  splitAssistantChunks: { type: Boolean, default: false },
})

const chatId = ref('')
const messages = ref([])
const draft = ref('')
const loading = ref(false)
const error = ref('')
const scrollRef = ref(null)
let abortController = null

const shortId = computed(() => {
  const id = chatId.value
  if (!id) return ''
  return id.length > 14 ? `${id.slice(0, 6)}...${id.slice(-4)}` : id
})

function genChatId() {
  if (typeof crypto !== 'undefined' && crypto.randomUUID) {
    return crypto.randomUUID()
  }
  return `chat-${Date.now()}-${Math.random().toString(16).slice(2)}`
}

if (props.withChatId) {
  chatId.value = genChatId()
}

function showAiAvatarForIndex(i) {
  const prev = messages.value[i - 1]
  return !prev || prev.role !== 'assistant'
}

function showUserAvatarForIndex(i) {
  const prev = messages.value[i - 1]
  return !prev || prev.role !== 'user'
}

async function scrollToBottom() {
  await nextTick()
  const el = scrollRef.value
  if (el) el.scrollTop = el.scrollHeight
}

watch(
  () => messages.value.map((m) => `${m.role}:${m.content}:${m.streaming ? 1 : 0}`).join('\n'),
  () => {
    scrollToBottom()
  },
)

function onKeydown(e) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    onSubmit()
  }
}

function abortStream() {
  abortController?.abort()
  abortController = null
}

function canToggleCollapse(message) {
  return message?.role === 'assistant' && message?.toggleable === true && !message?.streaming
}

function toggleBubbleCollapse(message) {
  if (!canToggleCollapse(message)) return
  message.collapsed = !message.collapsed
}

onBeforeUnmount(() => {
  abortStream()
})

async function onSubmit() {
  const text = draft.value.trim()
  if (!text || loading.value) return

  error.value = ''
  messages.value.push({ role: 'user', content: text })
  draft.value = ''
  loading.value = true
  abortController = new AbortController()

  const params = { message: text }
  if (props.withChatId) {
    params.chatId = chatId.value
  }

  if (props.splitAssistantChunks) {
    messages.value.push({ role: 'assistant', content: '...', streaming: true, placeholder: true })
    let assistantIndex = messages.value.length - 1

    const pushAssistantMessage = (chunkText) => {
      if (chunkText == null || chunkText === '') return
      const current = messages.value[assistantIndex]
      if (current?.role === 'assistant' && current.placeholder) {
        current.content = chunkText
        current.placeholder = false
        current.collapsed = false
        current.toggleable = true
        return
      }
      if (current?.role === 'assistant') {
        current.streaming = false
        current.collapsed = true
        current.toggleable = true
      }
      messages.value.push({
        role: 'assistant',
        content: chunkText,
        streaming: true,
        collapsed: false,
        toggleable: true,
      })
      assistantIndex = messages.value.length - 1
    }

    try {
      await streamSseGet(
        props.ssePath,
        params,
        (chunk) => {
          pushAssistantMessage(chunk)
        },
        abortController.signal,
      )
    } catch (e) {
      if (e.name === 'AbortError') {
        error.value = '已停止生成'
      } else {
        error.value = e?.message || '请求失败'
      }
    } finally {
      const last = messages.value[assistantIndex]
      if (last) {
        last.streaming = false
        last.placeholder = false
        last.collapsed = false
      }
      loading.value = false
      abortController = null
      scrollToBottom()
    }
    return
  }

  messages.value.push({ role: 'assistant', content: '', streaming: true })
  const assistantIndex = messages.value.length - 1

  try {
    await streamSseGet(
      props.ssePath,
      params,
      (chunk) => {
        const last = messages.value[assistantIndex]
        if (last) {
          last.content += chunk
        }
      },
      abortController.signal,
    )
  } catch (e) {
    if (e.name === 'AbortError') {
      error.value = '已停止生成'
    } else {
      error.value = e?.message || '请求失败'
    }
  } finally {
    const last = messages.value[assistantIndex]
    if (last) last.streaming = false
    loading.value = false
    abortController = null
    scrollToBottom()
  }
}
</script>

<style scoped>
.chat-page {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  max-width: 880px;
  width: 100%;
  margin: 0 auto;
  padding: 0 0.85rem 0.75rem;
  overflow: hidden;
}

.chat-toolbar {
  flex-shrink: 0;
  position: sticky;
  top: 0;
  z-index: 20;
  display: flex;
  align-items: center;
  gap: 0.85rem;
  padding: 0.55rem 0.15rem 0.75rem;
  margin: 0 -0.15rem;
  background: linear-gradient(
    180deg,
    rgba(255, 250, 252, 0.97) 0%,
    rgba(255, 245, 249, 0.92) 55%,
    rgba(255, 245, 249, 0) 100%
  );
  backdrop-filter: blur(8px);
  border-bottom: 1px solid rgba(255, 182, 193, 0.35);
}

.back {
  color: #b87a9a;
  text-decoration: none;
  font-size: 0.9rem;
  font-weight: 600;
}
.back:hover {
  color: #d65a8b;
}

.toolbar-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.45rem 0.65rem;
  min-width: 0;
}

.title {
  font-weight: 700;
  font-size: 1.05rem;
  color: #c94f7c;
  letter-spacing: 0.02em;
}

.chip {
  font-size: 0.72rem;
  color: #9b7a8c;
  background: rgba(255, 255, 255, 0.85);
  border: 1px solid rgba(255, 182, 193, 0.55);
  padding: 0.2rem 0.55rem;
  border-radius: 999px;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-scroll {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  overscroll-behavior: contain;
  padding: 0.35rem 0.1rem 0.85rem;
  display: flex;
  flex-direction: column;
  gap: 0.85rem;
  background-image: radial-gradient(
      circle at 12% 18%,
      rgba(255, 182, 193, 0.2) 0%,
      transparent 42%
    ),
    radial-gradient(circle at 88% 72%, rgba(233, 198, 255, 0.18) 0%, transparent 45%),
    radial-gradient(circle at 50% 95%, rgba(255, 218, 230, 0.35) 0%, transparent 38%);
}

.empty {
  text-align: center;
  color: #b89aa8;
  font-size: 0.92rem;
  padding: 2rem 0.5rem;
}

.row {
  display: flex;
  align-items: flex-end;
  gap: 0.45rem;
  width: 100%;
}

.row-user {
  justify-content: flex-end;
}

.row-ai {
  justify-content: flex-start;
}

.avatar-slot {
  width: 40px;
  flex-shrink: 0;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  align-self: flex-end;
  padding-bottom: 2px;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(255, 143, 178, 0.25);
}

.avatar-svg {
  display: block;
  width: 100%;
  height: 100%;
}

.bubble {
  max-width: min(76%, 500px);
  padding: 0.6rem 0.85rem;
  border-radius: 18px;
  line-height: 1.55;
  font-size: 0.94rem;
  position: relative;
  white-space: pre-wrap;
  word-break: break-word;
  box-shadow: 0 6px 18px rgba(255, 143, 178, 0.12);
  overflow: visible;
}

.bubble-user {
  background: linear-gradient(145deg, #ffc8dd 0%, #ffb3d0 45%, #ffa8cb 100%);
  color: #5c2d45;
  border-bottom-right-radius: 6px;
  border: 1px solid rgba(255, 255, 255, 0.65);
}

.bubble-ai {
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid rgba(210, 200, 205, 0.55);
  color: #4a3f48;
  border-bottom-left-radius: 6px;
}

.bubble-collapsed {
  color: #8f8f98;
}

.bubble-collapsed .bubble-text {
  display: block;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.bubble-clickable {
  cursor: pointer;
}

.tail-ai::before {
  content: '';
  position: absolute;
  left: -6px;
  bottom: 11px;
  width: 0;
  height: 0;
  border-style: solid;
  border-width: 5px 7px 5px 0;
  border-color: transparent rgba(255, 255, 255, 0.96) transparent transparent;
  filter: drop-shadow(-1px 0 0 rgba(210, 200, 205, 0.45));
}

.tail-user::before {
  content: '';
  position: absolute;
  right: -6px;
  bottom: 11px;
  width: 0;
  height: 0;
  border-style: solid;
  border-width: 5px 0 5px 7px;
  border-color: transparent transparent transparent #ffb3d0;
  filter: drop-shadow(1px 0 0 rgba(255, 255, 255, 0.45));
}

.bubble-text {
  display: inline;
}

.cursor {
  display: inline-block;
  animation: blink 1s step-end infinite;
  margin-left: 1px;
  color: #ff8fab;
}

@keyframes blink {
  50% {
    opacity: 0;
  }
}

.error-banner {
  align-self: center;
  max-width: 100%;
  padding: 0.5rem 0.75rem;
  border-radius: 12px;
  background: rgba(255, 228, 232, 0.95);
  border: 1px solid rgba(255, 143, 178, 0.55);
  color: #a84860;
  font-size: 0.875rem;
}

.composer {
  flex-shrink: 0;
  display: flex;
  gap: 0.55rem;
  align-items: flex-end;
  padding-top: 0.55rem;
  border-top: 1px solid rgba(255, 182, 193, 0.4);
  background: linear-gradient(0deg, rgba(255, 250, 252, 0.96) 0%, rgba(255, 250, 252, 0) 100%);
}

.input {
  flex: 1;
  resize: none;
  border-radius: 16px;
  border: 1px solid rgba(255, 182, 193, 0.65);
  background: rgba(255, 255, 255, 0.88);
  color: #5c4a5f;
  padding: 0.6rem 0.85rem;
  outline: none;
  box-shadow: inset 0 1px 3px rgba(255, 182, 193, 0.15);
}
.input::placeholder {
  color: #c4a8b5;
}
.input:focus {
  border-color: rgba(255, 143, 178, 0.95);
  box-shadow: 0 0 0 3px rgba(255, 182, 193, 0.35);
}
.input:disabled {
  opacity: 0.65;
}

.send {
  flex-shrink: 0;
  border: none;
  border-radius: 16px;
  padding: 0.6rem 1rem;
  font-weight: 700;
  cursor: pointer;
  background: linear-gradient(145deg, #ff8fab, #ff6b9d);
  color: #fff;
  box-shadow: 0 6px 16px rgba(255, 107, 157, 0.35);
}
.send:disabled {
  opacity: 0.55;
  cursor: not-allowed;
  box-shadow: none;
}
.send:not(:disabled):hover {
  filter: brightness(1.04);
}
</style>
