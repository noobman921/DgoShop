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
      请先登录后查看购物车~
    </v-alert>

    <!-- 空数据提示 -->
    <v-alert
      v-else-if="cartList.length === 0 && !loading"
      variant="text"
      color="gray-lighten-2"
      class="my-8 text-center"
      elevation="0"
    >
      购物车空空如也~
    </v-alert>

    <!-- 购物车列表 -->
    <v-card v-else class="mb-16">
      <v-card-title class="bg-gray-lighten-1">
        <v-row class="w-100 align-center">
          <v-col cols="1">选择</v-col>
          <v-col cols="4">商品名称</v-col>
          <v-col cols="2">单价</v-col>
          <v-col cols="2">数量</v-col>
          <v-col cols="3">总价</v-col>
        </v-row>
      </v-card-title>

      <v-card-text class="pa-0">
        <v-row
          v-for="item in cartList"
          :key="item.productId || `cart-item-${Math.random()}`"
          class="align-center py-2 px-4 border-bottom"
        >
          <v-col cols="1">
            <v-checkbox
              v-model="selectedProductIds"
              :value="item.productId"
              color="primary"
            />
          </v-col>
          <v-col cols="4">{{ item.productName }}</v-col>
          <v-col cols="2">¥{{ formatPrice(item.productPrice) }}</v-col>
          <v-col cols="2">{{ item.quantity }}</v-col>
          <v-col cols="3">¥{{ formatPrice(Number(item.quantity) * Number(item.productPrice)) }}</v-col>
        </v-row>
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

    <!-- 底部固定操作栏 -->
    <v-card
      v-if="currentAccount"
      class="fixed bottom-0 left-0 right-0 z-10"
      elevation="4"
    >
      <v-card-text class="py-3 px-4">
        <v-row class="w-100 justify-between align-center">
          <v-col cols="4">
            <v-checkbox
              v-model="selectAll"
              label="全选"
              color="primary"
              @change="handleSelectAll"
            />
          </v-col>
          <v-col cols="8" class="text-right">
            <v-btn
              color="error"
              variant="outlined"
              class="mr-2"
              @click="deleteSelectedItems"
              :disabled="selectedProductIds.length === 0"
            >
              删除选中
            </v-btn>
            <v-btn
              color="primary"
              @click="confirmOperation"
              :disabled="selectedProductIds.length === 0"
            >
              确定
            </v-btn>
          </v-col>
        </v-row>
      </v-card-text>
    </v-card>
  </v-container>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import axios from 'axios'

// 1. 响应式数据定义
const cartList = ref([])
const selectedProductIds = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const totalPages = ref(0)
const loading = ref(false)
const currentAccount = ref('')

// 2. 格式化价格（兜底处理，避免数值解析错误）
const formatPrice = (price) => {
  try {
    return Number(price).toFixed(2)
  } catch (e) {
    return '0.00'
  }
}

// 3. 更新当前登录账号（避免模板直接调用JSON.parse抛错）
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

// 4. 计算属性：是否有更多数据
const hasMoreData = computed(() => {
  return currentPage.value < totalPages.value
})

// 5. 计算属性：全选状态
const selectAll = computed({
  get() {
    return cartList.value.length > 0 && selectedProductIds.value.length === cartList.value.length
  },
  set(val) {}
})

// 6. 核心方法：加载购物车数据
const loadCartData = async (pageNum) => {
  if (!currentAccount.value || loading.value) return
  loading.value = true

  try {
    const baseURL = 'http://localhost:8080'
    const response = await axios.get(`${baseURL}/api/cart/listpage`, {
      params: {
        account: currentAccount.value,
        pageNo: pageNum,
        pageSize: pageSize.value
      }
    })

    const resData = response.data
    if (resData?.code === 200 && resData?.data) {
      const pageResult = resData.data
      cartList.value = pageNum > 1 
        ? [...cartList.value, ...(pageResult.list || [])]
        : pageResult.list || []
      total.value = pageResult.total || 0
      totalPages.value = pageResult.pages || 0
      currentPage.value = pageResult.pageNo || 1
    } else {
      alert(`加载购物车失败：${resData?.msg || '未知错误'}`)
    }
  } catch (error) {
    console.error('加载购物车异常：', error)
    if (error.message.includes('Network Error')) {
      alert('网络错误，请检查后端服务是否启动！')
    } else if (error.message.includes('SyntaxError')) {
      alert('用户信息异常，请重新登录！')
      currentAccount.value = ''
    } else {
      alert('加载购物车失败，请重试！')
    }
  } finally {
    loading.value = false
  }
}

// 7. 加载更多数据
const loadMore = () => {
  if (hasMoreData.value) {
    loadCartData(currentPage.value + 1)
  }
}

// 8. 全选/取消全选
const handleSelectAll = (val) => {
  selectedProductIds.value = val 
    ? cartList.value.map(item => item.productId).filter(Boolean)
    : []
}

// 9. 删除选中项
const deleteSelectedItems = async () => {
  if (!currentAccount.value || selectedProductIds.value.length === 0) return
  if (!confirm('确定要删除选中的购物车商品吗？')) return

  try {
    const baseURL = 'http://localhost:8080'
    const deletePromises = selectedProductIds.value.map(productId => {
      return axios.post(`${baseURL}/api/cart/delete`, null, {
        params: {
          account: currentAccount.value,
          productId: productId
        }
      })
    })

    const results = await Promise.allSettled(deletePromises)
    const failItems = results.filter(res => {
      if (res.status !== 'fulfilled') return true
      return res.value?.data?.code !== 200
    })

    if (failItems.length === 0) {
      alert('删除成功！')
      loadCartData(1)
      selectedProductIds.value = []
    } else {
      alert(`部分商品删除失败：${failItems.length} 条`)
    }
  } catch (error) {
    console.error('删除购物车异常：', error)
    alert('删除失败，请重试！')
  }
}

// 10. 确定操作
const confirmOperation = () => {
  if (selectedProductIds.value.length === 0) return
  const selectedItems = cartList.value.filter(item => 
    selectedProductIds.value.includes(item.productId)
  )
  const totalAmount = selectedItems.reduce((sum, item) => {
    return sum + Number(item.quantity) * Number(item.productPrice)
  }, 0)
  
  alert(`你选中了 ${selectedItems.length} 件商品，总金额：¥${formatPrice(totalAmount)}`)
}

// 11. 监听localStorage中用户信息变化（跨标签页切换账号）
const handleStorageChange = (e) => {
  if (e.key === 'userInfo') {
    updateCurrentAccount()
    loadCartData(1)
  }
}

// 12. 监听账号变化，自动刷新购物车
watch(currentAccount, (newVal) => {
  if (newVal) {
    loadCartData(1)
  } else {
    cartList.value = []
    selectedProductIds.value = []
  }
}, { immediate: true })

// 13. 初始化和销毁监听
onMounted(() => {
  updateCurrentAccount()
  window.addEventListener('storage', handleStorageChange)
})

onUnmounted(() => {
  window.removeEventListener('storage', handleStorageChange)
})
</script>

<style scoped>
.fixed {
  position: fixed;
}
.bottom-0 {
  bottom: 0;
}
.left-0 {
  left: 0;
}
.right-0 {
  right: 0;
}
.z-10 {
  z-index: 10;
}
.border-bottom {
  border-bottom: 1px solid #eeeeee;
}
.mb-16 {
  margin-bottom: 4rem;
}
</style>