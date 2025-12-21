import {ref} from 'vue'
import axios from 'axios'

import { useAuthStore } from '@/stores/authStore'


const loginStatus = ref({
    isLogin: false,
    name: null,
    loginDialogVisible: false,
    errorDialogVisible: false,
    errorDialogMessage: null,
    loginName: null,
    loginPassword: null
})

function handleLogin(){
    loginStatus.value = {
        ...loginStatus.value,
        isLogin: false,
        name: null,
        loginDialogVisible: true,
        errorDialogVisible: false,
        loginName: null,
        loginPassword: null
    }
}

async function handleLoginIn(){
    
    const authStore = useAuthStore()
    const {userInfo, isLogin, login, register, logout, initAuth } = authStore

    try {
        const userData = {
            username:loginStatus.value.loginName,
            password:loginStatus.value.loginPassword
        }
        await login(userData)

        loginStatus.value = {
        ...loginStatus.value,
        isLogin: true,
        name: loginStatus.value.loginName,
        loginDialogVisible: false
    }
    } catch (error) {
        console.error('登录失败：', error)
    }
}

async function handleLoginRegister(){

    const authStore = useAuthStore()
    const {userInfo, isLogin, login, register, logout, initAuth } = authStore

    try {
        const userData = {
            username:loginStatus.value.loginName,
            password:loginStatus.value.loginPassword
        }

        await register(userData)
        
        loginStatus.value = {
        ...loginStatus.value,
        isLogin: true,
        name: loginStatus.value.loginName,
        loginDialogVisible: false
    }
    } catch (error) {
        console.error('注册失败：', error)
    }
}

function handleLoginOut(){
    logout();
    loginStatus.value = {
        ...loginStatus.value,
        isLogin: false,
        name: null,
        loginDialogVisible: true,
        errorDialogVisible: false,
        loginName: null,
        loginPassword: null
    }
}

function handleLoginInit(){

    const authStore = useAuthStore()
    const {userInfo, isLogin, login, register, logout, initAuth } = authStore

    initAuth();
    if(isLogin){
        loginStatus.value = {
        ...loginStatus.value,
        isLogin: true,
        name: userInfo.username,
        loginDialogVisible: false
    }
    }
}

export {
    loginStatus, 
    handleLogin,
    handleLoginInit,
    handleLoginIn,
    handleLoginRegister,
    handleLoginOut
}