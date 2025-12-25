<template>
  <v-app style="background-color: #f5f5f5; box-sizing: border-box;">
    <!-- 左侧侧边栏：无修改 -->
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

    <!-- 主内容区：修改删除按钮为下架按钮 + 商品列表显示上架状态 + 上架按钮 -->
    <v-main 
      class="pa-6" 
      style="margin-left: 240px; min-height: 100vh; box-sizing: border-box; overflow-x: hidden; width: calc(100vw - 240px);"
    >
      <v-card 
        elevation="6" 
        class="mb-6"
        style="background-color: #fff; border-top: 4px solid #80cbc4; width: 100%; overflow: hidden;"
      >
        <!-- 卡片头部：核心修改 → 删除按钮改为下架按钮 -->
        <v-card-title class="d-flex flex-wrap justify-between align-center p-4 bg-teal-lighten-5 gap-2">
          <h2 class="text-h5 text-teal-darken-4 font-weight-bold">商品管理</h2>
          <v-btn-group class="flex-wrap gap-2">
            <v-btn color="teal" @click="openAddDialog" size="large" class="px-6 py-3 text-lg white-space-nowrap">
              <v-icon left size="24">mdi-plus</v-icon>添加商品
            </v-btn>
            <!-- 核心修改：删除按钮 → 下架按钮 -->
            <v-btn color="teal-darken-1" @click="handleShelveOffProduct" :disabled="!selectedProduct" size="large" class="px-6 py-3 text-lg white-space-nowrap">
              <v-icon left size="24">mdi-arrow-down-bold-box</v-icon>下架商品
            </v-btn>
            <v-btn color="teal-lighten-1" @click="openUpdateStockDialog" :disabled="!selectedProduct" size="large" class="px-6 py-3 text-lg white-space-nowrap">
              <v-icon left size="24">mdi-pencil</v-icon>更新库存
            </v-btn>
          </v-btn-group>
        </v-card-title>

        <!-- 商品列表：核心修改 → 新增上架状态列 + 上架/下架操作按钮 -->
        <v-card-text class="p-4" style="width: 100%; overflow-x: auto;">
          <div style="width: 100%; min-width: 800px;"> <!-- 调整最小宽度适配新列 -->
            <v-list dense style="width: 100%;">
              <!-- 表头：核心修改 → 新增上架状态列 -->
              <v-list-item class="bg-teal-lighten-4 text-teal-darken-4 font-weight-bold text-lg">
                <v-list-item-content class="d-flex w-100 gap-1rem">
                  <span style="flex: 1; min-width: 150px; word-break: break-word;">商品名称</span>
                  <span style="flex: 0 0 120px; min-width: 120px; text-align: center;">定价（元）</span>
                  <span style="flex: 0 0 100px; min-width: 100px; text-align: center;">库存</span>
                  <span style="flex: 0 0 100px; min-width: 100px; text-align: center;">上架状态</span> <!-- 新增 -->
                  <span style="flex: 0 0 200px; min-width: 200px; text-align: center;">操作</span> <!-- 调整宽度 -->
                </v-list-item-content>
              </v-list-item>

              <!-- 商品项：核心修改 → 显示上架状态 + 新增上架/下架按钮 -->
              <v-list-item
                v-for="product in productList"
                :key="product.productId"
                @click="selectProduct(product)"
                :class="{ 'bg-teal-darken-3 text-white': selectedProduct?.productId === product.productId }"
                class="cursor-pointer transition-colors py-4 text-lg"
              >
                <v-list-item-content class="d-flex w-100 align-items-center gap-1rem">
                  <span style="flex: 1; min-width: 150px; word-break: break-word; line-height: 1.5;">{{ product.productName }}</span>
                  <span style="flex: 0 0 120px; min-width: 120px; text-align: center;">{{ product.productPrice }}</span>
                  <span style="flex: 0 0 100px; min-width: 100px; text-align: center;">{{ product.stock }}</span>
                  <!-- 新增：显示上架状态 -->
                  <span style="flex: 0 0 100px; min-width: 100px; text-align: center;">
                    <v-chip :color="product.isOnShelf === 1 ? 'teal' : 'gray'" size="small">
                      {{ product.isOnShelf === 1 ? '已上架' : '已下架' }}
                    </v-chip>
                  </span>
                  <!-- 操作列：新增上架/下架切换按钮 -->
                  <span style="flex: 0 0 200px; min-width: 200px; text-align: center;">
                    <v-btn size="large" color="teal" @click.stop="openUpdateStockDialog(product)" class="px-2 py-2 mr-2">
                      更新库存
                    </v-btn>
                    <v-btn 
                      size="large" 
                      :color="product.isOnShelf === 1 ? 'gray' : 'teal'" 
                      @click.stop="handleChangeShelfStatus(product)" 
                      class="px-2 py-2"
                    >
                      {{ product.isOnShelf === 1 ? '下架' : '上架' }}
                    </v-btn>
                  </span>
                </v-list-item-content>
              </v-list-item>

              <!-- 空数据：无修改 -->
              <v-list-item class="py-6">
                <v-list-item-content class="text-center text-gray-500 text-lg">暂无商品数据</v-list-item-content>
              </v-list-item>
            </v-list>
          </div>
        </v-card-text>

        <!-- 分页：无修改 -->
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

      <!-- 添加商品弹窗：无修改 -->
      <v-dialog v-model="addDialogShow" max-width="700px" app>
        <v-card style="border-top: 4px solid #80cbc4;">
          <v-card-title class="text-h4 text-teal-darken-4 font-weight-bold p-4 bg-teal-lighten-5">添加新商品</v-card-title>
          <v-card-text class="p-4">
            <v-form ref="addProductFormRef" v-model="addFormValid">
              <!-- 商品名称：原有 -->
              <v-text-field
                v-model="addProductForm.productName"
                label="商品名称"
                required
                class="mb-4 text-lg"
                :rules="[v => !!v || '商品名称不能为空']"
                color="teal"
                density="comfortable"
                style="width: 100%;"
              ></v-text-field>

              <!-- 商品定价：原有 -->
              <v-text-field
                v-model.number="addProductForm.price"
                label="商品定价（元）"
                type="number"
                required
                class="mb-4 text-lg"
                :rules="[v => !!v || '定价不能为空', v => v > 0 || '定价必须大于0']"
                color="teal"
                density="comfortable"
                style="width: 100%;"
              ></v-text-field>

              <!-- 商品库存：原有 -->
              <v-text-field
                v-model.number="addProductForm.stock"
                label="商品库存"
                type="number"
                required
                class="mb-4 text-lg"
                :rules="[v => !!v || '库存不能为空', v => v >= 0 || '库存不能为负数']"
                color="teal"
                density="comfortable"
                style="width: 100%;"
              ></v-text-field>

              <!-- 商品描述：原有 -->
              <v-textarea
                v-model="addProductForm.productDesc"
                label="商品描述（选填）"
                class="mb-4 text-lg"
                color="teal"
                density="comfortable"
                rows="3"
                style="width: 100%;"
              ></v-textarea>

              <!-- 图片选择控件：原有 -->
              <v-file-input
                v-model="selectedFile"
                label="选择商品图片（选填）"
                accept="image/*"
                class="mb-4 text-lg"
                color="teal"
                density="comfortable"
                prepend-icon="mdi-image"
                style="width: 100%;"
                @change="handleFileChange"
              ></v-file-input>

              <!-- 图片预览区域：原有 -->
              <div v-if="previewUrl" class="mb-4">
                <p class="text-lg text-teal-darken-4 mb-2">图片预览：</p>
                <v-img
                  :src="previewUrl"
                  height="200px"
                  width="200px"
                  style="object-fit: cover; border: 1px solid #e0f2f1;"
                ></v-img>
              </div>
            </v-form>
          </v-card-text>
          <v-card-actions class="justify-end p-4 bg-teal-lighten-5 gap-2">
            <v-btn text @click="addDialogShow = false" class="text-lg text-teal-darken-4">取消</v-btn>
            <v-btn color="teal" @click="handleAddProduct" size="large" class="px-6 py-2 text-lg">确认添加</v-btn>
          </v-card-actions>
        </v-card>
      </v-dialog>

      <!-- 更新库存弹窗：无修改 -->
      <v-dialog v-model="updateStockDialogShow" max-width="600px" app>
        <v-card style="border-top: 4px solid #80cbc4;">
          <v-card-title class="text-h4 text-teal-darken-4 font-weight-bold p-4 bg-teal-lighten-5">更新商品库存</v-card-title>
          <v-card-text class="p-4">
            <v-form ref="updateStockFormRef" v-model="updateStockFormValid">
              <v-text-field
                v-model="updateStockForm.productName"
                label="商品名称"
                disabled
                class="mb-4 text-lg"
                color="teal"
                density="comfortable"
                style="width: 100%;"
              ></v-text-field>
              <v-text-field
                v-model.number="updateStockForm.newStock"
                label="新库存数量"
                type="number"
                required
                class="mb-4 text-lg"
                :rules="[v => !!v || '库存不能为空', v => v >= 0 || '库存不能为负数']"
                color="teal"
                density="comfortable"
                style="width: 100%;"
              ></v-text-field>
            </v-form>
          </v-card-text>
          <v-card-actions class="justify-end p-4 bg-teal-lighten-5 gap-2">
            <v-btn text @click="updateStockDialogShow = false" class="text-lg text-teal-darken-4">取消</v-btn>
            <v-btn color="teal-lighten-1" @click="handleUpdateStock" size="large" class="px-6 py-2 text-lg">确认更新</v-btn>
          </v-card-actions>
        </v-card>
      </v-dialog>

      <!-- 提示弹窗：无修改 -->
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

// 基础配置
axios.defaults.baseURL = 'http://localhost:8080'
axios.defaults.timeout = 10000
axios.defaults.withCredentials = true

const router = useRouter()
const route = useRoute()
const merchantStore = useMerchantStore()

// 响应式数据
const drawer = ref(true)
const merchantId = ref(merchantStore.merchantId || localStorage.getItem('merchantId'))
const pageNo = ref(1)
const pageSize = ref(10)
const total = ref(0)
const pages = ref(0)
const productList = ref([])
const selectedProduct = ref(null)

// 弹窗相关
const addDialogShow = ref(false)
const updateStockDialogShow = ref(false)
const addFormValid = ref(true)
const updateStockFormValid = ref(true)
const addProductFormRef = ref(null)
const updateStockFormRef = ref(null)

// 图片上传相关响应式数据
const selectedFile = ref(null)
const previewUrl = ref('')

// 表单数据
const addProductForm = reactive({ 
  productName: '', 
  price: '', 
  stock: '',
  productDesc: ''
})
const updateStockForm = reactive({ productId: '', productName: '', newStock: '' })

// 提示弹窗
const snackbar = reactive({ show: false, message: '', color: 'teal', timeout: 3000 })
const showSnackbar = (message, color = 'teal') => {
  snackbar.message = message
  snackbar.color = color
  snackbar.show = true
}

// 路由激活判断
const isActive = (routeName) => route.name === routeName

// 获取商品列表：无修改
const getProductList = async () => {
  if (!merchantId.value) {
    showSnackbar('未获取到商家ID，请重新登录', 'teal-darken-1')
    return
  }
  try {
    const res = await axios.get('/api/merchant/log/product/page', {
      params: { merchantId: merchantId.value, pageNo: pageNo.value, pageSize: pageSize.value }
    })
    productList.value = res.data.list || []
    total.value = res.data.total || 0
    pages.value = res.data.pages || 0
  } catch (error) {
    console.error('获取商品列表失败：', error)
    showSnackbar('获取商品列表失败，请重试', 'teal-darken-1')
  }
}

// 分页切换：无修改
const changePage = (newPage) => {
  if (newPage < 1 || newPage > pages.value) return
  pageNo.value = newPage
  getProductList()
}

// 选中商品：无修改
const selectProduct = (product) => { selectedProduct.value = product }

// 打开添加弹窗：无修改
const openAddDialog = () => {
  addProductForm.productName = ''
  addProductForm.price = ''
  addProductForm.stock = ''
  addProductForm.productDesc = ''
  selectedFile.value = null
  previewUrl.value = ''
  addFormValid.value = true
  addDialogShow.value = true
}

// 处理文件选择：无修改
const handleFileChange = (file) => {
  if (file && file.length > 0) {
    previewUrl.value = URL.createObjectURL(file[0])
  } else {
    previewUrl.value = ''
  }
}

// 打开更新库存弹窗：无修改
const openUpdateStockDialog = (product = selectedProduct.value) => {
  if (!product) {
    showSnackbar('请先选中要更新的商品', 'teal-darken-1')
    return
  }
  updateStockForm.productId = product.productId
  updateStockForm.productName = product.productName
  updateStockForm.newStock = product.stock
  updateStockFormValid.value = true
  updateStockDialogShow.value = true
}

// 添加商品：无修改
const handleAddProduct = async () => {
  if (!addProductFormRef.value.validate()) return
  if (!merchantId.value) {
    showSnackbar('未获取到商家ID，请重新登录', 'teal-darken-1')
    return
  }

  try {
    const formData = new FormData()
    formData.append('productName', addProductForm.productName)
    formData.append('productPrice', addProductForm.price)
    formData.append('stock', addProductForm.stock)
    formData.append('merchantId', merchantId.value)
    formData.append('productDesc', addProductForm.productDesc || '')

    if (selectedFile.value && selectedFile.value.length > 0) {
      formData.append('file', selectedFile.value[0])
    }

    await axios.post('/api/merchant/log/product/add', formData)
    showSnackbar('商品添加成功')
    addDialogShow.value = false
    getProductList()
  } catch (error) {
    console.error('添加商品失败：', error)
    const errorMsg = error.response?.data?.msg || '添加商品失败，请重试'
    showSnackbar(errorMsg, 'teal-darken-1')
  }
}

// 核心修改1：删除商品 → 下架商品（调用上下架接口设置为0）
const handleShelveOffProduct = async () => {
  if (!selectedProduct.value) {
    showSnackbar('请先选中要下架的商品', 'teal-darken-1')
    return
  }
  try {
    // 调用上下架接口，设置isOnShelf为0（下架）
    await axios.put('/api/merchant/log/product/changeShelfStatus', null, {
      params: {
        productId: selectedProduct.value.productId,
        isOnShelf: 0 // 0=下架
      }
    })
    showSnackbar('商品下架成功')
    selectedProduct.value = null // 清空选中状态
    getProductList() // 刷新列表
  } catch (error) {
    console.error('商品下架失败：', error)
    const errorMsg = error.response?.data?.msg || '商品下架失败，请重试'
    showSnackbar(errorMsg, 'teal-darken-1')
  }
}

// 核心修改2：新增上下架状态切换方法（支持上架/下架）
const handleChangeShelfStatus = async (product) => {
  try {
    const newStatus = product.isOnShelf === 1 ? 0 : 1 // 1=上架，0=下架
    await axios.put('/api/merchant/log/product/changeShelfStatus', null, {
      params: {
        productId: product.productId,
        isOnShelf: newStatus
      }
    })
    // 提示语根据状态调整
    const msg = newStatus === 1 ? '商品上架成功' : '商品下架成功'
    showSnackbar(msg)
    getProductList() // 刷新列表
  } catch (error) {
    console.error('修改上下架状态失败：', error)
    showSnackbar('修改上下架状态失败，请重试', 'teal-darken-1')
  }
}

// 更新库存：无修改
const handleUpdateStock = async () => {
  if (!updateStockFormRef.value.validate()) return
  try {
    await axios.put('/api/merchant/log/product/updateStock', {
      productId: updateStockForm.productId,
      stock: updateStockForm.newStock
    })
    showSnackbar('库存更新成功')
    updateStockDialogShow.value = false
    getProductList()
  } catch (error) {
    showSnackbar('更新库存失败，请重试', 'teal-darken-1')
  }
}

// 挂载时加载列表：无修改
onMounted(() => { getProductList() })
</script>

<style scoped>
/* 原有样式 + 新增状态样式 */
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
/* 图片预览样式优化 */
.v-img {
  border-radius: 4px;
}
.v-file-input {
  --v-input-error-border-color: #b71c1c !important;
  --v-input-focus-border-color: #80cbc4 !important;
}
/* 新增：上架状态芯片样式 */
.v-chip {
  font-size: 14px !important;
}
</style>