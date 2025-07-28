import axios from 'axios';
import { useUserStore } from '../stores/user'

const apiClient = axios.create({
    baseURL: 'http://localhost:8080', // 设置基础 URL
    timeout: 10000,
});

// 创建一个请求拦截器
apiClient.interceptors.request.use(
    (config) => {
        // 在发送请求之前做些什么
        const userStore = useUserStore();
        const token = userStore.token;

        // 如果存在 token，则在请求头中添加 Authorization 字段
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }

        return config;
    },
    (error) => {
        // 对请求错误做些什么
        return Promise.reject(error);
    }
);

export default apiClient;
