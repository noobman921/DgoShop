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


        </v-app-bar>
    </div>
</template>

<script setup>
import { useAppNavigation } from '@/composables/useAppNavigation';
const { navItems, handleNavClick } = useAppNavigation()

</script>