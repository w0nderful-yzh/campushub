<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h1 class="page-title">校园帖子广场</h1>
        <p class="page-subtitle">
          看见校园里的新鲜事，也把你的想法留在这里。
        </p>
      </div>

      <n-space>
        <router-link to="/posts/create">
          <n-button type="primary">发布帖子</n-button>
        </router-link>
      </n-space>
    </div>

    <section class="section-card filter-card">
      <div class="toolbar-wrap">
        <n-input
          v-model:value="keywordInput"
          clearable
          placeholder="搜索帖子标题或内容"
          @keyup.enter="applySearch"
        />
        <n-select
          v-model:value="filters.categoryId"
          :options="categoryOptions"
          placeholder="全部分类"
          clearable
        />
        <n-select
          v-model:value="filters.sortType"
          :options="sortOptions"
          placeholder="排序"
        />
        <n-button type="primary" @click="applySearch">搜索</n-button>
        <n-button quaternary @click="resetFilters">重置</n-button>
      </div>
    </section>

    <div v-if="loading" class="loading-wrap section-card">
      <n-spin size="large" />
    </div>

    <n-alert v-else-if="errorText" type="error" :show-icon="false">
      {{ errorText }}
    </n-alert>

    <template v-else>
      <div v-if="posts.length" class="stack">
        <PostCard
          v-for="item in posts"
          :key="item.id"
          :post="item"
        />
        <section class="section-card pager-card">
          <PaginationBar
            :page="filters.pageNum"
            :page-size="filters.pageSize"
            :total="total"
            @update:page="handlePageChange"
            @update:page-size="handlePageSizeChange"
          />
        </section>
      </div>

      <EmptyState
        v-else
        icon="◎"
        title="还没有帖子"
        description="试试切换分类、排序或关键词，或者直接发布第一条帖子。"
      >
        <div style="margin-top: 16px;">
          <router-link to="/posts/create">
            <n-button type="primary">去发布</n-button>
          </router-link>
        </div>
      </EmptyState>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { NAlert, NButton, NInput, NSelect, NSpace, NSpin } from 'naive-ui'
import type { SelectOption } from 'naive-ui'

import { getPostsApi } from '@/api/post'
import EmptyState from '@/components/EmptyState.vue'
import PaginationBar from '@/components/PaginationBar.vue'
import PostCard from '@/components/PostCard.vue'
import { useAppStore } from '@/stores/app'
import type { PostVO } from '@/types/post'

const appStore = useAppStore()

const loading = ref(false)
const errorText = ref('')
const posts = ref<PostVO[]>([])
const total = ref(0)
const keywordInput = ref('')

const filters = reactive({
  pageNum: 1,
  pageSize: 10,
  categoryId: null as number | null,
  sortType: 'latest' as 'latest' | 'hottest',
  keyword: ''
})

const sortOptions = [
  { label: '最新发布', value: 'latest' },
  { label: '最热帖子', value: 'hottest' }
]

const categoryOptions = computed<SelectOption[]>(() =>
  appStore.categories.map((item) => ({
    label: item.name,
    value: item.id
  }))
)

async function loadCategories() {
  try {
    await appStore.fetchCategories()
  } catch {
    // 失败时保持静默
  }
}

async function loadPosts() {
  loading.value = true
  errorText.value = ''

  try {
    const res = await getPostsApi({
      pageNum: filters.pageNum,
      pageSize: filters.pageSize,
      sortType: filters.sortType,
      categoryId: filters.categoryId ?? undefined,
      keyword: filters.keyword || undefined
    })
    posts.value = res.data || []
    total.value = res.total || 0
  } catch (error) {
    errorText.value = error instanceof Error ? error.message : '列表加载失败'
  } finally {
    loading.value = false
  }
}

function applySearch() {
  filters.keyword = keywordInput.value.trim()
  filters.pageNum = 1
  loadPosts()
}

function resetFilters() {
  keywordInput.value = ''
  filters.keyword = ''
  filters.categoryId = null
  filters.sortType = 'latest'
  filters.pageNum = 1
  filters.pageSize = 10
  loadPosts()
}

function handlePageChange(value: number) {
  filters.pageNum = value
}

function handlePageSizeChange(value: number) {
  filters.pageSize = value
  filters.pageNum = 1
}

watch(
  () => [filters.pageNum, filters.pageSize, filters.categoryId, filters.sortType],
  () => {
    loadPosts()
  }
)

onMounted(async () => {
  await loadCategories()
  await loadPosts()
})
</script>

<style scoped lang="scss">
.filter-card {
  padding: 18px;
  margin-bottom: 18px;
}

.loading-wrap,
.pager-card {
  padding: 24px;
}

.loading-wrap {
  display: grid;
  place-items: center;
  min-height: 280px;
}
</style>
