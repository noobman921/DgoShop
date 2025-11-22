import {ref} from 'vue'

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

function handleLoginIn(){
    //后端交互未添加
    loginStatus.value = {
        ...loginStatus.value,
        isLogin: true,
        name: loginStatus.value.loginName,
        loginDialogVisible: false
    }
}

function handleLoginRegister(){
    //后端交互未添加
    loginStatus.value = {
        ...loginStatus.value,
        isLogin: false,
        name: null,
        loginDialogVisible: true,
        errorDialogVisible: true,
        errorDialogMessage: "注册功能尚未实现，敬请期待！",
    }
}

function handleLoginOut(){
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

export {
    loginStatus, 
    handleLogin,
    handleLoginIn,
    handleLoginRegister,
    handleLoginOut
}