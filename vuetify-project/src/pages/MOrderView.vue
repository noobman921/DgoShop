<template>
  <v-app style="background-color: #f5f5f5; box-sizing: border-box;">
    <!-- 左侧侧边栏：与商品管理页保持一致，仅激活订单管理 -->
    <v-navigation-drawer
      v-model="drawer"
      permanent
      width="240"
      app
      class="bg-teal-lighten-3 z-index-10"
      style="position: fixed; top: 0; bottom: 0; left: 0;"
    >
      <v-toolbar-title class="px-4 py-3 text-teal-darken-4 font-weight-bold">
        商家管理中心
      </v-toolbar-title>
      <v-divider class="my-2"></v-divider>
      
      <v-list dense nav class="text-teal-darken-4" style="overflow-y: auto;">
        <v-list-item
          :to="{ name: 'MerchantProduct' }"
          :active="isActive('MerchantProduct')"
          class="hover:bg-teal-lighten-2 transition-colors"
        >
          <v-list-item-icon><v-icon>mdi-cube</v-icon></v-list-item-icon>
          <v-list-item-title class="text-lg">商品管理</v-list-item-title>
        </v-list-item>
        <v-list-item
          :to="{ name: 'MerchantOrder' }"
          :active="isActive('MerchantOrder')"
          class="hover:bg-teal-lighten-2 transition-colors"
        >
          <v-list-item-icon><v-icon>mdi-file-document</v-icon></v-list-item-icon>
          <v-list-item-title class="text-lg">订单管理</v-list-item-title>
        </v-list-item>
      </v-list>
    </v-navigation-drawer>

    <!-- 主内容区：结构与商品页一致，替换为订单内容 -->
    <v-main 
      class="pa-6" 
      style="margin-left: 240px; min-height: 100vh; box-sizing: border-box; overflow-x: hidden; width: calc(100vw - 240px);"
    >
      <v-card 
        elevation="6" 
        class="mb-6"
        style="background-color: #fff; border-top: 4px solid #80cbc4; width: 100%; overflow: hidden;"
      >
        <!-- 卡片头部：仅保留标题，去掉操作按钮（无修改/删除需求） -->
        <v-card-title class="d-flex flex-wrap justify-between align-center p-4 bg-teal-lighten-5 gap-2">
          <h2 class="text-h5 text-teal-darken-4 font-weight-bold">订单管理</h2>
        </v-card-title>

        <!-- 订单项列表：自动换行 + 溢出滚动 -->
        <v-card-text class="p-4" style="width: 100%; overflow-x: auto;">
          <div style="width: 100%; min-width: 600px;">
            <v-list dense style="width: 100%;">
              <!-- 表头：订单号、商品名、订购数量 -->
              <v-list-item class="bg-teal-lighten-4 text-teal-darken-4 font-weight-bold text-lg">
                <v-list-item-content class="d-flex w-100 gap-1rem">
                  <span style="flex: 1; min-width: 200px; word-break: break-word;">订单号</span>
                  <span style="flex: 1; min-width: 150px; word-break: break-word;">商品名称</span>
                  <span style="flex: 0 0 120px; min-width: 120px; text-align: center;">订购数量</span>
                </v-list-item-content>
              </v-list-item>

              <!-- 订单项 -->
              <v-list-item
                v-for="item in orderItemList"
                :key="item.orderItem.id"
                @click="selectOrderItem(item)"
                :class="{ 'bg-teal-darken-3 text-white': selectedOrderItem?.orderItem.id === item.orderItem.id }"
                class="cursor-pointer transition-colors py-4 text-lg"
              >
                <v-list-item-content class="d-flex w-100 align-items-center gap-1rem">
                  <span style="flex: 1; min-width: 200px; word-break: break-word; line-height: 1.5;">{{ item.orderItem.orderNo }}</span>
                  <span style="flex: 1; min-width: 150px; word-break: break-word; line-height: 1.5;">{{ item.productName }}</span>
                  <span style="flex: 0 0 120px; min-width: 120px; text-align: center;">{{ item.orderItem.quantity }}</span>
                </v-list-item-content>
              </v-list-item>

              <!-- 空数据 -->
              <v-list-item v-if="orderItemList.length === 0" class="py-6">
                <v-list-item-content class="text-center text-gray-500 text-lg">暂无订单数据</v-list-item-content>
              </v-list-item>
            </v-list>
          </div>
        </v-card-text>

        <!-- 分页：与商品页保持一致 -->
        <v-card-actions class="justify-center pa-4 bg-teal-lighten-5 flex-wrap gap-2">
          <v-btn :disabled="pageNo === 1" @click="changePage(pageNo - 1)" size="large" color="teal-lighten-1" class="px-4">
            <v-icon>mdi-chevron-left</v-icon>
          </v-btn>
          <span class="mx-4 text-lg text-teal-darken-4">第 {{ pageNo }} 页 / 共 {{ pages }} 页</span>
          <v-btn :disabled="pageNo === pages" @click="changePage(pageNo + 1)" size="large" color="teal-lighten-1" class="px-4">
            <v-icon>mdi-chevron-right</v-icon>
          </v-btn>
        </v-card-actions>
      </v-card>

      <!-- 提示弹窗：复用原有样式 -->
      <v-snackbar
        v-model="snackbar.show"
        :color="snackbar.color"
        :timeout="snackbar.timeout"
        location="bottom"
        app
        style="max-width: calc(100vw - 300px); margin-left: 240px;"
      >
        {{ snackbar.message }}
        <template v-slot:action="{ attrs }">
          <v-btn v-bind="attrs" text @click="snackbar.show = false" class="text-lg">关闭</v-btn>
        </template>
      </v-snackbar>
    </v-main>
  </v-app>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useMerchantStore } from '@/stores/merchantStore'
import axios from 'axios'

// 基础配置：与商品页保持一致
axios.defaults.baseURL = 'http://localhost:8080'
axios.defaults.timeout = 10000
axios.defaults.withCredentials = true

const router = useRouter()
const route = useRoute()
const merchantStore = useMerchantStore()

// 响应式数据：替换为订单相关
const drawer = ref(true)
const merchantId = ref(merchantStore.merchantId || localStorage.getItem('merchantId'))
const pageNo = ref(1)
const pageSize = ref(10)
const total = ref(0)
const pages = ref(0)
const orderItemList = ref([]) // 订单项列表（含商品名）
const selectedOrderItem = ref(null) // 选中的订单项（仅展示，无操作）

// 提示弹窗：复用原有逻辑
const snackbar = reactive({ show: false, message: '', color: 'teal', timeout: 3000 })
const showSnackbar = (message, color = 'teal') => {
  snackbar.message = message
  snackbar.color = color
  snackbar.show = true
}

// 路由激活判断：匹配MerchantOrder
const isActive = (routeName) => route.name === routeName

// 获取商家订单项列表（核心接口：/api/merchant/log/order-item/page）
const getOrderItemList = async () => {
  if (!merchantId.value) {
    showSnackbar('未获取到商家ID，请重新登录', 'teal-darken-1')
    return
  }
  try {
    const res = await axios.get('/api/merchant/log/order-item/page', {
      params: { merchantId: merchantId.value, pageNo: pageNo.value, pageSize: pageSize.value }
    })
    orderItemList.value = res.data.list || []
    total.value = res.data.total || 0
    pages.value = res.data.pages || 0
  } catch (error) {
    console.error('获取订单列表失败：', error)
    showSnackbar('获取订单列表失败，请重试', 'teal-darken-1')
  }
}

// 分页切换：复用原有逻辑
const changePage = (newPage) => {
  if (newPage < 1 || newPage > pages.value) return
  pageNo.value = newPage
  getOrderItemList()
}

// 选中订单项：仅展示，无操作
const selectOrderItem = (item) => { selectedOrderItem.value = item }

// 挂载时加载订单列表
onMounted(() => { getOrderItemList() })
</script>

<style scoped>
/* 完全复用原有样式，保证风格统一 */
.cursor-pointer { cursor: pointer; }
.transition-colors { transition: background-color 0.2s ease; }
.hover\:bg-teal-lighten-2:hover { background-color: #a5d6d1 !important; }
.hover\:bg-teal-lighten-5:hover { background-color: #e0f2f1 !important; }
.text-lg { font-size: 18px !important; line-height: 1.5 !important; }
.text-base { font-size: 16px !important; line-height: 1.5 !important; }
.px-6 { padding-left: 1.5rem !important; padding-right: 1.5rem !important; }
.py-3 { padding-top: 1rem !important; padding-bottom: 1rem !important; }
.white-space-normal { white-space: normal !important; }
.white-space-nowrap { white-space: nowrap !important; }
.z-index-10 { z-index: 10 !important; }
.gap-1rem { gap: 1rem !important; }
</style>