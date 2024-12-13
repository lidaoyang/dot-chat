let IMHandler = function () {

    this.onopen = function (event, ws) {
        logger.info('WebSocket Connected Success');
    }

    this.onclose = function (e, ws) {
        // 连接关闭，尝试重新连接
        logger.info('WebSocket Closed. Attempting to Reconnect..., Error: ', e, ' ws:', ws, ' date:', new Date);
    }

    this.onerror = function (e, ws) {
        // 处理错误，可能需要重新连接
        logger.info('WebSocket Error: ', e, ' ws:', ws);
    }

    /**
     * 发送心跳，本框架会自动定时调用该方法，请在该方法中发送心跳
     * @param {*} ws
     */
    this.ping = function (ws) {
        let token = $.cookie(TOKEN_KEY);
        if (!token) {
            logger.info("未登录或token过期，请重新登录");
            logout();
            return;
        }
        let msg = new TioMessage(MsgType.HEART_BEAT, ChatType.SINGLE, '心跳', '0', '0');
        ws.send(JSON.stringify(msg));
    }

    /**
     * 收到服务器发来的消息
     * @param {*} event
     * @param {*} ws
     */
    this.onmessage = function (event, ws) {
        let msgBody = JSON.parse(event.data);
        // logger.info(event.data);
        if (msgBody.msgType === MsgType.SYSTEM_WARNING) {
            myAlert('系统提示', msgBody.msg, 'warn');
            return;
        }
        if (msgBody.code === 401) {
            logout();
            return;
        }

        if (msgBody.msgType === MsgType.EVENT) {
            switchMsgEvent(msgBody);
        } else if (msgBody.chatType) {
            if (MsgType.AUDIO_CALL === msgBody.msgType || MsgType.VIDEO_CALL === msgBody.msgType) { // 语音或视频通话
                // logger.info('收到语音或视频通话消息');
                switchMsgCallType(msgBody);
            } else {
                logger.info('收到一条文字消息');
                operationChatMsg(msgBody);
            }

        }
    }
}

/**
 * 响应通话消息遍历
 * @param msgBody
 */
function switchMsgCallType(msgBody) {
    let isMy = msgBody.sendUserId === chatUser.id;
    let callMsg = JSON.parse(msgBody.msg);
    callMsg.isMy = msgBody.sendUserId === chatUser.id;
    callMsg.msgType = msgBody.msgType;
    switch (callMsg.callType) {
        case CallType.invite[0]:
            logger.info('收到通话邀请');
            getUserAndOpenAVCall(msgBody);
            break;
        case CallType.accept[0]:
            logger.info('已同意通话邀请');
            acceptCallAndOpenMediaOffer(callMsg);
            break;
        case CallType.offer[0]:
            logger.info('收到offer');
            saveSDPAndOpenMediaAnswer(callMsg);
            break;
        case CallType.candidate1[0]:
            logger.info('收到candidate1');
            saveRemoteIceCandidate(callMsg);
            break;
        case CallType.candidate2[0]:
            logger.info('收到candidate2');
            saveRemoteIceCandidate(callMsg);
            break;
        case CallType.answer[0]:
            logger.info('收到answer');
            saveAnswerSDP(callMsg);
            break;
        case CallType.refuse[0]:
        case CallType.cancel[0]:
        case CallType.no_answer[0]:
        case CallType.hangup[0]:
        case CallType.busying[0]:
        case CallType.dropped[0]:
            logger.info('收到通话挂断');
            hangupAndClosePeerConnection();
            operationChatMsg(msgBody);
            break;
        default:
            logger.info('未知通话类型');
    }
}

/**
 * 响应事件消息遍历
 * @param msgBody
 */
function switchMsgEvent(msgBody) {
    switch (msgBody.eventType) {
        case EventType.FRIEND_APPLY:
            logger.info('收到一个好友申请');
            // 好友申请消息处理
            operationFriendApplyMsg(msgBody);
            break;
        case EventType.FRIEND_AGREE:
            logger.info('收到一个同意申请信息');
            operationFriendAgreeMsg(msgBody);
            break
        case EventType.GROUP_APPLY:
            //TODO 入群申请
            break;
        case EventType.GROUP_BAN:
            //TODO 群禁言
            break;
        case EventType.GROUP_EXIT:
            // 退出群聊
            outOrJoinGroup(msgBody);
            break;
        case EventType.GROUP_KICK:
            // 踢出群聊
            outOrJoinGroup(msgBody);
            break;
        case EventType.GROUP_JOIN:
            // 加入群聊
            outOrJoinGroup(msgBody);
            break;
        case EventType.REVOKE_MSG:
            // 撤销消息
            revokeMsgHan(msgBody);
            break;
    }
}

function operationChatMsg(msgBody) {
    toastsInfo("收到一条新消息,点击查看!", msgBody.chatId, 'chat');
    if (msgBody.sendUserId !== chatUser.id) {
        //播放提示音
        playNotificationRingtone();
    }
    //更新未读消息集合
    unreadChatIdSet.add(msgBody.chatId);

    let chatMsgListDom = $("#chat-msg-list");
    if (chatMsgListDom.length > 0 && chatToUser && chatToUser.chatId === msgBody.chatId) {
        let user = getImChatUser(msgBody);
        let chatMsgDom = generateChatMsgDom(user, msgBody);
        chatMsgListDom.append(chatMsgDom);
        //延迟250毫秒再滚动
        setTimeout(function () {
            //显示进度条
            startUploadProgressBar();

            let msgContainer = document.getElementsByClassName("container")[0];
            $(msgContainer).animate({
                scrollTop: msgContainer.scrollHeight
            }, 450);//450毫秒秒滑动到指定位置
        }, 250);

        if (!mobile) {
            let chatListDom = $("#chat-list");
            let selectChatDom = chatListDom.find(".select");
            selectChatDom.find(".chat-last-msg").text(getChatRoomMsg(msgBody));
            //设置是否解散群聊和是否被移除群聊
            setIsDissolve();
            // 更新菜单
            refreshContextMenu();
        }
        //更新未读消息集合
        unreadChatIdSet.delete(msgBody.chatId);
        if (!mobile && toastCount < 5) {
            //刷新聊天列表
            setTimeout(function () {
                refreshChatRoomListDom();
            }, 600);
        }
    } else if (toastCount < 5) {
        //刷新聊天列表
        setTimeout(function () {
            refreshChatRoomListDom();
        }, 600);
    }
    //更新未读消息总数
    updateChatTotalUnreadCount();
}

function getChatRoomMsg(msgBody) {
    let msg = msgBody.msg;
    if (msgBody.msgType === MsgType.IMAGE) {
        msg = "[图片]";
    } else if (msgBody.msgType === MsgType.FILE) {
        msg = "[文件]";
    } else if (msgBody.msgType === MsgType.VOICE) {
        msg = "[语音]";
    } else if (msgBody.msgType === MsgType.VIDEO) {
        msg = "[视频]";
    } else if (msgBody.msgType === MsgType.SYSTEM || msgBody.msgType === MsgType.SYSTEM_WARNING) {
        msg = "[系统]";
    } else if (msgBody.msgType === MsgType.EVENT) {
        msg = "[事件]";
    } else if (msgBody.msgType === MsgType.BIZ_CARD) {
        msg = "[个人名片]";
    } else if (msgBody.msgType === MsgType.GROUP_BIZ_CARD) {
        msg = "[群名片]";
    } else if (msgBody.msgType === MsgType.AUDIO_CALL) {
        msg = "[语音通话]";
    } else if (msgBody.msgType === MsgType.VIDEO_CALL) {
        msg = "[视频通话]";
    }
    return msg;
}

function updateChatTotalUnreadCount() {
    let totalUnreadDom = $("#top-chat-unread");
    let totalUnreadCount = unreadChatIdSet.size;
    if (totalUnreadCount === 0) {
        totalUnreadDom.addClass("hide");
    } else {
        totalUnreadDom.removeClass("hide");
    }
    totalUnreadDom.attr("unread-count", totalUnreadCount);
    totalUnreadDom.text(totalUnreadCount);
}

function getImChatUser(msgBody) {
    let user;
    if (msgBody.sendUserId === chatUser.id) {
        //发送者是自己
        user = chatUser;
        user.msgClass = "self";
    } else {
        //获取其他聊天对象用户
        user = getOtherSendUser(msgBody);
        user.msgClass = "other";
        if (!user) {
            logger.info("聊天用户不存在");
            return;
        }
    }
    return user;
}

/**
 * 获取其他发送者用户信息
 * @param msgBody
 * @returns {{nickname: (*|jQuery), id: (*|jQuery), avatar: (*|jQuery)}|{nickname: *, id: *, avatar: (string|*)}|null}
 */
function getOtherSendUser(msgBody) {
    if (ChatType.GROUP === msgBody.chatType) {
        //群聊从群成员中获取用户信息
        return getGroupSendUser(msgBody);
    } else {
        //TODO 第一次进来没有选择聊天室时 chatToUser为空
        return chatToUser;
    }
}

/**
 * 获取群成员用户信息
 * @param msgBody
 * @returns {null|{nickname: *, id: *, avatar: (string|*)}}
 */
function getGroupSendUser(msgBody) {
    let groupMembers = JSON.parse(localStorage.getItem(GROUP_MEMBER_PREFIX + msgBody.toUserId));
    if (!groupMembers) {
        logger.info("群成员不存在,toUserId:", msgBody.toUserId);
        return null;
    }
    for (let i = 0; i < groupMembers.length; i++) {
        let groupMember = groupMembers[i];
        if (msgBody.sendUserId === groupMember.userId) {
            return {
                id: groupMember.userId,
                groupId: groupMember.groupId,
                nickname: groupMember.nickname,
                avatar: groupMember.avatar
            };
        }
    }
}

function operationFriendApplyMsg(msgBody) {
    //添加到未读集合
    unreadFriendApplyIdSet.add(msgBody.chatId);
    updateFriendTotalUnreadCount();

    toastsInfo("收到一条好友申请信息,点击查看!", msgBody.chatId, 'friend');
    let friendApplyListDom = $("#friend-apply-list");
    if (!mobile && (!friendApplyListDom || friendApplyListDom.length === 0)) {
        return;
    }
    let friendApplyMsgDom = $("#friend-apply-msg-list");
    let applyId = friendApplyMsgDom.attr("apply-id");
    if (friendApplyMsgDom && friendApplyMsgDom.length > 0 && applyId && applyId === msgBody.chatId) {
        if (!mobile) {
            getFriendApplyList();
        }
        if (msgBody.sendUserId === chatUser.id) {
            friendApplyMsgDom.append(` <li>我: ${msgBody.msg}</li>`);
        } else {
            friendApplyMsgDom.append(` <li>${friendApplyMsgDom.attr("nickname")}: ${msgBody.msg}</li>`);
        }
        //更新未读消息总数
        unreadFriendApplyIdSet.delete(msgBody.chatId);
        updateFriendTotalUnreadCount();
    } else {
        // 更新未读消息数
        updateFriendUnreadCount(msgBody, friendApplyListDom);
    }
}

function updateFriendTotalUnreadCount() {
    let totalUnreadDom = $("#top-friend-unread");
    let totalUnreadDom2 = $(".friend-btn .top-unread");
    let totalUnreadCount = unreadFriendApplyIdSet.size;
    if (totalUnreadCount === 0) {
        totalUnreadDom.addClass("hide");
        totalUnreadDom2.addClass("hide");
    } else {
        totalUnreadDom.removeClass("hide");
        totalUnreadDom2.removeClass("hide");
    }
    totalUnreadDom.attr("unread-count", totalUnreadCount).text(totalUnreadCount);
    totalUnreadDom2.attr("unread-count", totalUnreadCount).text(totalUnreadCount);
}

function updateFriendUnreadCount(msgBody, friendApplyListDom) {
    if (!friendApplyListDom || friendApplyListDom.length === 0) {
        return;
    }
    for (let i = 0; i < friendApplyListDom.children().length; i++) {
        let applyInfoDom = friendApplyListDom.children().eq(i);
        let fApplyId = applyInfoDom.attr("apply-id");
        if (msgBody.chatId === fApplyId) {
            let unreadCountDom = applyInfoDom.find(".friend-unread");
            let unreadCount = parseInt(unreadCountDom.attr("unread-count"));
            unreadCount = unreadCount + 1;
            if (unreadCount > 0) {
                unreadCountDom.removeClass("hide");
            }else {
                unreadCountDom.addClass("hide");
            }
            unreadCountDom.attr("unread-count", unreadCount);
            unreadCountDom.text(unreadCount);
            if (msgBody.sendUserId === chatUser.id) {
                applyInfoDom.find(".apply-msg").text("我: " + msgBody.msg);
            } else {
                applyInfoDom.find(".apply-msg").text(applyInfoDom.attr("nickname") + ": " + msgBody.msg);
            }
            break;
        }
    }
}

function operationFriendAgreeMsg(msgBody) {
    toastsInfo("好友申请已通过,点击查看!", msgBody.chatId, 'friend');
    let friendApplyInfoDom = $("#friend-apply-info");
    let applyId = $("#friend-apply-id").val();
    if (!friendApplyInfoDom || friendApplyInfoDom.length === 0) {
        return;
    }
    if (applyId === msgBody.chatId) {
        friendApplyInfoDom.find(".apply-replay-input").addClass("hide");
        friendApplyInfoDom.find(".apply-status").text("已添加");
    }

    let friendApplyListDom = $("#friend-apply-list");
    if (!friendApplyListDom || friendApplyInfoDom.length === 0) {
        return;
    }
    for (let i = 0; i < friendApplyListDom.children().length; i++) {
        let applyInfoDom = friendApplyListDom.children().eq(i);
        let applyId = applyInfoDom.attr("apply-id");
        if (applyId === msgBody.chatId) {
            applyInfoDom.find(".apply-status").removeClass("hide").text("已添加");
            applyInfoDom.find(".agree-btn").addClass("hide");
            break;
        }
    }
}

function revokeMsgHan(msgBody) {
    removeRevokeMsg(msgBody);
    operationChatMsg(msgBody);
}

function removeRevokeMsg(msgBody) {
    let chatMsgListDom = $("#chat-msg-list");
    if (chatMsgListDom && chatMsgListDom.length > 0 && chatToUser && chatToUser.chatId === msgBody.chatId) {
        for (let i = 0; i < chatMsgListDom.children().length; i++) {
            let msgDom = chatMsgListDom.children().eq(i).find(".msg-text");
            let msgId = msgDom.attr("msg-id");
            if (parseInt(msgId) === msgBody.revokeId) {
                msgDom.closest("li").remove();
            }
        }
    }
}

/**
 * 退出或加入群聊
 * @param msgBody
 */
function outOrJoinGroup(msgBody) {
    setRemoteGroupMemberListToLocalStore(msgBody.toUserId);
    operationChatMsg(msgBody);
}


function toastsClick() {
    let extD = this.extraData;
    if (!extD) {
        return;
    }
    let action = extD.action;
    if (action === "chat") { //收到聊天消息
        let chatList = $("#chat-list");
        if (chatList.length === 0) {
            //缓存1天聊天id
            setCookieDay(PREV_CHAT_ID_KEY, extD.chatId, 1);
            loadChatRoom();
        } else {
            //找到对应的聊天窗口并点击
            toMsgList(chatList, extD.chatId);
        }
    } else if (action === "friend") { //收到好友申请
        let friendApplyList = $("#friend-apply-list");
        if (friendApplyList.length === 0) {
            loadChatFriend();
        } else {
            //找到对应的好友窗口并点击
            toFriendApplyInfo(friendApplyList, extD.chatId);
        }
    }
}

let toastCount = 1

function toastsInfo(msg, chatId, action) {
    let prevChatId = $.cookie(PREV_CHAT_ID_KEY);
    if (chatId === prevChatId && $("#chat-msg-list").length > 0) {
        return;
    }
    let tstCount = $.cookie(TOAST_COUNT_KEY);
    if (tstCount) {
        toastCount = parseInt(tstCount) + 1;
    }
    if (toastCount > 5) {
        logger.info("信息提示框超过5个,toastCount:", toastCount);
        return;
    }
    setCookie(TOAST_COUNT_KEY, toastCount, 2);
    toastr.options.extraData = {chatId: chatId, action: action}
    toastr.info(msg);
}

function toMsgList(chatList, chatId) {
    //找到对应的聊天窗口并点击
    for (let i = 0; i < chatList.children().length; i++) {
        let chat = chatList.children().eq(i);
        if (chat.attr("chat-id") === chatId) {
            $(chat).click();
        }
    }
}

function toFriendApplyInfo(friendApplyList, chatId) {
    //找到对应的好友窗口并点击
    for (let i = 0; i < friendApplyList.children().length; i++) {
        let applyInfoDom = friendApplyList.children().eq(i);
        let applyId = applyInfoDom.attr("apply-id");
        if (applyId === chatId) {
            $(applyInfoDom).click();
        }
    }
}