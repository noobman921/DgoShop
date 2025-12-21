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
      <!-- 无数据提示（复用Vuetify内置类，移除自定义样式） -->
      <v-alert
        v-if="productList.length === 0"
        variant="text"
        color="grey-lighten-2"
        class="my-8"
        elevation="0"
      >
        <div class="text-center text-gray-500">{{ emptyText }}</div>
      </v-alert>

      <div class="product-grid my-4" v-else>
        <div 
          v-for="(item, index) in productListWithEmpty" 
          :key="index" 
          class="product-item"
        >
          <!-- 空项：仅占位，无内容 -->
          <div v-if="!item" class="empty-placeholder h-full"></div>
          
          <!-- 商品卡片：非空项显示（添加点击事件） -->
          <v-card 
            v-else 
            elevation="2" 
            hover 
            class="h-full w-100 rounded-lg"
            @click="openProductDetail(item)"
            style="cursor: pointer"
          >
            <v-img
              :src="item.product_pic ?? ''"
              height="200px"
              style="object-fit: cover" 
              @error="(e) => e.target.style.display = 'none'"
              placeholder="data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciPgogIDxyZWN0IHg9IjAiIHk9IjAiIHdpZHRoPSIxMDAiIGhlaWdodD0iMTAwIiBmaWxsPSIjZjVmNWY1Ii8+CiAgPHRleHQgeD0iNTAlIiB5PSI1MCUiIGZvbnQtZmFtaWx5PSJBcmlhbCIgZm9udC1zaXplPSIxNiIgZmlsbD0iIzU1NSI+5peg5p6X5Lq6PC90ZXh0Pgo8L3N2Zz4="
            >
            </v-img>
            <!-- 商品信息 -->
            <v-card-text class="pt-2 px-3">
              <h3 class="text-subtitle-1 font-medium mb-1 truncate">{{ item.productName }}</h3>
              <p class="text-primary text-h6 font-bold">¥{{ item.productPrice }}</p>
              <p class="text-caption text-gray-500">库存：{{ item.stock }}件</p>
            </v-card-text>
          </v-card>
        </div>
      </div>
    </template>

    <v-pagination
      v-if="total >= 0"
      v-model="localCurrentPage"
      :length="totalPages"
      :disabled="loading"
      color="primary"
      @input="handlePageChange"
      class="justify-center my-4"
    />

    <v-dialog
      v-model="detailDialogVisible"
      max-width="600px"
      persistent
    >
      <v-card class="rounded-lg">
        <!-- 弹窗头部：标题 + 关闭按钮 -->
        <v-card-title class="justify-between items-center px-6 py-4">
          <span class="text-h5 font-medium">商品详情</span>
          <v-btn icon @click="closeDetailDialog" variant="text">
            <v-icon>mdi-close</v-icon>
          </v-btn>
        </v-card-title>

        <!-- 弹窗内容：商品信息 + 数量选择 -->
        <v-card-text class="px-6 py-4">
          <v-container fluid class="pa-0">
            <v-row no-gutters class="gap-6">
              <!-- 左侧：商品图片 -->
              <v-col cols="12" md="6" class="d-flex justify-center align-center">
                <v-img
                  :src="selectedProduct.product_pic ?? ''"
                  height="300px"
                  class="rounded-lg"
                  @error="(e) => e.target.style.display = 'none'"
                  placeholder="data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciPgogIDxyZWN0IHg9IjAiIHk9IjAiIHdpZHRoPSIxMDAiIGhlaWdodD0iMTAwIiBmaWxsPSIjZjVmNWY1Ii8+CiAgPHRleHQgeD0iNTAlIiB5PSI1MCUiIGZvbnQtZmFtaWx5PSJBcmlhbCIgZm9udC1zaXplPSIxNiIgZmlsbD0iIzU1NSI+5peg5p6X5Lq6PC90ZXh0Pgo8L3N2Zz4="
                />
              </v-col>

              <!-- 右侧：商品信息 + 数量选择 -->
              <v-col cols="12" md="6" class="d-flex flex-column justify-center">
                <h3 class="text-h4 font-bold mb-2">{{ selectedProduct.productName }}</h3>
                <p class="text-gray-600 mb-4">{{ selectedProduct.productDesc || '暂无商品描述' }}</p>
                <p class="text-primary text-h3 font-bold mb-2">¥{{ selectedProduct.productPrice }}</p>
                <p class="text-caption text-gray-500 mb-6">库存：{{ selectedProduct.stock }}件</p>

                <!-- 数量选择区域 -->
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
                    :disabled="selectedQuantity >= selectedProduct.stock"
                    variant="outlined"
                    class="mr-0"
                  >
                    <v-icon>mdi-plus</v-icon>
                  </v-btn>
                </div>

                <!-- 添加购物车按钮 -->
                <v-btn 
                  color="primary" 
                  @click="addToCart"
                  :disabled="selectedProduct.stock <= 0"
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

// 定义Props
const props = defineProps({
  productList: { type: Array, required: true, default: () => [] },
  loading: { type: Boolean, required: true, default: false },
  currentPage: { type: Number, required: true, default: 1 },
  totalPages: { type: Number, required: true, default: 0 },
  total: { type: Number, required: true, default: 0 },
  pageSize: { type: Number, default: 8 },
  emptyText: { type: String, default: '暂无商品数据' }
})

// 定义Emits
const emit = defineEmits(['page-change'])

// 本地分页值
const localCurrentPage = ref(props.currentPage)

// 监听页码变化
watch(
  () => props.currentPage,
  (newVal) => {
    localCurrentPage.value = newVal
  },
  { immediate: true }
)

// 分页切换事件
const handlePageChange = (newPage) => {
  emit('page-change', newPage)
}

// 补位空项，确保列表长度是4的倍数
const productListWithEmpty = computed(() => {
  const list = [...props.productList]
  const remainder = list.length % 4
  if (remainder !== 0) {
    const emptyCount = 4 - remainder
    for (let i = 0; i < emptyCount; i++) {
      list.push(null)
    }
  }
  return list
})

// 弹窗显示状态
const detailDialogVisible = ref(false)
// 选中的商品数据
const selectedProduct = ref({})
// 选中的购买数量
const selectedQuantity = ref(1)

// 打开商品详情弹窗
const openProductDetail = (item) => {
  selectedProduct.value = { ...item }
  selectedQuantity.value = 1 // 每次打开重置数量为1
  detailDialogVisible.value = true
}

// 关闭商品详情弹窗
const closeDetailDialog = () => {
  detailDialogVisible.value = false
}

// 减少购买数量（最小为1）
const decreaseQuantity = () => {
  if (selectedQuantity.value > 1) {
    selectedQuantity.value--
  }
}

// 增加购买数量（最大为库存）
const increaseQuantity = () => {
  if (selectedQuantity.value < selectedProduct.value.stock) {
    selectedQuantity.value++
  }
}

// 添加购物车逻辑
const addToCart = async () => {
  try {
    const savedUser = localStorage.getItem('userInfo')

    if (!savedUser) {
      alert('请先登录！')
      closeDetailDialog()
      return
    }
    const account = JSON.parse(savedUser).username
    // 调用购物车新增接口
    const response = await axios.post(
      'http://localhost:8080/api/cart/add',
      null,
      {
        params: {
          account: account,
          productId: selectedProduct.value.productId,
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
/* 仅保留核心布局样式（无法用Vuetify类替代），其余全部复用内置类 */
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

/* 响应式适配（核心布局，必须保留） */
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

/* 空占位/图片占位仅保留基础样式 */
.empty-placeholder, .img-placeholder {
  background-color: transparent;
}
.img-placeholder {
  background-color: #f5f5f5;
  border: 1px solid #eeeeee;
}
</style>