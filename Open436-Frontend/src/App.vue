<template>
  <n-config-provider>
    <n-message-provider>
      <div id="app">
        <router-view />
      </div>
    </n-message-provider>
  </n-config-provider>
</template>

<script setup>
import { NConfigProvider, NMessageProvider } from 'naive-ui'
</script>

<style>
/* 全局样式已在 main.css 中定义 */
</style>
