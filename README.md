# 🚀 EasyChat - 一款功能丰富的桌面端即时通讯应用

<img width="400" height="300" alt="image" src="https://github.com/user-attachments/assets/ab05ea3c-2926-494a-8125-71228bc2f377" />


---

## ✨ 项目简介

**EasyChat** 是一款从零开始，基于 **Spring Boot + Electron + Vue 3** 技术栈打造的全功能、仿微信风格的桌面端在线聊天系统。本项目旨在通过实践，完整地覆盖一个现代化全栈应用的**设计、开发、部署与发布**全链路流程。

它不仅仅是一个聊天工具，更是一个包含了**用户认证、实时通信、好友管理、群组社交、富媒体消息、云存储集成**等多个核心模块的综合性项目。

**[🔗 点击这里，下载最新发布的客户端！](https://github.com/Swordingman/easychat/releases/tag/Easychat))**

---

## 🌟 功能亮点

*   **现代化的 UI/UX**: 采用经典的IM布局，界面简洁，交互流畅。
*   **核心聊天功能**:
    *   **私聊 & 群聊**: 支持点对点和多对多的实时消息通信。
<img width="400" height="300" alt="image" src="https://github.com/user-attachments/assets/a189ee98-4d21-4181-9be1-87aec59dc252" />

    *   **富媒体消息**: 不再局限于文字！支持发送 **Emoji表情、图片、视频、文件**，并能在聊天窗口中完美预览。
<img width="400" height="300" alt="image" src="https://github.com/user-attachments/assets/3aa44425-8ff8-4509-89b5-2067f1d36180" />

    *   **实时提醒**: “未读消息”与“新的好友请求”均有小红点提示，重要信息不错过。
<img width="400" height="300" alt="image" src="https://github.com/user-attachments/assets/47b3d4e0-f5c8-4c5b-a8b9-8228923a675c" />

    *   **消息持久化**: 所有聊天记录（包括离线消息）都安全地存储在云端数据库中。
*   **完善的社交体系**:
    *   **身份系统**: 基于唯一的 `EasyChat号`，支持注册和登录。
    *   **好友管理**: 支持**搜索用户、发送/处理好友请求、删除好友**的完整流程。
<img width="400" height="300" alt="image" src="https://github.com/user-attachments/assets/5f6cee87-2850-4317-8fdb-6fe068335931" />
<img width="400" height="300" alt="image" src="https://github.com/user-attachments/assets/fbd95faf-63fe-4b59-a38c-d7c6a2cc1a79" />

    *   **群组管理**: 支持**创建群聊、邀请/移出成员、修改群信息**（群头像、群名、群公告）。
<img width="400" height="300" alt="image" src="https://github.com/user-attachments/assets/9deddfe9-6436-4385-a7ba-8f576d63b12c" />
<img width="400" height="300" alt="image" src="https://github.com/user-attachments/assets/2d6ccc35-0d98-4f49-a01c-b4bbc2cb0dfe" />

*   **个性化设置**:
    *   支持用户自由修改个人**头像、昵称**。
<img width="400" height="300" alt="image" src="https://github.com/user-attachments/assets/ef1c3e93-179b-4ae5-8f01-59b1ea5510bc" />

---

## 🛠️ 技术栈

本项目是一个典型的**前后端分离**的全栈应用。

### **后端 (`easychat-server`)**
*   **框架**: Spring Boot
*   **语言**: Java 21
*   **身份认证**: Spring Security + JWT (JSON Web Token)
*   **数据库**: MySQL 8.0 + Spring Data JPA
*   **实时通信**: WebSocket
*   **文件存储**: 阿里云 OSS
*   **构建工具**: Maven

### **客户端 (`easychat-client`)**
*   **框架**: Electron + Vite
*   **前端核心**: Vue 3 + TypeScript
*   **状态管理**: Pinia
*   **UI 组件库**: Element Plus
*   **HTTP 请求**: Axios
*   **打包工具**: Electron Builder

### **部署与运维**
*   **服务器**: 阿里云 ECS (Ubuntu Server 22.04 LTS)
*   **反向代理**: Nginx
*   **安全**: HTTPS (由 Let's Encrypt + Certbot 自动配置)
*   **进程守护**: systemd

---

## 🚀 快速上手 (作为用户)

1.  访问本项目的 **[Releases 页面](https://github.com/Swordingman/easychat/releases/tag/Easychat)**。
2.  根据你的操作系统，下载最新的安装包（例如 `EasyChat.Setup.1.0.0.exe`）。
3.  双击运行安装程序，一路“下一步”即可。
4.  打开 EasyChat，注册一个新账号，开始你的聊天之旅！

