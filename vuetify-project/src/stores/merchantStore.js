// src/stores/merchantStore.js
import { defineStore } from 'pinia'

// 定义并导出商家状态的Store
export const useMerchantStore = defineStore('merchant', {
  // 全局状态
  state: () => ({
    merchantId: '', // 存储商家ID
    merchantInfo: null 
  }),
  // 同步修改状态的方法
  actions: {
    // 设置商家信息
    setMerchantInfo(merchantId, merchantInfo) {
      this.merchantId = merchantId
      this.merchantInfo = merchantInfo
    },
    // 清空商家信息
    clearMerchantInfo() {
      this.merchantId = ''
      this.merchantInfo = null
    }
  },
  getters: {
    isMerchantLogin: (state) => !!state.merchantId
  }
})