package com.example.easychat_server.dto;

import lombok.Data;

@Data
public class WebSocketMessage {

    /**
     * 消息类型:
     * "PRIVATE_CHAT" - 私聊消息
     * "HEARTBEAT_PING" - 客户端心跳
     * ... 其他类型如 GROUP_CHAT 等
     */
    private String type;

    /*消息接收者ID (私聊时需要)*/
    private Long receiverId;

    /**
     * 消息文件类型:
     * "IMAGE" - 图片消息
     * "FILE" - 文件消息
     * ... 其他类型如 VIDEO 等
     */
    private String messageType;

    /*消息内容*/
    private String content;

    // 以后还可以添加更多字段，如 senderId, sendTime 等
    // 但目前 senderId 可以从 session 中获取，sendTime 可以由服务器生成
}
