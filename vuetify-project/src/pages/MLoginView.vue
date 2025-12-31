<template>
  <!-- 页面外层容器：全屏居中 -->
  <v-container
    fluid
    class="d-flex align-center justify-center"
    style="min-height: 100vh; background-color: #f5f7fa;"
  >
    <!-- 登录/注册卡片 -->
    <v-card
      elevation="4"
      max-width="450px"
      width="100%"
      class="pa-6"
    >
      <!-- 顶部切换按钮组 -->
      <v-btn-toggle
        v-model="activeTab"
        class="mb-6"
        density="comfortable"
        group
        mandatory
      >
        <v-btn value="login">登录</v-btn>
        <v-btn value="register">注册</v-btn>
      </v-btn-toggle>

      <!-- 表单区域 -->
      <v-form
        ref="formRef"
        v-model="isFormValid"
        @submit.prevent="handleSubmit"
      >
        <!-- 注册专属：商家名称输入项 -->
        <v-text-field
          v-if="activeTab === 'register'"
          v-model="formData.merchantName"
          label="商家名称"
          placeholder="请输入商家名称"
          :rules="merchantNameRules"
          required
          clearable
          class="mb-4"
        ></v-text-field>

        <!-- 公共：账号输入项 -->
        <v-text-field
          v-model="formData.account"
          label="商家账号"
          placeholder="请输入商家账号"
          :rules="accountRules"
          required
          clearable
          class="mb-4"
        ></v-text-field>

        <!-- 公共：密码输入项 -->
        <v-text-field
          v-model="formData.password"
          label="密码"
          type="password"
          placeholder="请输入密码（至少6位）"
          :rules="passwordRules"
          required
          clearable
          :append-inner-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
          @click:append-inner="showPassword = !showPassword"
          class="mb-6"
        ></v-text-field>

        <!-- 提交按钮：移除无效类名 text-none 解决解析异常 -->
        <v-btn
          type="submit"
          color="primary"
          variant="flat"
          block
          :loading="loading"
        >
          {{ activeTab === 'login' ? '登录' : '注册' }}
        </v-btn>
      </v-form>
    </v-card>

    <!-- 全局提示弹窗（Vuetify Snackbar） -->
    <v-snackbar
      v-model="snackbar.show"
      :color="snackbar.color"
      :timeout="snackbar.timeout"
      location="bottom"
    >
      {{ snackbar.message }}
      <template v-slot:action="{ attrs }">
        <v-btn
          v-bind="attrs"
          text
          @click="snackbar.show = false"
        >
          关闭
        </v-btn>
      </template>
    </v-snackbar>
  </v-container>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useMerchantStore } from '@/stores/merchantStore'
import axios from 'axios'

// 基础配置
axios.defaults.baseURL = ''
axios.defaults.timeout = 10000
axios.defaults.withCredentials = true

const router = useRouter()
const merchantStore = useMerchantStore()

// 响应式数据
const activeTab = ref('login')
const isFormValid = ref(true)
const formRef = ref(null)
const loading = ref(false)
const showPassword = ref(false)

const formData = reactive({
  merchantName: '',
  account: '',
  password: ''
})

// 表单规则
const merchantNameRules = [v => !!v || '商家名称不能为空']
const accountRules = [v => !!v || '商家账号不能为空']
const passwordRules = [
  v => !!v || '密码不能为空',
  v => (v && v.length >= 6) || '密码长度不能少于6位'
]

// 弹窗配置
const snackbar = reactive({
  show: false,
  message: '',
  color: 'success',
  timeout: 3000
})

const showSnackbar = (message, color = 'success') => {
  snackbar.message = message
  snackbar.color = color
  snackbar.show = true
}

// 表单提交
const handleSubmit = async () => {
  if (!formRef.value) {
    showSnackbar('表单初始化失败，请刷新页面！', 'error')
    return
  }
  const valid = await formRef.value.validate()
  if (!valid) {
    showSnackbar('表单填写有误，请检查！', 'error')
    return
  }

  loading.value = true
  try {
    activeTab.value === 'login' ? await handleLogin() : await handleRegister()
  } catch (error) {
    let errorMsg = '操作失败，请重试'
    if (error.message.includes('Network Error')) {
      errorMsg = '网络错误，请检查后端是否启动'
    } else if (error.response) {
      errorMsg = `请求失败：${error.response.data?.msg || error.response.statusText}`
    }
    showSnackbar(errorMsg, 'error')
  } finally {
    loading.value = false
  }
}

// 登录逻辑：添加路由跳转异常捕获
const handleLogin = async () => {
  const { account, password } = formData
  const res = await axios.get('/api/merchant/log/login', { params: { account, password } })

  if (res.data.code === 200) {
    const merchantInfo = res.data.data || res.data
    const merchantId = merchantInfo.merchantId || merchantInfo.id
    
    localStorage.setItem('merchantId', merchantId)
    localStorage.setItem('merchantInfo', JSON.stringify(merchantInfo))
    merchantStore.setMerchantInfo?.(merchantId, merchantInfo) // 可选链避免方法不存在报错

    showSnackbar('登录成功')
    // 关键：添加路由跳转异常捕获，避免目标页面报错导致登录流程崩溃
    await router.push({ name: 'MerchantProduct' }).catch(err => {
      console.error('跳转失败：', err)
      showSnackbar('页面跳转失败，请手动进入商品管理页', 'warning')
    })
  } else {
    showSnackbar(res.data.msg || '登录失败', 'error')
  }
}

// 注册逻辑
const handleRegister = async () => {
  const { merchantName, account, password } = formData
  const res = await axios.get('/api/merchant/log/reg', { params: { merchantName, account, password } })

  if (res.data.code === 1 || res.data.success === true) {
    showSnackbar('注册成功，正在自动登录...')
    formData.password = ''
    activeTab.value = 'login'
    await handleLogin()
  } else {
    showSnackbar(res.data.msg || '注册失败', 'error')
  }
}
</script>