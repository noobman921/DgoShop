import { defineStore } from 'pinia'
import { ref } from 'vue'
import axios from 'axios'

export const useAuthStore = defineStore('auth', () => {
  const userInfo = ref(null) 
  const isLogin = ref(false) 

  // 登录
  const login = async (userData) => {
    const response = await axios.get('http://localhost:8080/api/user/log/login', {
        params: {
          username: userData.username,
          password: userData.password
        }
      })
    const resData = response.data
      if (resData.code == 200) { // 匹配后端成功码
        userInfo.value = userData
        isLogin.value = true
        localStorage.setItem('userInfo', JSON.stringify(userData))
        console.log(userInfo.value)
      } else {
        throw new Error(resData.msg || '登录失败')
      }
  }

  // 注册
  const register = async (userData) => {
    const response = await axios.get('http://localhost:8080/api/user/log/reg', {
        params: {
          username: userData.username,
          password: userData.password
        }
      })
    const resData = response.data
      if (resData.code == 200) { // 匹配后端成功码
        userInfo.value = userData
        isLogin.value = true
        localStorage.setItem('userInfo', JSON.stringify(userData))
      } else {
        throw new Error(resData.msg || '登录失败')
      }
  }

  // 退出登录
  const logout = () => {
    userInfo.value = null
    isLogin.value = false
    localStorage.removeItem('userInfo')
  }

  // 初始化
  const initAuth = () => {
    const savedUser = localStorage.getItem('userInfo')
    if (savedUser) {
      userInfo.value = JSON.parse(savedUser)
      isLogin.value = true
    }
  }

  // 暴露状态和方法供其他组件使用
  return {
    userInfo,
    isLogin,
    login,
    register,
    logout,
    initAuth
  }
})