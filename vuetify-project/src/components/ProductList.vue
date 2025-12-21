<!-- src/components/ProductListWithPagination.vue -->
<template>
  <v-container fluid class="py-2 px-0"> <!-- 移除默认内边距，避免宽度留白 -->
    <!-- 加载状态 -->
    <v-progress-linear
      v-if="loading"
      color="primary"
      indeterminate
      class="my-2"
    />

    <template v-else>
      <!-- 无数据提示 -->
      <div v-if="productList.length === 0" class="text-center py-8 text-gray-500">
        {{ emptyText }}
      </div>

      <div class="product-grid my-4" v-else>
        <div 
          v-for="(item, index) in productListWithEmpty" 
          :key="index" 
          class="product-item"
        >
          <!-- 空项：仅占位，无内容 -->
          <div v-if="!item" class="empty-placeholder h-full"></div>
          
          <!-- 商品卡片：非空项显示 -->
          <v-card v-else elevation="2" hover class="h-full w-100">
            <v-img
              :src="item.product_pic ?? ''"
              height="200px"
              class="product-img"
              @error="(e) => e.target.style.display = 'none'"
            >
              <template #placeholder>
                <div class="img-placeholder h-full w-full"></div>
              </template>
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

    <!-- 分页控件 -->
    <v-pagination
      v-if="total >= 0"
      v-model="localCurrentPage"
      :length="totalPages"
      :disabled="loading"
      color="primary"
      @input="handlePageChange"
      class="justify-center my-4"
    />
  </v-container>
</template>

<script setup>
import { ref, watch, defineProps, defineEmits, computed } from 'vue'

// 1. 定义Props
const props = defineProps({
  productList: { type: Array, required: true, default: () => [] },
  loading: { type: Boolean, required: true, default: false },
  currentPage: { type: Number, required: true, default: 1 },
  totalPages: { type: Number, required: true, default: 0 },
  total: { type: Number, required: true, default: 0 },
  pageSize: { type: Number, default: 8 },
  // 移除gridCols，改用固定4列百分比布局
  emptyText: { type: String, default: '暂无商品数据' }
})

// 2. 定义Emits
const emit = defineEmits(['page-change'])

// 3. 本地分页值
const localCurrentPage = ref(props.currentPage)

// 4. 监听页码变化
watch(
  () => props.currentPage,
  (newVal) => {
    localCurrentPage.value = newVal
  },
  { immediate: true }
)

// 5. 分页切换事件
const handlePageChange = (newPage) => {
  emit('page-change', newPage)
}

// 6. 核心：补位空项，确保列表长度是4的倍数（不足4个也按4列排列）
const productListWithEmpty = computed(() => {
  const list = [...props.productList]
  const remainder = list.length % 4 // 计算余数
  if (remainder !== 0) {
    // 补空项，直到长度为4的倍数
    const emptyCount = 4 - remainder
    for (let i = 0; i < emptyCount; i++) {
      list.push(null)
    }
  }
  return list
})
</script>

<style scoped>
/* 核心：4列网格布局，百分比占满宽度 */
.product-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16px; /* 对应原gap="4"（Vuetify的gap单位是4px，4*4=16px） */
  width: 100%;
}

/* 每个商品项占25%宽度，减去gap的均分，确保占满 */
.product-item {
  width: calc(25% - 12px); /* 16px gap，每行4个，每个减去12px（16*3/4） */
  flex-shrink: 0; /* 禁止收缩，确保25%宽度 */
}

/* 图片加载失败/无图片时的空白框 */
.img-placeholder {
  background-color: #f5f5f5; /* 浅灰色空白框 */
  border: 1px solid #eeeeee;
}

/* 商品图片样式：覆盖默认，确保填充 */
.product-img {
  object-fit: cover !important;
}

/* 空占位项样式：仅占位置，无内容 */
.empty-placeholder {
  background-color: transparent;
}

/* 无数据提示样式优化 */
.text-center {
  text-align: center;
}
.py-8 {
  padding-top: 2rem;
  padding-bottom: 2rem;
}
.text-gray-500 {
  color: #9e9e9e;
}

/* 响应式适配：小屏幕自动调整为2列/1列 */
@media (max-width: 960px) {
  .product-item {
    width: calc(50% - 8px); /* 2列，gap 16px，每个减去8px */
  }
}
@media (max-width: 600px) {
  .product-item {
    width: 100%; /* 1列，占满宽度 */
  }
}
</style>