<template>
  <div class="pagination-wrap">
    <div class="muted summary">
      共 {{ total }} 条，当前第 {{ page }} 页
    </div>

    <n-pagination
      :page="page"
      :page-size="pageSize"
      :item-count="total"
      :page-sizes="[5, 10, 20, 30]"
      show-size-picker
      @update:page="(value) => emit('update:page', value)"
      @update:page-size="(value) => emit('update:page-size', value)"
    />
  </div>
</template>

<script setup lang="ts">
import { NPagination } from 'naive-ui'

defineProps<{
  page: number
  pageSize: number
  total: number
}>()

const emit = defineEmits<{
  (e: 'update:page', value: number): void
  (e: 'update:page-size', value: number): void
}>()
</script>

<style scoped lang="scss">
.pagination-wrap {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
  margin-top: 8px;
  flex-wrap: wrap;
}

.summary {
  font-size: 13px;
}
</style>
