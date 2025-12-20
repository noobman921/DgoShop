<template>
  <v-row>
    <img 
      src="/images/dcrew1.png" 
      alt="商品检索logo"     
      style="margin-right: 12px;" 
      width="18%"
    >
    <v-container fluid class="py-8 text-center" width="60%">
      <div
        class="text-h1 font-black text-center mb-1 mt-5 text-teal-lighten-2"
        style="
          display: inline-block;
          text-shadow: 0 3px 5px rgba(0,0,0,0.3);
          font-style: italic;
        "
      >
        Dgo, 购到你够！
      </div>
      <div
        class="text-subtitle-1 font-thin text-right mb-9 text-teal-lighten-3">
        --专业团队为您服务
      </div>
      <v-card class="mx-auto" color="surface-light">
        <v-card-text>
          <v-text-field
            v-model="keyword"
            :loading="loading"
            append-inner-icon="mdi-magnify"
            density="comfortable"
            label="请输入商品名称"
            variant="solo"
            hide-details
            single-line
            @click:append-inner="handleSearchClick"
          ></v-text-field>
        </v-card-text>
      </v-card>
    </v-container>
    <img 
      src="/images/dcrew2.png" 
      alt="商品检索logo"     
      style="margin-right: 12px;" 
      width="18%"
    >
  </v-row>
</template>


<script setup>
import { ref } from 'vue'

const keyword = ref('')
const loading = ref(false)

const emit = defineEmits(['search'])

const handleSearchClick = () => {
    loading.value = true
    emit('search', keyword.value)
}

defineExpose({ 
  loading,
  keyword
});
</script>