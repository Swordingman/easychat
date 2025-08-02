<template>
    <el-dialog v-model="dialogVisible" title="邀请好友加入群聊" width="600px">
        <!-- 使用穿梭框来选择要邀请的好友 -->
        <el-transfer
            v-model="selectedFriendIds"
            :data="availableFriends"
            :titles="['好友列表', '邀请列表']"
            filterable
            direction="horizontal"
            :props="{ key: 'id', label: 'nickname' }"
        >
            <!-- 自定义渲染，显示头像 -->
            <template #default="{ option }">
                <div class="transfer-item">
                    <el-avatar :size="28" :src="option.avatar" />
                    <span class="transfer-nickname">{{ option.nickname }}</span>
                </div>
            </template>
        </el-transfer>

        <template #footer>
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" @click="handleInvite" :loading="isInviting">确认邀请</el-button>
        </template>
    </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useChatStore } from '../stores/chat';
import apiClient from '../services/api';
import { ElMessage } from 'element-plus';
import type { GroupMember } from '../stores/chat';

const dialogVisible = ref(false);
const isInviting = ref(false);
const chatStore = useChatStore();

// 从父组件接收必要的信息
const props = defineProps<{
    groupId: number | null;
    existingMembers: GroupMember[];
}>();

const selectedFriendIds = ref<number[]>([]);

// 【核心逻辑】计算出可以被邀请的好友列表
// 逻辑：从我的所有好友中，排除掉那些已经是群成员的人
const availableFriends = computed(() => {
    const existingMemberIds = new Set(props.existingMembers.map(m => m.memberId));
    return chatStore.contacts
        .filter(contact => !existingMemberIds.has(contact.id))
        .map(contact => ({
            id: contact.id,
            nickname: contact.nickname,
            avatar: contact.avatar
        }));
});

const handleInvite = async () => {
    if (!props.groupId || selectedFriendIds.value.length === 0) {
        ElMessage.warning('请选择要邀请的好友');
        return;
    }
    isInviting.value = true;
    try {
        await apiClient.post(`/api/group/${props.groupId}/invite`, selectedFriendIds.value);
        ElMessage.success('邀请已发送！');
        dialogVisible.value = false;
        // TODO: 邀请成功后，应该通知 GroupSettingsDrawer 刷新成员列表
    } catch (error: any) {
        ElMessage.error(error.response?.data || '邀请失败');
    } finally {
        isInviting.value = false;
    }
};

defineExpose({
    open: () => {
        selectedFriendIds.value = []; // 每次打开都清空
        dialogVisible.value = true;
    }
});
</script>

<style scoped>

</style>
