let ip = HOST;
let port = ws_port;

let heartbeatTimeout = 5000; // 心跳超时时间，单位：毫秒
let reconnInterval = 1000; // 重连间隔时间，单位：毫秒

let binaryType = 'blob'; // 'blob' or 'arraybuffer';//arraybuffer是字节
let handler = new IMHandler()


let tiows

/**
 * 初始化 websocket
 */
function initWs() {
    let token = $.cookie(TOKEN_KEY);
    if (!token) {
        logger.info("未登录");
        logout();
        return;
    }
    let queryString = 'token=' + token;

    let param = "";
    tiows = new tio.ws(ws_protocol, ip, port, queryString, param, handler, heartbeatTimeout, reconnInterval, binaryType);
    tiows.connect();
}

/**
 * 发送文本消息
 */
function sendMsg() {
    if (!checkChatToUser()) {
        return;
    }
    let contentInput = $('#content-input');
    let msg = contentInput.val();
    if (msg === '' || msg.trim() === '') {
        alert("内容不能为空");
        contentInput.val("");
        return;
    }
    let chatType = ChatType.SINGLE;
    if (chatToUser.groupId > 0) {
        chatType = ChatType.GROUP;
    }
    let msgObj = new TioMessage(MsgType.TEXT, chatType, msg, chatUser.id, chatToUser.id);
    tiows.send(JSON.stringify(msgObj));
    contentInput.val("").addClass("content-input");
    $("#send-msg-btn").addClass("hide").attr("disabled", true);
    $("#nav-operation").removeClass("hide");
}

/**
 * 发送各种对象格式的信息(图片,视频,文件,名片,文本等)
 * @param contentObj json格式的消息
 * @param msgType 消息类型
 */
function sendObjMsg(contentObj, msgType) {
    if (!checkChatToUser()) {
        return;
    }
    let chatType = ChatType.SINGLE;
    if (parseInt(chatToUser.groupId) > 0) {
        chatType = ChatType.GROUP;
    }
    let msgObj = new TioMessage(msgType, chatType, contentObj, chatUser.id, chatToUser.id);
    tiows.send(JSON.stringify(msgObj));
    $("#send-msg-btn").attr("disabled", true);
}

/**
 * 发送视频/语音信息(图片,视频,文件,名片等)
 * @param contentObj json格式的消息
 * @param msgType 消息类型
 * @param toUserId 接收用户ID
 */
function sendAVCallMsg(contentObj, msgType, toUserId) {
    // logger.info("发送视频/语音信息,content:", contentObj, "msgType:", msgType, "toUserId:", toUserId);
    let chatType = ChatType.SINGLE;
    let msgObj = new TioMessage(msgType, chatType, contentObj, chatUser.id, toUserId);
    tiows.send(JSON.stringify(msgObj));
}

function checkChatToUser() {
    if (!chatToUser) {
        alert("请选择聊天对象");
        return false;
    }
    if (chatToUser.isDissolve === "true") {
        alert("当前群聊已解散");
        return false;
    }
    if (parseInt(chatToUser.groupId) > 0 && !isGroupMember()) {
        alert("你被移除群聊，不能发送消息");
        return false;
    }
    return true;
}

$(function () {
    initWs();
});