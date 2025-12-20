<!-- views/HomeView.vue -->
<template>
  <SearchBar ref="searchBarRef" @search="handleSearch"/>
</template>

<script setup>
import { ref } from 'vue'
import SearchBar from '@/components/SearchBar.vue'

const searchBarRef = ref(null)

const handleSearch = (keyword) => {
  // 填充搜索逻辑
  console.log('搜索栏触发了搜索', keyword)
  searchBarRef.value.loading = false
}
</script>