<template>
    <Main v-if="userStore.isLoggedIn" />
    <Login v-else />
</template>

<script setup lang="ts">
import { watch, onMounted } from 'vue'
import { useUserStore } from './stores/user'
import Login from './components/Login.vue'
import Main from './components/Main.vue'
import { connectWebSocket, closeWebSocket } from './services/websocket'
import { ElMessage } from 'element-plus'
import { useChatStore} from './stores/chat'

const userStore = useUserStore();
const chatStore = useChatStore();

watch(
    () => userStore.isLoggedIn,
    (newIsLoggedIn) => {
        if (newIsLoggedIn) {
            console.log('用户已登录，正在连接 WebSocket...');
            connectWebSocket();
            chatStore.startPollingRequests();
        } else {
            console.log('用户已登出，正在断开 WebSocket...');
            closeWebSocket();
            chatStore.stopPollingRequests();
        }
    },
    {
        immediate: true // 关键：让 watch 在组件初始化时立即执行一次
    }
)

// onMounted 负责应用的启动检查
onMounted(() => {
    // 我们只在 onMounted 里做检查和决策，不直接调用 connectWebSocket
    // 真正的连接/断开操作交给上面的 watch 来处理
    console.log('App已启动，正在检查登录状态...');
    if (userStore.isLoggedIn) {
        if (!userStore.checkTokenValidity()) {
            // Token 失效，执行登出
            ElMessage.warning('登录凭证已过期，请重新登录。');
            userStore.handleInvalidToken();
            // handleInvalidToken 会调用 logout()，这会使 isLoggedIn 变为 false，
            // 从而触发上面的 watch 执行 closeWebSocket() 和视图更新。
        } else {
            // Token 有效，什么都不用做。
            // 因为上面的 watch 设置了 immediate: true，它在初始化时已经根据
            // isLoggedIn 为 true 的状态，调用了 connectWebSocket()。
            console.log('Token 有效，WebSocket 已连接。');
        }
    } else {
        console.log('用户未登录，正在跳转到登录页面...');
    }
})
</script>

<style>
body {
    margin: 0;
    font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
}
</style>
