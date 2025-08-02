<template>
    <div class="sidebar-container">
        <div class="user-avatar" @click="openProfileDialog">
            <el-avatar :size="36" :src="userStore.userInfo?.avatar" />
        </div>
        <div class="nav-icons">
            <!-- 聊天图标 -->
            <el-badge :is-dot="chatStore.totalUnreadCount > 0" class="nav-badge">
                <div
                    class="nav-icon"
                    :class="{ active: chatStore.activeView === 'chat' }"
                    @click="chatStore.setActiveView('chat')"
                >
                    <el-tooltip content="聊天" placement="right">
                        <el-icon><ChatDotRound /></el-icon>
                    </el-tooltip>
                </div>
            </el-badge>
            <!-- 通讯录图标 -->
            <el-badge :is-dot="chatStore.pendingRequestsCount > 0" class="nav-badge">
                <div
                    class="nav-icon"
                    :class="{ active: chatStore.activeView === 'contacts' }"
                    @click="chatStore.setActiveView('contacts')"
                >
                    <el-tooltip content="通讯录" placement="right">
                        <el-icon><User /></el-icon>
                    </el-tooltip>
                </div>
            </el-badge>
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
        <UserProfileDialog
            ref="profileDialogRef"
        />
    </div>
</template>

<script setup lang="ts">
import { useUserStore } from '../stores/user';
import { useChatStore } from '../stores/chat'
import { ElMessageBox, ElMessage } from 'element-plus';
import { ref } from 'vue';
import UserProfileDialog from './UserProfileDialog.vue';

const chatStore = useChatStore();
const userStore = useUserStore();
const profileDialogRef = ref<InstanceType<typeof UserProfileDialog> | null>(null);

function openProfileDialog() {
    profileDialogRef.value?.open();
}

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

.user-avatar {
    cursor: pointer; /* 增加手型指针，提示用户可以点击 */
    transition: transform 0.2s;
}
.user-avatar:hover {
    transform: scale(1.1);
}
.nav-badge {
    display: block; /* 或者其他合适的样式 */
}
</style>
