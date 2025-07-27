<template>
    <div class="contact-list-container">
        <div class="search-bar">
            <el-input placeholder="搜索" :prefix-icon="Search" />
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
    </div>
</template>

<script setup lang="ts">
import { Search } from '@element-plus/icons-vue'
import { useChatStore } from '../stores/chat';

const chatStore = useChatStore();

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
    padding: 10px;
    border-bottom: 1px solid #e0e0e0;
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
