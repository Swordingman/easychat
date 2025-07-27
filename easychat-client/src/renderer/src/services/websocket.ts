import { useUserStore } from '../stores/user'
import { ElMessage, ElNotification } from 'element-plus'

let ws: WebSocket | null = null
let heartBeatTimer: number | null = null
const heartBeatInterval = 30 * 1000 // 30秒心跳一次

// websocket 服务地址
// 注意：这里是 ws:// 或 wss://, 而不是 http:// 或 https://
const wsUrl = 'ws://localhost:8080/ws/chat'

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
    ElNotification({
        title: '连接成功',
        message: '已成功连接到聊天服务器！',
        type: 'success',
        duration: 2000
    })
    // 连接成功后，启动心跳
    startHeartBeat()
}

function handleMessage(event: MessageEvent) {
    const message = event.data
    console.log('收到服务器消息:', message)

    // 在这里根据消息内容进行处理
    // 例如，如果是心跳响应，则可以忽略
    if (message === 'pong') {
        return;
    }

    // TODO: 如果是聊天消息，则需要提交到 chatStore
    ElNotification({
        title: '收到新消息',
        message: message,
        type: 'info'
    })
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
export function sendMessage(message: object) {
    if (ws && ws.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify(message))
    } else {
        console.error('WebSocket 未连接，无法发送消息。')
        ElMessage.error('聊天服务器未连接，无法发送消息！')
    }
}
