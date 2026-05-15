<template>
  <div class="page-container">
    <section class="section-card" style="padding: 22px;">
      <div class="page-header" style="margin-bottom: 16px;">
        <h2 class="page-title">校园活动</h2>
        <router-link to="/activities/create">
          <n-button type="primary">发布活动</n-button>
        </router-link>
      </div>

      <div class="filter-bar">
        <n-tabs v-model:value="filterType" type="segment" animated @update:value="handleFilterChange">
          <n-tab-pane name="all" tab="全部" />
          <n-tab-pane name="1" tab="讲座" />
          <n-tab-pane name="2" tab="聚会" />
          <n-tab-pane name="3" tab="运动" />
          <n-tab-pane name="4" tab="其他" />
        </n-tabs>
      </div>

      <div v-if="loading" class="loading-card"><n-spin /></div>

      <template v-else-if="activities.length">
        <div class="activity-grid">
          <div v-for="act in activities" :key="act.id" class="activity-card" @click="router.push(`/activities/${act.id}`)">
            <div class="act-header">
              <n-tag :type="act.status === 1 ? 'success' : 'default'" size="small">
                {{ act.status === 1 ? '报名中' : act.status === 0 ? '已取消' : '已结束' }}
              </n-tag>
              <span class="act-type">{{ typeText(act.activityType) }}</span>
            </div>
            <h3 class="act-title">{{ act.title }}</h3>
            <div class="act-meta">
              <div>📍 {{ act.location || '待定' }}</div>
              <div>🕐 {{ formatDateTime(act.startTime) }}</div>
              <div>👥 {{ act.currentCount }}{{ act.maxParticipants > 0 ? ' / ' + act.maxParticipants : '' }} 人</div>
            </div>
            <div class="act-author">
              <img :src="resolveAvatarUrl(act.authorAvatar, act.authorNickname || 'U')" class="act-avatar" />
              <span>{{ act.authorNickname }}</span>
            </div>
          </div>
        </div>

        <PaginationBar :page="query.pageNum" :page-size="query.pageSize" :total="total"
          @update:page="handlePageChange" @update:page-size="handlePageSizeChange" />
      </template>

      <EmptyState v-else icon="🎯" title="暂无活动" description="还没有人发布活动，快来创建第一个吧！" />
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { NButton, NSpin, NTag, NTabs, NTabPane } from 'naive-ui'
import { useRouter } from 'vue-router'

import { getActivitiesApi, type ActivityVO } from '@/api/activity'
import EmptyState from '@/components/EmptyState.vue'
import PaginationBar from '@/components/PaginationBar.vue'
import { formatDateTime } from '@/utils/format'
import { resolveAvatarUrl } from '@/utils/url'

const router = useRouter()
const loading = ref(false)
const activities = ref<ActivityVO[]>([])
const total = ref(0)
const filterType = ref('all')
const query = reactive({ pageNum: 1, pageSize: 12 })

function typeText(t?: number) {
  const map: Record<number, string> = { 1: '讲座', 2: '聚会', 3: '运动', 4: '其他' }
  return t ? map[t] || '其他' : '其他'
}

async function loadActivities() {
  loading.value = true
  try {
    const res = await getActivitiesApi({
      activityType: filterType.value === 'all' ? undefined : Number(filterType.value),
      pageNum: query.pageNum,
      pageSize: query.pageSize
    })
    activities.value = res.data || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

function handleFilterChange() { query.pageNum = 1; loadActivities() }
function handlePageChange(v: number) { query.pageNum = v }
function handlePageSizeChange(v: number) { query.pageSize = v; query.pageNum = 1 }
watch(() => [query.pageNum, query.pageSize], () => loadActivities())
onMounted(() => loadActivities())
</script>

<style scoped lang="scss">
.loading-card { min-height: 200px; display: grid; place-items: center; }
.filter-bar { margin-bottom: 16px; }
.activity-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}
.activity-card {
  padding: 18px;
  border: 1px solid #f1f5f9;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;
}
.activity-card:hover {
  border-color: #bfdbfe;
  box-shadow: 0 4px 16px rgba(59, 130, 246, 0.1);
  transform: translateY(-2px);
}
.act-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.act-type { font-size: 12px; color: #64748b; }
.act-title { font-size: 16px; font-weight: 700; color: #0f172a; margin-bottom: 10px; line-height: 1.4; }
.act-meta { font-size: 13px; color: #64748b; display: flex; flex-direction: column; gap: 4px; margin-bottom: 12px; }
.act-author { display: flex; align-items: center; gap: 8px; font-size: 13px; color: #334155; }
.act-avatar { width: 24px; height: 24px; border-radius: 50%; object-fit: cover; }
</style>
