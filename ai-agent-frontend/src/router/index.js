import { createRouter, createWebHistory } from 'vue-router'
import Home from '@/views/Home.vue'
import LoveAppChat from '@/views/LoveAppChat.vue'
import ManusChat from '@/views/ManusChat.vue'

const routes = [
  { path: '/', name: 'home', component: Home, meta: { title: '应用中心' } },
  { path: '/love', name: 'love', component: LoveAppChat, meta: { title: 'AI 恋爱大师' } },
  { path: '/manus', name: 'manus', component: ManusChat, meta: { title: 'AI 超级智能体' } },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

router.afterEach((to) => {
  if (to.meta?.title) {
    document.title = `${to.meta.title} · AI Agent`
  }
})

export default router
