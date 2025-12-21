<!-- src/views/ProductSearchPage.vue -->
<template>
  <v-container fluid class="py-4">
    <!-- 1. 搜索栏（独立组件） -->
    <SearchBar ref="searchBarRef" @search="handleSearchWrapper" />

    <!-- 2. 商品+分页组件（封装后的核心组件） -->
    <ProductListWithPagination
      :product-list="productList"
      :loading="loading"
      :current-page="currentPage"
      :total-pages="totalPages"
      :total="total"
      :page-size="pageSize"
      @page-change="handlePageChange"
    />
  </v-container>
</template>

<script setup>
import { ref } from 'vue'
// 引入搜索栏组件
import SearchBar from '@/components/SearchBar.vue'
// 引入封装的商品+分页组件
import ProductListWithPagination from '@/components/ProductList.vue'
// 引入处理层逻辑（完全不变）
import { useProductNameSearch } from '@/composables/useProductNameSearch'

// 1. 获取处理层的状态和方法
const {
  productList,
  loading,
  currentPage,
  pageSize,
  total,
  totalPages,
  handleSearch,
  changePage
} = useProductNameSearch()

// 2. 搜索栏ref（控制loading状态）
const searchBarRef = ref(null)

// 3. 包装搜索方法（同步搜索栏loading）
const handleSearchWrapper = (keyword) => {
  handleSearch(keyword) // 调用处理层的搜索逻辑
}

// 4. 监听分页组件的切换事件（调用处理层的分页逻辑）
const handlePageChange = (newPage) => {
  changePage(newPage) // 调用处理层的分页逻辑
}
</script>