// src/composables/useAppNavigation.js
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

export function useAppNavigation() {
  const router = useRouter()
  const route = useRoute()

  // 导航菜单配置
  const navItems = ref([
    {
      title: '首页',
      name: 'Home',
      path: '/',
    },
    {
      title: '购物车',
      name: 'Cart', 
      path: '/cart',
    },
    {
      title: '订单详情',
      name: 'Orders',
      path: '/orders',
    }
  ])

  // 计算当前激活的菜单项
  const activeNavItems = computed(() => {
    return navItems.value.map(item => ({
      ...item,
      active: route.path === item.path
    }))
  })

  // 导航到指定页面
  const navigateTo = (path) => {
    router.push(path)
  }

  // 处理导航点击
  const handleNavClick = (item) => {
    navigateTo(item.path)
  }

  return {
    navItems: activeNavItems,
    handleNavClick,
    navigateTo
  }
}