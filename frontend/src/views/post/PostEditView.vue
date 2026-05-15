<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h1 class="page-title">{{ isEdit ? '编辑帖子' : '发布帖子' }}</h1>
        <p class="page-subtitle">
          后端当前未提供帖子图片上传接口，图片使用 URL 数组方式提交。
        </p>
      </div>

      <router-link to="/">
        <n-button quaternary>返回首页</n-button>
      </router-link>
    </div>

    <section class="section-card form-card">
      <n-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-placement="top"
      >
        <n-form-item label="分类" path="categoryId">
          <n-select
            v-model:value="form.categoryId"
            :options="categoryOptions"
            placeholder="请选择分类"
          />
        </n-form-item>

        <n-form-item label="标题" path="title">
          <n-input
            v-model:value="form.title"
            maxlength="100"
            show-count
            placeholder="请输入帖子标题"
          />
        </n-form-item>

        <n-form-item label="正文" path="content">
          <n-input
            v-model:value="form.content"
            type="textarea"
            :autosize="{ minRows: 10, maxRows: 18 }"
            placeholder="请输入帖子内容"
          />
        </n-form-item>

        <div class="soft-divider" />

        <div class="page-header" style="margin-bottom: 12px;">
          <div>
            <h3 style="margin: 0;">图片 URL</h3>
            <p class="page-subtitle" style="margin-top: 6px;">
              最少 0 张，最多 6 张，留空项会在提交时自动过滤。
            </p>
          </div>

          <n-button
            quaternary
            @click="addImageField"
          >
            新增一项
          </n-button>
        </div>

        <div class="stack">
          <div
            v-for="(item, index) in form.images"
            :key="`image-${index}`"
            class="image-row"
          >
            <n-input
              v-model:value="form.images[index]"
              placeholder="请输入图片 URL"
            />
            <n-button
              quaternary
              type="error"
              :disabled="form.images.length <= 1"
              @click="removeImageField(index)"
            >
              删除
            </n-button>
          </div>
        </div>

        <div class="form-actions">
          <n-space>
            <router-link to="/">
              <n-button>取消</n-button>
            </router-link>
            <n-button
              type="primary"
              :loading="submitting"
              @click="handleSubmit"
            >
              {{ isEdit ? '保存修改' : '立即发布' }}
            </n-button>
          </n-space>
        </div>
      </n-form>
    </section>
  </div>
</template>

<script setup lang="ts">
import type { FormInst, FormRules } from 'naive-ui'
import { NButton, NForm, NFormItem, NInput, NSelect, NSpace } from 'naive-ui'
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { getPostDetailApi, createPostApi, updatePostApi } from '@/api/post'
import { useAppStore } from '@/stores/app'
import { message } from '@/utils/message'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()

const formRef = ref<FormInst | null>(null)
const submitting = ref(false)

const isEdit = computed(() => Boolean(route.params.id))
const editPostId = computed(() => Number(route.params.id || 0))

const form = reactive({
  categoryId: null as number | null,
  title: '',
  content: '',
  images: ['']
})

const rules: FormRules = {
  categoryId: [{ required: true, type: 'number', message: '请选择分类', trigger: ['change'] }],
  title: [{ required: true, message: '请输入标题', trigger: ['blur', 'input'] }],
  content: [{ required: true, message: '请输入正文', trigger: ['blur', 'input'] }]
}

const categoryOptions = computed(() =>
  appStore.categories.map((item) => ({
    label: item.name,
    value: item.id
  }))
)

function addImageField() {
  if (form.images.length >= 6) {
    message.warning('最多添加 6 张图片')
    return
  }
  form.images.push('')
}

function removeImageField(index: number) {
  form.images.splice(index, 1)
  if (!form.images.length) {
    form.images.push('')
  }
}

async function loadCategories() {
  await appStore.fetchCategories()
}

async function loadDetailIfEdit() {
  if (!isEdit.value) return

  const res = await getPostDetailApi(editPostId.value)
  const data = res.data

  form.categoryId = data.categoryId ?? null
  form.title = data.title || ''
  form.content = data.content || ''
  form.images = data.images?.length ? [...data.images] : ['']
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
    submitting.value = true

    const payload = {
      categoryId: Number(form.categoryId),
      title: form.title.trim(),
      content: form.content.trim(),
      images: form.images.map((item) => item.trim()).filter(Boolean)
    }

    if (isEdit.value) {
      await updatePostApi(editPostId.value, payload)
      message.success('帖子修改成功')
      router.push(`/posts/${editPostId.value}`)
    } else {
      await createPostApi(payload)
      message.success('帖子发布成功')
      router.push('/')
    }
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  await loadCategories()
  await loadDetailIfEdit()
})
</script>

<style scoped lang="scss">
.form-card {
  padding: 22px;
}

.image-row {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 12px;
  align-items: center;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 24px;
}
</style>
