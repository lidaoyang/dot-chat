/**
 * 上传图片
 * @param file 图片文件
 * @param model 模式
 * @param successFn 成功回调
 */
function uploadImageFile(file, model, successFn) {
    let formData = new FormData();
    formData.append('image', file);
    formData.append("userType", USER_TYPE);
    formData.append("model", model);
    let url = `${SYS_URL_PREFIX}/upload/image`;
    $.ajax({
        url: url,
        data: formData,
        type: 'post',
        async: false,
        processData: false,
        contentType: false,
        dataType: 'json',
        xhrFields: {
            withCredentials: false
        },
        success: function (res) {
            if (!checkResCode(res)){
                return;
            }
            let msgFileO = new MsgFileO(res.data.fileName, res.data.newFileName, res.data.fileSize, res.data.extName, res.data.url, res.data.status);
            successFn(msgFileO);
        },
        error: function (err) {
            console.error(err);
        }
    });
}


/**
 * 上传图片(异步)
 * @param file 图片文件
 * @param model 模式
 * @param successFn 成功回调
 */
function uploadImageFileAsync(file, model, successFn) {
    let formData = new FormData();
    formData.append('image', file);
    formData.append("userType", USER_TYPE);
    formData.append("model", model);
    let url = `${SYS_URL_PREFIX}/upload/async/image`;
    $.ajax({
        url: url,
        data: formData,
        type: 'post',
        async: false,
        processData: false,
        contentType: false,
        dataType: 'json',
        xhrFields: {
            withCredentials: false
        },
        success: function (res) {
            if (!checkResCode(res)){
                return;
            }
            let msgFileO = new MsgFileO(res.data.fileName, res.data.newFileName, res.data.fileSize, res.data.extName, res.data.url, res.data.status);
            successFn(msgFileO);
        },
        error: function (err) {
            console.error(err);
        }
    });
}


/**
 * 上传视频文件(异步)
 * @param file 文件
 * @param model 模式
 * @param successFn 成功回调
 */
function uploadVideoAsync(file, model, successFn) {
    let formData = new FormData();
    formData.append('file', file);
    formData.append("userType", USER_TYPE);
    formData.append("model", model);
    let url = `${SYS_URL_PREFIX}/upload/async/video`;
    $.ajax({
        url: url,
        data: formData,
        type: 'post',
        async: false,
        processData: false,
        contentType: false,
        dataType: 'json',
        xhrFields: {
            withCredentials: false
        },
        success: function (res) {
            if (!checkResCode(res)){
                return;
            }
            let msgFileO = new MsgFileO(res.data.fileName, res.data.newFileName, res.data.fileSize, res.data.extName, res.data.url, res.data.status, res.data.coverUrl);
            successFn(msgFileO);
        },
        error: function (err) {
            console.error(err);
        }
    });
}

/**
 * 上传文件
 * @param file 文件
 * @param model 模式
 * @param successFn 成功回调
 */
function uploadFile(file, model, successFn) {
    let formData = new FormData();
    formData.append('file', file);
    formData.append("userType", USER_TYPE);
    formData.append("model", model);
    let url = `${SYS_URL_PREFIX}/upload/file`;
    $.ajax({
        url: url,
        data: formData,
        type: 'post',
        async: false,
        processData: false,
        contentType: false,
        dataType: 'json',
        xhrFields: {
            withCredentials: false //为true时用途就是跨域请求是要不要携带cookie,既然是跨域请求，服务端要设置Access-Control-Allow-Origin，告诉浏览器允许跨域，而且这个值必须指定域名，不能设置为*
        },
        success: function (res) {
            if (!checkResCode(res)){
                return;
            }
            let msgFileO = new MsgFileO(res.data.fileName, res.data.newFileName, res.data.fileSize, res.data.extName, res.data.url, res.data.status);
            successFn(msgFileO);
        },
        error: function (err) {
            console.error(err);
        }
    });
}

/**
 * 上传文件(异步)
 * @param file 文件
 * @param model 模式
 * @param successFn 成功回调
 */
function uploadFileAsync(file, model, successFn) {
    let formData = new FormData();
    formData.append('file', file);
    formData.append("userType", USER_TYPE);
    formData.append("model", model);
    let url = `${SYS_URL_PREFIX}/upload/async/file`;
    $.ajax({
        url: url,
        data: formData,
        type: 'post',
        async: false,
        processData: false,
        contentType: false,
        dataType: 'json',
        xhrFields: {
            withCredentials: false
        },
        success: function (res) {
            if (!checkResCode(res)){
                return;
            }
            let msgFileO = new MsgFileO(res.data.fileName, res.data.newFileName, res.data.fileSize, res.data.extName, res.data.url, res.data.status);
            successFn(msgFileO);
        },
        error: function (err) {
            console.error(err);
        }
    });
}

/**
 * 上传聊天用户头像
 * @param file 图片文件
 * @param successFn
 */
function uploadChatUserAvatar(file, successFn) {
    let token = $.cookie(TOKEN_KEY);
    if (!token && url !== `${SYS_URL_PREFIX}/user/login`) {
        logger.info("token过期,res:", res);
        deleteUserCookie();
        return;
    }
    let formData = new FormData();
    formData.append('image', file);
    formData.append("userType", USER_TYPE);
    let url = `${MSG_URL_PREFIX}/chat/user/updateAvatar`;
    $.ajax({
        url: url,
        data: formData,
        type: 'post',
        async: false,
        processData: false,
        contentType: false,
        dataType: 'json',
        headers: {
            'Authorization': token
        },
        xhrFields: {
            withCredentials: false
        },
        success: function (res) {
            if (!checkResCode(res)){
                return;
            }
            chatUser.avatar = res.data;
            localStorage.setItem(CHAT_USER_KEY, JSON.stringify(chatUser));
            successFn(res.data);
        },
        error: function (err) {
            console.error(err);
        }
    });
}

function  checkResCode(res) {
    if (res.code === 401) {
        logger.info("token过期,res:", res);
        deleteUserCookie();
        return false;
    }
    if (res.code !== 200) {
        logger.info("文件上传失败,res:", res);
        myAlert('', res.message, "err");
        return false;
    }
    logger.info("上传成功,res:", res);
    return true;
}

/**
 * 缓存进度条消息id集合(注册进度条事件使用)
 * @type {Set<any>}
 */
let progressBarMsgIdSet = new Set();

/**
 * 缓存进度条消息id集合(循环获取进度使用)
 * @type {Set<any>}
 */
let progressMsgIdSet = new Set();

/**
 * 启动进度条
 */
function startUploadProgressBar() {
    progressBarMsgIdSet.forEach(msgId => {
        let progressbar = $(".progress-bar[msg-id=" + msgId + "]");
        startUploadProgressBar2(progressbar)
        progressMsgIdSet.add(msgId);
    });
    if (progressMsgIdSet.size > 0) {
        setTimeout(progress, 1000);
    }
    progressBarMsgIdSet.clear();
}

/**
 * 启动进度条2
 * @param progressbar
 */
function startUploadProgressBar2(progressbar) {
    logger.info("启动进度条", progressbar);
    if (progressbar.length === 0) {
        return;
    }
    progressbar.progressbar({
        value: false,
        change: function () {
            //上传中
        },
        complete: function () {
            uploadCompleteAfterSetImgSrc(progressbar);
        }
    });
}

function uploadCompleteAfterSetImgSrc(progressbar) {
    let msgDom = progressbar.parent();
    let msgType = msgDom.attr("msg-type");
    logger.info("上传完成! msgType:", msgType, msgDom.attr("new-file-name"));
    //图片或视频消息更换显示图片
    if (MsgType.VIDEO === msgType || MsgType.IMAGE === msgType) {
        let fileUrl = progressbar.attr("file-url");
        let imgDom = msgDom.find("img");
        imgDom.attr("src", fileUrl).removeClass("msg-img-loading");
        if (MsgType.VIDEO === msgType) {
            imgDom.addClass("msg-video-cover");
            msgDom.find(".msg-video-play-icon").removeClass("hide");
            //注册点击播放事件

        } else {
            imgDom.addClass("msg-img");
            imgDom.on("click", function () {
                chatMsgImgZoomDom(this);
            })
        }
    }
    //更新文件消息上传状态
    let msgId = parseInt(msgDom.attr("msg-id"));
    updateFileMsgStatus(msgId);
    progressMsgIdSet.delete(msgId);
    progressbar.remove();
}

let noStartFile = new Map;

function progress() {
    let isComplete = true;
    progressMsgIdSet.forEach(msgId => {
        let progressbar = $(".progress-bar[msg-id=" + msgId + "]");
        isComplete = progress2(progressbar);
    });
    if (!isComplete) {
        setTimeout(progress, 300);
    }
}

function progress2(progressbar) {
    if (progressbar.length === 0) {
        return true;
    }
    let isComplete = true;
    let fileName = progressbar.parent().attr("new-file-name");
    if (!fileName) {
        return isComplete
    }
    getUploadProgressBar(fileName, function (res) {
        if (res.status === "NO_START") {
            if (noStartFile.has(fileName)) {
                let noStartFileVal = noStartFile.get(fileName) + 1;
                if (noStartFileVal >= 5) {
                    logger.info("文件上传缓存过期,fileName:", fileName, "res:", res);
                    uploadCompleteAfterSetImgSrc(progressbar);
                    noStartFile.delete(fileName);
                    return;
                }
                noStartFile.set(fileName, noStartFileVal);
            } else {
                noStartFile.set(fileName, 1);
            }
        }
        let val = 0;
        if (res.percent) {
            val = parseInt(res.percent);
        }
        progressbar.progressbar("value", val);

        if (res.status !== "COMPLETED") {
            isComplete = false;
        }
    });
    return isComplete;
}


/**
 * 获取上传进度条
 * @param fileName 文件名称
 * @param successFn 成功回调
 */
function getUploadProgressBar(fileName, successFn) {
    let url = `${SYS_URL_PREFIX}/upload/progressBar`;
    ajaxSyncRequest(url, "get", {fileName: fileName}, null, function (res) {
        if (res.code !== 200) {
            logger.info("文件上传进度获取失败,fileName:", fileName, "res:", res);
            myAlert('', res.message, "err");
            return;
        }
        successFn(res.data);
    });
}

/**
 * 更新文件消息上传状态
 * @param msgId 消息id
 */
function updateFileMsgStatus(msgId) {
    let url = `${MSG_URL_PREFIX}/chat/msg/updateFileMsgStatus`;
    let data = {
        msgId: msgId
    };
    ajaxRequest(url, "post", data, null, function (res) {
        if (res.code !== 200) {
            logger.info("更新文件消息上传状态失败,msgId:", msgId, "res:", res);
            return;
        }
        logger.info("更新文件消息上传状态完成,msgId:", msgId, "res:", res);
    });
}


function base64ToFile(base64Data, filename) {
    // 将base64的数据部分提取出来
    const parts = base64Data.split(';base64,');
    const contentType = parts[0].split(':')[1];
    const raw = window.atob(parts[1]);
    const rawLength = raw.length;
    const uInt8Array = new Uint8Array(rawLength);

    for (let i = 0; i < rawLength; ++i) {
        uInt8Array[i] = raw.charCodeAt(i);
    }

    // 使用Blob和File构造函数创建一个File对象
    const blob = new Blob([uInt8Array], {type: contentType});
    return new File([blob], filename, {type: contentType});
}

/**
 * 上传图片文件并发送消息
 * @param file
 */
function uploadAndSendImageFile(file) {
    uploadImageFileAsync(file, "chat-msg", function (res) {
        sendObjMsg(res, MsgType.IMAGE);
    });
}

/**
 * 上传视频文件并发送消息
 * @param file
 */
function uploadAndSendVideoFile(file) {
    uploadVideoAsync(file, "chat-msg", function (res) {
        sendObjMsg(res, MsgType.VIDEO);
    });
}

/**
 * 上传文件并发送消息
 * @param file
 */
function uploadAndSendFile(file) {
    uploadFileAsync(file, "chat-msg", function (res) {
        sendObjMsg(res, MsgType.FILE);
    });
}
