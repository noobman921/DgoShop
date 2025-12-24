import { ref } from 'vue'
import axios from 'axios'

/**
 * 商品搜索分页的业务处理逻辑（和展示层分离）
 * @returns {Object} 暴露状态和方法给展示层
 */
export const useProductNameSearch = () => {
  // 响应式状态（供展示层使用）
  const productList = ref([]) // 商品列表
  const loading = ref(false) // 加载状态
  const currentPage = ref(1) // 当前页
  const pageSize = ref(8) // 每页8个（固定）
  const total = ref(0) // 总条数
  const totalPages = ref(0) // 总页数
  const currentKeyword = ref('') // 当前搜索关键词

  // 核心：请求后端商品搜索接口（处理层核心逻辑）
  const fetchProductList = async () => {
    loading.value = true
    try {
      // 对接后端接口（替换为你的实际接口路径）
      const response = await axios.get('http://localhost:8080/api/product/productName', {
        params: {
          name: currentKeyword.value,
          pageNum: currentPage.value,
          pageSize: pageSize.value
        }
      })

      const resData = response.data
      if (resData.code === 200) { // 匹配后端成功码
        productList.value = resData.data.list || []
        console.log(productList.value)
        total.value = resData.data.total || 0
        totalPages.value = resData.data.pages || 0
      } else {
        throw new Error(resData.msg || '查询商品失败')
      }
    } catch (error) {
      console.error('商品搜索请求失败：', error)
      // 可接入全局提示（如Vuetify的v-snackbar）
      window.alert(error.message || '网络异常，请重试')
    } finally {
      loading.value = false
    }
  }

  // 3. 搜索触发逻辑（重置页码+请求数据）
  const handleSearch = (keyword) => {
    currentKeyword.value = keyword
    currentPage.value = 1 // 搜索时重置到第一页
    fetchProductList()
  }

  // 4. 分页切换逻辑（边界校验）
  const changePage = (newPage) => {
    if (newPage < 1 || newPage > totalPages.value || loading.value) return
    currentPage.value = newPage
    fetchProductList()
  }

  // 5. 暴露给展示层的状态和方法
  return {
    // 状态
    productList,
    loading,
    currentPage,
    pageSize,
    total,
    totalPages,
    // 方法
    handleSearch,
    changePage
  }
}