import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import apiClient from '../services/api'
import { useUserStore } from './user'
import { ElMessage } from 'element-plus'

// ===================================================================
// 类型定义 (Types)
// 我们将所有相关的类型定义放在这里，并导出
// ===================================================================

export type MessageStatus = 'sending' | 'sent' | 'failed'
export type ActiveView = 'chat' | 'contacts'
export type SessionType = 'PRIVATE' | 'GROUP'

export interface Message {
    id?: number
    tempId?: string
    senderId: number
    receiverId: number | null // 私聊有，群聊为null
    receiverGroupId: number | null // 群聊有，私聊为null
    content: string
    messageType: 'TEXT' | 'IMAGE' | 'VIDEO' | 'FILE'
    chatType: SessionType
    createTime: string
    status: MessageStatus
    localUrl?: string
}

export interface Contact {
    id: number
    easychatId: string
    username: string
    nickname: string
    avatar: string
    lastMessage?: string
    lastMessageTime?: string
}

export interface Group {
    id: number
    groupName: string
    ownerId: number
    avatar: string
    announcement: string | null
    createTime: string
}

export interface Session {
    id: string // "PRIVATE-id" or "GROUP-id"
    type: SessionType
    name: string
    avatar: string
    lastMessage?: string
    lastMessageTime?: string
    targetId: number // 用户ID 或 群ID
    formattedLastMessage?: string
}

export interface GroupMember {
    memberId: number
    nickname: string
    avatar: string
    role: string
}


// ===================================================================
// Pinia Store 定义
// ===================================================================

export const useChatStore = defineStore('chat', () => {
    // ===============================================================
    // State (状态)
    // ===============================================================

    const activeView = ref<ActiveView>('chat')
    const contacts = ref<Contact[]>([])
    const groups = ref<Group[]>([])
    const sessions = ref<Session[]>([])
    const activeSessionId = ref<string | null>(null) // 重命名，更清晰
    const messageSessions = ref<Record<string, { list: Message[]; loaded: boolean }>>({});
    const unreadCounts = ref<Record<string, number>>({});
    const pendingRequestsCount = ref(0)
    let pollingTimer: number | null = null
    const groupMembers = ref<Record<number, GroupMember[]>>({})

    // ===============================================================
    // Getters (计算属性)
    // ===============================================================

    const activeSession = computed(() => {
        if (!activeSessionId.value) return null
        return sessions.value.find((s) => s.id === activeSessionId.value)
    })

    const activeMessages = computed(() => {
        if (!activeSessionId.value) return []
        // messageSessions 的 key 是 targetId (number)
        return messageSessions.value[activeSessionId.value]?.list || []
    })

    const totalUnreadCount = computed(() => {
        // 计算所有对话的未读消息总和
        return Object.values(unreadCounts.value).reduce((sum, count) => sum + count, 0)
    })

    // 获取最后一条消息
    const getLastMessage = computed(() => {
        return (sessionId: string): Message | null => {
            // 1. 直接用 sessionId 作为 key 来访问 messageSessions
            const sessionMessages = messageSessions.value[sessionId];

            // 2. 如果这个会话有消息记录，就返回最后一条
            if (sessionMessages && sessionMessages.list.length > 0) {
                return sessionMessages.list[sessionMessages.list.length - 1];
            }

            // 3. 如果没有消息记录，返回 null
            return null;
        };
    });

    const getGroupMemberInfo = computed(() => {
        return (groupId: number, userId: number): GroupMember | null => {
            const members = groupMembers.value[groupId];
            if (members) {
                return members.find(m => m.memberId === userId) || null;
            }
            return null;
        }
    })

    // ===============================================================
    // Actions (方法)
    // ===============================================================

    function setActiveView(view: ActiveView) {
        activeView.value = view
    }

    function setActiveSessionId(sessionId: string | null) {
        activeSessionId.value = sessionId
        if (sessionId && unreadCounts.value[sessionId]) {
            unreadCounts.value[sessionId] = 0;
        }
    }

    // 获取和设置联系人列表
    async function fetchContacts() {
        const userStore = useUserStore()
        if (!userStore.isLoggedIn) return
        try {
            const response = await apiClient.get<Contact[]>('/api/contact/list')
            contacts.value = response.data
        } catch (error) {
            console.error('刷新联系人列表失败:', error)
            ElMessage.error('刷新联系人列表失败')
        }
    }

    function formatLastMessage(message: Message | null | undefined): string {
        if (!message) return '';
        switch (message.messageType) {
            case 'TEXT':
                return message.content.length > 15 ? message.content.slice(0, 15) + '...' : message.content;
            case 'IMAGE':
                return '[图片]';
            case 'VIDEO':
                return '[视频]';
            case 'FILE':
                return '[文件]';
            default:
                return '[未知]'
        }
    }

    async function fetchSessions() {
        const userStore = useUserStore()
        if (!userStore.isLoggedIn) return
        try {
            const [contactsRes, groupsRes] = await Promise.all([
                apiClient.get<Contact[]>('/api/contact/list'),
                apiClient.get<Group[]>('/api/group/list')
            ]);

            // 1. 把纯粹的数据存起来
            contacts.value = contactsRes.data;
            groups.value = groupsRes.data;

            const contactSessions: Session[] = contactsRes.data.map(c => {
                const sessionId = `PRIVATE-${c.id}`;
                const lastMsg = getLastMessage.value(sessionId);

                return {
                    id: sessionId,
                    type: 'PRIVATE',
                    name: c.nickname,
                    avatar: c.avatar,
                    lastMessage: c.lastMessage,
                    lastMessageTime: c.lastMessageTime,
                    targetId: c.id,
                    formattedLastMessage: formatLastMessage(lastMsg) || c.lastMessage || ''
                }});

            const groupSessions: Session[] = groupsRes.data.map(g => {
                const sessionId = `GROUP-${g.id}`;
                const lastMsg = getLastMessage.value(sessionId);
                return {
                    id: sessionId,
                    type: 'GROUP',
                    name: g.groupName,
                    avatar: g.avatar,
                    targetId: g.id,
                    formattedLastMessage: formatLastMessage(lastMsg)
                }});

            sessions.value = [...contactSessions,...groupSessions];

            await Promise.all(sessions.value.map(async (session) => {
                await loadHistoryMessages(session.targetId, session.type, 1);
                const lastMsg = getLastMessage.value(session.id);
                session.formattedLastMessage = formatLastMessage(lastMsg);
                session.lastMessageTime = lastMsg?.createTime || session.lastMessageTime;
            }));

            sessions.value.sort((a, b) => {
                const tA = a.lastMessageTime ? new Date(a.lastMessageTime).getTime() : 0;
                const tB = b.lastMessageTime ? new Date(b.lastMessageTime).getTime() : 0;
                return tB - tA;
            })
        } catch (error) {
            console.error(`获取会话列表失败:`, error)
        }
    }

    // 加载指定对话的历史消息
    async function loadHistoryMessages(targetId: number, chatType: SessionType, limit: number = 100  ) {
        const sessionId = `${chatType}-${targetId}`;
        if (!messageSessions.value[sessionId]) {
            messageSessions.value[sessionId] = { list: [], loaded: false };
        }
        const session = messageSessions.value[sessionId];
        if (session.loaded && limit !== 1) return;

        const userStore = useUserStore();
        if (!userStore.userInfo) return;

        try {
            let response;
            if (chatType === 'PRIVATE') {
                response = await apiClient.get<Message[]>('/api/message/conversation', {
                    params: { userId1: userStore.userInfo.id, userId2: targetId, limit },
                });
            } else { // chatType === 'GROUP'
                // 【核心修改】调用新的群聊历史接口
                response = await apiClient.get<Message[]>('/api/message/group_conversation', {
                    params: { groupId: targetId, limit },
                });
            }

            const historyMessages = response.data.map((m) => ({ ...m, status: 'sent' as MessageStatus }));
            if (limit === 1) {
                session.list = historyMessages;
            } else {
                session.list = historyMessages;
                session.loaded = true;
            }

        } catch (error) {
            console.error(`获取 ${sessionId} 的历史消息失败:`, error);
        }
    }

    // 添加或更新消息（用于发送和接收）
    function addMessage(message: Message) {
        const userStore = useUserStore()
        if (!userStore.userInfo) return

        let targetId: number | null = null
        if (message.chatType === 'PRIVATE') {
            targetId = message.senderId === userStore.userInfo.id ? message.receiverId : message.senderId
        } else {
            targetId = message.receiverGroupId
        }
        if (targetId === null) return

        const sessionId = `${message.chatType}-${targetId}`

        if (!messageSessions.value[sessionId]) {
            messageSessions.value[sessionId] = { list: [], loaded: true }
        }
        messageSessions.value[sessionId].list.push(message)

        // 更新会话列表的预览
        const sessionIndex = sessions.value.findIndex(s => s.id === sessionId);
        if(sessionIndex > -1) {
            sessions.value[sessionIndex].lastMessageTime = message.createTime;
            sessions.value[sessionIndex].formattedLastMessage = formatLastMessage(message);

            sessions.value.sort((a, b) => {
                const tA = a.lastMessageTime ? new Date(a.lastMessageTime).getTime() : 0
                const tB = b.lastMessageTime ? new Date(b.lastMessageTime).getTime() : 0
                return tB - tA
            })
        }


        if (message.senderId !== userStore.userInfo.id && sessionId !== activeSessionId.value) {
            const currentUnread = unreadCounts.value[sessionId] || 0
            unreadCounts.value[sessionId] = currentUnread + 1
        }
    }

    // --- 好友请求轮询相关 ---
    async function fetchPendingRequests() {
        const userStore = useUserStore()
        if (!userStore.isLoggedIn) return
        try {
            const response = await apiClient.get('/api/contact/requests')
            pendingRequestsCount.value = response.data.length
        } catch (error) {
            console.error('轮询好友请求失败:', error)
        }
    }

    async function fetchGroupMembers(groupId: number) {
        // 如果已缓存，则不重复获取
        if (groupMembers.value[groupId]) return;
        try {
            const response = await apiClient.get<GroupMember[]>(`/api/group/${groupId}/members`);
            groupMembers.value[groupId] = response.data;
        } catch (error) {
            console.error(`获取群 ${groupId} 成员失败:`, error);
        }
    }

    function startPollingRequests() {
        if (pollingTimer) return
        fetchPendingRequests()
        pollingTimer = window.setInterval(fetchPendingRequests, 30000)
    }

    function stopPollingRequests() {
        if (pollingTimer) {
            clearInterval(pollingTimer)
            pollingTimer = null
        }
        pendingRequestsCount.value = 0
    }

    return {
        // State
        activeView,
        contacts,
        groups,
        activeSession,
        activeSessionId,
        messageSessions,
        unreadCounts,
        pendingRequestsCount,
        sessions,
        groupMembers,
        // Getters
        activeMessages,
        totalUnreadCount,
        getLastMessage,
        getGroupMemberInfo,
        // Actions
        setActiveView,
        setActiveSessionId,
        fetchContacts,
        loadHistoryMessages,
        addMessage,
        fetchPendingRequests,
        startPollingRequests,
        stopPollingRequests,
        fetchSessions,
        fetchGroupMembers,
    }
})
