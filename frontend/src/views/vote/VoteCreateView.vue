<template>
  <div class="page-container">
    <section class="section-card" style="padding: 22px; max-width: 600px; margin: 0 auto;">
      <h2 class="page-title" style="margin-bottom: 20px;">创建投票</h2>

      <n-form :model="form" label-placement="left" label-width="80">
        <n-form-item label="投票标题">
          <n-input v-model:value="form.title" placeholder="输入投票标题" maxlength="100" show-count />
        </n-form-item>

        <n-form-item label="投票描述">
          <n-input v-model:value="form.description" type="textarea" placeholder="补充说明（可选）" :rows="2" />
        </n-form-item>

        <n-form-item label="最多可选">
          <n-input-number v-model:value="form.maxSelect" :min="1" :max="options.length" style="width: 120px;" />
        </n-form-item>

        <n-form-item label="截止时间">
          <n-date-picker v-model:formatted-value="form.endTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" style="width: 100%;" clearable />
        </n-form-item>

        <n-form-item label="投票选项">
          <div class="options-list">
            <div v-for="(opt, i) in options" :key="i" class="option-row">
              <n-input v-model:value="options[i]" :placeholder="`选项 ${i + 1}`" />
              <n-button v-if="options.length > 2" quaternary type="error" @click="options.splice(i, 1)">删除</n-button>
            </div>
            <n-button v-if="options.length < 10" dashed block @click="options.push('')">+ 添加选项</n-button>
          </div>
        </n-form-item>
      </n-form>

      <n-button type="primary" block :loading="submitting" @click="handleSubmit">创建投票</n-button>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { NButton, NDatePicker, NForm, NFormItem, NInput, NInputNumber } from 'naive-ui'
import { useRouter } from 'vue-router'

import { createVoteApi } from '@/api/vote'
import { message } from '@/utils/message'

const router = useRouter()
const submitting = ref(false)
const form = ref({ title: '', description: '', maxSelect: 1, endTime: '' })
const options = ref(['', ''])

async function handleSubmit() {
  if (!form.value.title.trim()) { message.warning('请输入投票标题'); return }
  const validOptions = options.value.map(o => o.trim()).filter(Boolean)
  if (validOptions.length < 2) { message.warning('至少需要2个有效选项'); return }

  submitting.value = true
  try {
    const res = await createVoteApi({
      title: form.value.title,
      description: form.value.description || undefined,
      maxSelect: form.value.maxSelect,
      endTime: form.value.endTime || undefined,
      options: validOptions
    })
    message.success('投票创建成功')
    router.push(`/votes/${res.data}`)
  } catch { /* */ } finally { submitting.value = false }
}
</script>

<style scoped lang="scss">
.options-list { width: 100%; display: flex; flex-direction: column; gap: 8px; }
.option-row { display: flex; gap: 8px; align-items: center; }
</style>
