<template>
  <div class="page-container">
    <section class="section-card" style="padding: 22px; max-width: 680px; margin: 0 auto;">
      <h2 class="page-title" style="margin-bottom: 20px;">发布活动</h2>

      <n-form ref="formRef" :model="form" label-placement="left" label-width="80">
        <n-form-item label="活动标题" path="title">
          <n-input v-model:value="form.title" placeholder="输入活动标题" maxlength="100" show-count />
        </n-form-item>

        <n-form-item label="活动类型" path="activityType">
          <n-select v-model:value="form.activityType" :options="typeOptions" placeholder="选择类型" />
        </n-form-item>

        <n-form-item label="活动描述" path="description">
          <n-input v-model:value="form.description" type="textarea" placeholder="详细描述活动内容" :rows="4" />
        </n-form-item>

        <n-form-item label="活动地点" path="location">
          <n-input v-model:value="form.location" placeholder="输入地点" />
        </n-form-item>

        <n-form-item label="开始时间" path="startTime">
          <n-date-picker v-model:formatted-value="form.startTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" style="width: 100%;" />
        </n-form-item>

        <n-form-item label="结束时间" path="endTime">
          <n-date-picker v-model:formatted-value="form.endTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" style="width: 100%;" clearable />
        </n-form-item>

        <n-form-item label="人数上限" path="maxParticipants">
          <n-input-number v-model:value="form.maxParticipants" :min="0" placeholder="0表示不限" style="width: 100%;" />
        </n-form-item>

        <n-form-item label="封面图" path="coverImg">
          <n-input v-model:value="form.coverImg" placeholder="图片URL（可选）" />
        </n-form-item>
      </n-form>

      <n-button type="primary" block :loading="submitting" @click="handleSubmit" style="margin-top: 12px;">
        发布活动
      </n-button>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { NButton, NDatePicker, NForm, NFormItem, NInput, NInputNumber, NSelect } from 'naive-ui'
import { useRouter } from 'vue-router'

import { createActivityApi } from '@/api/activity'
import { message } from '@/utils/message'

const router = useRouter()
const formRef = ref()
const submitting = ref(false)

const form = ref({
  title: '',
  description: '',
  coverImg: '',
  location: '',
  activityType: null as number | null,
  startTime: null as string | null,
  endTime: null as string | null,
  maxParticipants: 0
})

const typeOptions = [
  { label: '讲座', value: 1 },
  { label: '聚会', value: 2 },
  { label: '运动', value: 3 },
  { label: '其他', value: 4 }
]

async function handleSubmit() {
  if (!form.value.title.trim()) { message.warning('请输入活动标题'); return }
  if (!form.value.startTime) { message.warning('请选择开始时间'); return }

  submitting.value = true
  try {
    const res = await createActivityApi({
      title: form.value.title,
      description: form.value.description || undefined,
      coverImg: form.value.coverImg || undefined,
      location: form.value.location || undefined,
      activityType: form.value.activityType || undefined,
      startTime: form.value.startTime,
      endTime: form.value.endTime || undefined,
      maxParticipants: form.value.maxParticipants || 0
    })
    message.success('活动发布成功')
    router.push(`/activities/${res.data}`)
  } catch {
    message.error('发布失败')
  } finally {
    submitting.value = false
  }
}
</script>
