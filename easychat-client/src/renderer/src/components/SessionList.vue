<template>
    <div class="contact-list-container">
        <div class="search-bar">
            <el-input placeholder="搜索" :prefix-icon="Search" />
            <!-- 1. 新增的“添加好友”按钮 -->
            <el-button
                :icon="Plus"
                circle
                class="add-btn"
                @click="openAddContactDialog"
            />
        </div>
        <el-scrollbar class="contact-scroll">
            <div
                v-for="contact in chatStore.contacts"
                :key="contact.id"
                class="contact-item"
                :class="{ active: contact.id === chatStore.activeContactId }"
                @click="handleSelectContact(contact.id)"
            >
                <el-avatar :size="36" :src="contact.avatar" />
                <div class="contact-info">
                    <div class="nickname">{{ contact.nickname }}</div>
                    <div class="last-message">{{ contact.lastMessage }}</div>
                </div>
            </div>
        </el-scrollbar>

        <AddContactDialog ref="addContactDialogRef" />
    </div>
</template>

<script setup lang="ts">
import { Plus, Search } from '@element-plus/icons-vue'
import { useChatStore } from '../stores/chat';
import AddContactDialog from './AddContactDialog.vue';
import { ref } from 'vue'

const chatStore = useChatStore();

const addContactDialogRef = ref<InstanceType<typeof AddContactDialog> | null>(null);

function openAddContactDialog() {
    addContactDialogRef.value?.open();
}

function handleSelectContact(contactId: number) {
    chatStore.setActiveContactId(contactId);
    chatStore.loadHistoryMessages(contactId);
}
</script>

<style scoped>
.contact-list-container {
    width: 250px;
    height: 100%;
    background: #fafafa;
    display: flex;
    flex-direction: column;
}
.search-bar {
    display: flex; /* 使用 flex 布局让输入框和按钮在一行 */
    align-items: center; /* 垂直居中 */
    padding: 10px;
    border-bottom: 1px solid #e0e0e0;
}
.add-btn {
    margin-left: 8px; /* 给按钮一点左边距 */
}
.contact-scroll {
    flex-grow: 1;
}
.contact-item {
    display: flex;
    align-items: center;
    padding: 10px;
    cursor: pointer;
}
.contact-item:hover {
    background-color: #f0f0f0;
}
.contact-item.active {
    background-color: #e0e0e0;
}
.contact-info {
    margin-left: 10px;
    flex-grow: 1;
    overflow: hidden;
}
.nickname {
    font-size: 14px;
}
.last-message {
    font-size: 12px;
    color: #999;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}
</style>
