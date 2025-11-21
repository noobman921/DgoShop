import {ref} from 'vue'

const loginStatus = ref({
    isLogin: false,
    name: null
})

function handleLoginIn(){
    loginStatus.value = {
        isLogin: true,
        name: "龟龙本龙"
    }
}

function handleLoginOut(){
    loginStatus.value = {
        isLogin: false,
        name: null
    }
}

export {
    loginStatus, 
    handleLoginIn,
    handleLoginOut
}