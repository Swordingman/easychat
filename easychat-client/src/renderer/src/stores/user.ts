import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { jwtDecode } from 'jwt-decode'

// 定义 userInfo 的接口类型，增强代码健壮性
interface UserInfo {
    id: number;
    username: string;
    nickname: string;
    avatar: string;
    fullAvatarUrl: string;
    easychatId: string;
}

// 使用 setup-style 的方式定义 store，这是 Vue 3 和 TS 推荐的写法
export const useUserStore = defineStore('user', () => {
    // ---- state ----
    const token = ref<string | null>(null)
    const userInfo = ref<UserInfo | null>(null)

    // ---- getters ----
    // 使用 computed 来创建 getter
    const isLoggedIn = computed(() => !!token.value && !!userInfo.value)

    // ---- actions ----
    function setLoginInfo(data: { token: string; userInfo: UserInfo }) {
        console.log('Setting login info:', data)
        token.value = data.token
        userInfo.value = data.userInfo
    }

    function logout() {
        console.log('正在登出...')
        token.value = null
        userInfo.value = null
    }

    function checkTokenValidity(): boolean {
        if (!token.value) {
            return false
        }
        try {
            const decoded: { exp: number } = jwtDecode(token.value);
            const nowInSeconds = Date.now() / 1000;

            if (decoded.exp < nowInSeconds) {
                console.warn('Token 已过期');
                return false;
            }
            return true;
        } catch(error) {
            console.error('无效的 token', error);
            return false;
        }
    }

    function handleInvalidToken() {
        console.error('Token 无效，请重新登录');
        logout();
    }

    // 必须返回 state、getters 和 actions
    return {
        token,
        userInfo,
        isLoggedIn,
        setLoginInfo,
        logout,
        checkTokenValidity,
        handleInvalidToken
    }
},
{
    persist: {
        key: 'user-v2'
    } // 开启持久化存储
})
