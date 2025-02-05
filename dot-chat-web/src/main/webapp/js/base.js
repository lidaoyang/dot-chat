let logger = new Logger('debug');

let env = "dev";// dev:开发环境,test:测试环境,prod:生产环境
if (location.host !== "dot-chat.jrmall.cn") {
    env = "local"
}

let HOST, BASE_URL, ws_protocol, ws_port; // ws 或 wss;
switch (env) {
    case "dev":
        ws_protocol = 'wss';
        ws_port = "443";//9326
        HOST = "dot-chat.api.jrmall.cn";
        BASE_URL = "https://" + HOST + "/";
        break;
    case "prod":
        ws_protocol = 'wss';
        ws_port = "443";
        HOST = "chat.im.dot.cn";
        BASE_URL = "https://" + HOST + "/";
        break;
    default:
        ws_protocol = 'wss';
        ws_port = "9326";
        HOST = "172.16.85.134";
        BASE_URL = "https://" + HOST + ":8089/";
}
// 定义常量对象
const SYS_API_PREFIX = "api/sys"; // 接口前缀
const MSG_API_PREFIX = "api/msg"; // 接口前缀
const SYS_URL_PREFIX = BASE_URL + SYS_API_PREFIX; // url前缀
const MSG_URL_PREFIX = BASE_URL + MSG_API_PREFIX; // url前缀
const TOKEN_KEY = "Authorization";
const ENT_ID_KEY = "enterpriseId";
const USER_ID_KEY = "userId";
const CHAT_USER_KEY = "chatUser";
const LAST_ACCESS_TIME_KEY = "lastAccessedTime";
const EXPIRES_IN = "expiresIn";
const PREV_CHAT_ID_KEY = "prev_chat_id"; // 上次选择的聊天id
const GROUP_MEMBER_PREFIX = "group_member_";//群成员前缀
const CONTENT_TYPE_JSON = "application/json";
const TOAST_COUNT_KEY = "toast_count";


/**
 * 本地用户信息
 */
let chatUser = JSON.parse(localStorage.getItem(CHAT_USER_KEY));

/**
 * 消息发送用户/群组信息
 */
let chatToUser;
/**
 * 未读聊天室ID集合
 * @type {Set<any>}
 */
let unreadChatIdSet = new Set();

/**
 * 未读好友申请ID集合
 * @type {Set<any>}
 */
let unreadFriendApplyIdSet = new Set();

/**
 * 聊天室列表缓存
 */
let chatRoomListCache = [];


/**
 * 消息类型
 * @type {{SYSTEM: string, VOICE: string, NOTICE: string, IMAGE: string, VIDEO: string, TEXT: string, HEART_BEAT: string, GROUP_BIZ_CARD: string, FILE: string, WARNING: string, BIZ_CARD: string, EVENT: string}}
 */
const MsgType = {
    TEXT: "TEXT", // 文本消息
    IMAGE: "IMAGE", // 图片消息
    VOICE: "VOICE", // 语音消息
    VIDEO: "VIDEO", // 视频消息
    FILE: "FILE", // 文件消息
    BIZ_CARD: "BIZ_CARD", // 个人名片消息
    GROUP_BIZ_CARD: "GROUP_BIZ_CARD",//群名片消息
    SYSTEM: "SYSTEM", // 系统消息
    SYSTEM_WARNING: "SYSTEM_WARNING", // 系统警告消息
    EVENT: "EVENT", //事件消息
    WARNING: "WARNING", // 预警消息
    HEART_BEAT: "HEART_BEAT", // 心跳监消息
    NOTICE: "NOTICE", // 通知消息
    VIDEO_CALL: "VIDEO_CALL", //视频通话
    AUDIO_CALL: "AUDIO_CALL" //语音通话
};

/**
 * 聊天类型
 * @type {{GROUP: string, SINGLE: string}}
 */
const ChatType = {
    SINGLE: "SINGLE", // 单聊
    GROUP: "GROUP" // 群聊
};

/**
 * 事件类型
 * @type {{FRIEND_APPLY: string, FRIEND_AGREE: string, FRIEND_REJECT: string, GROUP_CREATE: string, GROUP_APPLY: string, GROUP_ACCEPT: string, GROUP_REJECT: string, GROUP_JOIN: string, GROUP_KICK: string, GROUP_EXIT: string, GROUP_DISMISS: string, GROUP_BAN: string, REVOKE_MSG: string}}
 */
const EventType = {
    FRIEND_APPLY: "FRIEND_APPLY",//好友申请
    FRIEND_AGREE: "FRIEND_AGREE",//同意好友申请
    FRIEND_REJECT: "FRIEND_REJECT",//拒绝好友
    GROUP_CREATE: "GROUP_CREATE",// 创建群聊
    GROUP_APPLY: "GROUP_APPLY",//入群申请
    GROUP_ACCEPT: "GROUP_ACCEPT",//群接受
    GROUP_REJECT: "GROUP_REJECT",//群拒绝
    GROUP_JOIN: "GROUP_JOIN",//加入群聊
    GROUP_KICK: "GROUP_KICK",//群踢出
    GROUP_EXIT: "GROUP_EXIT",//群退出
    GROUP_DISMISS: "GROUP_DISMISS",//群解散
    GROUP_BAN: "GROUP_BAN", //群禁言
    REVOKE_MSG: "REVOKE_MSG" //撤销消息
}
/**
 * 设备类型
 * @type {{PC: string, MOBILE: string}}
 */
const DEVICE_TYPE = {
    PC: "PC",
    MOBILE: "MOBILE",
    SYS: "SYS"
}

/**
 * 好友来源
 * @type {{SEARCH: string, CARD: string, GROUP: string}}
 */
const FRIEND_SOURCE = {
    SEARCH: "SEARCH",//通过搜索添加
    CARD: "CARD",//通过名片添加
    GROUP: "GROUP"//通过群聊添加
}

/**
 * 群来源
 * @type {{SEARCH: string, CARD: string, INVITE: string, QRCODE: string}}
 */
const GROUP_SOURCE = {
    SEARCH: "SEARCH",//通过搜索入群
    CARD: "CARD",//通过名片入群
    INVITE: "INVITE",//通过邀请入群
    QRCODE: "QRCODE"//通过二维码入群
}

/**
 * 消息对象
 * @param msgType 消息类型
 * @param chatType 聊天类型
 * @param msgContent 消息内容
 * @param sendUserId 发送用户ID
 * @param toUserId 接收用户ID
 * @constructor
 */
function TioMessage(msgType, chatType, msgContent, sendUserId, toUserId) {
    this.msgType = msgType;
    this.chatType = chatType;
    this.msg = msgContent;
    this.sendUserId = sendUserId;
    this.toUserId = toUserId;
    this.deviceType = mobile ? DEVICE_TYPE.MOBILE : DEVICE_TYPE.PC;
}

/**
 * 文件消息对象(图片,视频,文件)
 * @param coverUrl 封面图片(上传视频时有值)
 * @param fileName 文件名称
 * @param newFileName 新文件名称(上传后的新文件名称)
 * @param fileSize 文件大小
 * @param extName 扩展名
 * @param fileUrl 文件URL
 * @param status 文件上传状态(1:上传完成;0:上传中)
 * @constructor
 */
function MsgFileO(fileName, newFileName, fileSize, extName, fileUrl, status, coverUrl) {
    this.fileName = fileName;
    this.newFileName = newFileName;
    this.fileSize = fileSize;
    this.extName = extName;
    this.fileUrl = fileUrl;
    this.status = status;
    if (coverUrl) {
        this.coverUrl = coverUrl;
    }
}

/**
 * 名片消息对象(个人名片/群名片)
 * @param userId 用户ID/群ID
 * @param nickname 用户昵称/群名称
 * @param avatar 用户头像/群头像
 * @constructor
 */
function MsgCardO(userId, nickname, avatar) {
    this.userId = userId;
    this.nickname = nickname;
    this.avatar = avatar;
}

/**
 * 通话消息对象(语音通话/视频通话)
 * @param callType 通话类型
 * @param msgId 消息ID
 * @param candidate 候选者对象
 * @param desc 创建offer或answer产生session描述对象,(包含字段 type,sdp)
 * @param mobile 是否移动端发起
 * @constructor
 */
function MsgCallO(callType, msgId, candidate, desc, mobile) {
    this.callType = callType;
    this.msgId = msgId;
    this.mobile = mobile;
    this.candidate = candidate;
    this.desc = desc;
}

/**
 * 通话消息类型枚举,(描述1:通话发起方描述,描述2:通话接收方描述)
 * @type {{cancel: string[], offer: string[], candidate: string[], refuse: string[], answer: string[], no_answer: string[], invite: string[], accept: string[], hangup: string[]}}
 */
const CallType = {
    invite: ["invite", "通话邀请", "邀请通话"],
    accept: ["accept", "接受通话", "接受通话"],
    offer: ["offer", "邀请信令", "邀请信令"],
    answer: ["answer", "应答信令", "应答信令"],
    candidate1: ["candidate1", "候选者1", "候选者1"],
    candidate2: ["candidate2", "候选者2", "候选者2"],
    refuse: ["refuse", "对方已拒绝", "已拒绝"],// 拒绝通话
    cancel: ["cancel", "已取消", "对方已取消"],// 取消通话
    hangup: ["hangup", "通话时长", "通话时长"],//通话时长 00:11 挂断通话
    no_answer: ["no_answer", "对方无应答", "未应答"],// 无人接听
    busying: ["busying", "对方正忙", "忙线未接听"],// 忙线未接听
    dropped: ["dropped", "通话中断", "通话中断"], //一个人异常下线通话中断
}

/**
 * 获取通话类型描述
 * @param sendUserId 信息发送用户ID
 * @param type 通话类型
 * @returns {*}
 */
function getCallTypeDesc(sendUserId, type) {
    if (chatUser && chatUser.id === sendUserId) {
        return CallType[type][1];
    }
    return CallType[type][2];
}

/**
 * 消息解析json
 * @param chatMsg
 * @returns {{fileName: string, newFileName: string, fileUrl: string}}
 */
function msgParseJson(chatMsg) {
    let msgObj = {fileName: "", fileUrl: "", newFileName: ""};
    if (chatMsg.msgType === MsgType.IMAGE || chatMsg.msgType === MsgType.VIDEO || chatMsg.msgType === MsgType.FILE) {
        msgObj = JSON.parse(chatMsg.msg);
    }
    return msgObj;
}


function Page(limit, totalPage, isLastPage) {
    this.limit = limit;
    this.totalPage = totalPage;
    this.isLastPage = isLastPage;
}

const PAGE_FLIPPING_TYPE = {
    // 翻页类型(PULL_UP:上拉翻页;HIS_PULL_UP:历史上拉翻页;PULL_DOWN:下拉翻页;LOCATE:定位翻页;FIRST:首次翻页;)
    PULL_UP: "PULL_UP",
    HIS_PULL_UP: "HIS_PULL_UP",
    PULL_DOWN: "PULL_DOWN",
    LOCATE: "LOCATE",
    FIRST: "FIRST"
}

function ChatToUser(chatId, id, groupId, nickname, avatar) {
    this.chatId = chatId;
    this.id = id;
    this.groupId = groupId;
    this.nickname = nickname;
    this.avatar = avatar;
}


function setCookieDay(name, value, days) {
    let expire = new Date();
    expire.setTime(expire.getTime() + (days * 24 * 60 * 60 * 1000));
    $.cookie(name, value, {
        path: '/',//cookie的作用域
        expires: expire
    });
}

function setCookieMin(name, value, min) {
    let expire = new Date();
    expire.setTime(expire.getTime() + (min * 60 * 1000));
    $.cookie(name, value, {
        path: '/',//cookie的作用域
        expires: expire
    });
}

function setCookie(name, value, second) {
    let expire = new Date();
    expire.setTime(expire.getTime() + (second * 1000));
    $.cookie(name, value, {
        path: '/',//cookie的作用域
        expires: expire
    });
}

function deleteCookie(name) {
    $.removeCookie(name, {
        path: '/'
    });
}


/**
 * ajax请求(不鉴权)
 * @param url 请求地址
 * @param method GET/POST
 * @param data 请求参数
 * @param contentType contentType
 * @param successFn 成功回调
 */
function ajaxRequestNotAuth(url, method, data, contentType, successFn) {
    $.ajax({
        url: url,
        type: method,
        contentType: contentType || "application/x-www-form-urlencoded; charset=utf-8",
        data: data,
        success: function (res) {
            if (res.code === 401) {
                logger.error("token过期");
                deleteUserCookie();
                return;
            }
            if (res.code !== 200) {
                logger.error("获取数据失败,data:", data, "res:", res);
            }
            successFn(res);
        },
        error: function (err) {
            console.error(err);
            // alert("请求错误,请稍后重试,msg:" + err.statusText);
        }
    });
}

/**
 * ajax请求
 * @param url 请求地址
 * @param method GET/POST
 * @param data 请求参数
 * @param contentType contentType
 * @param successFn 成功回调
 */
function ajaxRequest(url, method, data, contentType, successFn) {
    let token = $.cookie(TOKEN_KEY);
    if (!token && url !== `${SYS_URL_PREFIX}/user/login`) {
        logger.error("token过期");
        deleteUserCookie();
        return;
    }
    $.ajax({
        url: url,
        type: method,
        contentType: contentType || "application/x-www-form-urlencoded; charset=utf-8",
        headers: {
            'Authorization': token
        },
        data: data,
        success: function (res) {
            if (res.code === 401) {
                logger.error("token过期");
                deleteUserCookie();
                return;
            }
            if (res.code !== 200) {
                logger.error("操作失败,data:", data, "res:", res);
            }
            successFn(res);
        },
        error: function (err) {
            console.error(err);
            // alert("请求错误,请稍后重试,msg:" + err.statusText);
        }
    });
}


/**
 * ajax同步请求
 * @param url 请求地址
 * @param method GET/POST
 * @param data 请求参数
 * @param contentType contentType
 * @param successFn 成功回调
 */
function ajaxSyncRequest(url, method, data, contentType, successFn) {
    let token = $.cookie(TOKEN_KEY);
    if (!token && url !== `${SYS_URL_PREFIX}/user/login` && url !== `${SYS_URL_PREFIX}/user/registerAndLogin`) {
        logger.error("token过期");
        deleteUserCookie();
        return;
    }
    $.ajax({
        url: url,
        type: method,
        async: false,
        contentType: contentType || "application/x-www-form-urlencoded; charset=utf-8",
        headers: {
            'Authorization': token
        },
        data: data,
        success: function (res) {
            if (res.code === 401) {
                logger.error("token过期");
                deleteUserCookie();
                return;
            }
            if (res.code !== 200) {
                logger.error("获取数据失败,data:", data, "res:", res);
            }
            successFn(res);
        },
        error: function (err) {
            console.error(err);
            // alert("请求错误,请稍后重试,msg:" + err.statusText);
        }
    });
}

function myAlert(title, msg, error, callback) {
    let tit;
    let imgSrc;
    if (error === 'err') {
        tit = "错误!";
        imgSrc = "../ico/error.png"
    } else if (error === "warn") {
        tit = "警告!";
        imgSrc = "../ico/warn.png"
    } else if (error === "success") {
        tit = "成功!";
        imgSrc = "../ico/success.png"
    } else {
        tit = "提示!";
        imgSrc = "../ico/info.png"
    }
    if (title) {
        tit = title;
    }
    let dom =
        `<div id="dialog" style="font-size: 13px;padding-top: 10px;display: none">
            <div style="float:left;margin-right:5px;">
                <img src="${imgSrc}" style="width: 26px;height: 26px;" alt="img"/> 
             </div>
            <div style="float: left;width: 230px;font-size: 13px;margin-top: 3px;">${msg}</div>
      </div>`;
    $("body").append(dom);
    $("#dialog").dialog({
        title: tit,
        modal: true,// 设置为模态对话框
        resizable: false,// 设置为不可调整大小
        dialogClass: 'z-index-1010',
        close: function (event, ui) {
            $(this).remove();
            if (callback) {
                callback();
            }
        }
    });
}

function myConfirm(title, msg, successFn) {
    let dom =
        `<div id="dialog-confirm" style="display: none">
            <span class="ui-icon ui-icon-alert" style="float:left; margin:12px 12px 20px 0;"></span>
            <p class="confirm-msg">${msg}</p>
        </div>`;
    let $body = $("body");
    if ($body.find("#dialog-confirm").length > 0) {
        $("#dialog-confirm .confirm-msg").text(msg);
    } else {
        $body.append(dom);
    }

    $("#dialog-confirm").dialog({
        title: title,
        resizable: false,
        height: "auto",
        modal: true,
        buttons: {
            "确认": function () {
                successFn();
                $(this).dialog("close");
                // $(this).remove();
            },
            "取消": function () {
                $(this).dialog("close");
                // $(this).remove();
            }
        }
    });
}

function welcomeAlert() {
    myAlert('欢迎', '欢迎进入聊天室,开始愉快的聊天吧!', 'info', function () {
        callRingtoneAudio.muted = false;
        callRingtoneAudio.volume = 1;
        //检查是否有未接的通话
        checkAndOpenCallDialog();
    });
}

/**
 * 判断访问类型是PC端还是手机端
 */
function isMobile() {
    let userAgentInfo = navigator.userAgent;

    let mobileAgents = ["Android", "iPhone", "SymbianOS", "Windows Phone", "iPad", "iPod", "OpenHarmony", "Phone"];

    let mobile_flag = false;

    //根据userAgent判断是否是手机
    for (let v = 0; v < mobileAgents.length; v++) {
        if (userAgentInfo.indexOf(mobileAgents[v]) > 0) {
            mobile_flag = true;
            break;
        }
    }

    let screen_width = window.screen.width;
    let screen_height = window.screen.height;

    //根据屏幕分辨率判断是否是手机
    if (screen_width < 500 && screen_height < 800) {
        mobile_flag = true;
    }

    return mobile_flag;
}

let mobile = isMobile(); // false为PC端，true为手机端


function logout() {
    let url = `${SYS_URL_PREFIX}/user/logout`;
    ajaxRequest(url, "post", {}, null, function (res) {
        if (res.code !== 200) {
            logger.error("退出失败");
            myAlert('', res.message, 'err');
            return;
        }
        deleteUserCookie();
    });
}

function deleteUserCookie() {
    if (typeof (tiows) !== "undefined") {
        tiows.close("退出关闭");
    }
    deleteCookie(TOKEN_KEY);
    deleteCookie(LAST_ACCESS_TIME_KEY);
    deleteCookie(EXPIRES_IN);
    deleteCookie(PREV_CHAT_ID_KEY);
    localStorage.clear();
    toLogin();
}

function toLogin() {
    if (mobile) {
        location.href = "../mobile/login.html";
    } else {
        location.href = "../login.html";
    }
}

let autoRefreshTokenTimer = 0;

function autoRefreshToken() {
    if (autoRefreshTokenTimer === 0) {
        logger.info("添加自动刷新token timer");
        let lastAccessTime = $.cookie(LAST_ACCESS_TIME_KEY);
        let expiresIn = $.cookie(EXPIRES_IN);
        if (lastAccessTime && expiresIn) {
            let now = new Date().getTime();
            let lastAccessTimeNum = parseInt(lastAccessTime);
            let expiresInNum = parseInt(expiresIn);
            if (now - lastAccessTimeNum >= expiresInNum * 1000) {
                refreshToken();
            } else {
                autoRefreshTokenTimer = setTimeout(refreshToken, expiresInNum * 1000 - (now - lastAccessTimeNum) - 3 * 60 * 1000);//提前3分钟刷新token
            }
        }
    }
}

function refreshToken() {
    logger.info("刷新token");
    let url = `${SYS_URL_PREFIX}/user/refreshToken`;
    ajaxRequest(url, "post", {}, null, function (res) {
        if (res.code !== 200) {
            logger.error("刷新失败");
            myAlert('', res.message, 'err');
            return;
        }
        autoRefreshTokenTimer = 0;
        saveTokenToCookie(res.data);
        // 自动刷新token
        autoRefreshToken();
    });
}


function saveTokenToCookie(data) {
    setCookie(TOKEN_KEY, data.token, data.expiresIn);
    setCookie(LAST_ACCESS_TIME_KEY, data.lastAccessedTime, data.expiresIn);
    setCookie(EXPIRES_IN, data.expiresIn, data.expiresIn);
}

/**
 * 注册并登录
 */
function registerAndLogin() {
    let account = $(".register .account").val();
    let password = $(".register .password").val();
    let repassword = $(".register .repassword").val();

    if (account === "" || password === "") {
        myAlert("", "请输入账号和密码", 'warn');
        return;
    }
    let regExp = /^1[3-9]\d{9}$/;
    if (!regExp.test(account)) {
        myAlert("", "手机格式不正确", 'warn');
        return;
    }
    if (password.length < 6 || password.length > 20) {
        myAlert("", "密码长度不能小于6位或大于20位", 'warn');
        return;
    }
    if (repassword === "") {
        myAlert("", "请输入确认密码", 'warn');
        return;
    }
    if (password !== repassword) {
        myAlert("", "两次密码不一致", 'warn');
        return;
    }
    let url = `${SYS_URL_PREFIX}/user/registerAndLogin`;
    let data = {
        account: account,
        password: password,
        nickname: $(".register .nickname").val()
    };
    ajaxSyncRequest(url, 'post', data, null, function (res) {
        if (res.code !== 200) {
            myAlert("注册登录失败", res.message, 'err');
            return;
        }
        setCookie(TOKEN_KEY, res.data.token, res.data.expiresIn);
        setCookie(LAST_ACCESS_TIME_KEY, res.data.lastAccessedTime, res.data.expiresIn);
        //获取聊天用户信息
        getChatUser();
    });
}

/**
 * 登录
 */
function login() {
    let account = $(".login .account").val();
    let password = $(".login .password").val();
    if (account === "" || password === "") {
        myAlert("", "请输入账号和密码", 'warn');
        return;
    }
    let url = `${SYS_URL_PREFIX}/user/login`;
    let data = JSON.stringify({
        account: account,
        password: password
    });
    ajaxSyncRequest(url, 'post', data, 'application/json', function (res) {
        if (res.code !== 200) {
            myAlert("登录失败", res.message, 'err');
            return;
        }
        // 保存token
        saveTokenToCookie(res.data);
        // 自动刷新token
        // autoRefreshToken();
        //获取聊天用户信息
        getChatUser();
    });
}

function getChatUser() {
    let url = `${MSG_URL_PREFIX}/chat/user/get`;
    ajaxSyncRequest(url, "get", {}, null, function (res) {
        if (res.code !== 200) {
            myAlert("获取用户信息失败", res.message, 'err');
            return;
        }
        chatUser = res.data;
        localStorage.setItem(CHAT_USER_KEY, JSON.stringify(res.data));
        if (mobile) {
            location.href = "/mobile/index.html";
        } else {
            location.href = "/index.html";
        }
    });
}

/**
 * 刷新聊天列表DOM
 */
function refreshChatRoomListDom() {
    refreshChatRoomList((data) => generateChatRoomDom(data));
}

/**
 * 刷新聊天列表
 */
function refreshChatRoomList(successFn) {
    let url = `${MSG_URL_PREFIX}/chat/room/refresh/list`;
    ajaxSyncRequest(url, "get", {}, null, function (res) {
        if (res.code !== 200) {
            logger.info("获取聊天列表失败");
            myAlert('', res.message, "err");
            return;
        }
        chatRoomListCache = res.data;
        if (successFn) {
            successFn(res.data);
        }
    });
}

function chatRoomListSearch(keyword) {
    let chatRoomList = [];
    if (chatRoomListCache.length === 0) {
        refreshChatRoomList();
    }
    if (keyword === "") {
        chatRoomList = chatRoomListCache;
    } else {
        for (let i = 0; i < chatRoomListCache.length; i++) {
            if (chatRoomListCache[i].toUser.nickname.indexOf(keyword) !== -1) {
                chatRoomList.push(chatRoomListCache[i]);
            }
        }
    }
    return chatRoomList;
}

function getChatRoomIdList() {
    let chatIdList = [];
    if (chatRoomListCache.length === 0) {
        refreshChatRoomList();
    }
    for (let i = 0; i < chatRoomListCache.length; i++) {
        let chatRoom = chatRoomListCache[i];
        chatIdList.push(chatRoom.chatId);
    }
    return chatIdList;
}

function isMyChatRoom(chatId) {
    let isMyChat = false;
    let chatIdList = getChatRoomIdList();
    for (let i = 0; i < chatIdList.length; i++) {
        if (chatIdList[i] === chatId) {
            isMyChat = true;
        }
    }
    return isMyChat;
}

function userFriendListSearch(keyword, filterGroupId, successFn) {
    let url = `${MSG_URL_PREFIX}/chat/friend/choose/list`;
    ajaxRequest(url, "get", {
        filterGroupId: filterGroupId,
        keywords: keyword
    }, null, function (res) {
        if (res.code !== 200) {
            logger.info("搜索好友列表失败,keyword:", keyword);
            myAlert('', res.message, "err");
            return;
        }
        successFn(res.data);
    });
}

function getChatUserInfo(userId, callback) {
    let url = `${MSG_URL_PREFIX}/chat/user/info`;
    ajaxSyncRequest(url, "get", {userId: userId}, null, function (res) {
        if (res.code !== 200) {
            logger.error("获取用户详情失败,userId:", userId, "res:", res);
            myAlert('', res.message, "err");
            return;
        }
        callback(res.data);
    });
}

function getChatGroupMemberList(groupId) {
    let localKey = GROUP_MEMBER_PREFIX + groupId;
    let memberList = JSON.parse(localStorage.getItem(localKey));
    if (memberList) {
        $(".group-member-num").removeClass("hide").text("(" + memberList.length + ")");
        return;
    }
    let url = `${MSG_URL_PREFIX}/chat/group/member/list`;
    ajaxSyncRequest(url, "get", {groupId: groupId}, null, function (res) {
        if (res.code !== 200) {
            myAlert('', res.message, "err");
            return;
        }
        memberList = res.data;
        localStorage.setItem(localKey, JSON.stringify(memberList));
        $(".group-member-num").removeClass("hide").text("(" + memberList.length + ")");
    });
}

function isGroupMember() {
    let isGroupMember = false;
    let groupManagerList = getLocalGroupMemberList();
    if (!groupManagerList) {
        return true;
    }
    for (let i = 0; i < groupManagerList.length; i++) {
        let groupManager = groupManagerList[i];
        if (groupManager.userId === chatUser.id) {
            isGroupMember = true;
            break;
        }
    }
    return isGroupMember;
}

/**
 * 获取群成员列表
 * @returns {any}
 */
function getLocalGroupMemberList() {
    let memberKey = GROUP_MEMBER_PREFIX + chatToUser.groupId;
    return JSON.parse(localStorage.getItem(memberKey));
}

function setCurrentRemoteGroupMemberListToLocalStore() {
    setRemoteGroupMemberListToLocalStore(chatToUser.groupId);
}


function setRemoteGroupMemberListToLocalStore(groupId) {
    let url = `${MSG_URL_PREFIX}/chat/group/member/list`;
    ajaxSyncRequest(url, "get", {
        groupId: groupId,
        keywords: ""
    }, null, function (res) {
        if (res.code !== 200) {
            logger.error("获取群成员失败,groupId:", groupId, "res:", res);
            myAlert('', res.message, "err");
            return;
        }
        logger.error("获取群成员成功,groupId:", groupId);
        let localKey = GROUP_MEMBER_PREFIX + groupId;
        localStorage.removeItem(localKey);
        localStorage.setItem(localKey, JSON.stringify(res.data));
    });
}

function setMsgPageLastMsgId(chatMsgList, msgPage) {
    if (chatMsgList.length === 0) {
        msgPage.isLastPage = true;
        return;
    }
    msgPage.isLastPage = msgPage.limit > chatMsgList.length;
    msgPage.lastMsgId = chatMsgList[chatMsgList.length - 1].id;
}

function setMsgPageUpLastMsgId(chatMsgList, msgPage) {
    if (chatMsgList.length === 0) {
        msgPage.isLastPage = true;
        return;
    }
    msgPage.isLastPage = msgPage.limit > chatMsgList.length;
    msgPage.lastMsgId = chatMsgList[0].id;
}


/**
 * 注册聊天室列表右键菜单
 */
function registerChatRoomListContextMenu() {
    $.contextMenu({
        selector: '.chat-list .list-box',
        className: "my-context-list",
        callback: function (key, options) {
            let m = "clicked: " + key;
            // logger.info(m, "option:", options);
        },
        events: {
            show: function (opt) {
                // logger.info("show", opt);
                opt.$trigger.addClass("context-menu-active");
                // return false; //返回false, 禁止菜单打开
            },
            hide: function (opt) {
                // logger.info("hide", opt);
                opt.$trigger.removeClass("context-menu-active");
                // return false; //返回false, 禁止菜单关闭
            }
        },
        items: {
            "top": {
                name: "置顶",
                icon: function (opt, $itemElement, itemKey, item) {
                    return "context-menu-icon context-menu-icon-top"
                },
                visible: function (key, opt) {
                    return !opt.$trigger.hasClass("chat-top");
                },
                callback: function (itemKey, opt, e) {
                    let m = "置顶 was clicked " + itemKey;
                    updateTopFlag(opt.$trigger.attr("chat-id"), true);
                }
            },
            "top-cancel": {
                name: "取消置顶",
                icon: function (opt, $itemElement, itemKey, item) {
                    return "context-menu-icon context-menu-icon-top-cancel"
                },
                visible: function (key, opt) {
                    return opt.$trigger.hasClass("chat-top");
                },
                callback: function (itemKey, opt, e) {
                    let m = "取消置顶 was clicked" + itemKey;
                    updateTopFlag(opt.$trigger.attr("chat-id"), false);
                }
            },
            /*"no-disturb": {
                name: "消息免打扰",
                icon: function (opt, $itemElement, itemKey, item) {
                    return "context-menu-icon context-menu-icon-no-disturb"
                },
                visible: function (key, opt) {
                    return !opt.$trigger.hasClass("chat-no-disturb");
                },
                callback: function (itemKey, opt, e) {
                    let m = "消息免打扰 was clicked " + itemKey;
                    updateMsgNoDisturb(opt.$trigger.attr("chat-id"), true, () => opt.$trigger.addClass("chat-no-disturb"));
                }
            },
            "allow-disturb": {
                name: "允许消息通知",
                icon: function (opt, $itemElement, itemKey, item) {
                    return "context-menu-icon context-menu-icon-allow-disturb"
                },
                visible: function (key, opt) {
                    return opt.$trigger.hasClass("chat-no-disturb");
                },
                callback: function (itemKey, opt, e) {
                    let m = "允许消息通知 was clicked" + itemKey;
                    updateMsgNoDisturb(opt.$trigger.attr("chat-id"), false, () => opt.$trigger.removeClass("chat-no-disturb"));
                }
            },*/
            "sep1": "---------",
            "delete": {
                name: "删除",
                icon: "delete",
                callback: function (itemKey, opt, e) {
                    let m = "删除聊天室 was clicked" + itemKey;
                    // logger.info(m, "option:", opt);
                    deleteChatRoom(opt.$trigger);
                }
            }
        }
    });
}

/**
 * 注册聊天记录右键菜单
 */
function registerChatMsgListContextMenu() {
    $.contextMenu({
        selector: '.msg-text-outer .msg-text',
        className: "my-context-list",
        callback: function (key, options) {
            let m = "clicked: " + key;
            // logger.info(m, "option:", options) || alert(m);
        },
        items: {
            "copy": {
                name: "复制",
                icon: function (opt, $itemElement, itemKey, item) {
                    return "context-menu-icon context-menu-icon-copy"
                },
                visible: function (key, opt) {
                    let msgType = opt.$trigger.attr("msg-type");
                    if (msgType === MsgType.TEXT) {
                        return true;
                    }
                },
                callback: function (itemKey, opt, e) {
                    let m = "复制 was clicked " + itemKey;
                    copyToClipboard(opt.$trigger.text());
                }
            },
            "relay": {
                name: "转发...",
                icon: function (opt, $itemElement, itemKey, item) {
                    return "context-menu-icon context-menu-icon-relay"
                },
                callback: function (itemKey, opt, e) {
                    let m = "edit was clicked" + itemKey;
                    // logger.info("itemKey:", itemKey, "option:", opt.$trigger.attr("msg-id"));
                    openRelayMsgUserDialog(opt.$trigger.attr("msg-id"));
                }
            },
            "delete": {
                name: "删除",
                icon: "delete",
                callback: function (itemKey, opt, e) {
                    let m = "删除 was clicked" + itemKey;
                    // logger.info(m, "option:", opt);
                    deleteUserMsg(opt.$trigger.attr("msg-id"));
                }
            },
            "sep1": "---------",
            "revoke": {
                name: "撤回",
                icon: function (opt, $itemElement, itemKey, item) {
                    return "context-menu-icon context-menu-icon-revoke"
                },
                callback: function (itemKey, opt, e) {
                    let m = "删除 was clicked" + itemKey;
                    // logger.info(m, "option:", opt);
                    revokeMsg(opt.$trigger.attr("msg-id"));
                }
            },
            "sep2": "---------",
            "storage": {
                name: "存储...",
                icon: function (opt, $itemElement, itemKey, item) {
                    return "context-menu-icon context-menu-icon-saveas"
                },
                visible: function (key, opt) {
                    let msgType = opt.$trigger.attr("msg-type");
                    if (msgType === MsgType.FILE || msgType === MsgType.IMAGE
                        || msgType === MsgType.VOICE || msgType === MsgType.VIDEO) {
                        return true;
                    }
                },
                callback: function (itemKey, opt, e) {
                    let m = "存储 was clicked " + itemKey;
                    // logger.info(m, "option:", opt);
                    downloadFile(opt.$trigger.attr("file-url"), opt.$trigger.attr("file-name"), opt.$trigger.attr("msg-type"));
                }
            }
        }
    });
}


/**
 * 注册聊天历史记录右键菜单
 */
function registerChatMsgListHisContextMenu() {
    $.contextMenu({
        selector: '.search-msg-item-right',
        className: "my-context-list",
        callback: function (key, options) {
            // let m = "clicked: " + key;
            // logger.info(m, "option:", options) || alert(m);
        },
        items: {
            "locatemsg": {
                name: "定位到聊天位置",
                icon: function (opt, $itemElement, itemKey, item) {
                    return "context-menu-icon context-menu-icon-locatemsg"
                },
                callback: function (itemKey, opt, e) {
                    let m = "edit was clicked" + itemKey;
                    // logger.info("itemKey:", itemKey, "option:", opt.$trigger.attr("msg-id"));
                    msgHistLocate(opt.$trigger.attr("msg-id"));
                }
            },
            "relay": {
                name: "转发...",
                icon: function (opt, $itemElement, itemKey, item) {
                    return "context-menu-icon context-menu-icon-relay"
                },
                callback: function (itemKey, opt, e) {
                    let m = "edit was clicked" + itemKey;
                    // logger.info("itemKey:", itemKey, "option:", opt.$trigger.attr("msg-id"));
                    openRelayMsgUserDialog(opt.$trigger.attr("msg-id"));
                }
            },
            "delete": {
                name: "删除",
                icon: "delete",
                callback: function (itemKey, opt, e) {
                    let m = "删除 was clicked" + itemKey;
                    // logger.info(m, "option:", opt);
                    deleteUserMsg(opt.$trigger.attr("msg-id"));
                }
            },
            "sep1": "---------",
            "copy": {
                name: "复制",
                icon: function (opt, $itemElement, itemKey, item) {
                    return "context-menu-icon context-menu-icon-copy"
                },
                visible: function (key, opt) {
                    let msgType = opt.$trigger.attr("msg-type");
                    if (msgType === MsgType.TEXT) {
                        return true;
                    }
                },
                callback: function (itemKey, opt, e) {
                    let m = "复制 was clicked " + itemKey;
                    copyToClipboard(opt.$trigger.find(".msg-text").text());
                }
            },
            "storage": {
                name: "存储...",
                icon: function (opt, $itemElement, itemKey, item) {
                    return "context-menu-icon context-menu-icon-saveas"
                },
                visible: function (key, opt) {
                    let msgType = opt.$trigger.attr("msg-type");
                    if (msgType === MsgType.FILE || msgType === MsgType.IMAGE
                        || msgType === MsgType.VOICE || msgType === MsgType.VIDEO) {
                        return true;
                    }
                },
                callback: function (itemKey, opt, e) {
                    let m = "存储 was clicked " + itemKey;
                    // logger.info(m, "option:", opt);
                    downloadFile(opt.$trigger.attr("file-url"), opt.$trigger.attr("file-name"), opt.$trigger.attr("msg-type"));
                }
            }
        }
    });
}


/**
 * 初始化转发消息对话框
 */
function initRelayMsgUserDialog() {
    $("#relay-msg-user-dialog").dialog({
        title: "转发消息",
        autoOpen: false,   // 是否自动弹出窗口
        modal: true,    // 设置为模态对话框
        resizable: false,
        width: 350,   //弹出框宽度
        height: 498,   //弹出框高度
        close: function (event, ui) {
            $("#relay-keyword-input").val("");
            $(".replay-msg-btn").attr("disabled", true);
            $("#relay-chat-list ul").html("");
            $("#relay-user-list ul").html("");
            if (chatRoomListCache.length > 0) {
                chatRoomListCache = [];
            }
        }
    });
}

/**
 * 删除聊天消息
 * @param msgId 消息id
 */
function deleteUserMsg(msgId) {
    myConfirm("删除消息", "确定删除该消息吗？", function () {
        let url = `${MSG_URL_PREFIX}/chat/msg/delete`;
        let data = {
            msgId: msgId
        };
        ajaxRequest(url, "post", data, null, function (res) {
            if (res.code !== 200) {
                logger.error("删除聊天消息失败,msgId:", msgId, "res:", res);
                myAlert('', res.message, "err");
                return;
            }
            // 删除聊天元素
            $(".msg-text[msg-id=" + msgId + "]").closest("li").remove();
            $(".search-msg-item-right[msg-id=" + msgId + "]").closest("li").remove();
        });
    });
}

/**
 * 撤回消息
 * @param msgId 消息id
 */
function revokeMsg(msgId) {
    myConfirm("撤回消息", "确定撤回该消息吗？", function () {
        let url = `${MSG_URL_PREFIX}/chat/msg/revoke`;
        let data = {
            msgId: msgId
        };
        ajaxRequest(url, "post", data, null, function (res) {
            if (res.code !== 200) {
                logger.error("撤回聊天消息失败,msgId:", msgId, "res:", res);
                myAlert('', res.message, "err");
                return;
            }
            // 删除聊天元素
            $(".msg-text[msg-id=" + msgId + "]").closest("li").remove();
            refreshChatRoomListDom();
        });
    });
}

/**
 * 修改置顶标志
 *
 * @param chatId 聊天室ID
 * @param isTop 置顶标志
 */
function updateTopFlag(chatId, isTop) {
    let url = `${MSG_URL_PREFIX}/chat/room/updateIsTop`;
    ajaxRequest(url, "post", {
        chatId: chatId,
        flag: isTop
    }, null, function (res) {
        if (res.code !== 200) {
            logger.info("修改置顶标志失败,chatId:", chatId, "res:", res);
            myAlert('', res.message, "err");
            return;
        }
        refreshChatRoomListDom();
    });
}

/**
 * 修改消息免打扰标志
 *
 * @param chatId 聊天室ID
 * @param flag 标志
 */
function updateMsgNoDisturb(chatId, flag) {
    let url = `${MSG_URL_PREFIX}/chat/room/updateMsgNoDisturb`;
    ajaxRequest(url, "post", {
        chatId: chatId,
        flag: flag
    }, null, function (res) {
        if (res.code !== 200) {
            logger.info("修改消息免打扰标志失败,chatId:", chatId, "res:", res);
            myAlert('', res.message, "err");
            return;
        }
        logger.info("修改消息免打扰标志成功,chatId:", chatId);
    });
}


/**
 * 删除聊天聊天室
 * @param $this 聊天室ID
 */
function deleteChatRoom($this) {
    myConfirm("删除聊天", "即将删除选中的聊天,并同时清空该聊天记录,确定删除该聊天吗？", function () {
        let url = `${MSG_URL_PREFIX}/chat/room/delete`;
        let data = {
            chatId: $this.attr("chat-id")
        };
        ajaxRequest(url, "post", data, null, function (res) {
            if (res.code !== 200) {
                logger.error("删除聊天室失败,msgId:", msgId, "res:", res);
                myAlert('', res.message, "err");
                return;
            }
            $this.remove();
        });
    });
}


/**
 * 图片缩放dom
 * @param _this
 */
function chatMsgImgZoomDom(_this) {
    let src = $(_this).attr('src');
    let alt = $(_this).attr('alt');
    $(".chat-msg-modal").remove();
    let modal = $('<div class="chat-msg-modal">');
    let modalImg = $('<img class="modal-img" alt="放大图片" src="">');
    let caption = $('<div class="caption">');

    modalImg.attr('src', src);
    modalImg.attr('alt', alt);

    caption.text(alt);

    modal.append(modalImg);
    modal.append(caption);

    $('body').append(modal);

    if (mobile) {
        modalImg.on('click', function (e) {
            modalImg.draggable("destroy");
            modalImg.off('click').remove();
            modal.off('click').remove();
        });
    }
    modal.on('click', function (e) {
        modalImg.draggable("destroy");
        modalImg.off('click').remove();
        modal.off('click').remove();
    });

    imgScale(modalImg);
}

/**
 * 图片缩放
 * @param imgDom 图片dom
 */
function imgScale(imgDom) {
    let scale = 1;
    imgDom.on('mousewheel', function (event) {
        let delta = event.originalEvent.wheelDelta; // 获取滚动的距离
        // let direction = delta > 0 ? '向上滚动' : '向下滚动'; // 判断滚动方向
        // logger.info(direction + '，滚动了 ' + Math.abs(delta) + ' 个像素', scale);
        if (delta > 0) {
            scale += 0.1
            imgDom.css("transform", "scale(" + scale + ")")
        } else if (scale > 0.2) {
            scale -= 0.1
            imgDom.css("transform", "scale(" + scale + ")")
        }
    });
    imgDom.draggable();
}

function formatFileSize(size) {
    if (size < 1024) {
        return size + "B";
    }
    if (size < 1024 * 1024) {
        return (size / 1024).toFixed(2) + "KB";
    }
    if (size < 1024 * 1024 * 1024) {
        return (size / (1024 * 1024)).toFixed(2) + "MB";
    }
}

function getFileIcoClass(extName) {
    switch (extName) {
        case "doc":
        case "docx":
            return "msg-file-ico-doc";
        case "xls":
        case "xlsx":
            return "msg-file-ico-xls";
        case "ppt":
        case "pptx":
            return "msg-file-ico-ppt";
        case "pdf":
            return "msg-file-ico-pdf";
        case "xml":
            return "msg-file-ico-xml";
        case "html":
        case "htm":
            return "msg-file-ico-html";
        case "txt":
        case "md":
        case "log":
        case "json":
        case "css":
        case "js":
        case "java":
        case "c":
        case "cpp":
        case "h":
        case "py":
        case "php":
        case "sh":
        case "bat":
            return "msg-file-ico-txt";
        case "zip":
        case "rar":
        case "gzip":
        case "7z":
        case "tar":
        case "gz":
        case "bz2":
        case "bz":
        case "jar":
        case "war":
        case "ear":
        case "iso":
        case "dmg":
        case "cab":
            return "msg-file-ico-zip";
        case "mp3":
        case "wav":
        case "wma":
        case "aac":
        case "flac":
        case "ape":
        case "ogg":
        case "m4a":
        case "aiff":
        case "amr":
        case "mid":
        case "midi":
            return "msg-file-ico-audio";
        case "mp4":
        case "m4v":
        case "avi":
        case "mpg":
        case "mpeg":
        case "mov":
        case "mkv":
        case "rmvb":
        case "rm":
        case "wmv":
        case "flv":
        case "vob":
        case "3gp":
        case "3g2":
        case "ts":
        case "m2ts":
        case "mts":
        case "m2v":
            return "msg-file-ico-video";
        default:
            return "msg-file-ico-unknown";
    }
}

/**
 * 更新聊天消息右键菜单
 */
function refreshContextMenu() {
    let msgTextDom = $('.msg-text-outer .msg-text');
    if (msgTextDom.length === 0) {
        return;
    }
    // 更新菜单
    msgTextDom.contextMenu('update');
}

/**
 * 更新聊天历史消息右键菜单
 */
function refreshHisContextMenu() {
    let msgTextDom = $('.search-msg-item-right');
    if (msgTextDom.length === 0) {
        return;
    }
    // 更新菜单
    msgTextDom.contextMenu('update');
}

function getBlob(url, callback) {
    let xhr = new XMLHttpRequest();
    xhr.open('GET', url, true);
    xhr.responseType = 'blob';
    xhr.onload = function () {
        if (xhr.status === 200) {
            callback(xhr.response);
        }
    };
    xhr.send();
}

function saveAs(blob, filename) {
    let link = document.createElement('a');
    let body = document.querySelector('body');
    link.href = window.URL.createObjectURL(blob);
    link.download = filename;
    link.style.display = 'none';
    body.appendChild(link);
    link.click();
    body.removeChild(link);
    window.URL.revokeObjectURL(link.href);
}

function downloadFile(url, filename, msgType) {
    // logger.error("downloadFile", url, filename);
    if (mobile && msgType === MsgType.IMAGE) {
        saveImageToGallery(url, filename, filename.split(".")[1]);
        return;
    }
    getBlob(url, function (blob) {
        saveAs(blob, filename);
    });
}

function saveImageToGallery(imgUrl, filename, extName) {
    // logger.error("saveImageToGallery", imgUrl, filename, extName);
    // 创建一个Image对象
    let image = new Image();
    image.src = imgUrl;
    image.crossOrigin = "anonymous"; // 这行很重要
    // 等待图片加载完成
    image.onload = function () {
        // 创建一个Canvas元素
        let canvas = document.createElement('canvas');
        canvas.width = image.width;
        canvas.height = image.height;

        // 绘制图片到Canvas上
        let ctx = canvas.getContext('2d');
        ctx.drawImage(image, 0, 0, image.width, image.height);

        // 将Canvas内容转换为Blob URL
        let blob = canvas.toDataURL('image/' + extName);

        let link = document.createElement('a');
        let body = document.querySelector('body');
        link.href = blob;
        link.download = filename;
        link.style.display = 'none';
        body.appendChild(link);
        link.click();
        body.removeChild(link);
    };
}

/**
 * 下载base64图片
 * @param filePic base64图片
 * @param fileName 下载图片名称
 */
function downloadBase64Img(filePic, fileName) {
    let base64ToBlob = function (code) {
        let parts = code.split(';base64,');
        let contentType = parts[0].split(':')[1];
        let raw = window.atob(parts[1]);
        let rawLength = raw.length;
        let uInt8Array = new Uint8Array(rawLength);
        for (let i = 0; i < rawLength; ++i) {
            uInt8Array[i] = raw.charCodeAt(i);
        }
        return new Blob([uInt8Array], {
            type: contentType
        });
    };
    let aLink = document.createElement('a');
    let blob = base64ToBlob(filePic); //new Blob([content]);
    let evt = document.createEvent("HTMLEvents");
    evt.initEvent("click", true, true); //initEvent 不加后两个参数在FF下会报错  事件类型，是否冒泡，是否阻止浏览器的默认行为
    aLink.download = fileName;
    aLink.href = URL.createObjectURL(blob);
    aLink.click();
    aLink.remove();
}

function downloadGroupQrcode() {
    let filePic = $("#group-qrcode-img").attr("src");
    let filename = "group-qrcode.png";
    downloadBase64Img(filePic, filename);
}

// 复制文本内容方法
function copyToClipboard(text) {
    if (document.execCommand('copy')) {
        // 创建textarea
        let textArea = document.createElement("textarea");
        textArea.value = text;
        // 使textarea不在viewport，同时设置不可见
        textArea.style.position = "fixed";
        textArea.style.opacity = 0;
        textArea.style.left = "-999999px";
        textArea.style.top = "-999999px";
        document.body.appendChild(textArea);
        textArea.focus();
        textArea.select();
        return new Promise((res, rej) => {
            // 执行复制命令并移除文本框
            document.execCommand('copy') ? res() : rej();
            textArea.remove();
        });
    } else if (navigator.clipboard && typeof navigator.clipboard.writeText === 'function') {
        // navigator clipboard 向剪贴板写文本
        return navigator.clipboard.writeText(text);
    }
}

function initToasts() {
    toastr.options = {
        "closeButton": false,
        "debug": false,
        "positionClass": "toast-top-right",
        "onclick": toastsClick,
        "showDuration": "300",
        "hideDuration": "1000",
        "timeOut": "5000",
        "extendedTimeOut": "1000",
        "showEasing": "swing",
        "hideEasing": "linear",
        "showMethod": "fadeIn",
        "hideMethod": "fadeOut"
    }
}

/**
 * 播放聊天信息中的视频
 * @param _this
 */
function playVideo(_this) {
    let $videoDialog = $('.video-dialog');
    $videoDialog.find("video").attr("src", $(_this).attr("data-url"));
    $videoDialog.show();
}

/**
 * 关闭视频弹窗
 */
function closeVideo() {
    let video = document.getElementById('video');//获取视频节点
    $('.video-dialog').hide();//点击关闭按钮关闭暂停视频;
    video.pause();
}


/**
 * 收到信息提示音
 * @type {HTMLAudioElement}
 */
let msgNotifyAudio = new Audio('../mp3/msg-notify-ringtone.mp3');

/**
 * 播放通知铃声
 */
function playNotificationRingtone() {
    msgNotifyAudio.play();
}

/**
 * 视频/语音通话铃声对象
 * @type {HTMLAudioElement}
 */
let callRingtoneAudio = new Audio('../mp3/call-ringtone.mp3');
//设置音频循环播放
callRingtoneAudio.loop = true;

/**
 * 播放通话铃声
 */
function playCallRingtone() {
    callRingtoneAudio.play();
}

/**
 * 暂停播放通话铃声
 */
function pauseCallRingtone() {
    callRingtoneAudio.pause();
    callRingtoneAudio.currentTime = 0;
}

/**
 * 通话挂断铃声
 * @type {HTMLAudioElement}
 */
let callHangupRingtoneAudio = new Audio('../mp3/call-hangup-ringtone.mp3');

/**
 * 播放通话挂断铃声
 */
function plaCallHangupRingtone() {
    callHangupRingtoneAudio.play();
}