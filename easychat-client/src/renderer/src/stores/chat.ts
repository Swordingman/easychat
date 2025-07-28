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

interface Message {
    id: number;
    senderId: number;
    receiverId: number;
    content: string;
    messageType: string;
    createTime: string;
}

export const useChatStore = defineStore('chat', () => {
    // ---- state ----
    const contacts = ref<Contact[]>([])
    const activeContactId = ref<number | null>(null)
    // 使用 Map 存储每个联系人的消息列表，Key: contactId, Value: Message[]
    const messageMap = ref<Map<number, { list: Message[], loaded: boolean }>>(new Map())

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
        // 判断消息属于哪个对话
        const userStore = useUserStore() // 需要引入
        const contactId = message.senderId === userStore.userInfo?.id ? message.receiverId : message.senderId

        if(!messageMap.value.has(contactId)) {
            messageMap.value.set(contactId, { list: [], loaded: false });
        }

        messageMap.value.get(contactId)!.list.push(message)
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


    return { contacts, activeContactId, activeContact, activeMessages, setContacts, setActiveContactId, addMessage, loadHistoryMessages }
})
