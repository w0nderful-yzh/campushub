<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h1 class="page-title">编辑个人资料</h1>
        <p class="page-subtitle">
          支持昵称、学院、专业、简介修改，并按后端约定上传头像。
        </p>
      </div>

      <router-link to="/me">
        <n-button quaternary>返回我的主页</n-button>
      </router-link>
    </div>

    <section class="section-card edit-card">
      <div class="avatar-section">
        <img
          class="avatar-preview"
          :src="resolveAvatarUrl(avatarPreview, form.nickname || authStore.user?.nickname || 'U')"
          alt="avatar"
        />

        <div class="stack" style="flex: 1;">
          <div>
            <div style="font-size: 18px; font-weight: 700;">头像上传</div>
            <div class="muted" style="margin-top: 6px;">
              接口为 `POST /api/users/avatar`，参数：`userId` + `file`
            </div>
          </div>

          <div class="row" style="flex-wrap: wrap;">
            <input
              ref="fileInputRef"
              type="file"
              accept="image/*"
              @change="handleFileChange"
            />
            <n-button
              type="primary"
              :loading="uploadingAvatar"
              :disabled="!selectedFile"
              @click="handleUploadAvatar"
            >
              上传头像
            </n-button>
          </div>
        </div>
      </div>

      <div class="soft-divider" />

      <n-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-placement="top"
      >
        <n-form-item label="昵称" path="nickname">
          <n-input
            v-model:value="form.nickname"
            placeholder="请输入昵称"
          />
        </n-form-item>

        <n-form-item label="性别">
          <n-select
            v-model:value="form.gender"
            :options="genderOptions"
            placeholder="请选择性别"
          />
        </n-form-item>

        <n-form-item label="邮箱">
          <n-input
            v-model:value="form.email"
            placeholder="请输入邮箱"
          />
        </n-form-item>

        <n-form-item label="学院">
          <n-input
            v-model:value="form.college"
            placeholder="请输入学院"
          />
        </n-form-item>

        <n-form-item label="专业">
          <n-input
            v-model:value="form.major"
            placeholder="请输入专业"
          />
        </n-form-item>

        <n-form-item label="个人简介">
          <n-input
            v-model:value="form.profile"
            type="textarea"
            :autosize="{ minRows: 4, maxRows: 8 }"
            placeholder="请输入个人简介"
          />
        </n-form-item>

        <div class="form-actions">
          <n-space>
            <router-link to="/me">
              <n-button>取消</n-button>
            </router-link>
            <n-button
              type="primary"
              :loading="savingProfile"
              @click="handleSaveProfile"
            >
              保存资料
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
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

import { getUserHomeApi, updateProfileApi, uploadAvatarApi } from '@/api/user'
import { useAuthStore } from '@/stores/auth'
import type { UpdateProfileDTO } from '@/types/user'
import { message } from '@/utils/message'
import { resolveAvatarUrl } from '@/utils/url'

const router = useRouter()
const authStore = useAuthStore()

const formRef = ref<FormInst | null>(null)
const fileInputRef = ref<HTMLInputElement | null>(null)
const selectedFile = ref<File | null>(null)
const avatarPreview = ref('')
const savingProfile = ref(false)
const uploadingAvatar = ref(false)

const form = reactive<UpdateProfileDTO>({
  nickname: '',
  gender: null,
  email: '',
  college: '',
  major: '',
  profile: ''
})

const rules: FormRules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: ['blur', 'input'] }]
}

const genderOptions = [
  { label: '未知', value: 0 },
  { label: '男', value: 1 },
  { label: '女', value: 2 }
]

async function loadProfile() {
  if (!authStore.user?.id) {
    router.push('/login')
    return
  }

  const res = await getUserHomeApi(authStore.user.id)
  const data = res.data

  form.nickname = data.nickname || ''
  form.gender = data.gender ?? 0
  form.email = data.email || ''
  form.college = data.college || ''
  form.major = data.major || ''
  form.profile = data.profile || ''
  avatarPreview.value = data.avatar || authStore.user.avatar || ''
}

function handleFileChange(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]

  selectedFile.value = file || null
  if (file) {
    avatarPreview.value = URL.createObjectURL(file)
  }
}

async function handleUploadAvatar() {
  if (!authStore.user?.id || !selectedFile.value) {
    message.warning('请先选择图片')
    return
  }

  uploadingAvatar.value = true
  try {
    const res = await uploadAvatarApi(authStore.user.id, selectedFile.value)
    avatarPreview.value = res.data || avatarPreview.value

    authStore.updateUser({
      ...authStore.user,
      avatar: res.data || authStore.user.avatar
    })

    message.success('头像上传成功')
    selectedFile.value = null

    if (fileInputRef.value) {
      fileInputRef.value.value = ''
    }
  } finally {
    uploadingAvatar.value = false
  }
}

async function handleSaveProfile() {
  if (!authStore.user?.id) return

  try {
    await formRef.value?.validate()
    savingProfile.value = true

    await updateProfileApi(authStore.user.id, {
      nickname: form.nickname.trim(),
      gender: form.gender,
      email: form.email?.trim(),
      college: form.college?.trim(),
      major: form.major?.trim(),
      profile: form.profile?.trim()
    })

    authStore.updateUser({
      ...authStore.user,
      nickname: form.nickname.trim()
    })

    message.success('个人资料更新成功')
    router.push('/me')
  } finally {
    savingProfile.value = false
  }
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped lang="scss">
.edit-card {
  padding: 22px;
}

.avatar-section {
  display: flex;
  gap: 20px;
  align-items: start;
}

.avatar-preview {
  width: 120px;
  height: 120px;
  border-radius: 28px;
  object-fit: cover;
  background: #eff6ff;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 24px;
}

@media (max-width: 768px) {
  .avatar-section {
    flex-direction: column;
  }
}
</style>
