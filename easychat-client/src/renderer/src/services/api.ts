import axios from 'axios';
import { useUserStore } from '../stores/user'

const apiClient = axios.create({
    baseURL: 'https://api.chirpchump.xyz', // 设置基础 URL
    timeout: 10000,
});

// 创建一个请求拦截器
apiClient.interceptors.request.use(
    (config) => {
        const userStore = useUserStore();
        if (userStore.token) {
            config.headers.Authorization = `Bearer ${userStore.token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

export default apiClient;
