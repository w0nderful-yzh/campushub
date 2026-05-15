<template>
  <div class="auth-shell">
    <div class="auth-card">
      <h1 class="auth-title">登录 CampusHub</h1>
      <p class="auth-desc">
        用最少的页面把校园社区主流程跑通。
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

        <n-form-item label="密码" path="password">
          <n-input
            v-model:value="form.password"
            type="password"
            show-password-on="click"
            placeholder="请输入密码"
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
            登录
          </n-button>
          <router-link to="/register">
            <n-button block quaternary>没有账号？去注册</n-button>
          </router-link>
        </n-space>
      </n-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { FormInst, FormRules } from 'naive-ui'
import { NButton, NForm, NFormItem, NInput, NSpace } from 'naive-ui'
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { useAuthStore } from '@/stores/auth'
import { message } from '@/utils/message'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const formRef = ref<FormInst | null>(null)
const submitting = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: ['blur', 'input'] }],
  password: [{ required: true, message: '请输入密码', trigger: ['blur', 'input'] }]
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
    submitting.value = true

    await authStore.loginAction({
      username: form.username.trim(),
      password: form.password
    })

    message.success('登录成功')
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
    router.replace(redirect)
  } catch (error) {
    if (!(error instanceof Error)) return
    // 请求错误已由拦截器提示
  } finally {
    submitting.value = false
  }
}
</script>
