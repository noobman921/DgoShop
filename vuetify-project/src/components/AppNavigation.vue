<template>
    <div>
        <v-app-bar
            color="teal-lighten-3"
            density="compact"
        >
            <v-app-bar-nav-icon></v-app-bar-nav-icon>
            <v-btn
                v-for="item in navItems"
                :key="item.name"
                variant="text"
                @click="handleNavClick(item)"
            >
                {{ item.title }}
            </v-btn>
            <v-spacer></v-spacer>
            <div class="mr-4">
                <v-btn
                    v-if = "loginStatus.isLogin"
                    variant="text"
                >
                    {{loginStatus.name}}
                    <v-menu
                        activator="parent"
                    >
                        <v-list>
                            <v-list-item
                                @click="handleLoginOut(false)"
                            >
                                登出
                            </v-list-item>
                        </v-list>
                    </v-menu>
                </v-btn> 
                <v-btn
                    v-else
                    variant="text"
                    @click="handleLogin()"
                >
                    登录
                </v-btn>
            </div> 
        </v-app-bar>
        <v-dialog
            v-model="loginStatus.loginDialogVisible"
            width="400"
        >
            <v-card title="登录">
                <v-form
                >
                    <v-text-field
                        hint="请输入用户名"
                        clearable
                        required
                        :rules="[
                            (value) => {
                            if (!value) return '用户名不能为空';
                            return value.trim().length > 0 || '用户名不能为空';
                            }
                        ]"
                        label="用户名"
                        width="350"
                        density="comfortable"
                        class="mx-auto"
                        v-model="loginStatus.loginName"
                    ></v-text-field>
                    <v-text-field
                        hint="请输入密码"
                        clearable
                        required
                        :rules="[
                            (value) => {
                            if (!value) return '用户名不能为空';
                            return value.trim().length > 0 || '用户名不能为空';
                            }
                        ]"
                        label="密码"
                        width="350"
                        density="comfortable"
                        type="password"
                        class="mx-auto"
                        v-model="loginStatus.loginPassword"
                    ></v-text-field>
                </v-form>
                <v-card-actions>
                    <v-btn
                        @click="handleLoginIn()"
                    >
                        登录
                    </v-btn>
                    <v-btn
                        @click="handleLoginRegister()"
                    >
                        注册
                    </v-btn>
                </v-card-actions>
            </v-card>
        </v-dialog>
        <v-dialog
            v-model="loginStatus.errorDialogVisible"
            width="200"
        >
            <v-card
                title= "提示"
            >
                <v-card-text>
                    {{ loginStatus.errorDialogMessage }}
                </v-card-text>
            </v-card>
        </v-dialog>
    </div>
</template>

<script setup>
import { ref } from 'vue'
import { onMounted } from 'vue'

import { useAppNavigation } from '@/composables/useAppNavigation'
const { navItems, handleNavClick } = useAppNavigation()
import {loginStatus, handleLogin, handleLoginInit, handleLoginIn, handleLoginRegister, handleLoginOut} from '@/composables/loginHandle'

onMounted(() => {
    handleLoginInit();
})
</script>