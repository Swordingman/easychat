<template>
    <div class="common-layout">
        <el-container class="main-container">
            <!-- 最左侧导航 -->
            <el-aside width="50px">
                <SideBar />
            </el-aside>
            <!-- 联系人/会话列表 -->
            <el-aside width="250px">
                <ContactList />
            </el-aside>
            <!-- 主聊天窗口 -->
            <el-main>
                <ChatWindow />
            </el-main>
        </el-container>
    </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import SideBar from './SideBar.vue';
import ContactList from './ContactList.vue';
import ChatWindow from './ChatWindow.vue';
import { useChatStore } from '../stores/chat';
import { useUserStore } from '../stores/user';
import apiClient from '../services/api';

const chatStore = useChatStore();
const userStore = useUserStore();

async function fetchContacts() {
    if (!userStore.userInfo) return;
    try {
        // 调用我们之前在后端写的获取联系人列表API
        const response = await apiClient.get('/api/contact/list', {
            params: {
                userId: userStore.userInfo.id
            }
        });
        // 将获取到的联系人列表存入 pinia
        chatStore.setContacts(response.data);
    } catch (error) {
        console.error('获取联系人列表失败:', error);
    }
}

// 在组件挂载后，立即获取联系人列表
onMounted(() => {
    fetchContacts();
});
</script>

<style scoped>
.main-container {
    height: 100vh; /* 占满整个视窗高度 */
}
.el-main {
    padding: 0; /* 移除 main区域的默认内边距 */
}
</style>
