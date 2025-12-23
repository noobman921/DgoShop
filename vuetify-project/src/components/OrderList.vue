<template>
  <v-container fluid class="py-4 px-2">
    <!-- 加载状态 -->
    <v-progress-linear
      v-if="loading"
      color="primary"
      indeterminate
      class="my-2"
    />

    <!-- 未登录提示 -->
    <v-alert
      v-if="!currentAccount && !loading"
      variant="text"
      color="orange-lighten-2"
      class="my-8 text-center"
      elevation="0"
    >
      请先登录后查看订单~
    </v-alert>

    <!-- 空订单提示 -->
    <v-alert
      v-else-if="orderList.length === 0 && !loading"
      variant="text"
      color="gray-lighten-2"
      class="my-8 text-center"
      elevation="0"
    >
      暂无订单记录~
    </v-alert>

    <!-- 订单列表 -->
    <v-card
      v-else
      v-for="order in orderList"
      :key="order.orderMain.orderNo"
      class="mb-4"
      elevation="2"
    >
      <!-- 订单主项：订单号 + 总价格 -->
      <v-card-title class="bg-primary text-white justify-between align-center">
        <div class="font-medium">订单号：{{ order.orderMain.orderNo }}</div>
        <div class="font-bold">
          订单总价：¥{{ formatPrice(order.totalPrice) }}
        </div>
      </v-card-title>

      <!-- 订单项列表容器（关键：统一容器样式） -->
      <v-card-text class="pa-0">
        <!-- 订单项表头 -->
        <v-row class="bg-gray-lighten-1 py-2 px-4 font-medium order-item-header">
          <v-col cols="4">商品名称</v-col>
          <v-col cols="2" class="text-center">单价</v-col>
          <v-col cols="2" class="text-center">数量</v-col>
          <v-col cols="4" class="text-center">小计</v-col>
        </v-row>
        
        <!-- 订单项列表（关键：用div包裹，统一行样式） -->
        <div class="order-item-list">
          <v-row
            v-for="(item, index) in order.itemList"
            :key="item.id || `order-item-${item.productId}`"
            :class="['order-item-row', { 'last-row': index === order.itemList.length - 1 }]"
            class="align-center py-3 px-4"
          >
            <v-col cols="4">
              {{ item.productName || '未知商品' }}
            </v-col>
            <v-col cols="2" class="text-center">
              ¥{{ formatPrice(item.productPrice) }}
            </v-col>
            <v-col cols="2" class="text-center">
              {{ item.quantity }}
            </v-col>
            <v-col cols="4" class="text-center text-primary font-medium">
              ¥{{ formatPrice(item.productPrice * item.quantity) }}
            </v-col>
          </v-row>
        </div>
      </v-card-text>
    </v-card>

    <!-- 加载更多按钮 -->
    <v-btn
      v-if="hasMoreData && !loading && currentAccount"
      color="primary"
      variant="outlined"
      class="mx-auto d-block my-4"
      @click="loadMore"
    >
      加载更多
    </v-btn>
  </v-container>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import axios from 'axios'

// ===================== 响应式数据定义 =====================
const orderList = ref([])       // 订单列表（核心数据）
const currentPage = ref(1)      // 当前页码
const pageSize = ref(10)        // 每页条数
const total = ref(0)            // 总订单数
const totalPages = ref(0)       // 总页数
const loading = ref(false)      // 加载状态
const currentAccount = ref('')  // 当前登录账号

// ===================== 工具方法 =====================
/**
 * 格式化价格（兼容BigDecimal转来的数字，兜底处理空值/非数字）
 * @param {Number|String} price - 价格值
 * @returns {String} 格式化后的价格（保留2位小数）
 */
const formatPrice = (price) => {
  try {
    // 兼容BigDecimal序列化后的数字/字符串，转为数字后保留2位小数
    return Number(price).toFixed(2)
  } catch (e) {
    console.warn('价格格式化失败：', price, e)
    return '0.00'
  }
}

/**
 * 更新当前登录账号（从localStorage读取，兼容异常）
 */
const updateCurrentAccount = () => {
  try {
    const savedUserStr = localStorage.getItem('userInfo') || '{}'
    const userInfo = JSON.parse(savedUserStr)
    currentAccount.value = userInfo.username || ''
  } catch (e) {
    console.warn('解析用户信息失败：', e)
    currentAccount.value = ''
  }
}

// ===================== 计算属性 =====================
/**
 * 判断是否有更多数据可加载
 */
const hasMoreData = computed(() => {
  return currentPage.value < totalPages.value
})

// ===================== 核心业务逻辑 =====================
/**
 * 加载订单数据（支持分页）
 * @param {Number} pageNum - 页码（默认1）
 */
const loadOrderData = async (pageNum = 1) => {
  // 前置校验：未登录/加载中则不请求
  if (!currentAccount.value || loading.value) return
  loading.value = true

  try {
    const baseURL = 'http://localhost:8080'
    const response = await axios.get(`${baseURL}/api/order/page`, {
      params: {
        userAccount: currentAccount.value,
        pageNo: pageNum,
        pageSize: pageSize.value
      }
    })

    const resData = response.data
    // 接口返回成功
    if (resData?.code === 200 && resData?.data) {
      const pageResult = resData.data
      // 分页逻辑：第一页覆盖，后续页追加
      orderList.value = pageNum > 1 
        ? [...orderList.value, ...(pageResult.list || [])]
        : pageResult.list || []
      // 更新分页信息
      total.value = pageResult.total || 0
      totalPages.value = pageResult.pages || 0
      currentPage.value = pageResult.pageNo || 1
    } else {
      alert(`加载订单失败：${resData?.msg || '未知错误'}`)
    }
  } catch (error) {
    console.error('加载订单异常：', error)
    // 友好的错误提示
    if (error.message.includes('Network Error')) {
      alert('网络错误，请检查后端服务是否启动！')
    } else if (error.message.includes('404')) {
      alert('订单接口不存在，请检查后端接口路径！')
    } else {
      alert('加载订单失败，请重试！')
    }
  } finally {
    // 无论成功失败，结束加载状态
    loading.value = false
  }
}

/**
 * 加载更多订单（下一页）
 */
const loadMore = () => {
  if (hasMoreData.value) {
    loadOrderData(currentPage.value + 1)
  }
}

// ===================== 监听与生命周期 =====================
/**
 * 监听账号变化，自动刷新订单
 */
watch(currentAccount, (newVal) => {
  if (newVal) {
    loadOrderData(1) // 切换账号重新加载第一页
  } else {
    orderList.value = [] // 未登录清空订单列表
  }
}, { immediate: true })

/**
 * 监听localStorage中用户信息变化（跨标签页切换账号）
 */
const handleStorageChange = (e) => {
  if (e.key === 'userInfo') {
    updateCurrentAccount()
    loadOrderData(1)
  }
}

/**
 * 组件挂载：初始化账号 + 监听storage变化
 */
onMounted(() => {
  updateCurrentAccount()
  window.addEventListener('storage', handleStorageChange)
})

/**
 * 组件卸载：移除storage监听，避免内存泄漏
 */
onUnmounted(() => {
  window.removeEventListener('storage', handleStorageChange)
})
</script>

<style scoped>
/* 通用样式 */
.font-medium {
  font-weight: 500;
}
.font-bold {
  font-weight: 700;
}

/* 订单项表头样式 */
.order-item-header {
  background-color: #f5f5f5;
  margin: 0; /* 移除v-row默认margin */
}

/* 订单项列表容器：消除默认间距 */
.order-item-list {
  /* 关键：统一容器内边距，避免最后一行margin溢出 */
  display: flex;
  flex-direction: column;
}

/* 订单项行样式（统一所有行高度） */
.order-item-row {
  margin: 0; /* 移除v-row默认margin-bottom */
  min-height: 60px; /* 固定最小行高，确保所有行高度一致 */
  box-sizing: border-box; /* 边框计入高度 */
  border-bottom: 1px solid #eeeeee; /* 所有行都加底部边框 */
}

/* 最后一行样式：保留边框，统一视觉 */
.order-item-row.last-row {
  /* 可选：如果想去掉最后一行边框，可加此样式，但高度仍用min-height保证一致 */
  /* border-bottom: none; */
  /* 关键：即使去掉边框，也加一个透明边框保持高度一致 */
  border-bottom: 1px solid transparent;
}

/* 兼容Vuetify的v-col样式，确保垂直对齐 */
.order-item-row .v-col {
  display: flex;
  align-items: center;
  justify-content: center; /* 居中对齐，统一视觉 */
}
.order-item-row .v-col:first-child {
  justify-content: flex-start; /* 商品名称左对齐 */
}
</style>