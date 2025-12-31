// router/index.js
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/pages/CHomeView.vue')
  },
  {
    path: '/cust',
    name: 'CHome',
    component: () => import('@/pages/CHomeView.vue')
  },
  {
    path: '/cust/cart',
    name: 'CCart', 
    component: () => import('@/pages/CCartView.vue')
  },
  {
    path: '/cust/orders',
    name: 'COrders',
    component: () => import('@/pages/COrdersView.vue')
  },
  {
    path: '/merchant',
    name: 'MHome',
    component: () => import('@/pages/MLoginView.vue')
  },
  {
    path: '/merchant/login',
    name: 'MLogin',
    component: () => import('@/pages/MLoginView.vue')
  },
  {
    path: '/merchant/order', 
    name: 'MerchantOrder',
    component: () => import('@/pages/MOrderView.vue')
  },
  {
    path: '/merchant/product', 
    name: 'MerchantProduct',
    component: () => import('@/pages/MProductView.vue')
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

export default router