<template>
  <div class="home">
    <h1>Open436 论坛系统</h1>
    <p>前端基础架构已就绪</p>
    <div class="info">
      <p>环境：{{ env }}</p>
      <p>API 地址：{{ apiBaseUrl }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const env = ref(import.meta.env.VITE_ENV)
const apiBaseUrl = ref(import.meta.env.VITE_API_BASE_URL)
</script>

<style scoped>
.home {
  padding: 60px 20px;
  text-align: center;
}

h1 {
  font-size: 32px;
  margin-bottom: 20px;
  color: #409eff;
}

p {
  font-size: 16px;
  color: #666;
  margin: 10px 0;
}

.info {
  margin-top: 40px;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
}

.info p {
  text-align: left;
  font-family: 'Courier New', monospace;
}
</style>

