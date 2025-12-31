<template>
  <v-container fluid class="py-2 px-0">
    <!-- 加载状态 -->
    <v-progress-linear
      v-if="loading"
      color="primary"
      indeterminate
      class="my-2"
    />

    <template v-else>
      <!-- 无数据提示：加容错，确保productList是数组 -->
      <v-alert
        v-if="Array.isArray(productList) && productList.length === 0"
        variant="text"
        color="grey-lighten-2"
        class="my-8"
        elevation="0"
      >
        <div class="text-center text-gray-500">{{ emptyText }}</div>
      </v-alert>

      <div class="product-grid my-4" v-else-if="Array.isArray(productList)">
        <div 
          v-for="(item, index) in productListWithEmpty" 
          :key="`product-${index}-${item?.productId || 'empty'}`"  
          class="product-item"
        >
          <div v-if="!item" class="empty-placeholder h-full"></div>
          
          <!-- 商品卡片：用原生CSS控制图片容器尺寸 + 加可选链容错 -->
          <v-card 
            v-else 
            elevation="2" 
            hover 
            class="h-full w-100 rounded-lg"
            @click="openProductDetail(item)"
            style="cursor: pointer"
          >
            <div class="img-container" style="height: 200px; width: 100%; overflow: hidden;">
              <v-img
                :src="getProductImgUrl(item?.productPic)" 
                style="height: 100%; width: 100%; object-fit: cover;"
                @error="handleImgError"
              >
              </v-img>
            </div>
            <!-- 商品信息：加可选链容错 -->
            <v-card-text class="pt-2 px-3">
              <h3 class="text-subtitle-1 font-medium mb-1 truncate">{{ item?.productName || '暂无名称' }}</h3>
              <p class="text-primary text-h6 font-bold">¥{{ item?.productPrice || 0 }}</p>
              <p class="text-caption text-gray-500">库存：{{ item?.stock || 0 }}件</p>
            </v-card-text>
          </v-card>
        </div>
      </div>
    </template>

    <!-- 分页组件：【修改2】修复分页触发事件+显示条件 -->
    <v-pagination
      v-if="total > 0 && totalPages > 1" 
      v-model="localCurrentPage"
      :length="totalPages"
      :disabled="loading"
      color="primary"
      @update:modelValue="handlePageChange" 
      class="justify-center my-4"
    />

    <v-dialog
      v-model="detailDialogVisible"
      max-width="600px"
      persistent
    >
      <v-card class="rounded-lg">
        <v-card-title class="justify-between items-center px-6 py-4">
          <span class="text-h5 font-medium">商品详情</span>
          <v-btn icon @click="closeDetailDialog" variant="text">
            <v-icon>mdi-close</v-icon>
          </v-btn>
        </v-card-title>

        <v-card-text class="px-6 py-4">
          <v-container fluid class="pa-0">
            <v-row no-gutters class="gap-6">
              <!-- 左侧：商品图片 - 加可选链容错 -->
              <v-col cols="12" md="6" class="d-flex justify-center align-center">
                <div style="height: 300px; width: 100%; max-width: 300px; overflow: hidden; border-radius: 8px;">
                  <v-img
                    :src="getProductImgUrl(selectedProduct?.productPic)" 
                    style="height: 100%; width: 100%; object-fit: cover;"
                    @error="handleImgError"
                  >
                  </v-img>
                </div>
              </v-col>

              <!-- 右侧商品信息：加可选链容错 -->
              <v-col cols="12" md="6" class="d-flex flex-column justify-center">
                <h3 class="text-h4 font-bold mb-2">{{ selectedProduct?.productName || '暂无名称' }}</h3>
                <p class="text-gray-600 mb-4">{{ selectedProduct?.productDesc || '暂无商品描述' }}</p>
                <p class="text-primary text-h3 font-bold mb-2">¥{{ selectedProduct?.productPrice || 0 }}</p>
                <p class="text-caption text-gray-500 mb-6">库存：{{ selectedProduct?.stock || 0 }}件</p>

                <div class="d-flex align-center mb-6">
                  <span class="mr-3 text-body-1">购买数量：</span>
                  <v-btn 
                    icon 
                    @click="decreaseQuantity"
                    :disabled="selectedQuantity <= 1"
                    variant="outlined"
                    class="mr-0"
                  >
                    <v-icon>mdi-minus</v-icon>
                  </v-btn>
                  <span class="px-4 text-body-1">{{ selectedQuantity }}</span>
                  <v-btn 
                    icon 
                    @click="increaseQuantity"
                    :disabled="selectedQuantity >= (selectedProduct?.stock || 0)" 
                    variant="outlined"
                    class="mr-0"
                  >
                    <v-icon>mdi-plus</v-icon>
                  </v-btn>
                </div>

                <v-btn 
                  color="primary" 
                  @click="addToCart"
                  :disabled="(selectedProduct?.stock || 0) <= 0"
                  class="w-full"
                >
                  <v-icon left>mdi-cart-plus</v-icon>
                  添加到购物车
                </v-btn>
              </v-col>
            </v-row>
          </v-container>
        </v-card-text>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup>
import { ref, watch, defineProps, defineEmits, computed } from 'vue'
import axios from 'axios'

// 【修改4】修正图片基础URL（适配后端接口路径）
// 原接口路径：/api/product/img/products/文件名，与之前useProductNameSearch中的路径一致
const IMG_BASE_URL = 'http://8.134.119.70:8080'

// 定义Props（语法无错误）
const props = defineProps({
  productList: { type: Array, required: true, default: () => [] },
  loading: { type: Boolean, required: true, default: false },
  currentPage: { type: Number, required: true, default: 1 },
  totalPages: { type: Number, required: true, default: 0 },
  total: { type: Number, required: true, default: 0 },
  pageSize: { type: Number, default: 8 },
  emptyText: { type: String, default: '暂无商品数据' }
})

// 定义Emits（语法无错误）
const emit = defineEmits(['page-change'])

// 本地分页值
const localCurrentPage = ref(props.currentPage)

// 监听页码变化（语法无错误）
watch(
  () => props.currentPage,
  (newVal) => {
    localCurrentPage.value = newVal
  },
  { immediate: true }
)

const getProductImgUrl = (picPath) => {
  if (!picPath || picPath.trim() === '') {
    return ''  // 空值返回空字符串，触发error事件
  }
  // 提取文件名（兼容：/opt/shop_uploads/products/xxx.jpg 或 /uploads/products/xxx.jpg 或 直接xxx.jpg）
  const fileName = picPath.split('/').pop()
  // 拼接正确的图片接口路径：http://8.134.119.70:8081/api/product/img/products/xxx.jpg
  const fullUrl = `${IMG_BASE_URL}/api/product/img/products/${fileName}`
  console.log('最终图片URL：', fullUrl)
  return fullUrl
}


// 图片加载错误处理（优化）
const handleImgError = (e) => {
  // 隐藏加载失败的img
  if (e.target) {
    e.target.style.display = 'none'
  }
  // 给父容器设置占位样式
  const parent = e.target?.parentElement
  if (parent) {
    parent.style.backgroundColor = '#f5f5f5'
    parent.style.display = 'flex'
    parent.style.justifyContent = 'center'
    parent.style.alignItems = 'center'
    parent.style.color = '#999'
    parent.innerText = '暂无图片'
  }
}

// 【修改6】修复分页切换逻辑（加边界校验，与处理层逻辑对齐）
const handlePageChange = (newPage) => {
  // 边界校验：页码不能小于1、不能大于总页数、加载中不触发
  if (newPage < 1 || newPage > props.totalPages || props.loading) {
    return
  }
  emit('page-change', newPage)
}

// 补位空项（加容错，确保list是数组）
const productListWithEmpty = computed(() => {
  // 【修改7】容错：确保props.productList是数组
  const list = Array.isArray(props.productList) ? [...props.productList] : []
  const remainder = list.length % 4
  if (remainder !== 0) {
    const emptyCount = 4 - remainder
    for (let i = 0; i < emptyCount; i++) {
      list.push(null)
    }
  }
  return list
})

// 弹窗相关（加可选链容错）
const detailDialogVisible = ref(false)
const selectedProduct = ref({})
const selectedQuantity = ref(1)

const openProductDetail = (item) => {
  selectedProduct.value = { ...item }
  selectedQuantity.value = 1
  detailDialogVisible.value = true
}

const closeDetailDialog = () => {
  detailDialogVisible.value = false
}

const decreaseQuantity = () => {
  if (selectedQuantity.value > 1) {
    selectedQuantity.value--
  }
}

const increaseQuantity = () => {
  // 【修改8】加可选链+默认值，避免空值报错
  const maxStock = selectedProduct.value?.stock || 0
  if (selectedQuantity.value < maxStock) {
    selectedQuantity.value++
  }
}

// 添加购物车（保留原有逻辑，加可选链容错）
const addToCart = async () => {
  try {
    const savedUser = localStorage.getItem('userInfo')
    if (!savedUser) {
      alert('请先登录！')
      closeDetailDialog()
      return
    }
    const account = JSON.parse(savedUser).username
    // 【修改9】加可选链+空值校验
    const productId = selectedProduct.value?.productId
    if (!productId) {
      alert('商品ID异常，无法添加购物车！')
      return
    }
    const response = await axios.post(
      'http://8.134.119.70:8080/api/cart/add',
      null,
      {
        params: {
          account: account,
          productId: productId,
          quantity: selectedQuantity.value
        }
      }
    )
    const resData = response.data
    if (resData.code === 200) {
      alert('添加购物车成功！')
      closeDetailDialog()
    } else {
      alert(`添加失败：${resData.msg || '未知错误'}`)
    }
  } catch (error) {
    console.error('添加购物车异常：', error)
    alert('添加购物车失败，请检查网络或重试！')
  }
}
</script>

<style scoped>
/* 核心布局样式（原生CSS，无Tailwind语法） */
.product-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  width: 100%;
}

.product-item {
  width: calc(25% - 12px);
  flex-shrink: 0;
}

/* 响应式适配（原生CSS） */
@media (max-width: 960px) {
  .product-item {
    width: calc(50% - 8px);
  }
}
@media (max-width: 600px) {
  .product-item {
    width: 100%;
  }
}

/* 空占位样式 */
.empty-placeholder {
  background-color: transparent;
}

/* 图片容器默认背景（兜底） */
.img-container {
  background-color: #f5f5f5;
}

/* 穿透样式：隐藏img的原生属性文字 */
:deep(.v-img img) {
  border: none !important;
  text-indent: -9999px !important;
  overflow: hidden !important;
}
</style>