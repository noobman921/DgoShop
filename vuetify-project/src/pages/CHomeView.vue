<!-- views/HomeView.vue -->
<template>
  <v-container fluid class="py-8">
    <v-text
      class="text-h1 font-black text-center mb-2"
      style="
        display: inline-block;
        background: linear-gradient(135deg, #2196F3, #1976D2); 
        -webkit-background-clip: text;
        -moz-background-clip: text;
        background-clip: text;
        color: transparent !important; 
        text-shadow: 0 2px 4px rgba(0,0,0,0.1);
        letter-spacing: 1px;
      "
    >
      商品检索中心
    </v-text>
    <v-card class="mx-auto" color="surface-light" width="60%">
    <v-card-text>
      <v-text-field
        :loading="loading"
        append-inner-icon="mdi-magnify"
        density="comfortable"
        label="Search templates"
        variant="solo"
        hide-details
        single-line
        @click:append-inner="onClick"
      ></v-text-field>
    </v-card-text>
  </v-card>
  </v-container>
</template>

<script setup>
  import { ref } from 'vue'

  const loaded = ref(false)
  const loading = ref(false)

  function onClick () {
    loading.value = true
    setTimeout(() => {
      loading.value = false
      loaded.value = true
    }, 2000)
  }
</script>