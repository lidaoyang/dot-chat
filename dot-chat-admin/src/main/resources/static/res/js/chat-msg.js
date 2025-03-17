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
 * 生成信息dom
 * @param user1
 * @param user2
 * @param chatMsg
 * @returns {string}
 */
function generateChatMsgDom(user1, user2, chatMsg) {
    if (chatMsg.msgType === MsgType.SYSTEM || chatMsg.msgType === MsgType.EVENT) {
        return '<li class="sys"><div class="sysinfo">' + chatMsg.msg + '</div></li>';
    }
    let msgDom = getMsgDom(user2, chatMsg);
    if (msgDom === '') {
        return "";
    }
    let user = user2;
    if (chatMsg.sendUserId === user1.id){
        user = user1;
    }
    return `
            <li class="${user.msgClass}">
                <div class="avatar" user-id="${user.id}" ><img src="${user.avatar}" alt=""></div>
                <div class="msg">
                    <div class="msg-name">${user.nickname}</div>
                    <div class="msg-text-outer">
                        <span class="msg-arrow"></span>
                        <span class="msg-text" > ${msgDom}</span>
                    </div>
                    <time>${mini.formatDate(chatMsg.sendTime, "yyyy-MM-dd HH:mm:ss")}</time>
                </div>
            </li>
        `;
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

function getMsgDom(user2, chatMsg) {
    let msgDom = ``;
    if (chatMsg.msgType === MsgType.IMAGE) {
        msgDom = getImgMsgDom(chatMsg);
    } else if (chatMsg.msgType === MsgType.VIDEO) {
        msgDom = getVideoMsgDom(chatMsg);
    } else if (chatMsg.msgType === MsgType.FILE) {
        msgDom = getFileMsgDom(chatMsg);
    } else if (chatMsg.msgType === MsgType.BIZ_CARD || chatMsg.msgType === MsgType.GROUP_BIZ_CARD) {
        msgDom = getCardMsgDom(chatMsg);
    } else if (chatMsg.msgType === MsgType.VIDEO_CALL || chatMsg.msgType === MsgType.AUDIO_CALL) {
        msgDom = getCallMsgDom(user2, chatMsg);
    } else {
        msgDom = getTextMsgDom(chatMsg);
    }
    return msgDom;
}

/**
 * 生成图片信息dom
 * @param chatMsg
 * @returns {string}
 */
function getImgMsgDom(chatMsg) {
    let msgObj = JSON.parse(chatMsg.msg);
    let msgDom = `<img class="msg-img" src="${msgObj.fileUrl}" alt="" onclick="chatMsgImgZoomDom(this)" onerror="this.src='../../res/icon/pic_error.png'">`;
    if (msgObj.status === 0) {
        msgDom = `<img class="msg-img-loading" src="../../res/icon/loading.gif" alt="">`;
    }
    return msgDom;
}

/**
 * 生成视频信息dom
 * @param chatMsg
 * @returns {string}
 */
function getVideoMsgDom(chatMsg) {
    let msgObj = JSON.parse(chatMsg.msg);
    let msgDom = `<div class="msg-video-outer">
                               <img class="msg-video-cover" src="${msgObj.coverUrl}" alt="" onerror="this.src='../../res/icon/pic_error.png'">
                                <i class="msg-video-play-icon" data-url="${msgObj.fileUrl}" onclick="playVideo(this)" ></i>
                         </div>`;
    if (msgObj.status === 0) {
        msgDom = `<div class="msg-video-outer">
                        <img class="msg-img-loading" src="../../res/icon/loading.gif" alt="">
                  </div>`;
    }
    return msgDom;
}

/**
 * 生成文件信息dom
 * @param chatMsg
 * @returns {string}
 */
function getFileMsgDom(chatMsg) {
    let msgObj = JSON.parse(chatMsg.msg);
    let fileSize = formatFileSize(msgObj.fileSize);
    let extName = msgObj.extName.toLowerCase();
    let icoCls = getFileIcoClass(extName);
    let msgDom = `<div class="msg-file-wrap">
                    <div class="msg-file-name-wrap">
                        <span class="msg-file-name">${msgObj.fileName}</span>
                        <span class="msg-file-size">${fileSize}</span>
                    </div>
                    <div class="msg-file-ico ${icoCls}"></div>
               </div>`;
    return msgDom;
}

/**
 * 生成名片信息dom
 * @param chatMsg
 * @returns {string}
 */
function getCardMsgDom(chatMsg) {
    let cardLeb = chatMsg.msgType === MsgType.BIZ_CARD ? "个人名片" : "群名片";
    let msgObj = JSON.parse(chatMsg.msg);
    return `<div class="msg-card-wrap" user-id="${msgObj.userId}">
                    <div class="msg-card-name-wrap">
                        <img class="msg-card-avatar" src="${msgObj.avatar}" alt="头像" />
                        <span class="msg-card-nickname">${msgObj.nickname}</span>
                    </div>
                    <div class="msg-card-lab"> ${cardLeb}</div>
               </div>`;
}

/**
 * 生成名片信息dom
 * @param user2
 * @param chatMsg
 * @returns {string}
 */
function getCallMsgDom(user2, chatMsg) {
    let msgObj = JSON.parse(chatMsg.msg);
    let isRight = chatMsg.sendUserId === user2.id;
    let bgCls = '';
    if (chatMsg.msgType === MsgType.AUDIO_CALL) {
        bgCls = "call-audio-msg-icon";
    } else {
        bgCls = isRight ? "call-video-msg-icon-right" : "call-video-msg-icon-left"
    }
    let text = CallType[msgObj.callType][1];
    if (msgObj.callType === CallType.hangup[0] || msgObj.callType === CallType.dropped[0]) {
        text = text + " " + formatCallTime(msgObj.duration);
    }
    let textIcoDom = ``;
    if (isRight) {
        textIcoDom = `<div class="call-msg-text">${text}</div><div class="call-msg-icon margin-left-3 ${bgCls}"></div>`;
    } else {
        textIcoDom = `<span class="call-msg-icon margin-right-3 ${bgCls}"></span><div class="call-msg-text">${text}</div>`;
    }
    return `<div class="msg-call-wrap">
                    ${textIcoDom}
               </div>`;
}

/**
 * 生成文本信息dom
 * @param chatMsg
 * @returns {string}
 */
function getTextMsgDom(chatMsg) {
    return `<p class="msg-text-wrap">${chatMsg.msg}</p>`;
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
 *
 * @param uid 当前用户ID
 * @param sendUserId 信息发送用户ID
 * @param type 通话类型
 * @returns {*}
 */
function getCallTypeDesc(uid, sendUserId, type) {
    if (uid === sendUserId) {
        return CallType[type][1];
    }
    return CallType[type][2];
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

function formatCallTime(callTime) {
    let hours = Math.floor(callTime / 3600);
    let minutes = Math.floor((callTime % 3600) / 60);
    let seconds = callTime % 60;
    let hoursTxt = hours.toString().padStart(2, '0');
    let minutesTxt = minutes.toString().padStart(2, '0');
    let secondsTxt = seconds.toString().padStart(2, '0');
    if (hours > 0) {
        return hoursTxt + ':' + minutesTxt + ':' + secondsTxt;
    }
    return minutesTxt + ':' + secondsTxt;
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

/**
 * 获取文件图标
 * @param extName
 * @returns {string}
 */
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