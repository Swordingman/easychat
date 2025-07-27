<template>
    <div class="sidebar-container">
        <div class="user-avatar">
            <el-avatar :size="36" :src="userStore.userInfo?.avatar" />
        </div>
        <div class="nav-icons">
            <div class="nav-icon active">
                <el-icon><ChatDotRound /></el-icon>
            </div>
            <div class="nav-icon">
                <el-icon><User /></el-icon>
            </div>
        </div>
        <div class="settings-icon">
            <el-popover
                placement="right-start"
                :width="200"
                trigger="click">
                <template #reference>
                    <el-icon><Tools /></el-icon>
                </template>
                <div class="settings-menu">
                    <div class="menu-item" @click="handleLogout">
                        <el-icon><SwitchButton /></el-icon>
                        <span>退出登录</span>
                    </div>
                    <div class="menu-item">
                        <el-icon><Setting /></el-icon>
                        <span>设置</span>
                    </div>
                </div>
            </el-popover>
        </div>
    </div>
</template>

<script setup lang="ts">
import { useUserStore } from '../stores/user';
import { useChatStore } from '@renderer/stores/chat'
import { ElMessageBox } from 'element-plus';

const chatStore = useChatStore();
const userStore = useUserStore();

function handleLogout() {
    ElMessageBox.confirm(
        '确认退出登录吗？',
        '退出登录',
        {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        }
    ).then(() => {
        userStore.logout();
        chatStore.$reset();
        ElMessage.success('退出登录成功');
    }).catch(() => {})
}
</script>

<style scoped>
.sidebar-container {
    width: 50px;
    height: 100%;
    background-color: #f7f7f7;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 20px 0;
    box-sizing: border-box;
    border-right: 1px solid #e0e0e0;
}
.user-avatar {
    margin-bottom: 30px;
}
.nav-icons {
    flex-grow: 1;
}
.nav-icon {
    font-size: 24px;
    color: #606266;
    margin-bottom: 25px;
    cursor: pointer;
}
.nav-icon.active, .nav-icon:hover {
    color: #3370ff;
}
.settings-icon {
    font-size: 24px;
    color: #606266;
    cursor: pointer;
}

.settings-menu {
    display: flex;
    flex-direction: column;
}
.menu-item {
    display: flex;
    align-items: center;
    padding: 8px 12px;
    cursor: pointer;
    font-size: 14px;
}
.menu-item:hover {
    background-color: #f5f5f5;
}
.menu-item .el-icon {
    margin-right: 8px;
}
</style>
