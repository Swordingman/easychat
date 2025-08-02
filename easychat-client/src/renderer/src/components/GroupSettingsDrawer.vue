<template>
    <el-drawer v-model="drawerVisible" title="群聊信息" @close="handleClose">

        <!-- 群头像、名称区域 -->
        <div class="group-info-section">
            <el-upload
                class="group-avatar-uploader"
                action="https://api.chirpchump.xyz/api/file/upload"
                :headers="uploadHeaders"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                :before-upload="beforeAvatarUpload"
                :disabled="!isOwner"
            >
                <el-avatar :size="60" shape="square" :src="form.avatar" />
            </el-upload>

            <div class="group-details">
                <el-input v-if="isEditingName" v-model="form.groupName" ref="groupNameInputRef" @blur="isEditingName = false" />
                <div v-else class="group-name" @click="editGroupName">
                    <span>{{ form.groupName }}</span>
                    <el-icon v-if="isOwner"><Edit /></el-icon>
                </div>
            </div>
        </div>

        <!-- 群公告区域 -->
        <div class="announcement-section">
            <h4>群公告</h4>
            <el-input
                v-model="form.announcement"
                type="textarea"
                :rows="4"
                placeholder="群主很懒，什么都没有留下..."
                :readonly="!isOwner"
            />
        </div>

        <!-- 群成员列表区域 -->
        <div class="member-list-section">
            <p>群成员 ({{ members.length }}人)</p>
            <el-scrollbar max-height="400px">
                <div class="member-grid">
                    <!-- 【核心改造】我们将 v-for 循环的目标，从 div.member-item 改为 div.member-wrapper -->
                    <div v-for="member in members" :key="member.memberId" class="member-wrapper">
                        <div class="member-item">
                            <el-avatar :src="member.avatar" />
                            <span class="member-name">{{ member.nickname }}</span>
                        </div>
                        <!-- 踢人按钮，现在是 member-item 的兄弟，而不是儿子 -->
                        <el-button
                            v-if="isOwner && member.memberId !== userStore.userInfo?.id"
                            type="danger"
                            :icon="Delete"
                            circle
                            size="small"
                            class="kick-btn"
                            @click="handleKickMember(member)"
                        />
                    </div>

                    <!-- 邀请按钮 (结构保持简单) -->
                    <div class="member-item add-member" @click="openInviteDialog">
                        <el-avatar :icon="Plus" />
                        <span class="member-name">邀请</span>
                    </div>
                </div>
            </el-scrollbar>
        </div>
    </el-drawer>

    <!-- 邀请好友的对话框 -->
    <InviteFriendDialog
        ref="inviteDialogRef"
        :group-id="currentGroupId"
        :existing-members="members"
        @invite-success="refreshMembers"
    />
</template>

<script setup lang="ts">
import { ref, computed, reactive, nextTick } from 'vue';
import { useUserStore } from '../stores/user';
import apiClient from '../services/api';
import {useChatStore, type GroupMember} from '../stores/chat';
import { ElMessage, ElMessageBox, type UploadProps } from 'element-plus'
import { Delete, Plus } from '@element-plus/icons-vue';
import InviteFriendDialog from './InviteFriendDialog.vue';

const drawerVisible = ref(false);
const members = ref<GroupMember[]>([]);
const currentGroupId = ref<number | null>(null);
const ownerId = ref<number | null>(null);
const chatStore = useChatStore();
const userStore = useUserStore();
const inviteDialogRef = ref<InstanceType<typeof InviteFriendDialog> | null>(null);
const isEditingName = ref(false);
const groupNameInputRef = ref();

const form = reactive({
    groupName: '',
    avatar: '',
    announcement: ''
});

// 计算属性，判断当前用户是否为群主
const isOwner = computed(() => userStore.userInfo?.id === ownerId.value);

// 打开抽屉时，获取群信息（包括群主ID）和成员列表
const open = async (groupId: number) => {
    drawerVisible.value = true;
    currentGroupId.value = groupId;
    const groupInfo = chatStore.groups.find(g => g.id === groupId);
    if (groupInfo) {
        ownerId.value = groupInfo.ownerId;
        form.groupName = groupInfo.groupName;
        form.avatar = groupInfo.avatar;
        form.announcement = groupInfo.announcement || '';
    }
    await refreshMembers();
};

const beforeAvatarUpload: UploadProps['beforeUpload'] = (rawFile) => {
    if (rawFile.type !== 'image/jpeg' && rawFile.type !== 'image/png') {
        ElMessage.error('头像只能是 JPG 或 PNG 格式!');
        return false;
    }
    if (rawFile.size / 1024 / 1024 > 2) {
        ElMessage.error('头像大小不能超过 2MB!');
        return false;
    }
    return true;
};

const editGroupName = () => {
    if (!isOwner.value) return;
    isEditingName.value = true;
    nextTick(() => {
        groupNameInputRef.value.focus();
    })
}

const uploadHeaders = computed(() => {
    return {
        Authorization: `Bearer ${userStore.token}`
    };
});

// 上传群头像成功后的回调
const handleAvatarSuccess = (response: any) => {
    form.avatar = response.url;
    ElMessage.success('头像上传成功');
    handleUpdateGroup();
};

const handleUpdateGroup = async () => {
    if (!isOwner.value || !currentGroupId.value) return;
    try {
        const updateData = {
            groupName: form.groupName,
            avatar: form.avatar,
            announcement: form.announcement
        };
        await apiClient.put(`/api/group/${currentGroupId.value}`, updateData);
        ElMessage.success('群信息更新成功！');
        // 更新成功后，必须刷新会话列表，让左侧的头像和名称也变过来
        await chatStore.fetchSessions();
    } catch (error: any) {
        ElMessage.error(error.response?.data || '更新失败');
    }
};

// 刷新成员列表的公共方法
const refreshMembers = async () => {
    if (!currentGroupId.value) return;
    try {
        const response = await apiClient.get<GroupMember[]>(`/api/group/${currentGroupId.value}/members`);
        members.value = response.data;
    } catch (error) {
        console.error("获取群成员失败:", error);
    }
}

// 处理踢人
const handleKickMember = async (member: GroupMember) => {
    await ElMessageBox.confirm(`确定要将成员 "${member.nickname}" 移出群聊吗？`, '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
    });
    try {
        await apiClient.post(`/api/group/${currentGroupId.value}/kick`, null, {
            params: { memberId: member.memberId }
        });
        ElMessage.success('已成功移出该成员');
        // 实时刷新成员列表
        await refreshMembers();
    } catch (error: any) {
        ElMessage.error(error.response?.data || '操作失败');
    }
};

// 打开邀请对话框
const openInviteDialog = () => {
    inviteDialogRef.value?.open();
};

// 关闭抽屉时清空数据，避免下次打开看到旧数据
const handleClose = () => {
    members.value = [];
    currentGroupId.value = null;
    ownerId.value = null;
}

defineExpose({ open });
</script>

<style scoped>
.group-info-section {
    display: flex;
    align-items: center;
    margin-bottom: 20px;
}
.group-avatar-uploader {
    margin-right: 15px;
}
.group-avatar-uploader:hover :deep(.el-avatar) {
    opacity: 0.7;
}
.group-details {
    font-size: 16px;
    font-weight: bold;
}
.group-name {
    display: flex;
    align-items: center;
    cursor: pointer;
}
.group-name .el-icon {
    margin-left: 5px;
    visibility: hidden; /* 默认隐藏编辑图标 */
}
.group-name:hover .el-icon {
    visibility: visible; /* 悬浮时显示 */
}
.announcement-section h4 {
    margin: 0 0 10px;
    font-size: 14px;
    color: #303133;
}
.save-btn {
    width: 100%;
    margin-top: 15px;
}
.member-list-section p {
    font-size: 14px;
    color: #606266;
    margin-bottom: 10px;
}
.member-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(60px, 1fr));
    gap: 20px 15px; /* 网格项之间的间距 */

    /* 【核心修正】在这里添加内边距！ */
    /* 这会在网格的 上、右、下 三个方向，留出 10px 的空间 */
    padding: 10px 10px 10px 0; /* 上 右 下 左 */
}
.member-wrapper {
    position: relative; /* 定位的“锚” */
    /* 让 wrapper 的尺寸和内部的 item 保持一致 */
    display: inline-flex;
    justify-content: center;
}
.member-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 5px;
}
.member-name {
    font-size: 12px;
    width: 60px;
    text-align: center;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}
.kick-btn {
    position: absolute;
    top: -8px;
    right: -8px;
}
.add-member {
    cursor: pointer;
}
.add-member .el-avatar {
    border: 1px dashed #c0c4cc;
    background-color: #f5f7fa;
    color: #909399;
}
</style>
