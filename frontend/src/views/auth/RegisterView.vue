<template>
  <div class="auth-shell">
    <div class="auth-card">
      <h1 class="auth-title">注册 CampusHub</h1>
      <p class="auth-desc">
        创建账号后，就可以发布内容并和同学互动。
      </p>

      <n-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-placement="top"
      >
        <n-form-item label="用户名" path="username">
          <n-input
            v-model:value="form.username"
            placeholder="请输入用户名"
          />
        </n-form-item>

        <n-form-item label="昵称" path="nickname">
          <n-input
            v-model:value="form.nickname"
            placeholder="请输入昵称"
          />
        </n-form-item>

        <n-form-item label="密码" path="password">
          <n-input
            v-model:value="form.password"
            type="password"
            show-password-on="click"
            placeholder="请输入密码"
          />
        </n-form-item>

        <n-form-item label="确认密码" path="confirmPassword">
          <n-input
            v-model:value="form.confirmPassword"
            type="password"
            show-password-on="click"
            placeholder="请再次输入密码"
            @keyup.enter="handleSubmit"
          />
        </n-form-item>

        <n-space vertical :size="12">
          <n-button
            type="primary"
            block
            :loading="submitting"
            @click="handleSubmit"
          >
            注册
          </n-button>
          <router-link to="/login">
            <n-button block quaternary>已有账号？去登录</n-button>
          </router-link>
        </n-space>
      </n-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { FormInst, FormRules, FormItemRule } from 'naive-ui'
import { NButton, NForm, NFormItem, NInput, NSpace } from 'naive-ui'
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

import { registerApi } from '@/api/auth'
import { message } from '@/utils/message'

const router = useRouter()
const formRef = ref<FormInst | null>(null)
const submitting = ref(false)

const form = reactive({
  username: '',
  nickname: '',
  password: '',
  confirmPassword: ''
})

const validateConfirm = (_rule: FormItemRule, value: string) => {
  if (!value) return new Error('请再次输入密码')
  if (value !== form.password) return new Error('两次密码输入不一致')
  return true
}

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: ['blur', 'input'] }],
  nickname: [{ required: true, message: '请输入昵称', trigger: ['blur', 'input'] }],
  password: [{ required: true, message: '请输入密码', trigger: ['blur', 'input'] }],
  confirmPassword: [
    { required: true, trigger: ['blur', 'input'], validator: validateConfirm }
  ]
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
    submitting.value = true

    await registerApi({
      username: form.username.trim(),
      nickname: form.nickname.trim(),
      password: form.password,
      confirmPassword: form.confirmPassword
    })

    message.success('注册成功，请登录')
    router.push('/login')
  } catch (error) {
    if (!(error instanceof Error)) return
  } finally {
    submitting.value = false
  }
}
</script>
