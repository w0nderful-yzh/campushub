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
      <div class="search-row">
        <n-auto-complete
          class="main-search"
          v-model:value="keywordInput"
          :options="suggestionOptions"
          clearable
          placeholder="搜索帖子标题或内容"
          @update:value="handleKeywordInput"
          @select="handleSuggestionSelect"
          @keyup.enter="applySearch"
        />
      </div>
      <div v-if="hasActiveSearch" class="filter-row">
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
        :title="emptyTitle"
        :description="emptyDescription"
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
import { NAlert, NAutoComplete, NButton, NSelect, NSpace, NSpin } from 'naive-ui'
import type { SelectOption } from 'naive-ui'

import { getPostSearchSuggestionsApi, getPostsApi, searchPostsApi } from '@/api/post'
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
const suggestions = ref<string[]>([])
let suggestTimer: ReturnType<typeof window.setTimeout> | null = null
let suggestRequestId = 0

const filters = reactive({
  pageNum: 1,
  pageSize: 10,
  categoryId: null as number | null,
  sortType: 'latest' as 'latest' | 'hottest' | 'relevance',
  keyword: ''
})

const sortOptions = computed(() => {
  if (filters.keyword) {
    return [
      { label: '相关度优先', value: 'relevance' },
      { label: '最新发布', value: 'latest' }
    ]
  }

  return [
    { label: '最新发布', value: 'latest' },
    { label: '最热帖子', value: 'hottest' }
  ]
})

const categoryOptions = computed<SelectOption[]>(() =>
  appStore.categories.map((item) => ({
    label: item.name,
    value: item.id
  }))
)

const suggestionOptions = computed(() =>
  suggestions.value.map((item) => ({
    label: item,
    value: item
  }))
)

const hasActiveSearch = computed(() => Boolean(filters.keyword))
const emptyTitle = computed(() => filters.keyword ? '没有找到相关帖子' : '还没有帖子')
const emptyDescription = computed(() =>
  filters.keyword
    ? '换个关键词试试，或者清空搜索查看全部帖子。'
    : '试试切换分类、排序，或者直接发布第一条帖子。'
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
    const params = {
      pageNum: filters.pageNum,
      pageSize: filters.pageSize,
      categoryId: filters.categoryId ?? undefined,
      keyword: filters.keyword || undefined
    }
    const res = filters.keyword
      ? await searchPostsApi({
          ...params,
          sortType: filters.sortType === 'latest' ? 'latest' : 'relevance'
        })
      : await getPostsApi({
          ...params,
          sortType: filters.sortType === 'hottest' ? 'hottest' : 'latest'
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
  suggestions.value = []
  if (filters.keyword && filters.sortType === 'hottest') {
    filters.sortType = 'relevance'
  }
  if (!filters.keyword && filters.sortType === 'relevance') {
    filters.sortType = 'latest'
  }
  filters.pageNum = 1
  loadPosts()
}

function handleKeywordInput(value: string) {
  if (suggestTimer) {
    window.clearTimeout(suggestTimer)
  }
  const keyword = value.trim()
  const requestId = ++suggestRequestId
  if (!keyword) {
    suggestions.value = []
    if (filters.keyword) {
      resetFilters()
    }
    return
  }
  suggestTimer = window.setTimeout(async () => {
    try {
      const res = await getPostSearchSuggestionsApi({ keyword, size: 8 })
      if (requestId === suggestRequestId) {
        suggestions.value = res.data || []
      }
    } catch {
      if (requestId === suggestRequestId) {
        suggestions.value = []
      }
    }
  }, 220)
}

function handleSuggestionSelect(value: string) {
  if (suggestTimer) {
    window.clearTimeout(suggestTimer)
  }
  suggestions.value = []
  keywordInput.value = value
  applySearch()
}

function resetFilters() {
  keywordInput.value = ''
  filters.keyword = ''
  filters.categoryId = null
  filters.sortType = 'latest'
  suggestions.value = []
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

.search-row {
  display: flex;
}

.main-search {
  width: 100%;
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  margin-top: 12px;
}

.filter-row > .n-select {
  flex: 1 1 190px;
  min-width: 0;
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
