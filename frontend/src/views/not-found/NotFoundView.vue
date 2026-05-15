<template>
  <div class="page-container">
    <EmptyState
      icon="404"
      title="页面不存在"
      description="你访问的页面可能已经被移动，或者路径写错了。"
    >
      <div style="margin-top: 16px;">
        <router-link to="/">
          <n-button type="primary">回到首页</n-button>
        </router-link>
      </div>
    </EmptyState>
  </div>
</template>

<script setup lang="ts">
import { NButton } from 'naive-ui'
import EmptyState from '@/components/EmptyState.vue'
</script>
