<template>
    <Main v-if="userStore.isLoggedIn" />
    <Login v-else />
</template>

<script setup lang="ts">
import Login from './components/Login.vue'
import Main from './components/Main.vue'
import { useUserStore } from './stores/user'
import { connectWebSocket, closeWebSocket} from './services/websocket'
import { watchEffect } from 'vue'

const userStore = useUserStore()

watchEffect(() => {
    if (userStore.isLoggedIn) {
        console.log('用户已登录，正在连接 WebSocket')
        connectWebSocket()
    } else {
        console.log('用户未登录，正在断开 WebSocket')
        closeWebSocket()
    }
})
</script>

<style>
body {
    margin: 0;
    font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
}
</style>
