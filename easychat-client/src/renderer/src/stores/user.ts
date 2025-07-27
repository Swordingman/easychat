// src/renderer/src/stores/user.ts

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

// 定义 userInfo 的接口类型，增强代码健壮性
interface UserInfo {
    id: number;
    username: string;
    nickname: string;
    avatar: string;
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
        console.log('Logging out')
        token.value = null
        userInfo.value = null
    }

    // 必须返回 state、getters 和 actions
    return {
        token,
        userInfo,
        isLoggedIn,
        setLoginInfo,
        logout
    }
},
{
    persist: true // 开启持久化存储
})
