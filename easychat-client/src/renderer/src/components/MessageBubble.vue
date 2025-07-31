<template>
    <!-- 文本消息 -->
    <div v-if="message.messageType === 'TEXT'" class="message-content">
        {{ message.content }}
    </div>

    <!-- 图片消息 -->
    <div v-else-if="message.messageType === 'IMAGE'" class="message-image">
        <el-image
            class="image-content"
            :src="parsedContent.url"
            :preview-src-list="[parsedContent.url]"
            fit="cover"
            hide-on-click-modal
        />
    </div>

    <!-- 视频消息 -->
    <div v-else-if="message.messageType === 'VIDEO'" class="message-video">
        <video controls class="video-content" :src="parsedContent.url">
            您的浏览器不支持 video 标签。
        </video>
    </div>

    <!-- 文件消息 -->
    <div v-else-if="message.messageType === 'FILE'" class="message-file">
        <a :href="parsedContent.url" target="_blank">
            <div class="file-info">
                <el-icon><Folder /></el-icon>
                <div class="file-details">
                    <span class="file-name">{{ parsedContent.name }}</span>
                    <span class="file-size">{{ (parsedContent.size / 1024).toFixed(2) }} KB</span>
                </div>
            </div>
        </a>
    </div>

    <!-- 未知消息 -->
    <div v-else class="message-content">[暂不支持的消息类型]</div>
</template>

<script setup lang="ts">
import { computed, toRefs } from 'vue';

// 假设 Message 接口定义在某个地方
interface Message {
    id: number;
    senderId: number;
    receiverId: number;
    content: string;
    messageType: 'TEXT' | 'IMAGE' | 'VIDEO' | 'FILE';
    createTime: string;
    status: 'sending' | 'failed' | 'sent';
    localUrl?: string;
}

const props = defineProps<{ message: Message }>();
const { message } = toRefs(props);

// 计算属性，用于解析 content 里的 JSON 字符串
// 只有在消息不是 TEXT 类型时才执行解析
const parsedContent = computed(() => {
    if (message.value.messageType !== 'TEXT' && message.value.content) {
        try {
            return JSON.parse(message.value.content);
        } catch (e) {
            console.error('解析消息内容失败:', e);
            return {};
        }
    }
    return null;
});
</script>

<style scoped>
/* 将 ChatWindow.vue 中所有 .message-content, .message-image 等样式都移动到这里 */
.message-content {
    padding: 8px 12px;
    border-radius: 6px;
    max-width: 450px; /* 关键：给气泡一个最大宽度，防止它撑满整行 */
    font-size: 14px;
    line-height: 1.5;
    /* 背景色由父组件决定，所以这里不需要写 */
    /* background-color: #ffffff;  <-- 删掉或注释掉 */
    /* background-color: #95ec69;  <-- 删掉或注释掉 */

    /* 让文字可以换行 */
    word-break: break-all;
}
.message-image {
    max-width: 250px;
}
.image-content {
    vertical-align: bottom; /* 消除图片下方的空隙 */
}
.message-video {
    max-width: 300px;
}
.video-content {
    width: 100%;
    border-radius: 6px;
}
.message-file {
    background-color: #ffffff;
    border-radius: 6px;
    padding: 10px;
    width: 240px;
    cursor: pointer;
    border: 1px solid #e0e0e0;
}
.message-file a { text-decoration: none; color: inherit; }
.file-info { display: flex; align-items: center; }
.file-info .el-icon { font-size: 40px; margin-right: 10px; }
.file-details { display: flex; flex-direction: column; overflow: hidden; }
.file-name { font-size: 14px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.file-size { font-size: 12px; color: #999; }
.message-bubble-wrapper {
    position: relative; /* 为了状态遮罩定位 */
}
.status-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0,0,0,0.5);
    color: white;
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 24px;
    border-radius: 6px; /* 和图片保持一致 */
}
</style>
