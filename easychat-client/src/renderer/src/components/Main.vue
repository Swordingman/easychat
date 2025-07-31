<template>
    <div class="common-layout">
        <el-container class="main-container">
            <el-aside width="50px">
                <SideBar />
            </el-aside>

            <!-- 中间的主面板 -->
            <el-aside width="250px">
                <!-- 根据 chatStore.activeView 动态切换组件 -->
                <SessionList v-if="chatStore.activeView === 'chat'" />
                <AddressBook v-else-if="chatStore.activeView === 'contacts'" />
            </el-aside>

            <el-main>
                <ChatWindow />
            </el-main>
        </el-container>
    </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import SideBar from './SideBar.vue';
import SessionList from './SessionList.vue';
import ChatWindow from './ChatWindow.vue';
import { useChatStore } from '../stores/chat';
import AddressBook from './AddressBook.vue'

const chatStore = useChatStore();

// 在组件挂载后，立即获取联系人列表
onMounted(() => {
    chatStore.fetchContacts();
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
