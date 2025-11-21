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
                    @click="handleLoginIn()"
                >
                    登录
                </v-btn>
            </div> 
        </v-app-bar>
        
    </div>
</template>

<script setup>
import { ref } from 'vue'
import { useAppNavigation } from '@/composables/useAppNavigation'
const { navItems, handleNavClick } = useAppNavigation()
import {loginStatus, handleLoginIn, handleLoginOut} from '@/composables/loginHandle'
</script>