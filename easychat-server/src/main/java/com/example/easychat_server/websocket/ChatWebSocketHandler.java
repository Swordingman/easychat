package com.example.easychat_server.websocket;

import com.example.easychat_server.dto.WebSocketMessage;
import com.example.easychat_server.model.Message;
import com.example.easychat_server.service.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    // 用于存储所有在线用户的 WebSocketSession
    // Key: userId, Value: WebSocketSession 实例
    // 使用 ConcurrentHashMap 保证线程安全
    private static final Map<Long, WebSocketSession> ONLINE_USERS = new ConcurrentHashMap<>();

    private final MessageService messageService;
    private final ObjectMapper objectMapper;

    @Autowired
    public ChatWebSocketHandler(MessageService messageService, ObjectMapper objectMapper) {
        this.messageService = messageService;
        this.objectMapper = objectMapper;
    }

    /**
     * 当 WebSocket 连接成功建立后调用
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 从 session 的 attributes 中获取用户ID。这个ID是在握手拦截器中放入的。
        Long userId = (Long) session.getAttributes().get("userId");

        if (userId != null) {
            ONLINE_USERS.put(userId, session);
            log.info("用户 {} (session: {}) 连接成功。当前在线人数: {}", userId, session.getId(), ONLINE_USERS.size());
        } else {
            log.warn("无法获取用户ID，连接将被关闭。");
            session.close();
        }
    }

    /**
     * 当接收到客户端发送的文本消息时调用
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
        Long senderId = (Long) session.getAttributes().get("userId");
        if (senderId == null) return; // 如果没有 senderId，则忽略

        try {
            // 1. 解析收到的 JSON 消息
            WebSocketMessage webSocketMessage = objectMapper.readValue(textMessage.getPayload(), WebSocketMessage.class);

            switch (webSocketMessage.getType()) {
                case "PRIVATE_CHAT":
                    handlePrivateChatMessage(senderId, webSocketMessage);
                    break;
                case "HEARTBEAT_PING":
                    // 收到心跳ping，回复一个pong，保持连接
                    session.sendMessage(new TextMessage("{\"type\":\"HEARTBEAT_PONG\"}"));
                    break;
                default:
                    log.warn("收到未知类型的消息: {}", webSocketMessage.getType());
            }

        } catch (Exception e) {
            log.error("处理消息时出错: {}", e.getMessage());
            // 可以选择性地向客户端发送错误通知
            session.sendMessage(new TextMessage("{\"type\":\"ERROR\", \"content\":\"消息格式错误\"}"));
        }
    }

    private void handlePrivateChatMessage(Long senderId, WebSocketMessage webSocketMessage) throws IOException {
        Long receiverId = webSocketMessage.getReceiverId();
        String content = webSocketMessage.getContent();
        String messageType = webSocketMessage.getMessageType();

        if (receiverId == null || content == null || content.isEmpty() || messageType == null || messageType.isEmpty()) {
            log.warn("无效的私聊消息: {}", webSocketMessage);
            return;
        }

        // 2. 创建 Message 实体并存入数据库
        Message messageToSave = new Message();
        messageToSave.setSenderId(senderId);
        messageToSave.setReceiverId(receiverId);
        messageToSave.setContent(content);
        messageToSave.setMessageType(messageType); // 目前只处理文本消息
        Message savedMessage = messageService.saveMessage(messageToSave);
        log.info("消息已存入数据库: {}", savedMessage);

        // 3. 准备需要转发给客户端的消息体（包含更多信息，如ID和时间）
        // 这是为了让客户端收到消息后能直接渲染，而不需要再次请求
        String messageToSend = objectMapper.writeValueAsString(savedMessage);

        // 4. 查找接收者的 session 并转发
        WebSocketSession receiverSession = ONLINE_USERS.get(receiverId);
        if (receiverSession != null && receiverSession.isOpen()) {
            receiverSession.sendMessage(new TextMessage(messageToSend));
            log.info("消息已成功转发给在线用户: {}", receiverId);
        } else {
            log.info("用户 {} 不在线，消息已存入数据库，将作为离线消息。", receiverId);
        }

        // 5. (可选但推荐) 将已保存并带有完整信息的消息发回给发送者自己，
        //    这样发送者的客户端也能同步到消息ID和服务器时间，确保消息一致性。
        WebSocketSession senderSession = ONLINE_USERS.get(senderId);
        if (senderSession != null && senderSession.isOpen()) {
            senderSession.sendMessage(new TextMessage(messageToSend));
        }
    }

    /**
     * 当 WebSocket 连接关闭后调用
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            ONLINE_USERS.remove(userId);
            log.info("用户 {} (session: {}) 断开连接。状态: {}。当前在线人数: {}", userId, session.getId(), status, ONLINE_USERS.size());
        }
    }

    /**
     * 当通信发生错误时调用
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        log.error("用户 {} (session: {}) 的连接发生错误: {}", userId, session.getId(), exception.getMessage());
        if (session.isOpen()) {
            session.close();
        }
        if (userId != null) {
            ONLINE_USERS.remove(userId);
        }
    }
}
