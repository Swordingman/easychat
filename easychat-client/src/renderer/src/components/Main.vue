<template>
    <div class="main-container">
        <h1>欢迎, {{ userStore.userInfo?.nickname }}!</h1>
        <p>你已成功登录 EasyChat!</p>
        <p>你的用户ID是: {{ userStore.userInfo?.id }}</p>

        <div class="chat-container">
            <el-input placeholder="请输入聊天内容" v-model="testMessage" @keyup.enter="handleSendMessage"></el-input>
            <el-button type="primary" @click="handleSendMessage">发送</el-button>
        </div>
        <el-button type="danger" @click="handleLogout">退出登录</el-button>
    </div>
</template>

<script setup lang="ts">
import { useUserStore } from '../stores/user'
import { ref } from 'vue'
import { sendMessage } from '../services/websocket'

const testMessage = ref('')
const userStore = useUserStore()

function handleLogout() {
    userStore.logout()
}

function handleSendMessage() {
    if (!testMessage.value.trim()) return
    const messagePayload = {
        type: 'PRIVATE_CHAT',
        receiverId: 3,
        content: testMessage.value
    }
    sendMessage(messagePayload)
    testMessage.value = ''
}
</script>

<style scoped>
.main-container {
    padding: 20px;
    text-align: center;
}
.message-tester {
    display: flex;
    justify-content: center;
    gap: 10px;
    margin: 20px auto;
    max-width: 500px;
}
.logout-btn {
    margin-top: 20px;
}
</style>
