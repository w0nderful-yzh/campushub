<template>
  <div class="empty section-card">
    <div class="icon">{{ icon }}</div>
    <h3 class="title">{{ title }}</h3>
    <p class="desc">{{ description }}</p>
    <slot />
  </div>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    icon?: string
    title?: string
    description?: string
  }>(),
  {
    icon: '◌',
    title: '暂无内容',
    description: '这里暂时还没有数据'
  }
)
</script>

<style scoped lang="scss">
.empty {
  padding: 34px 20px;
  text-align: center;
}

.icon {
  width: 56px;
  height: 56px;
  margin: 0 auto 12px;
  border-radius: 8px;
  display: grid;
  place-items: center;
  background: #ecfdf5;
  color: #0f766e;
  font-size: 24px;
  font-weight: 800;
}

.title {
  margin: 0 0 8px;
  font-size: 20px;
}

.desc {
  margin: 0;
  color: #64748b;
}
</style>
