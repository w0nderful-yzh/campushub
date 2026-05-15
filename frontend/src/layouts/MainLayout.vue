<template>
  <div class="layout-shell">
    <AppHeader />
    <main class="layout-main">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import AppHeader from '@/components/AppHeader.vue'
</script>

<style scoped lang="scss">
.layout-shell {
  min-height: 100vh;
  background:
    radial-gradient(1200px 460px at 12% -8%, rgba(59, 130, 246, 0.14), transparent 55%),
    radial-gradient(1000px 360px at 88% -14%, rgba(99, 102, 241, 0.1), transparent 52%);
}

.layout-main {
  position: relative;
}
</style>
