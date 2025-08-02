<template>
    <el-dialog v-model="dialogVisible" title="创建群聊" width="750px">
        <el-form :model="form" label-width="80px">
            <el-form-item label="群名称" required>
                <el-input v-model="form.groupName" placeholder="给你的群起个名字吧" />
            </el-form-item>
            <el-form-item label="选择成员" required>
                <!-- 使用 Element Plus 的穿梭框 Transfer 组件，体验更好 -->
                <el-transfer
                    v-model="form.memberIds"
                    :data="contactData"
                    :titles="['好友列表', '群聊成员']"
                    filterable
                    direction="horizontal"
                    :props="{ key: 'id', label: 'nickname' }"
                >
                    <template #default="{ option }">
                        <div class="transfer-item">
                            <el-avatar :size="28" :src="option.avatar" />
                            <span class="transfer-nickname">{{ option.nickname }}</span>
                        </div>
                    </template>
                </el-transfer>
            </el-form-item>
        </el-form>
        <template #footer>
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" @click="handleCreateGroup" :loading="isCreating">立即创建</el-button>
        </template>
    </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue';
import { useChatStore } from '../stores/chat';
import apiClient from '../services/api';
import { ElMessage } from 'element-plus';

const dialogVisible = ref(false);
const isCreating = ref(false);
const chatStore = useChatStore();

const form = reactive({
    groupName: '',
    memberIds: [] as number[]
});

// 将好友列表转换为穿梭框需要的数据格式
const contactData = computed(() =>
    chatStore.contacts.map(contact => ({
        id: contact.id,
        nickname: contact.nickname,
        avatar: contact.avatar // <-- 确保 avatar 字段被包含
    }))
);

const handleCreateGroup = async () => {
    if (!form.groupName.trim()) {
        ElMessage.warning('请输入群名称');
        return;
    }
    if (form.memberIds.length === 0) {
        ElMessage.warning('请至少选择一位群成员');
        return;
    }
    isCreating.value = true;
    try {
        await apiClient.post('/api/group/create', {
            groupName: form.groupName,
            memberIds: form.memberIds
        });
        ElMessage.success('群聊创建成功！');
        dialogVisible.value = false;
        // 创建成功后，刷新会话列表，新的群聊就会出现在左侧
        await chatStore.fetchSessions();
    } catch (error: any) {
        ElMessage.error(error.response?.data || '创建失败');
    } finally {
        isCreating.value = false;
    }
};

defineExpose({
    open: () => {
        form.groupName = '';
        form.memberIds = [];
        dialogVisible.value = true;
    }
});
</script>

<style scoped>
/* 对话框表单的整体微调 */
.el-form {
    padding: 0 10px;
}

/* 1. 调整穿梭框整体尺寸 */
:deep(.el-transfer) {
    height: 280px;
    width: 100%;
    display: flex !important;
    flex-direction: row !important;
    gap: 5px
}

/* 2. 调整左右两个面板的布局和宽度 */
:deep(.el-transfer-panel) {
    width: 300px; /* 可以根据需要微调宽度 */
    height: 100%;
    display: flex;
    flex-direction: column;
}

/* 3. 让列表区域占满剩余空间并可以滚动 */
:deep(.el-transfer-panel__body) {
    flex-grow: 1;
    height: 100%;
}

:deep(.el-transfer__buttons) {
    display: flex !important;
    flex-direction: row !important;
    align-self: center;
}

/* 4. 【最最最关键的一步】 */
/* 重写每一行（也就是 el-checkbox）的布局方式！ */
:deep(.el-transfer-panel__item.el-checkbox) {
    /* a. 让 label 自身变成一个 flex 容器 */
    display: flex !important;
    /* b. 让内部元素（checkbox 和我们的内容）垂直居中 */
    align-items: center;
    /* c. 移除默认的高度限制，让它自适应内容 */
    height: auto;
    padding: 6px 15px;
    line-height: normal;
}

/* 5. 确保 checkbox 的标签部分（我们的插槽）能填满剩余空间 */
:deep(.el-transfer-panel__item .el-checkbox__label) {
    flex-grow: 1;
    font-size: 14px;
}

/* 6. 我们自定义的插槽内部样式 */
.transfer-item {
    display: flex;
    align-items: center;
    /* 移除不必要的宽度，让它自然填充 */
}

.transfer-nickname {
    margin-left: 10px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    /* 给一个最大宽度，防止过长的昵称破坏布局 */
    max-width: 100px;
}
</style>
