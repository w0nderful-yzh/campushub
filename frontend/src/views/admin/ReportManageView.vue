<template>
  <div class="page-container">
    <section class="section-card" style="padding: 22px;">
      <h2 class="page-title" style="margin-bottom: 16px;">举报管理</h2>

      <n-tabs v-model:value="filterStatus" type="line" animated @update:value="handleFilterChange">
        <n-tab-pane name="all" tab="全部" />
        <n-tab-pane name="pending" tab="待处理" />
        <n-tab-pane name="handled" tab="已处理" />
      </n-tabs>

      <div v-if="loading" class="loading-card">
        <n-spin />
      </div>

      <template v-else-if="reports.length">
        <div class="report-list">
          <div v-for="report in reports" :key="report.id" class="report-item">
            <div class="report-header">
              <span class="report-type">{{ targetTypeText(report.targetType) }}</span>
              <n-tag :type="report.status === 0 ? 'warning' : 'success'" size="small">
                {{ report.status === 0 ? '待处理' : '已处理' }}
              </n-tag>
            </div>
            <div class="report-reason">{{ report.reason }}</div>
            <div class="report-meta">
              举报人：{{ report.reportNickname }} · {{ formatDateTime(report.createTime) }}
            </div>
            <div v-if="report.handleResult" class="report-result">
              处理结果：{{ report.handleResult }}
            </div>
            <div v-if="report.status === 0" class="report-actions">
              <n-input v-model="handleResults[report.id]" placeholder="处理结果" size="small" style="width: 260px;" />
              <n-button type="primary" size="small" @click="handleReport(report.id)">处理</n-button>
              <n-popconfirm
                v-if="report.targetType === 1"
                @positive-click="handleDeleteReportedPost(report)"
              >
                <template #trigger>
                  <n-button type="error" size="small" quaternary>删除帖子</n-button>
                </template>
                确认删除这条被举报的帖子？删除后普通用户将无法再看到它。
              </n-popconfirm>
            </div>
          </div>
        </div>

        <PaginationBar
          :page="query.pageNum"
          :page-size="query.pageSize"
          :total="total"
          @update:page="handlePageChange"
          @update:page-size="handlePageSizeChange"
        />
      </template>

      <EmptyState v-else icon="📋" title="暂无举报" description="没有举报记录。" />
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { NButton, NInput, NPopconfirm, NSpin, NTag, NTabs, NTabPane } from 'naive-ui'

import { deletePostApi } from '@/api/post'
import { getReportsApi, handleReportApi, type ReportVO } from '@/api/report'
import EmptyState from '@/components/EmptyState.vue'
import PaginationBar from '@/components/PaginationBar.vue'
import { formatDateTime } from '@/utils/format'
import { message } from '@/utils/message'

const loading = ref(false)
const reports = ref<ReportVO[]>([])
const total = ref(0)
const filterStatus = ref<'all' | 'pending' | 'handled'>('all')
const handleResults = reactive<Record<number, string>>({})

const query = reactive({ pageNum: 1, pageSize: 10 })

const statusMap: Record<string, number | undefined> = {
  all: undefined,
  pending: 0,
  handled: 1
}

function targetTypeText(type: number) {
  if (type === 1) return '帖子'
  if (type === 2) return '评论'
  if (type === 3) return '用户'
  return '未知'
}

async function loadReports() {
  loading.value = true
  try {
    const res = await getReportsApi({
      status: statusMap[filterStatus.value],
      pageNum: query.pageNum,
      pageSize: query.pageSize
    })
    reports.value = res.data || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

async function handleReport(reportId: number) {
  const result = handleResults[reportId]
  if (!result?.trim()) {
    message.warning('请输入处理结果')
    return
  }
  await handleReportApi(reportId, result)
  message.success('处理成功')
  loadReports()
}

async function handleDeleteReportedPost(report: ReportVO) {
  const result = handleResults[report.id]?.trim() || '已删除违规帖子'
  await deletePostApi(report.targetId)
  await handleReportApi(report.id, result)
  message.success('帖子已删除，举报已处理')
  loadReports()
}

function handleFilterChange() {
  query.pageNum = 1
  loadReports()
}
function handlePageChange(v: number) { query.pageNum = v }
function handlePageSizeChange(v: number) { query.pageSize = v; query.pageNum = 1 }

watch(() => [query.pageNum, query.pageSize], () => loadReports())
onMounted(() => loadReports())
</script>

<style scoped lang="scss">
.loading-card { min-height: 200px; display: grid; place-items: center; }
.report-list { margin-top: 12px; }
.report-item {
  padding: 16px;
  border: 1px solid #f1f5f9;
  border-radius: 12px;
  margin-bottom: 12px;
}
.report-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.report-type { font-weight: 600; color: #0f172a; }
.report-reason { font-size: 14px; color: #334155; margin-bottom: 8px; }
.report-meta { font-size: 12px; color: #94a3b8; }
.report-result { font-size: 13px; color: #059669; margin-top: 8px; }
.report-actions { display: flex; gap: 8px; margin-top: 10px; align-items: center; }
</style>
