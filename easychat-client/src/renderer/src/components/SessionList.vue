<template>
    <div class="session-list-container">
        <div class="search-bar">
            <el-input placeholder="搜索" :prefix-icon="Search" />
        </div>
        <el-scrollbar class="session-scroll">
            <div v-if="sortedSessions.length === 0">
                <el-empty description="暂无会话" :image-size="80" />
            </div>
            <div
                v-for="session in sortedSessions"
                :key="session.id"
                class="session-item"
                :class="{ active: session.id === chatStore.activeSessionId }"
                @click="handleSelectSession(session)"
            >
                <el-badge
                    :value="chatStore.unreadCounts[session.id]"
                    :max="99"
                    :hidden="!chatStore.unreadCounts[session.id] || chatStore.unreadCounts[session.id] === 0"
                    class="unread-badge"
                >
                    <el-avatar :size="40" :src="session.avatar" :shape="session.type === 'GROUP' ? 'square' : 'circle'" />
                </el-badge>

                <div class="session-info">
                    <div class="nickname">{{ session.name }}</div>
                    <div class="last-message">
                        {{ session.formattedLastMessage || '暂无消息' }}
                    </div>
                </div>
            </div>
        </el-scrollbar>
    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { useChatStore, type Session } from '../stores/chat'

const chatStore = useChatStore()

// 创建一个计算属性，用于对会话列表进行实时排序
const sortedSessions = computed(() => {
    return [...chatStore.sessions].sort((a, b) => {
        const lastMsgA = chatStore.getLastMessage(a.id);
        const lastMsgB = chatStore.getLastMessage(b.id);

        const timeA = lastMsgA ? new Date(lastMsgA.createTime).getTime() : (a.lastMessageTime ? new Date(a.lastMessageTime).getTime() : 0);
        const timeB = lastMsgB ? new Date(lastMsgB.createTime).getTime() : (b.lastMessageTime ? new Date(b.lastMessageTime).getTime() : 0);

        return timeB - timeA;
    });
});


function handleSelectSession(session: Session) {
    chatStore.setActiveSessionId(session.id)
    // 加载历史消息的职责，已经移交给 ChatWindow.vue 的 watch 监听器
}
</script>

<style scoped>
.session-list-container {
    width: 249px;
    height: 100%;
    background: #f0f2f5;
    display: flex;
    flex-direction: column;
    border-right: 1px solid #dcdfe6;
}
.search-bar {
    padding: 10px;
    flex-shrink: 0;
}
.session-scroll {
    flex-grow: 1;
}
.session-item {
    display: flex;
    align-items: center;
    padding: 12px;
    cursor: pointer;
    transition: background-color 0.2s;
}
.session-item:hover {
    background-color: #e4e7ed;
}
.session-item.active {
    background-color: #c8c9cc;
}
.session-info {
    margin-left: 12px;
    flex-grow: 1;
    overflow: hidden;
}
.nickname {
    font-size: 14px;
    color: #303133;
    white-space: nowrap;
}
.last-message {
    font-size: 12px;
    color: #909399;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}
:deep(.unread-badge .el-badge__content) {
    top: 4px;
    right: 8px;
}
</style>
