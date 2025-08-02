<template>
    <div class="address-book-container">
        <!-- 功能栏 -->
        <div class="feature-list">
            <div class="feature-item" @click="openAddContactDialog">
                <el-icon><Plus /></el-icon>
                <span>添加好友</span>
            </div>
            <div class="feature-item" @click="openNewFriendDialog">
                <el-icon><CirclePlus /></el-icon>
                <span>新的朋友</span>
                <el-badge
                    :value="chatStore.pendingRequestsCount"
                    v-if="chatStore.pendingRequestsCount > 0"
                    :max="99"
                    class="count-badge"
                />
            </div>
            <div class="feature-item" @click="openCreateGroupDialog">
                <el-icon><UserFilled /></el-icon>
                <span>群聊</span>
            </div>
        </div>

        <!-- 好友列表 -->
        <el-scrollbar class="friend-list">
            <div class="list-title">好友</div>
            <div
                v-for="contact in chatStore.contacts"
                :key="contact.id"
                class="friend-item"
                @click="handleSelectFriend(contact)"
            >
                <el-avatar :src="contact.avatar" :size="36" />
                <span class="nickname">{{ contact.nickname }}</span>

                <el-dropdown class="more-actions" trigger="click" @command="handleDropdownCommand" @click.stop>
                    <el-icon class="more-icon" @click.stop><MoreFilled /></el-icon>
                    <template #dropdown>
                        <el-dropdown-menu>
                            <!-- 2. 给 el-dropdown-item 设置一个 command 属性 -->
                            <!-- 我们把“要删除的好友对象”作为 command 的值 -->
                            <el-dropdown-item :command="{ action: 'delete', contact: contact }">
                                <el-icon color="red"><Delete /></el-icon>
                                <span style="color: red;">删除好友</span>
                            </el-dropdown-item>
                            <!-- 未来可以有其他选项 -->
                            <!-- <el-dropdown-item :command="{ action: 'blacklist', contact: contact }">拉黑</el-dropdown-item> -->
                        </el-dropdown-menu>
                    </template>
                </el-dropdown>
            </div>
        </el-scrollbar>

        <!-- 两个悬浮的对话框组件 -->
        <AddContactDialog ref="addContactDialogRef" />
        <NewFriendDialog ref="newFriendDialogRef" />
        <CreateGroupDialog ref="createGroupDialogRef" />
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useChatStore, type Contact } from '../stores/chat'
import NewFriendDialog from './NewFriendDialog.vue'
import AddContactDialog from './AddContactDialog.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import CreateGroupDialog from './CreateGroupDialog.vue'
import apiClient from '../services/api'

const chatStore = useChatStore()

// --- Refs for Dialogs ---
const newFriendDialogRef = ref<InstanceType<typeof NewFriendDialog> | null>(null)
const addContactDialogRef = ref<InstanceType<typeof AddContactDialog> | null>(null)
const createGroupDialogRef = ref<InstanceType<typeof CreateGroupDialog> | null>(null)

// --- Methods ---
function openAddContactDialog() {
    addContactDialogRef.value?.open()
}

function openNewFriendDialog() {
    // 打开“新的朋友”对话框时，可以顺便更新一下红点
    chatStore.fetchPendingRequests();
    newFriendDialogRef.value?.open()
}

const confirmDeleteContact = async (contact: Contact) => {
    try {
        await ElMessageBox.confirm(
            `确定要删除好友 "${contact.nickname}" 吗？`,
            '删除好友',
            { type: 'warning' }
        );

        await apiClient.delete('/api/contact', {
            params: { friendId: contact.id }
        });

        ElMessage.success('好友已删除');
        await chatStore.fetchSessions();

    } catch (error: any) {
        if (error !== 'cancel') {
            console.error('删除好友失败:', error);
            ElMessage.error(error.response?.data || '删除失败');
        }
    }
}

const handleDropdownCommand = (command: { action: string, contact: Contact }) => {
    // command 对象就是我们在 template 里绑定的那个 { action: '...', contact: ... }

    switch (command.action) {
        case 'delete':
            // 当 action 是 'delete' 时，调用删除逻辑
            confirmDeleteContact(command.contact);
            break;
        // case 'blacklist':
        //   // 处理拉黑逻辑...
        //   break;
    }
}

/**
 * 【核心】点击通讯录好友的处理逻辑
 * @param contact 被点击的好友对象
 */
function handleSelectFriend(contact: Contact) {
    // 1. 构造出这个好友对应的 session ID (格式: "PRIVATE-用户ID")
    const sessionId = `PRIVATE-${contact.id}`

    // 2. 设置激活的会话
    chatStore.setActiveSessionId(sessionId)

    // 3. 切换回“聊天”视图
    chatStore.setActiveView('chat')

    // 4. 加载历史消息 (这一步是必须的，因为可能是第一次和这个人聊天)
    chatStore.loadHistoryMessages(contact.id, 'PRIVATE')
}

function openCreateGroupDialog() {
    createGroupDialogRef.value?.open()
}
</script>

<style scoped>
.address-book-container {
    width: 250px;
    height: 100%;
    background: #f0f2f5;
    display: flex;
    flex-direction: column;
    border-right: 1px solid #dcdfe6;
}
.feature-list {
    padding: 8px 0;
    border-bottom: 1px solid #e0e0e0;
    flex-shrink: 0;
}
.feature-item {
    display: flex;
    align-items: center;
    padding: 8px 18px;
    cursor: pointer;
    position: relative;
    transition: background-color 0.2s;
}
.feature-item:hover {
    background-color: #e4e7ed;
}
.feature-item .el-icon {
    font-size: 20px;
    margin-right: 12px;
    color: #606266;
}
.friend-list {
    flex-grow: 1;
}
.list-title {
    font-size: 12px;
    color: #909399;
    padding: 8px 18px;
}
.friend-item {
    display: flex;
    align-items: center;
    padding: 8px 18px;
    cursor: pointer;
    transition: background-color 0.2s;
    position: relative;
}
.more-actions {
    position: absolute;
    right: 18px;
    top: 50%;
    transform: translateY(-50%);
    display: none; /* 默认隐藏 */
}

/* 当鼠标悬浮在整个好友条目上时，显示“更多”按钮 */
.friend-item:hover .more-actions {
    display: block;
}

.more-icon {
    cursor: pointer;
    padding: 5px;
    border-radius: 50%;
}
.more-icon:hover {
    background-color: #dcdfe6;
}
.friend-item:hover {
    background-color: #e4e7ed;
}
.nickname {
    margin-left: 12px;
    font-size: 14px;
}
.count-badge {
    position: absolute;
    right: 18px;
    top: 50%;
    transform: translateY(-50%);
}
</style>
