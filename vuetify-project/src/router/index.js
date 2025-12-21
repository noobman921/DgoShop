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
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router