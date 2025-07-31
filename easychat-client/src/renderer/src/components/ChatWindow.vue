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
                                <el-avatar :src="chatStore.activeContact.avatar" class="message-avatar" />
                                <!-- 1. 消息渲染改造 -->
                                <MessageBubble :message="message" />
                            </div>
                            <!-- 自己的消息 -->
                            <div v-else class="message-item self">
                                <MessageBubble :message="message" />
                                <el-avatar :src="userStore.userInfo.avatar" class="message-avatar" />
                            </div>
                        </div>
                    </div>
                </el-scrollbar>
            </div>

            <!-- 输入区域 -->
            <div class="input-area">
                <div class="toolbar">
                    <!-- 表情包选择器 (无 Tooltip 精简版) -->
                    <el-popover
                        ref="emojiPopoverRef"
                        placement="top-start"
                        :width="400"
                        trigger="click"
                        popper-class="emoji-popover"
                    >
                        <template #reference>
                            <!-- 直接把带 wrapper 的图标作为触发器 -->
                            <div class="icon-wrapper">
                                <el-icon class="toolbar-icon"><Sugar /></el-icon>
                            </div>
                        </template>
                        <EmojiPicker :native="true" @select="onSelectEmoji" style="width: 100%;" />
                    </el-popover>

                    <!-- 文件选择器 (保留 Tooltip，因为它没有 click 冲突) -->
                    <el-tooltip content="发送文件/图片/视频" placement="top">
                        <div class="icon-wrapper" @click="handleSelectFile">
                            <el-icon class="toolbar-icon"><FolderAdd /></el-icon>
                        </div>
                    </el-tooltip>
                </div>

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
    <input
        type="file"
        ref="fileInputRef"
        style="display: none"
        @change="onFileSelected"
    />
</template>

<script setup lang="ts">
import { ref, watch, nextTick } from 'vue'
import { useChatStore, type Message } from '../stores/chat';
import { useUserStore } from '../stores/user';
import { sendMessage as wsSendMessage } from '../services/websocket';
import { ElMessage, ElLoading } from 'element-plus'
import apiClient from '../services/api'
import MessageBubble from './MessageBubble.vue'
import EmojiPicker from 'vue3-emoji-picker';
import 'vue3-emoji-picker/css'

const chatStore = useChatStore();
const userStore = useUserStore();

const inputMessage = ref('');
const messageAreaRef = ref<HTMLElement | null>(null);
const fileInputRef = ref<HTMLInputElement | null>(null);
const emojiPopoverRef = ref();

// 点击文件夹图标的触发函数
function handleSelectFile() {
    fileInputRef.value?.click();
}

// 文件被选择后的核心处理函数
async function onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;
    const file = input.files[0];
    if (fileInputRef.value) fileInputRef.value.value = '';

    // 1. 显示一个全屏的“上传中”加载动画
    const loadingInstance = ElLoading.service({
        lock: true,
        text: '文件上传中...',
        background: 'rgba(0, 0, 0, 0.7)',
    });

    try {
        // 2. 上传文件
        const formData = new FormData();
        formData.append('file', file);
        const response = await apiClient.post('/api/file/upload', formData, {
            headers: { 'Content-Type': 'multipart/form-data' },
        });

        const fileUrl = response.data.url;

        // 3. 确定消息类型
        let messageType: Message['messageType'] = 'FILE';
        if (file.type.startsWith('image/')) messageType = 'IMAGE';
        else if (file.type.startsWith('video/')) messageType = 'VIDEO';

        // 4. 构建消息内容和 WebSocket 消息体
        const messageContent = JSON.stringify({
            url: fileUrl,
            name: file.name,
            size: file.size,
        });

        const messagePayload = {
            type: 'PRIVATE_CHAT',
            receiverId: chatStore.activeContactId,
            messageType: messageType,
            content: messageContent,
        };

        // 5. 发送 WebSocket 消息
        wsSendMessage(messagePayload);

    } catch (error) {
        console.error('文件上传或发送失败:', error);
        ElMessage.error('文件发送失败，请重试');
    } finally {
        // 6. 关闭加载动画
        loadingInstance.close();
    }
}

// 发送消息的处理函数
function handleSendMessage() {
    if (!inputMessage.value.trim() || !chatStore.activeContactId) {
        ElMessage.error('请输入消息内容');
        return;
    }

    const messagePayload = {
        type: 'PRIVATE_CHAT',
        receiverId: chatStore.activeContactId,
        messageType: 'TEXT',
        content: inputMessage.value
    };

    // 调用 WebSocket 服务发送消息
    wsSendMessage(messagePayload);

    // 清空输入框
    inputMessage.value = '';
}

function onSelectEmoji(emoji: any) {
    console.log(emoji);
    // 将表情插入到输入框的当前光标位置
    // (一个简化的实现是直接追加到末尾)
    inputMessage.value += emoji.i;
    if (emojiPopoverRef.value) {
        emojiPopoverRef.value.hide();
    }
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
    align-items: flex-start; /* 头像和气泡顶部对齐 */
}

/* 对方的消息，整体靠左 */
.message-item.other {
    justify-content: flex-start;
}

.message-item.other :deep(.message-content) {
    background-color: #ffffff;
}

/* 自己的消息，整体靠右 */
.message-item.self {
    justify-content: flex-end;
}

.message-item.self :deep(.message-content) {
    background-color: #95ec69;
}

.message-avatar {
    /* 给头像一些外边距，把它和气泡隔开 */
    margin: 0 10px;
}
.input-area {
    border-top: 1px solid #e0e0e0;
    background-color: #ffffff;
}
/* 深度选择器，修改 el-input 内部样式 */
:deep(.input-area ) {
    border: none;
    box-shadow: none;
    padding: 10px;
}
.send-button-wrapper {
    text-align: right;
    padding: 0 10px 10px;
}
.toolbar {
    padding: 5px 10px;
    border-bottom: 1px solid #e0e0e0;
}
.icon-wrapper {
    /* 让 div 表现得像一个行内块元素，可以设置大小和边距 */
    display: inline-flex;
    /* 垂直居中内部的 icon */
    align-items: center;
    /* 继承父级的光标样式 */
    cursor: pointer;
}
.toolbar-icon {
    font-size: 20px;
    margin: 0 5px; /* 调整一下间距 */
    color: #606266;
}
.toolbar-icon:hover {
    color: var(--el-color-primary);
}
.message-file a {
    text-decoration: none;
    color: inherit;
}
</style>

<style>
.el-popover.el-popper.emoji-popover {
    padding: 0;
}
</style>
