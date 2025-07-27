<template>
    <div class="chat-window-container">
        <!-- 未选中联系人时的占位符 -->
        <div v-if="!chatStore.activeContact" class="placeholder">
            <el-icon><Promotion /></el-icon>
            <span>开始聊天吧</span>
        </div>

        <!-- 聊天窗口 -->
        <template v-else>
            <!-- 头部 -->
            <div class="chat-header">
                {{ chatStore.activeContact.nickname }}
            </div>

            <!-- 消息区域 -->
            <div class="message-area" ref="messageAreaRef">
                <el-scrollbar>
                    <div class="message-list">
                        <div v-for="message in chatStore.activeMessages" :key="message.id" class="message-item-wrapper">
                            <!-- 对方的消息 -->
                            <div v-if="message.senderId !== userStore.userInfo?.id" class="message-item other">
                                <el-avatar :size="36" :src="chatStore.activeContact.avatar" class="message-avatar" />
                                <div class="message-content">{{ message.content }}</div>
                            </div>
                            <!-- 自己的消息 -->
                            <div v-else class="message-item self">
                                <div class="message-content">{{ message.content }}</div>
                                <el-avatar :size="36" :src="userStore.userInfo.avatar" class="message-avatar" />
                            </div>
                        </div>
                    </div>
                </el-scrollbar>
            </div>

            <!-- 输入区域 -->
            <div class="input-area">
                <el-input
                    v-model="inputMessage"
                    type="textarea"
                    :rows="4"
                    resize="none"
                    placeholder="输入消息，按 Enter 发送"
                    @keydown.enter.prevent="handleSendMessage"
                />
                <div class="send-button-wrapper">
                    <el-button type="primary" @click="handleSendMessage" :disabled="!inputMessage.trim()">发送(S)</el-button>
                </div>
            </div>
        </template>
    </div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick } from 'vue';
import { useChatStore } from '../stores/chat';
import { useUserStore } from '../stores/user';
import { sendMessage as wsSendMessage } from '../services/websocket';
import { ElMessage } from 'element-plus'

const chatStore = useChatStore();
const userStore = useUserStore();

const inputMessage = ref('');
const messageAreaRef = ref<HTMLElement | null>(null);

// 发送消息的处理函数
function handleSendMessage() {
    if (!inputMessage.value.trim() || !chatStore.activeContactId) {
        ElMessage.error('请输入消息内容');
        return;
    }

    const messagePayload = {
        type: 'PRIVATE_CHAT',
        receiverId: chatStore.activeContactId,
        content: inputMessage.value
    };

    // 调用 WebSocket 服务发送消息
    wsSendMessage(messagePayload);

    // 清空输入框
    inputMessage.value = '';
}

// 观察 activeMessages 的变化，当有新消息时自动滚动到底部
watch(
    () => chatStore.activeMessages,
    () => {
        // nextTick 确保在 DOM 更新后执行滚动操作
        nextTick(() => {
            const scrollContainer = messageAreaRef.value?.querySelector('.el-scrollbar__wrap');
            if (scrollContainer) {
                scrollContainer.scrollTop = scrollContainer.scrollHeight;
            }
        });
    },
    { deep: true } // 深度观察，确保数组内部变化也能被检测到
);
</script>

<style scoped>
.chat-window-container {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    background-color: #f5f5f5;
}
.placeholder {
    flex-grow: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    color: #ccc;
    font-size: 24px;
}
.placeholder .el-icon {
    font-size: 60px;
    margin-bottom: 20px;
}
.chat-header {
    padding: 15px;
    border-bottom: 1px solid #e0e0e0;
    font-size: 16px;
    background-color: #fff;
}
.message-area {
    flex-grow: 1;
    overflow-y: hidden; /* 由 el-scrollbar 控制滚动 */
}
.message-list {
    padding: 10px;
}
.message-item-wrapper {
    margin-bottom: 15px;
}
.message-item {
    display: flex;
    align-items: flex-start;
}
.message-content {
    padding: 8px 12px;
    border-radius: 6px;
    max-width: 60%;
    font-size: 14px;
    line-height: 1.5;
}
/* 对方的消息 */
.message-item.other {
    justify-content: flex-start;
}
.message-item.other .message-avatar {
    margin-right: 10px;
}
.message-item.other .message-content {
    background-color: #ffffff;
    color: #333;
}
/* 自己的消息 */
.message-item.self {
    justify-content: flex-end;
}
.message-item.self .message-avatar {
    margin-left: 10px;
}
.message-item.self .message-content {
    background-color: #95ec69;
    color: #333;
}
.input-area {
    border-top: 1px solid #e0e0e0;
    background-color: #ffffff;
}
/* 深度选择器，修改 el-input 内部样式 */
:deep(.input-area .el-textarea__inner) {
    border: none;
    box-shadow: none;
    padding: 10px;
}
.send-button-wrapper {
    text-align: right;
    padding: 0 10px 10px;
}
</style>
