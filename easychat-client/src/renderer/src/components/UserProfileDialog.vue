<template>
    <el-dialog
        v-model="dialogVisible"
        title="修改个人信息"
        width="400px"
        @close="handleClose"
    >
        <el-form :model="form" label-width="80px" ref="formRef">
            <el-form-item label="账号">
                <el-input :model-value="userStore.userInfo?.easychatId" readonly />
            </el-form-item>
            <el-form-item label="头像">
                <!--
                  action: 指定上传的服务器地址
                  :show-file-list="false": 不显示已上传文件列表
                  :on-success: 上传成功后的回调
                  :before-upload: 上传前的钩子，可以用来检查文件类型和大小
                -->
                <el-upload
                    class="avatar-uploader"
                    :action="uploadActionUrl"
                    :headers="uploadHeaders"
                    :show-file-list="false"
                    :on-success="handleAvatarSuccess"
                    :before-upload="beforeAvatarUpload"
                >
                    <el-avatar v-if="form.avatar" :src="form.avatar" :size="100" />
                    <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
                </el-upload>
            </el-form-item>

            <el-form-item label="昵称" prop="nickname">
                <el-input v-model="form.nickname" placeholder="请输入新的昵称" />
            </el-form-item>
        </el-form>

        <template #footer>
      <span class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="isSubmitting">
          保存
        </el-button>
      </span>
        </template>
    </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch, computed } from 'vue';
import { ElMessage, type UploadProps, type FormInstance } from 'element-plus';
import apiClient from '../services/api';
import { useUserStore } from '../stores/user';

const userStore = useUserStore();

// ---- 对话框控制 ----
const dialogVisible = ref(false);
const isSubmitting = ref(false);

// ---- 表单数据 ----
const form = reactive({
    nickname: '',
    avatar: '',
});
const formRef = ref<FormInstance>();

const uploadActionUrl = computed(() => {
    return `${apiClient.defaults.baseURL}/api/file/upload`;
});

// 头像上传成功回调
// UserProfileDialog.vue
const handleAvatarSuccess: UploadProps['onSuccess'] = (response) => {
    // response.url 就是完整的 https://... OSS URL
    form.avatar = response.url;
    ElMessage.success('头像上传成功!');
};

// 头像上传前校验
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

// 提交表单
const handleSubmit = async () => {
    if (!form.nickname.trim()) {
       ElMessage.error('昵称不能为空!');
       return;
    }

    if (!formRef.value) return;
    isSubmitting.value = true;
    try {
        const userId = userStore.userInfo?.id;
        if (!userId) {
            throw new Error('无法获取用户ID');
        }

        // 准备要提交的数据
        const updateData = {
            nickname: form.nickname,
            // 注意：传给后端的 avatar 路径不应该包含域名部分
            avatar: form.avatar
        };

        console.log("准备提交的数据 (updateData):", JSON.stringify(updateData, null, 2));
        console.log("目标用户ID (userId):", userId);
        console.log("请求的URL:", `/api/user/${userId}`);

        // 调用更新 API
        const response = await apiClient.put(`/api/user/${userId}`, updateData);

        // 更新 Pinia store 中的用户信息
        userStore.setLoginInfo({
            token: userStore.token!, // token 不变
            userInfo: response.data    // 使用后端返回的最新用户信息
        });

        ElMessage.success('用户信息更新成功！');
        dialogVisible.value = false; // 关闭对话框
    } catch (error) {
        console.error('更新用户信息失败:', error);
        ElMessage.error('更新失败，请重试');
    } finally {
        isSubmitting.value = false;
    }
};

// 关闭对话框时的清理工作（可选）
const handleClose = () => {
    formRef.value?.resetFields();
};

const uploadHeaders = computed(() => {
    return {
        Authorization: `Bearer ${userStore.token}`
    };
});

// ---- 暴露给父组件的方法 ----
// 定义一个 open 方法，让父组件可以调用来显示对话框
defineExpose({
    open: () => {
        // 1. 在显示对话框之前，使用 Pinia store 中最新的用户信息来填充表单
        if (userStore.userInfo) {
            form.nickname = userStore.userInfo.nickname;
            form.avatar = userStore.userInfo.avatar;
        } else {
            // 如果因为某些原因无法获取用户信息，可以给个默认值或错误提示
            ElMessage.error("无法获取当前用户信息！");
            return; // 中断打开流程
        }

        // 2. 显示对话框
        dialogVisible.value = true;
    }
});
</script>

<style scoped>
.avatar-uploader .el-upload {
    border: 1px dashed var(--el-border-color);
    border-radius: 50%;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: var(--el-transition-duration-fast);
}
.avatar-uploader .el-upload:hover {
    border-color: var(--el-color-primary);
}
.el-icon.avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 100px;
    height: 100px;
    text-align: center;
}
</style>
