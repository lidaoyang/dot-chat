# Web版高仿微信聊天软件

## 简介

这是一个基于Web技术开发的高仿微信聊天软件，支持手机端和PC端。用户可以通过浏览器访问，实现即时通讯、消息推送、语音视频通话等功能。
已接入 **DeepSeek-V3** AI智能回复功能，如果不知道怎么回复，可以使用"**AI智能回复**"来帮你回复。

## 特性

- **跨平台支持**：支持手机端和PC端，用户可以在不同设备上无缝切换。
- **即时通讯**：支持文字、图片、文件等多种消息格式。
- **语音视频通话**：集成实时语音和视频通话功能。
- **用户管理**：支持用户注册、登录、好友添加等功能。
- **群聊功能**：支持创建和加入群聊，进行多人聊天。
- **AI智能回复**：AI智能回复功能，根据好友的回复内容，自动生成回复信息。

## 测试地址  https://dot-chat.jrmall.cn

## 框架

### 后端

- SpringBoot 3.3
- JDK17
- MySQL 8.0
- tio（[第三方封装的WebSocket框架](https://gitee.com/tywo45/t-io)）

### 前端

- HTML + CSS + JavaScript + jQuery

## 页面截图

### 登录注册
![登录](screenshot/login.png)
![注册](screenshot/register.png)

### 移动端聊天界面

![聊天室页面](screenshot/chatroom.png)
![好友页面](screenshot/friends.png)
![我的](screenshot/my.png)

![群聊天页面](screenshot/msg-group.png)
![单聊页面](screenshot/msg-s.png)
![单聊页面2](screenshot/msg-s2.png)
![单聊视频播放页面](screenshot/msg-video-play.png)
![发送文件视频页面](screenshot/msg-send-file.png)
![发送表情页面](screenshot/msg-send-emoji.png)
![发送视频通话页面](screenshot/msg-send-video.png)
![视频通话页面](screenshot/msg-video-call.png)
![聊天信息更多页面](screenshot/msg-more.png)
![聊天信息更多页面](screenshot/msg-group-more.png)
### AI智能回复移动端截图
![AI智能回复3](screenshot/msg-ai-3.jpg)
![AI智能回复2](screenshot/msg-ai-2.jpg)
![AI智能回复1](screenshot/msg-ai-1.jpg)

### PC端聊天界面
![聊天室页面](screenshot/pc-chatroom.png)
![发送聊天表情](screenshot/pc-msg-send-emoji.png)
![发送截图](screenshot/pc-msg-send-screenshot.png)
![发送视频通话](screenshot/pc-msg-send-video-call.png)
![视频通话](screenshot/pc-msg-video-call.png)
![好友页面](screenshot/pc-friends.png)

### AI智能回复PC端截图
![AI智能回复2](screenshot/pc-msg-ai-2.jpg)
![AI智能回复1](screenshot/pc-msg-ai-1.jpg)