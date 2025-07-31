import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { useUserStore } from '@renderer/stores/user'
import { ElMessage } from 'element-plus'
import apiClient from '../services/api'

// 定义 Contact 和 Message 的接口类型
interface Contact {
    id: number;
    username: string;
    nickname: string;
    avatar: string;
    lastMessage?: string;
    lastMessageTime?: string;
}

export interface Message {
    id: number;
    senderId: number;
    receiverId: number;
    content: string;
    messageType: 'TEXT' | 'IMAGE' | 'VIDEO' | 'FILE' ;
    createTime: string;
}

type ActiveView = 'chat' | 'contacts';

export const useChatStore = defineStore('chat', () => {
    // ---- state ----
    const contacts = ref<Contact[]>([])
    const activeContactId = ref<number | null>(null)
    // 使用 Map 存储每个联系人的消息列表，Key: contactId, Value: Message[]
    const messageMap = ref<Map<number, { list: Message[], loaded: boolean }>>(new Map())
    const activeView = ref<ActiveView>('chat');
    const pendingRequestsCount = ref(0);
    let pollingTimer: number | null = null;

    // ---- getters ----
    const activeContact = computed(() => {
        if(!activeContactId.value) return null
        return contacts.value.find(c => c.id === activeContactId.value)
    })
    const activeMessages = computed(() => {
        if(!activeContactId.value) return []
        return messageMap.value.get(activeContactId.value)?.list || []
    })


    // ---- actions ----
    function setActiveView(view: ActiveView) {
        activeView.value = view
    }

    function setContacts(contactList: Contact[]) {
        contacts.value = contactList
    }

    function setActiveContactId(contactId: number | null) {
        activeContactId.value = contactId
        // 如果这个联系人的消息列表还不存在，初始化为空数组
        if(contactId && !messageMap.value.has(contactId)) {
            messageMap.value.set(contactId, { list: [], loaded: false });
        }
    }

    function addMessage(message: Message) {
        const userStore = useUserStore();
        // 判断消息属于哪个对话 (这个逻辑依然有用)
        const contactId = message.senderId === userStore.userInfo?.id ? message.receiverId : message.senderId;

        if(!messageMap.value.has(contactId)) {
            messageMap.value.set(contactId, { list: [], loaded: true }); // loaded 可以先设为 true
        }

        messageMap.value.get(contactId)!.list.push(message);
    }

    async function loadHistoryMessages(contactId: number) {
        const chatSession = messageMap.value.get(contactId);
        if(!chatSession || chatSession.loaded) return;

        const userStore = useUserStore()
        if (!userStore.userInfo) return;

        try {
            const response = await apiClient.get('/api/message/conversation', {
                params: {
                    userId1: userStore.userInfo.id,
                    userId2: contactId,
                }
            });

            chatSession.list = response.data;
            chatSession.loaded = true;
        } catch (error) {
            console.error(`获取与 ${contactId} 的历史消息失败: ${error}`)
            ElMessage.error('获取与该联系人的历史消息失败，请稍后再试')
        }
    }

    async function fetchContacts() {
        const userStore = useUserStore();
        if (!userStore.isLoggedIn) return;
        try {
            // 这个 API 应该返回的是 ContactDto 列表
            const response = await apiClient.get('/api/contact/list');
            contacts.value = response.data;
        } catch (error) {
            console.error('刷新联系人列表失败:', error);
            ElMessage.error('刷新联系人列表失败');
        }
    }

    async function fetchPendingRequests() {
        const userStore = useUserStore();
        if (!userStore.isLoggedIn) return;
        try {
            // 我们只需要数量，所以可以让后端专门提供一个/count接口，但现在先用/requests
            const response = await apiClient.get('/api/contact/requests');
            pendingRequestsCount.value = response.data.length;
        } catch (error) {
            console.error("轮询好友请求失败:", error);
        }
    }

    function startPollingRequests() {
        if (pollingTimer) return;
        fetchPendingRequests(); // 立即执行一次
        pollingTimer = window.setInterval(fetchPendingRequests, 10000); // 每30秒一次
    }

    function stopPollingRequests() {
        if (pollingTimer) {
            clearInterval(pollingTimer);
            pollingTimer = null;
        }
        pendingRequestsCount.value = 0;
    }


    return { fetchPendingRequests, startPollingRequests, stopPollingRequests, pendingRequestsCount, fetchContacts, activeView, setActiveView, contacts, activeContactId, activeContact, activeMessages, setContacts, setActiveContactId, addMessage, loadHistoryMessages }
})
