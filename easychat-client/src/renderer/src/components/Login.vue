<template>
    <div class="login-container">
        <el-card class="login-card">
            <template #header>
                <div class="card-header">
                    <span>欢迎来到 EasyChat</span>
                </div>
            </template>

            <!-- 使用 el-tabs 来切换登录和注册 -->
            <el-tabs v-model="activeTab" lazy="false">
                <!-- 登录表单 -->
                <el-tab-pane label="登录" name="login">
                    <el-form
                        :model="loginForm"
                        ref="loginFormRef"
                        label-width="80px"
                        :rules="loginRules">
                        <el-form-item label="账号" prop="easychatId">
                            <el-input v-model="loginForm.easychatId" placeholder="请输入easychat账号"></el-input>
                        </el-form-item>
                        <el-form-item label="密码" prop="password">
                            <el-input type="password" v-model="loginForm.password" placeholder="请输入密码" show-password></el-input>
                        </el-form-item>
                        <el-form-item>
                            <el-button type="primary" @click="handleLogin">登录</el-button>
                        </el-form-item>
                    </el-form>
                </el-tab-pane>

                <!-- 注册表单 -->
                <el-tab-pane label="注册" name="register">
                    <el-form
                        :model="registerForm"
                        ref="registerFormRef"
                        label-width="80px"
                        :rules="registerRules">
                        <el-form-item label="用户名" prop="username">
                            <el-input v-model="registerForm.username" placeholder="请输入用户名"></el-input>
                        </el-form-item>
                        <el-form-item label="昵称" prop="nickname">
                            <el-input v-model="registerForm.nickname" placeholder="请输入你的昵称"></el-input>
                        </el-form-item>
                        <el-form-item label="密码" prop="password">
                            <el-input type="password" v-model="registerForm.password" placeholder="请输入密码" show-password></el-input>
                        </el-form-item>
                        <el-form-item>
                            <el-button type="primary" @click="handleRegister">注册</el-button>
                        </el-form-item>
                    </el-form>
                </el-tab-pane>
            </el-tabs>

        </el-card>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { useUserStore } from '../stores/user'
import apiClient from '../services/api'

const loginFormRef = ref<FormInstance>()
const registerFormRef = ref<FormInstance>()
const userStore = useUserStore()

// ---- 状态定义 ----
const activeTab = ref('login') // 'login' 或 'register'

// 登录表单数据
const loginForm = reactive({
    easychatId: '',
    password: ''
})


const loginRules = reactive<FormRules>({
    easychatId: [{ required: true, message: '请输入easychat账号', trigger: 'blur' }],
    password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
})

// 注册表单数据
const registerForm = reactive({
    username: '',
    nickname: '',
    password: ''
})

const registerRules = reactive<FormRules>({
    username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 15, message: '长度在 3 到 15 个字符', trigger: 'blur' }
    ],
    nickname: [
        { required: true, message: '请输入昵称', trigger: 'blur' }
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
    ],
})

// ---- 事件处理函数 ----
const handleLogin = async () => {
    if (!loginFormRef.value) return;

    await loginFormRef.value.validate(async (valid) => {
        if (valid) {
            console.log('登录表单验证通过！');
            try {
                // 发送 POST 请求到后端登录 API
                const response = await apiClient.post('/api/user/login', loginForm)

                // 登录成功
                console.log('登录成功:', response.data)
                ElMessage.success('登录成功！')

                userStore.setLoginInfo(response.data)
            } catch (error: any) {
                // 登录失败
                if (axios.isAxiosError(error)) {
                    console.error('登录失败:', error.response?.data)
                    ElMessage.error(error.response?.data || '登录失败，请检查网络或联系管理员')
                } else {
                    console.error('未知错误:', error)
                    ElMessage.error('发生未知错误，请联系管理员')
                }
                console.error('错误对象:', error)
            }
        } else {
            console.log('登录表单验证失败！')
            ElMessage.error('登录表单验证失败！请检查输入内容是否正确。')
            return false;
        }
    })
}

const handleRegister = async () => {
    if (!registerFormRef.value) return;

    await registerFormRef.value.validate(async (valid) => {
        if (valid) {
            console.log('注册表单验证通过！');
            try {
                // 发送 POST 请求到后端注册 API
                const response = await apiClient.post('/api/user/register', registerForm)

                const newEasychatId = response.data.easychatId;
                await ElMessageBox.alert(
                    `恭喜你，注册成功！<br/>你的 EasyChat 号是: <strong>${newEasychatId}</strong><br/>请牢记此号码，它是你唯一的登录凭证。`,
                    '注册成功',
                    {
                        confirmButtonText: '好的，去登录',
                        // dangerouslyUseHTMLString 允许我们在消息中使用 HTML 标签
                        dangerouslyUseHTMLString: true,
                        type: 'success',
                    }
                );

                // 4. 当用户点击“好的，去登录”按钮后，执行这里的代码
                // a. 自动填充登录表单
                loginForm.easychatId = newEasychatId;
                loginForm.password = ''; // 清空密码框，等待用户输入

                // b. 自动切换到登录标签页
                activeTab.value = 'login';
            } catch (error: any) {
                // 注册失败
                if (axios.isAxiosError(error)) {
                    console.error('注册失败:', error.response?.data)
                    ElMessage.error(error.response?.data || '注册失败，请检查网络或联系管理员')
                } else {
                    console.error('未知错误:', error)
                    ElMessage.error('发生未知错误，请联系管理员')
                }
                console.error('错误对象:', error)
            }
        } else {
            console.log('注册表单验证失败！')
            ElMessage.error('注册表单验证失败！请检查输入内容是否正确。')
            return false;
        }
    })
}
</script>

<style scoped>
.login-container {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100vh;
    /* 整个视窗高度 */
    background-color: #f0f2f5;
}

.login-card {
    width: 400px;
}

.card-header {
    text-align: center;
    font-size: 20px;
}
</style>
