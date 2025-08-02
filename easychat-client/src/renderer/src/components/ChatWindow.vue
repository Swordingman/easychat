<template>
    <div class="chat-window-container">
        <!-- 未选中会话时的占位符 -->
        <div v-if="!activeSession" class="placeholder">
            <el-icon><Promotion /></el-icon>
            <span>开启你的 EasyChat 之旅</span>
        </div>

        <!-- 聊天窗口 -->
        <template v-else>
            <!-- 头部 -->
            <div class="chat-header">
                <span class="header-name">{{ activeSession.name }}</span>
                <el-icon v-if="activeSession.type === 'GROUP'" class="header-icon" @click="openGroupSettings">
                    <MoreFilled />
                </el-icon>
            </div>

            <!-- 消息区域 -->
            <div class="message-area" ref="messageAreaRef">
                <el-scrollbar>
                    <div class="message-list">
                        <div v-for="message in activeMessages" :key="message.id || message.tempId" class="message-item-wrapper">
                            <!-- 对方的消息 -->
                            <div v-if="message.senderId !== userStore.userInfo?.id" class="message-item other">
                                <el-avatar :src="getSenderAvatar(message.senderId)" class="message-avatar" />
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
                    <el-popover
                        ref="emojiPopoverRef"
                        placement="top-start"
                        :width="400"
                        trigger="click"
                        popper-class="emoji-popover"
                    >
                        <template #reference>
                            <div class="icon-wrapper">
                                <el-icon class="toolbar-icon"><Sugar /></el-icon>
                            </div>
                        </template>
                        <EmojiPicker :native="true" @select="onSelectEmoji" style="width: 100%;" />
                    </el-popover>
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
                    @keydown.enter.prevent="handleSendTextMessage"
                />
                <div class="send-button-wrapper">
                    <el-button type="primary" @click="handleSendTextMessage" :disabled="!inputMessage.trim()">发送(S)</el-button>
                </div>
            </div>
        </template>

        <input type="file" ref="fileInputRef" style="display: none" @change="onFileSelected" />
    </div>

    <GroupSettingsDrawer ref="groupSettingsDrawerRef" />
</template>

<script setup lang="ts">
import { ref, watch, nextTick, computed } from 'vue'
import { useChatStore, type Message } from '../stores/chat'
import { useUserStore } from '../stores/user'
import { sendMessage as wsSendMessage } from '../services/websocket'
import apiClient from '../services/api'
import { ElMessage, ElLoading } from 'element-plus'
import MessageBubble from './MessageBubble.vue'
import EmojiPicker from 'vue3-emoji-picker'
import 'vue3-emoji-picker/css'
import { MoreFilled } from '@element-plus/icons-vue'
import GroupSettingsDrawer from './GroupSettingsDrawer.vue'

const chatStore = useChatStore()
const userStore = useUserStore()

// --- Refs ---
const inputMessage = ref('')
const fileInputRef = ref<HTMLInputElement | null>(null)
const emojiPopoverRef = ref()
const messageAreaRef = ref<HTMLElement | null>(null)
const groupSettingsDrawerRef = ref<InstanceType<typeof GroupSettingsDrawer> | null>(null)

// --- Computed Properties ---
const activeSession = computed(() => chatStore.activeSession)
const activeMessages = computed(() => chatStore.activeMessages)

// --- Watchers ---
// 监听激活会话的变化，加载历史消息
watch(
    () => chatStore.activeSessionId,
    (newSessionId) => {
        if (newSessionId) {
            const session = chatStore.activeSession;
            if (session) {
                if (session.type === 'PRIVATE') {
                    chatStore.loadHistoryMessages(session.targetId, session.type);
                } else if (session.type === 'GROUP') {
                    // 【核心修改】当进入群聊时，同时加载历史消息和群成员
                    chatStore.loadHistoryMessages(session.targetId, session.type);
                    chatStore.fetchGroupMembers(session.targetId);
                }
            }
        }
    },
    { immediate: true }
);

// 监听消息列表变化，自动滚动到底部
watch(
    activeMessages,
    () => {
        nextTick(() => {
            const scrollContainer = messageAreaRef.value?.querySelector('.el-scrollbar__wrap')
            if (scrollContainer) {
                scrollContainer.scrollTop = scrollContainer.scrollHeight
            }
        })
    },
    { deep: true }
)

// --- Methods ---
// (群聊适配) 获取消息发送者的头像
function getSenderAvatar(senderId: number): string {
    if (!activeSession.value) return '';

    if (activeSession.value.type === 'PRIVATE') {
        return activeSession.value.avatar;
    }

    // 如果是群聊
    const members = chatStore.groupMembers[activeSession.value.targetId];
    if (members) {
        const member = members.find(m => m.memberId === senderId);
        return member ? member.avatar : ''; // 找到成员头像就返回，找不到返回空
    }

    return ''; // 如果成员列表还没加载好，也返回空
}

// 发送文本消息
function handleSendTextMessage() {
    if (!inputMessage.value.trim() || !activeSession.value) return;

    const session = activeSession.value;

    // 【核心修正】确保 payload 包含所有必需字段，并正确处理 null
    const messagePayload = {
        type: session.type === 'PRIVATE' ? 'PRIVATE_CHAT' : 'GROUP_CHAT',
        receiverId: session.type === 'PRIVATE' ? session.targetId : null,
        receiverGroupId: session.type === 'GROUP' ? session.targetId : null,
        messageType: 'TEXT',
        content: inputMessage.value,
        chatType: 'PRIVATE'
    };

    wsSendMessage(messagePayload);
    inputMessage.value = '';
}

// 选择表情
function onSelectEmoji(emoji: any) {
    inputMessage.value += emoji.i;
    if (emojiPopoverRef.value) {
        emojiPopoverRef.value.hide();
    }
}

// 选择文件
function handleSelectFile() {
    fileInputRef.value?.click();
}

// 文件被选择后的处理
async function onFileSelected(event: Event) {
    if (!activeSession.value) return;
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;
    const file = input.files[0];
    if (fileInputRef.value) fileInputRef.value.value = '';

    const loadingInstance = ElLoading.service({ text: '文件上传中...' });
    try {
        const formData = new FormData();
        formData.append('file', file);
        const response = await apiClient.post('/api/file/upload', formData, {
            headers: { 'Content-Type': 'multipart/form-data' },
        });

        const fileUrl = response.data.url;
        let messageType: Message['messageType'] = 'FILE';
        if (file.type.startsWith('image/')) messageType = 'IMAGE';
        else if (file.type.startsWith('video/')) messageType = 'VIDEO';

        const messageContent = JSON.stringify({
            url: fileUrl,
            name: file.name,
            size: file.size,
        });

        const session = activeSession.value;
        const messagePayload = {
            type: session.type === 'PRIVATE' ? 'PRIVATE_CHAT' : 'GROUP_CHAT',
            receiverId: session.type === 'PRIVATE' ? session.targetId : null,
            receiverGroupId: session.type === 'GROUP' ? session.targetId : null,
            messageType: messageType, // messageType 是之前根据文件类型判断得出的
            content: messageContent,
            chatType: session.type
        };
        wsSendMessage(messagePayload);

    } catch (error) {
        console.error('文件上传或发送失败:', error);
        ElMessage.error('文件发送失败，请重试');
    } finally {
        loadingInstance.close();
    }
}

//群组设置
function openGroupSettings() {
    const session = activeSession.value;
    if ( session && session.type === 'GROUP' ) {
        const groupSessionData = chatStore.groups.find(g => g.id === session.targetId);
        if (groupSessionData) {
            groupSettingsDrawerRef.value?.open(session.targetId);
        }
    }
}
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
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-shrink: 0;
    padding: 15px 20px;
    border-bottom: 1px solid #e0e0e0;
    font-size: 16px;
    background-color: #f5f5f5;
}
.header-icon {
    cursor: pointer;
    font-size: 18px;
}
.message-area {
    flex-grow: 1;
    overflow-y: hidden;
}
.message-list {
    padding: 20px;
}
.message-item-wrapper {
    margin-bottom: 15px;
}
.message-item {
    display: flex;
    align-items: flex-start;
}
.message-item.other {
    justify-content: flex-start;
}
.message-item.self {
    justify-content: flex-end;
}
.message-avatar {
    margin: 0 10px;
}
/* 使用深度选择器为不同来源的消息气泡设置背景色 */
.message-item.other :deep(.message-content) {
    background-color: #ffffff;
}
.message-item.self :deep(.message-content) {
    background-color: #95ec69;
}
.input-area {
    flex-shrink: 0;
    border-top: 1px solid #e0e0e0;
    background-color: #ffffff;
}
.toolbar {
    padding: 5px 15px;
    display: flex;
    align-items: center;
}
.icon-wrapper {
    display: inline-flex;
    align-items: center;
    cursor: pointer;
    padding: 5px;
}
.toolbar-icon {
    font-size: 20px;
    color: #606266;
}
.toolbar-icon:hover {
    color: var(--el-color-primary);
}
:deep(.input-area .el-textarea__inner) {
    border: none;
    box-shadow: none;
    padding: 0 20px 10px;
    background-color: transparent;
}
.send-button-wrapper {
    text-align: right;
    padding: 0 20px 10px;
}
</style>

<style>
.el-popover.el-popper.emoji-popover {
    padding: 0;
}
</style>
