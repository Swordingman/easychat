<template>
    <el-dialog v-model="dialogVisible" title="添加好友" width="400px">
        <!-- 搜索区域 -->
        <div class="search-section">
            <el-input
                v-model="searchQuery"
                placeholder="搜索 EasyChat号 / 昵称"
                clearable
                @keyup.enter="handleSearch"
            />
            <el-button type="primary" @click="handleSearch" :loading="isSearching">搜索</el-button>
        </div>

        <!-- 搜索结果区域 -->
        <el-scrollbar class="result-list" v-if="searchResults.length > 0">
            <div v-for="user in searchResults" :key="user.id" class="result-item">
                <el-avatar :src="user.avatar" />
                <div class="user-info">
                    <div class="nickname">{{ user.nickname }}</div>
                    <div class="easychat-id">账号: {{ user.easychatId }}</div>
                </div>
                <el-button type="success" size="small" @click="handleAddContact(user.id)">添加</el-button>
            </div>
        </el-scrollbar>

        <!-- 空状态或提示信息 -->
        <el-empty v-else :description="emptyText" />

    </el-dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import apiClient from '../services/api';

// 定义 User 的接口
interface UserSearchResult {
    id: number;
    easychatId: string;
    nickname: string;
    avatar: string;
}

const dialogVisible = ref(false);
const isSearching = ref(false);
const searchQuery = ref('');
const searchResults = ref<UserSearchResult[]>([]);
const emptyText = ref('输入关键词搜索用户');

// 搜索按钮点击事件
const handleSearch = async () => {
    if (!searchQuery.value.trim()) {
        ElMessage.warning('请输入搜索关键词');
        return;
    }
    isSearching.value = true;
    try {
        const response = await apiClient.get('/api/user/search', {
            params: { query: searchQuery.value }
        });
        searchResults.value = response.data;
        emptyText.value = searchResults.value.length === 0 ? '未找到相关用户' : '';
    } catch (error) {
        console.error('搜索用户失败:', error);
        ElMessage.error('搜索失败，请重试');
    } finally {
        isSearching.value = false;
    }
};

// 添加好友按钮点击事件
const handleAddContact = async (friendId: number) => {
    try {
        // 注意：后端的 /api/contact/add 接口设计的是接收 friendId 作为 RequestParam
        const response = await apiClient.post('/api/contact/request', null, {
            params: { receiverId: friendId }
        });
        ElMessage.success('好友添加成功！');
        // TODO: 添加成功后，应该刷新左侧的联系人列表
    } catch (error: any) {
        console.error('添加好友失败:', error);
        ElMessage.error(error.response?.data || '添加好友失败，请重试');
    }
};

// 暴露 open 方法给父组件
defineExpose({
    open: () => {
        // 重置状态
        searchQuery.value = '';
        searchResults.value = [];
        emptyText.value = '输入关键词搜索用户';
        dialogVisible.value = true;
    }
});
</script>

<style scoped>
.search-section {
    display: flex;
    gap: 10px; /* 元素之间的间距 */
    margin-bottom: 20px;
}
.result-list {
    height: 200px; /* 给结果列表一个固定的高度 */
}
.result-item {
    display: flex;
    align-items: center;
    padding: 8px 0;
}
.user-info {
    flex-grow: 1;
    margin-left: 12px;
}
.nickname {
    font-size: 14px;
}
.easychat-id {
    font-size: 12px;
    color: #999;
}
</style>
