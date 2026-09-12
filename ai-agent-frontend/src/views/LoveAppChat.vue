<template>
  <SseChatRoom
    title="AI 恋爱大师"
    sse-path="/ai/chat/sse"
    :with-chat-id="true"
  />
</template>

<script setup>
import SseChatRoom from '@/components/SseChatRoom.vue'
</script>
