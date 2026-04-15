<template>
  <div class="app-root">
    <header class="app-header">
      <RouterLink to="/" class="brand">✿ AI 小窝</RouterLink>
    </header>
    <main class="app-main">
      <RouterView />
    </main>
  </div>
</template>

<script setup>
</script>

<style scoped>
.app-root {
  height: 100%;
  min-height: 100%;
  max-height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: linear-gradient(
    165deg,
    #fffaf3 0%,
    #fff1e4 34%,
    #ffe7d4 68%,
    #ffe1c8 100%
  );
  color: #5e4a3d;
}
.app-header {
  flex-shrink: 0;
  padding: 0.65rem 1.15rem;
  border-bottom: 1px solid rgba(255, 183, 141, 0.45);
  background: rgba(255, 250, 244, 0.86);
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 18px rgba(255, 152, 97, 0.14);
  z-index: 30;
}
.brand {
  font-weight: 700;
  letter-spacing: 0.04em;
  font-size: 1.05rem;
  color: #b8613f;
  text-decoration: none;
}
.brand:hover {
  color: #d27650;
}
.app-main {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
</style>
