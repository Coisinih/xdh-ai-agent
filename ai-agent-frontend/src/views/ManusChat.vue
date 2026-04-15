<template>
  <SseChatRoom
    title="AI 超级智能体"
    sse-path="/ai/chat/agent_stream"
    :with-chat-id="false"
    :split-assistant-chunks="true"
  />
</template>

<script setup>
import SseChatRoom from '@/components/SseChatRoom.vue'
</script>
