<template>
  <div class="home">
    <div class="hero">
      <p class="sparkle">✨</p>
      <h1>选择你想聊的方式</h1>
      <p class="sub">一个更懂你情绪，一个更擅长做事，按你当下需求选择就行。</p>
    </div>

    <div class="cards">
      <RouterLink class="card" to="/love">
        <span class="card-icon">💕</span>
        <div class="card-body">
          <h2>AI 恋爱大师</h2>
          <p>适合聊感情、求安慰、想回消息。它会更温柔地陪你分析和给建议。</p>
        </div>
        <span class="arrow">→</span>
      </RouterLink>

      <RouterLink class="card" to="/manus">
        <span class="card-icon">🤖</span>
        <div class="card-body">
          <h2>AI 超级智能体</h2>
          <p>适合要效率、做任务、拆步骤。它会快速给出可执行方案，帮你推进事情。</p>
        </div>
        <span class="arrow">→</span>
      </RouterLink>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.home {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  max-width: 980px;
  width: 100%;
  margin: 0 auto;
  padding: 2rem 1.25rem 3rem;
}
.hero {
  text-align: center;
  margin-bottom: 1.6rem;
}
.sparkle {
  margin: 0 0 0.25rem;
  font-size: 1.7rem;
  color: #ff9a6e;
  animation: pulse 2s ease-in-out infinite;
}
@keyframes pulse {
  50% {
    transform: scale(1.08);
    opacity: 0.85;
  }
}
.hero h1 {
  margin: 0 0 0.5rem;
  font-size: 1.85rem;
  font-weight: 700;
  color: #b85e3d;
}
.sub {
  margin: 0;
  color: #8d6b5d;
  font-size: 0.98rem;
}
.cards {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 1rem;
}
.card {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.25rem 1.35rem;
  border-radius: 20px;
  text-decoration: none;
  color: inherit;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.85), rgba(255, 243, 233, 0.95));
  border: 1px solid rgba(255, 183, 141, 0.55);
  box-shadow: 0 10px 30px rgba(255, 152, 97, 0.16);
  transition: transform 0.18s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}
.card:hover {
  border-color: rgba(255, 141, 82, 0.95);
  box-shadow: 0 14px 38px rgba(255, 141, 82, 0.26);
  transform: translateY(-3px);
}
.card-icon {
  font-size: 2.15rem;
  line-height: 1;
}
.card-body {
  flex: 1;
  min-width: 0;
}
.card-body h2 {
  margin: 0 0 0.35rem;
  font-size: 1.1rem;
  color: #a85a3b;
}
.card-body p {
  margin: 0;
  font-size: 0.875rem;
  color: #8a6f5d;
  line-height: 1.45;
}
.arrow {
  font-size: 1.1rem;
  color: #ff935c;
  flex-shrink: 0;
}

@media (max-width: 760px) {
  .cards {
    grid-template-columns: 1fr;
  }
}
</style>
