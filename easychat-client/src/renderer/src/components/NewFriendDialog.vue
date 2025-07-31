<template>
    <el-dialog v-model="dialogVisible" title="新的朋友" width="500px">
        <el-scrollbar class="request-list">
            <div v-if="requests.length === 0">
                <el-empty description="暂无新的好友请求" />
            </div>
            <div v-for="req in requests" :key="req.requestId" class="request-item">
                <!-- 这里将显示请求者的信息 -->
                <el-avatar :src="req.requesterInfo.avatar" />
                <div class="info">
                    <div class="nickname">{{ req.requesterInfo.nickname }}</div>
                    <div class="easychat-id">账号: {{ req.requesterInfo.easychatId }}</div>
                </div>
                <!-- 同意/拒绝按钮 -->
                <div class="actions">
                    <el-button type="success" size="small" @click="handleAction(req.requestId, 'accept')">同意</el-button>
                    <el-button type="danger" size="small" @click="handleAction(req.requestId, 'reject')">拒绝</el-button>
                </div>
            </div>
        </el-scrollbar>
    </el-dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import apiClient from '../services/api';
import { ElMessage } from 'element-plus';
import { useChatStore } from '../stores/chat.ts'

interface RequesterInfo {
    id: number;
    easychatId: string;
    nickname: string;
    avatar: string;
}

interface FriendRequest {
    requestId: number;
    requesterInfo: RequesterInfo;
}

const dialogVisible = ref(false);
const requests = ref<FriendRequest[]>([]); // 暂时用 any[]，后面定义接口

// 打开对话框时，获取好友请求列表
const open = async () => {
    dialogVisible.value = true;
    await chatStore.fetchPendingRequests();
    try {
        const response = await apiClient.get<FriendRequest[]>('/api/contact/requests');
        requests.value = response.data;
        console.log("获取到的好友请求：", requests.value);
    } catch (error) {
        console.error('获取好友请求失败:', error);
    }
};

const chatStore = useChatStore();

// 处理同意/拒绝
const handleAction = async (requestId: number, action: 'accept' | 'reject') => {
    try {
        await apiClient.post('/api/contact/action', null, {
            params: { requestId, action }
        });
        ElMessage.success(action === 'accept' ? '已同意好友请求' : '已拒绝好友请求');
        // 从列表中移除已处理的请求
        requests.value = requests.value.filter(req => req.requestId !== requestId);
        if (action === 'accept') {
            await chatStore.fetchContacts();
        }
        await chatStore.fetchPendingRequests();
    } catch (error: any) {
        console.error('处理好友请求失败:', error);
        ElMessage.error(error.response?.data || '操作失败');
    }
};

defineExpose({ open });
</script>

<style scoped>
.request-list {
    max-height: 400px;
}
.request-item {
    display: flex;
    align-items: center;
    padding: 10px 0;
    border-bottom: 1px solid #f0f0f0;
}
.info {
    flex-grow: 1;
    margin-left: 12px;
}
.nickname { font-size: 14px; }
.easychat-id { font-size: 12px; color: #999; }
</style>
