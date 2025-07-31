<template>
    <div class="address-book-container">
        <!-- 1. 功能栏 -->
        <div class="feature-list">
            <div class="feature-item" @click="openAddContactDialog"> <!-- 添加好友对话框 -->
                <el-icon><Plus /></el-icon>
                <span>添加好友</span>
            </div>
            <div class="feature-item" @click="openNewFriendDialog"> <!-- 新的朋友对话框 -->
                <el-icon><CirclePlus /></el-icon>
                <span>新的朋友</span>
                <el-badge :value="chatStore.pendingRequestsCount" v-if="chatStore.pendingRequestsCount > 0" :max="99" class="count-badge" />
            </div>
        </div>

        <!-- 2. 好友列表 -->
        <el-scrollbar class="friend-list">
            <!-- 这里将显示按字母排序的好友 -->
            <div
                v-for="contact in chatStore.contacts"
                :key="contact.id"
                class="friend-item"
                :class="{ active: contact.id === chatStore.activeContactId }"
                @click="handleSelectContact(contact.id)"
            >
                <el-avatar :src="contact.avatar" />
                <span class="nickname">{{ contact.nickname }}</span>
            </div>
        </el-scrollbar>

        <AddContactDialog ref="addContactDialogRef" />
        <NewFriendDialog ref="newFriendDialogRef" />
    </div>
</template>

<script setup lang="ts">
import { useChatStore } from '../stores/chat';
import NewFriendDialog from './NewFriendDialog.vue';
import { ref } from 'vue'
import AddContactDialog from './AddContactDialog.vue'

const chatStore = useChatStore();
const newFriendDialogRef = ref<InstanceType<typeof NewFriendDialog> | null>(null);
const addContactDialogRef = ref<InstanceType<typeof AddContactDialog> | null>(null);

function openAddContactDialog() {
    addContactDialogRef.value?.open();
}

function openNewFriendDialog() {
    newFriendDialogRef.value?.open();
}

function handleSelectContact(contactId: number) {
    chatStore.setActiveContactId(contactId);
    chatStore.loadHistoryMessages(contactId);
}
</script>

<style scoped>
.address-book-container {
    width: 250px;
    height: 100%;
    background: #fafafa;
    display: flex;
    flex-direction: column;
}
.feature-list {
    padding: 10px;
    border-bottom: 1px solid #e0e0e0;
}
.feature-item {
    display: flex;
    align-items: center;
    padding: 8px 0;
    cursor: pointer;
    position: relative;
}
.feature-item:hover {
    background-color: #f0f0f0;
}
.feature-item .el-icon {
    font-size: 24px;
    margin-right: 10px;
}
.friend-list {
    flex-grow: 1;
}
.friend-item {
    display: flex;
    align-items: center;
    padding: 10px;
    cursor: pointer;
}
.friend-item:hover {
    background-color: #f0f0f0;
}
.nickname {
    margin-left: 10px;
}
.count-badge {
    position: absolute;
    right: 10px; /* 定位到右侧 */
    top: 50%;
    transform: translateY(-50%);
}
</style>
