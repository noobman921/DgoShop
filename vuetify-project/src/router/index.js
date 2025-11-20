// router/index.js
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/pages/HomeView.vue')
  },
  {
    path: '/cart',
    name: 'Cart', 
    component: () => import('@/pages/CartView.vue')
  },
  {
    path: '/orders',
    name: 'Orders',
    component: () => import('@/pages/OrdersView.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router