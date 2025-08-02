import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import { useChatStore, type Message } from '../stores/chat'

let ws: WebSocket | null = null
let heartBeatTimer: number | null = null
const heartBeatInterval = 30 * 1000 // 30秒心跳一次

// websocket 服务地址
// 注意：这里是 ws:// 或 wss://, 而不是 http:// 或 https://
const wsUrl = 'wss://api.chirpchump.xyz/ws/chat'

/**
 * 连接 WebSocket
 */
export function connectWebSocket() {
    const userStore = useUserStore()


    // 1. 检查是否已登录，以及是否已存在连接
    if (!userStore.isLoggedIn || ws) {
        console.log('用户未登录或 WebSocket 已连接，无需重复连接。')
        return
    }

    // 2. 从 store 获取 token
    const token = userStore.token
    if (!token) {
        console.error('无法获取 token，无法建立 WebSocket 连接。')
        return
    }

    // 3. 拼接带 token 的 URL 并创建 WebSocket 实例
    const fullUrl = `${wsUrl}?token=${token}`

    console.log("【前端】准备连接 WebSocket，完整 URL:", fullUrl);
    console.log("【前端】发送的 Token:", token);

    ws = new WebSocket(fullUrl)

    // 4. 设置事件监听
    ws.onopen = handleOpen
    ws.onmessage = handleMessage
    ws.onclose = handleClose
    ws.onerror = handleError
}

/**
 * 关闭 WebSocket 连接
 */
export function closeWebSocket() {
    if (ws) {
        console.log('手动关闭 WebSocket 连接。')
        ws.close()
        ws = null
    }
    if (heartBeatTimer) {
        clearInterval(heartBeatTimer)
        heartBeatTimer = null
    }
}

// ---- 私有辅助函数 ----

function handleOpen() {
    console.log('WebSocket 连接成功！')
    // 连接成功后，启动心跳
    startHeartBeat()
}

function handleMessage(event: MessageEvent) {
    const messageData: Record<string, any> = JSON.parse(event.data)
    console.log('收到服务器消息:', messageData)

    const chatStore = useChatStore()

    if (messageData.type === 'HEARTBEAT_PONG') {
        console.log('收到心跳响应，心跳正常。')
    } else if (messageData.type === 'ERROR') {
        console.error('收到错误消息:', messageData.content)
        ElMessage.error(messageData.content || '服务器发生错误')
    }
    else if (messageData.senderId) {
        const chatMessage: Message = {
            id: messageData.id,
            senderId: messageData.senderId,
            receiverId: messageData.receiverId,
            receiverGroupId: messageData.receiverGroupId,
            content: messageData.content,
            messageType: messageData.messageType,
            chatType: messageData.chatType,
            createTime: messageData.createTime,
            status: 'sent',
        }
        chatStore.addMessage(chatMessage)
    }
    else {
        console.warn('收到未知类型的WebSocket消息:', messageData)
    }
}


function handleClose(event: CloseEvent) {
    console.log(`WebSocket 连接已关闭。Code: ${event.code}, Reason: ${event.reason}`)
    if (heartBeatTimer) {
        clearInterval(heartBeatTimer)
        heartBeatTimer = null
    }
    // TODO: 在这里可以实现断线重连逻辑
}

function handleError(event: Event) {
    console.error('WebSocket 连接发生错误:', event)
}

function startHeartBeat() {
    console.log('启动心跳机制...')
    heartBeatTimer = window.setInterval(() => {
        if (ws && ws.readyState === WebSocket.OPEN) {
            // 发送心跳包
            ws.send(JSON.stringify({ type: 'HEARTBEAT_PING' }))
        }
    }, heartBeatInterval)
}

/**
 * 发送消息
 * @param message
 */
// websocket.ts

export function sendMessage(message: object) {
    if (ws && ws.readyState === WebSocket.OPEN) {
        console.log("【即将发送的JS对象】:", message);
        try {
            const jsonString = JSON.stringify(message);
            console.log("【序列化后的JSON字符串】:", jsonString);
            ws.send(jsonString);
        } catch (error) {
            console.error("【致命错误】JSON 序列化失败！对象中包含无法序列化的内容！", error);
            console.error("【问题对象】:", message);
        }
    } else {
        ElMessage.error('聊天服务器未连接，无法发送消息！');
    }
}
