//端点对象
let rtcPeerConnection = null;

let localAudioDom = document.getElementById("localAudio");  //本地音频dom对象
let remoteAudioDom = document.getElementById("remoteAudio"); //对端音频dom对象

let localVideoDom = document.getElementById("localVideo");  //本地音视频dom对象
let remoteVideoDom = document.getElementById("remoteVideo"); //对端音视频dom对象

//通话状态
let callState = null;

//本地视频流
let localMediaStream = null;

//ice服务器信息, 用于创建 SDP 对象
let iceServers = {
    "iceServers": [
        {"urls": "stun:stun.l.google.com:19302"},
        // {"urls": ["stun:159.75.239.36:3478"]},
        // {"urls": ["turn:159.75.239.36:3478"], "username": "chr", "credential": "123456"},
    ]
};

/**
 * 本地音频信息, 用于打开本地音频流
 */
const mediaConstraints = {
    audio: {
        echoCancellation: true, // 开启回声消除
        noiseSuppression: true, // 开启噪声抑制
        autoGainControl: true, // 开启自动增益控制
    }
};

/**
 * 音视频约束对象, 用于打开本地音视频流 PC端
 * @type {{video: bigint, audio: {echoCancellation: boolean, autoGainControl: boolean, noiseSuppression: boolean}}}
 */
const mediaConstraintsPC = {
    video: {
        width: {min: 540, ideal: 1215, max: 2048}, // 根据不同屏幕宽度设置
        height: {min: 480, ideal: 1080, max: 1536},
        aspectRatio: 8 / 9
    },
    audio: {
        echoCancellation: true, // 开启回声消除
        noiseSuppression: true, // 开启噪声抑制
        autoGainControl: true, // 开启自动增益控制
    }
};
const mediaConstraintsPC2 = {
    video: {
        width: {min: 540, ideal: 1215, max: 2048}, // 根据不同屏幕宽度设置
        height: {min: 480, ideal: 1080, max: 1536},
        facingMode: "user", // 用户正面的摄像头
        aspectRatio: 9 / 8
    },
    audio: {
        echoCancellation: true, // 开启回声消除
        noiseSuppression: true, // 开启噪声抑制
        autoGainControl: true, // 开启自动增益控制
    }
};

/**
 * 音视频约束对象, 用于打开本地音视频流 移动端
 * @type {{video: bigint, audio: {echoCancellation: boolean, autoGainControl: boolean, noiseSuppression: boolean}}}
 */
const mediaConstraintsMobile = {
    video: {
        // width: {min: 1280, ideal: 1920, max: 2048},// TODO 验证 未铺满
        // height: {min: 720, ideal: 1080, max: 1152},
        width: {min: 640, ideal: 2240},
        height: {min: 300, ideal: 1050},
        frameRate: {ideal: 30},  //视频的帧率 30 帧每秒
        facingMode: "user", // 用户正面的摄像头
        aspectRatio: 16 / 9  // TODO 验证 可以铺满
        // aspectRatio: 16 / 7.5 //*14
    },
    audio: {
        echoCancellation: true, // 开启回声消除
        noiseSuppression: true, // 开启噪声抑制
        autoGainControl: true, // 开启自动增益控制
    }
};

/**
 * 音视频约束对象, 用于打开本地音视频流 移动端
 * @type {{video: bigint, audio: {echoCancellation: boolean, autoGainControl: boolean, noiseSuppression: boolean}}}
 */
const mediaConstraintsMobile2 = {
    video: {
        // height: {min: 640, ideal: 1280, max: 1920},
        // width: {min: 360, ideal: 720, max: 1080},
        frameRate: {max: 30},  //视频的帧率最大 30 帧每秒
        facingMode: "user", // 用户正面的摄像头
        aspectRatio: 7.5 / 16 //*14
    },
    audio: {
        echoCancellation: true, // 开启回声消除
        noiseSuppression: true, // 开启噪声抑制
        autoGainControl: true, // 开启自动增益控制
    }
};

function getMediaConstraints(callMsg) {
    if (callMsg.msgType === MsgType.AUDIO_CALL) {
        return mediaConstraints;
    }
    if ((mobile && callMsg.mobile)) { //本地和远程都是移动设备或者本地是PC设备,远程是移动设备时,使用移动设备约束
        return mediaConstraintsMobile;
    }
    if (!mobile && callMsg.mobile) {
        return mediaConstraintsMobile2;
    }
    if ((!mobile && !callMsg.mobile)) { //本地和远程都是PC设备或本地是移动设备,远程是PC设备时,使用PC设备约束
        return mediaConstraintsPC;
    }
    if (mobile && !callMsg.mobile) {
        return mediaConstraintsPC2;
    }
}


/* WebRTC init 0 */
function WebRTCInit(msgType) {
    logger.info('初始化 WebRTC, msgType:', msgType);
    if (rtcPeerConnection !== null) {
        return;
    }
    // 1、创建端点
    createPeerConnection();
    // 2、绑定 收集 candidate 的回调
    bindOnIceCandidate(candidate => {
            let {msgId, sendUserId, toUserId} = getMsgIdAndSendUidByRemoteCallDom(msgType);
            let callType = chatUser.id === parseInt(sendUserId) ? CallType.candidate1[0] : CallType.candidate2[0];
            // logger.info("收集candidate并发送到对端,callType:", callType, "msgType:", msgType, " msgId:", msgId, " sendUserId:", sendUserId, " toUserId:", toUserId);
            //发送 candidate 到 对端
            sendAVCallMsg(new MsgCallO(callType, msgId, candidate), msgType, getToUserId(sendUserId, toUserId));
        }
    );
    // 3、绑定 获得 远程视频流 的回调
    bindOnTrack(stream => {
        logger.info('获得远程视频流, 并显示远程视频流,msgType:', msgType);
        let remoteCallDom = getRemoteCallDom(msgType);
        setVideoDomStream(remoteCallDom, stream);
    });
    bindOnIceConnectionStateChange(() => {
        let {msgId, sendUserId, toUserId} = getMsgIdAndSendUidByRemoteCallDom(msgType);
        logger.info("远端网络异常中断通话,msgType:", msgType, "msgId:", msgId, "sendUserId:", sendUserId, "toUserId:", toUserId);
        //发送挂断信息
        sendAVCallMsg(new MsgCallO(CallType.dropped[0], msgId), msgType, getToUserId(sendUserId, toUserId));
    });
}

/**
 * 获取兼容的 UserMedia
 * @param constrains
 * @returns {Promise<MediaStream>|*}
 */
function getCompatibleUserMedia(constrains) {
    // 老的浏览器可能根本没有实现 mediaDevices，所以我们可以先设置一个空的对象
    if (navigator.mediaDevices === undefined) {
        navigator.mediaDevices = {};
    }
    // 一些浏览器部分支持 mediaDevices。我们不能直接给对象设置 getUserMedia
    // 因为这样可能会覆盖已有的属性。这里我们只会在没有 getUserMedia 属性的时候添加它。
    if (navigator.mediaDevices.getUserMedia === undefined) {
        navigator.mediaDevices.getUserMedia = function (constraints) {
            // 首先，如果有 getUserMedia 的话，就获得它
            let getUserMedia = navigator.webkitGetUserMedia || navigator.mozGetUserMedia;

            // 一些浏览器根本没实现它 - 那么就返回一个 error 到 promise 的 reject 来保持一个统一的接口
            if (!getUserMedia) {
                return Promise.reject(new Error("getUserMedia is not implemented in this browser"));
            }

            // 否则，为老的 navigator.getUserMedia 方法包裹一个 Promise
            return new Promise(function (resolve, reject) {
                getUserMedia.call(navigator, constraints, resolve, reject);
            });
        };
    }
    return navigator.mediaDevices.getUserMedia(constrains);
}

/**
 * 1、打开本地音视频流
 * @param constraints
 * @param callback 成功后回调
 * @param errorFun 失败后回调
 */
const openLocalMedia = (constraints, callback, errorFun) => {
    logger.info('打开本地视频流,constraints:', constraints);
    getCompatibleUserMedia(constraints)
        .then(stream => {
            localMediaStream = stream;
            //将 音视频流 添加到 端点 中
            for (const track of localMediaStream.getTracks()) {
                const settings = track.getSettings();
                logger.info('Actual video resolution: w:' + settings.width + ', h:' + settings.height);
                rtcPeerConnection.addTrack(track, localMediaStream);
            }
            // 停止播放铃声
            pauseCallRingtone();
            callback(localMediaStream);
        })
        .catch(function (err) {
            console.error('打开视频流失败', err.name + ": " + err.message);
            openMediaError(err);
            errorFun();
        });
}

function openMediaError(err) {
    switch (err.name) {
        case 'AbortError': //中止错误
            myAlert("麦克风或摄像头授权异常", "授权成功,但是硬件可能出现未知异常", "err");
            break;
        case 'NotAllowedError': //拒绝错误
            myAlert("麦克风或摄像头授权异常", "你拒绝了浏览器访问媒体设备的权限,如要继续通话请到设置中确认是否打开了相关权限", "err");
            break;
        case 'NotFoundError': //无法读取错误, 找不到设备
            myAlert("未找到麦克风或摄像头", "请确认是否插入了麦克风或摄像头", "err");
            break;
        case 'NotReadableError': //设备被占用
            myAlert("麦克风或摄像头被占用", "请确认是否打开了其他软件占用了麦克风或摄像头", "err");
            break;
        case 'OverconstrainedError': //约束错误
            myAlert("麦克风或摄像头授权异常", "授权成功,但是未找到合适的设备", "err");
            break;
        case 'SecurityError': //安全错误
            myAlert("麦克风或摄像头授权异常", "使用设备媒体被禁止,到设置中确认是否打开了相关权限", "err");
            break;
    }
}

/**
 * 2、创建 PeerConnection 对象
 */
const createPeerConnection = () => {
    logger.info('创建 PeerConnection 对象');
    rtcPeerConnection = new RTCPeerConnection(iceServers);
}

/**
 * 创建 offer 的配置对象
 * @type {{iceRestart: boolean, offerToReceiveAudio: boolean}}
 */
const offerOptions = {
    iceRestart: true,
    offerToReceiveAudio: true, //如果没有麦克风，当请求音频，会报错，不过不会影响视频流播放
};

/**
 * 3、创建用于 offer 的 SDP 对象
 * @param callback
 */
const createOffer = (callback) => {
    // 调用PeerConnection的 CreateOffer 方法创建一个用于 offer的SDP对象，SDP对象中保存当前音视频的相关参数。
    rtcPeerConnection.createOffer(offerOptions)
        .then(desc => {
            // 保存自己的 SDP 对象
            rtcPeerConnection.setLocalDescription(desc)
                .then(() => callback(desc));
        })
        .catch(() => logger.info('createOffer 失败'));
}
/**
 * 4、创建用于 answer 的 SDP 对象
 * @param callback
 */
const createAnswer = (callback) => {
    // 调用PeerConnection的 CreateAnswer 方法创建一个 answer的SDP对象
    rtcPeerConnection.createAnswer(offerOptions)
        .then(desc => {
            // 保存自己的 SDP 对象
            rtcPeerConnection.setLocalDescription(desc)
                .then(() => callback(desc));
        })
        .catch(() => logger.info('createAnswer 失败'))
}

/**
 * 5、保存远程的 SDP 对象
 * @param desc 创建offer或answer产生session描述对象,(包含字段 type,sdp)
 * @param callback
 */
const saveSdp = (desc, callback) => {
    rtcPeerConnection.setRemoteDescription(new RTCSessionDescription(desc))
        .then(callback);
}

/**
 * 6、保存 candidate 信息
 * @param candidate
 */
const saveIceCandidate = (candidate) => {
    let iceCandidate = new RTCIceCandidate(candidate);
    rtcPeerConnection.addIceCandidate(iceCandidate)
        .then(() => logger.info('addIceCandidate 成功'));
}

/**
 * 7、收集 candidate 的回调
 * @param callback
 */
const bindOnIceCandidate = (callback) => {
    // logger.info('绑定 收集 candidate 的回调');
    rtcPeerConnection.onicecandidate = (event) => {
        if (event.candidate) {
            callback(event.candidate);
        }
    };
};

/**
 * 7、监听ICE连接状态变化
 * @param callback
 */
const bindOnIceConnectionStateChange = (callback) => {
    logger.info('绑定 iceconnectionstatechange 事件');
    // 绑定 收集 candidate 的回调
    rtcPeerConnection.oniceconnectionstatechange = (event) => {
        logger.info('ICE连接状态变化:', rtcPeerConnection.iceConnectionState);
        switch (rtcPeerConnection.iceConnectionState) {
            case 'checking':
                logger.info('正在尝试建立连接ICE...');
                break;
            case 'connected':
                logger.info('ICE连接成功建立');
                break;
            case 'completed':
                logger.info('ICE连接已经完成，即将传输数据');
                break;
            case 'failed':
                logger.info('ICE连接失败', event);
                break;
            case 'disconnected':
                logger.info('ICE连接已经断开，但是仍然保持一段时间用于重新连接');
                break;
            case 'closed':
                logger.info('ICE连接已关闭');
                callback();
                break;
            default:
                logger.info('未知ICE状态', event);
                break;
        }
    };
};

/**
 * 8、获得 远程视频流 的回调
 * @param callback
 */
const bindOnTrack = (callback) => {
    logger.info('绑定 获得 远程视频流 的回调');
    rtcPeerConnection.ontrack = (event) => callback(event.streams[0]);
};

/**
 * 9、挂断时关闭连接 关闭 PeerConnection 和音视频流
 * 注意:挂断时需要通知远端同时挂断
 */
const hangupAndClosePeerConnection = () => {
    logger.info('关闭对话框');
    callState = CallType.hangup[0];
    hideCallBtnAndDialog();

    logger.info('关闭视频流');
    // 关闭视频流
    stopTracks(localVideoDom);
    stopTracks(remoteVideoDom);
    // 关闭音频流
    stopTracks(localAudioDom);
    stopTracks(remoteAudioDom);
    // 关闭本地视频流
    stopLocalStream();

    logger.info('关闭 PeerConnection');
    if (rtcPeerConnection) {
        rtcPeerConnection.ontrack = null;
        rtcPeerConnection.onremovetrack = null;
        rtcPeerConnection.onremovestream = null;
        rtcPeerConnection.onicecandidate = null;
        rtcPeerConnection.oniceconnectionstatechange = null;
        rtcPeerConnection.onsignalingstatechange = null;
        rtcPeerConnection.onicegatheringstatechange = null;
        rtcPeerConnection.onnegotiationneeded = null;

        rtcPeerConnection.close();
        rtcPeerConnection = null;
    }
}

function stopTracks(videoDom) {
    let stream = videoDom.srcObject;
    if (!stream) {
        return;
    }
    stream.getTracks().forEach(track => {
        track.stop();
    });
    videoDom.srcObject = null;
}

function stopLocalStream() {
    if (localMediaStream) {
        localMediaStream.getTracks().forEach(track => {
            track.stop();
        });
        localMediaStream = null;
    }
}

// 视频通话

/**
 * 发送通话邀请
 * @param msgType
 */
function sendInviteCall(msgType) {
    if (!chatToUser) {
        return;
    }
    if (isOpenCallDialog()) {
        toastr.info("当前正在通话中不能拨打其他电话");
        return;
    }
    logger.info("发送通话邀请,msgType:", msgType)
    sendAVCallMsg(new MsgCallO(CallType.invite[0]), msgType, chatToUser.id);
}

function isOpenCallDialog() {
    return !$("#call-audio-dialog").hasClass("hide") || !$("#call-video-dialog").hasClass("hide");
}

/**
 * 接受通话邀请
 * @param msgType
 */
function acceptCall(msgType) {
    let {msgId, sendUserId, toUserId} = getMsgIdAndSendUidByRemoteCallDom(msgType);
    logger.info("接受通话邀请,msgType:", msgType, "msgId:", msgId, "sendUserId:", sendUserId, "toUserId:", toUserId);
    sendAVCallMsg(new MsgCallO(CallType.accept[0], msgId, '', '', mobile), msgType, getToUserId(sendUserId, toUserId));
    // 显示挂断按钮
    showAcceptCallBtn();
}

function showAcceptCallBtn() {
    $(".call-local").addClass("hide");//隐藏取消按钮
    $(".call-remote").addClass("hide");//隐藏接受按钮
    $(".call-remote-accept").removeClass("hide");//显示挂断按钮
    $(".call-remote-video").removeClass("hide");
    hideAcceptCallBtnTimer();
}

// 隐藏挂断按钮定时器
let hideRemoteBtnTimer = 0;

/**
 * 显示隐藏挂断按钮定时器
 */
function hideAcceptCallBtnTimer() {
    if (hideRemoteBtnTimer > 0) {
        clearHideRemoteBtnTimer();
    }
    hideRemoteBtnTimer = setTimeout(() => {
        $(".call-remote-accept").addClass("hide");
        $('.call-local-video').addClass("hide");
    }, 30000);
}

/**
 * 清除隐藏挂断按钮定时器
 */
function clearHideRemoteBtnTimer() {
    if (hideRemoteBtnTimer > 0) {
        clearTimeout(hideRemoteBtnTimer);
        hideRemoteBtnTimer = 0;
    }
}

/**
 * 远程视频窗口点击事件
 * @param _this
 */
function remoteVideoClick(_this) {
    showHideAcceptCallBtn(_this);
    switchVideo(_this);
}

/**
 * 本地视频窗口点击事件
 * @param _this
 */
function localVideoClick(_this) {
    showHideAcceptCallBtn(_this);
    switchVideo(_this);
}

/**
 * 显示隐藏挂断按钮
 * @param _this
 */
function showHideAcceptCallBtn(_this) {
    if ($(_this).hasClass("call-local-video")) {
        return;
    }
    if ($(".call-remote-accept").hasClass("hide")) {
        showRemoteCallDom();
    } else {
        hideRemoteCallDom();
    }
}

function showHideAcceptCallBtnByMouse(eventType) {
    if ($(".call-remote-video").hasClass("hide")) {
        return;
    }
    if (eventType === "over") {
        if ($(".call-remote-accept").hasClass("hide")) {
            showRemoteCallDom();
        }
    } else {
        hideRemoteCallDom();
    }
}

function showRemoteCallDom() {
    $(".call-remote-accept").removeClass("hide");
    $('.call-local-video').removeClass("hide");
    hideAcceptCallBtnTimer();
}

function hideRemoteCallDom() {
    $(".call-remote-accept").addClass("hide");
    $('.call-local-video').addClass("hide");
    clearHideRemoteBtnTimer();
}


/**
 * 切换本地和远程视频窗口
 * @param _this
 */
function switchVideo(_this) {
    let videoDom = $(_this);
    if (videoDom.hasClass("call-local-video")) {
        $(".call-remote-video").removeClass("call-remote-video").addClass("call-local-video");
        videoDom.removeClass("call-local-video").addClass("call-remote-video");
    }
}

/**
 * 接受通话邀请
 * @param callMsg
 */
function acceptCallAndOpenMediaOffer(callMsg) {
    callState = CallType.accept[0];
    // 显示挂断按钮
    showAcceptCallBtn();
    // 清除等待邀请计时器
    clearWaitingCallTimer();
    if (callMsg.isMy) {
        return;
    }
    // 先打开视频流, 在创建用于 offer 的 SDP 对象
    openLocalMedia(getMediaConstraints(callMsg), (stream) => {
        // 显示本地视频流
        let localCallDom = getLocalCallDom(callMsg.msgType);
        setVideoDomStream(localCallDom, stream);
        if (callMsg.msgType === MsgType.VIDEO_CALL) {
            $(localCallDom).removeClass("hide");
        }
        createOffer(desc => {
            let {msgId, sendUserId, toUserId} = getMsgIdAndSendUidByRemoteCallDom(callMsg.msgType);
            logger.info('创建并发送 offer ,msgType:', callMsg.msgType, ",msgId:", msgId);
            sendAVCallMsg(new MsgCallO(CallType.offer[0], msgId, '', desc, mobile), callMsg.msgType, getToUserId(sendUserId, toUserId));
            // 开启计时器
            callTiming();
        });
    }, () => hangup(CallType.cancel[0], callMsg.msgType));
}

/**
 * 通话计时器
 * @param callMsg
 */
let callTimer = 0;
let callTime = 0;

/**
 * 通话计时中
 */
function callTiming() {
    callTimer = setInterval(() => {
        callTime += 1;

        $(".call-timing-label").html(formatCallTime(callTime));
    }, 1000);
}

function clearCallTimer() {
    if (callTimer !== 0) {
        clearInterval(callTimer);
        callTimer = 0;
        callTime = 0;
        $(".call-timing-label").html("00:00");
    }
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

function saveSDPAndOpenMediaAnswer(callMsg) {
    if (callMsg.isMy) { //自己发送的消息不处理
        return;
    }
    //先保存收到的 offer
    saveSdp(callMsg.desc, () => {
        logger.info('offer 保存成功');
        //再打开音视频流
        openLocalMedia(getMediaConstraints(callMsg), (stream) => {
            let localCallDom = getLocalCallDom(callMsg.msgType);
            setVideoDomStream(localCallDom, stream);
            localCallDom.classList.remove("hide");
            //最后创建用于 answer 的 SDP 对象
            createAnswer(desc => {
                let {msgId, sendUserId, toUserId} = getMsgIdAndSendUidByRemoteCallDom(callMsg.msgType);
                logger.info("创建并发送 answer,msgType:", callMsg.msgType, "msgId:", msgId, "sendUserId:", sendUserId, "toUserId:", toUserId);
                sendAVCallMsg(new MsgCallO(CallType.answer[0], msgId, '', desc), callMsg.msgType, getToUserId(sendUserId, toUserId));
                // 开启计时器
                callTiming();
            });
        }, () => hangup(CallType.cancel[0], callMsg.msgType));
    });
}

/**
 * @param {Object} stream
 * @param {Object} hint motion :保持流畅性 但是降低分辨率  detail/text：分辨率不变 但是fps可以变动
 */
function setVideoTrackContentHints(stream, hint) {
    const tracks = stream.getVideoTracks();
    tracks.forEach(track => {
        if ('contentHint' in track) {
            track.contentHint = hint;
            if (track.contentHint !== hint) {
                logger.info('Invalid video track contentHint: \'' + hint + '\'');
            }
        } else {
            logger.info('MediaStreamTrack contentHint attribute not supported');
        }
    });
}

function setVideoDomStream(videoDom, stream) {
    // setVideoTrackContentHints(stream, 'detail');
    // 旧的浏览器可能没有 srcObject
    if ("srcObject" in videoDom) {
        videoDom.srcObject = stream;
    } else {
        window.URL = (window.URL || window.webkitURL || window.mozURL || window.msURL);
        // 防止在新的浏览器里使用它，应为它已经不再支持了
        videoDom.src = window.URL && window.URL.createObjectURL(stream) || stream
    }

}

function saveRemoteIceCandidate(callMsg) {
    if (callMsg.isMy) {
        return;
    }
    saveIceCandidate(callMsg.candidate);
}

function saveAnswerSDP(callMsg) {
    if (callMsg.isMy) {
        return;
    }
    saveSdp(callMsg.desc, () => logger.info("answer sdp 保存成功,msgType:", callMsg.msgType, ",msgId:", callMsg.msgId));
}

function saveOfferSDP(callMsg) {
    if (callMsg.isMy) {
        return;
    }
    saveSdp(callMsg.desc, () => logger.info("offer1 sdp 保存成功,msgType:", callMsg.msgType, ",msgId:", callMsg.msgId));
}

function getMsgIdByRemoteCallDom(msgType) {
    return $(getRemoteCallDom(msgType)).attr("msg-id");
}

function getMsgIdAndSendUidByRemoteCallDom(msgType) {
    let remoteDom = $(getRemoteCallDom(msgType));
    let msgId = remoteDom.attr("msg-id");
    let sendUserId = remoteDom.attr("send-user-id");
    let toUserId = remoteDom.attr("to-user-id");

    return {msgId, sendUserId, toUserId};
}

function getLocalCallDom(msgType) {
    return msgType === MsgType.AUDIO_CALL ? localAudioDom : localVideoDom;
}

function getRemoteCallDom(msgType) {
    return msgType === MsgType.AUDIO_CALL ? remoteAudioDom : remoteVideoDom;
}

/**
 * 获取用户信息并打开通话对话框
 *
 * @param msgBody
 */
function getUserAndOpenAVCall(msgBody) {
    if (isOpenCallDialog()) { //已经打开通话对话框了
        return;
    }
    let toUserId;
    if (msgBody.sendUserId === msgBody.toUserId && msgBody.toUserId === chatUser.id) { //发送者发给自己的
        toUserId = chatToUser.id;
    } else {
        toUserId = msgBody.sendUserId;
    }
    getChatUserInfo(toUserId, function (callUser) {
        callUser.isMy = msgBody.sendUserId === chatUser.id;
        callUser.msgType = msgBody.msgType;
        callUser.msgId = msgBody.id;
        callUser.sendUserId = msgBody.sendUserId;
        callUser.toUserId = toUserId;
        openAVCall(callUser);
    });
}

/**
 * 打开视频或语音通话对话框
 * @param callUser
 */
function openAVCall(callUser) {
    logger.info("打开视频或语音通话对话框,callUser:", callUser);
    // 清除等待邀请计时器
    clearWaitingCallTimer();
    //打开通话对话框 并初始化webrtc
    openAVCallDialogAndInit(callUser);
    callState = CallType.invite[0];
    if (callUser.isMy) {
        callState = CallType.invite[0] + '-my';
    }
    //等待15秒未收到对方回应,发送无人接听消息关闭对话框
    waitingAndSendNoAnswer(callUser.msgType);
    // 播放铃声
    playCallRingtone();
}

/**
 * 打开语音通话对话框并初始化 webrtc
 * @param callUser
 */
function openAVCallDialogAndInit(callUser) {
    //显示对话框
    if (callUser.msgType === MsgType.AUDIO_CALL) { //语音通话
        $("#call-audio-dialog").css('background-image', `url("${callUser.avatar}")`).removeClass("hide");
        //增加msgId属性
        $(remoteAudioDom).attr("msg-id", callUser.msgId).attr("send-user-id", callUser.sendUserId).attr("to-user-id", callUser.toUserId);
    } else { //视频通话
        $("#call-video-dialog").css('background-image', `url("${callUser.avatar}")`).removeClass("hide");
        //增加msgId属性
        $(remoteVideoDom).attr("msg-id", callUser.msgId).attr("send-user-id", callUser.sendUserId).attr("to-user-id", callUser.toUserId);
        //设置本地前置摄像头样式,旋转180度,解决镜像问题,切换后置摄像头时需要去掉
        $(localVideoDom).addClass("call-local-video-mirror");
    }
    //设置头像和昵称
    $(".call-avatar-bg .avatar-a").attr("src", callUser.avatar);
    $(".call-avatar-bg .nickname").text(callUser.remark ? callUser.remark : callUser.nickname);
    //显示接通按钮
    if (callUser.isMy) { //显示本地按钮
        $(".call-local").removeClass("hide");
        $(".call-remote").addClass("hide");
        $(".call-remote-accept").addClass("hide");
    } else { //显示对方按钮
        $(".call-remote").removeClass("hide");
        $(".call-local").addClass("hide");
        $(".call-remote-accept").addClass("hide");
    }
    if (typeof (RTCPeerConnection) === "undefined") {
        myAlert("不兼容", '抱歉,你当前使用的浏览器不支持语音和视频聊天,请使用主流浏览器进行视频聊天', 'warn', () => {
            hangup(callUser.isMy ? CallType.cancel[0] : CallType.refuse[0], callUser.msgType);
        });
        return;
    }
    //初始化webrtc
    WebRTCInit(callUser.msgType);
}

//等待接通倒计时
let waitingCallTimer = 0;

/**
 * 清除等待接通倒计时
 */
function clearWaitingCallTimer() {
    if (waitingCallTimer !== 0) {
        clearTimeout(waitingCallTimer);
        waitingCallTimer = 0;
    }
}

/**
 * 等待15秒未收到对方回应,发送无人接听消息关闭对话框
 * @param msgType
 */
function waitingAndSendNoAnswer(msgType) {
    //等待接通中的倒计时
    waitingCallTimer = setTimeout(() => {
        let {msgId, sendUserId, toUserId} = getMsgIdAndSendUidByRemoteCallDom(msgType);
        logger.info("30秒后没有收到回应,发送挂断信息,msgType:", msgType, "msgId:", msgId, "sendUserId:", sendUserId, "toUserId:", toUserId);
        // 30秒后没有收到回应, 则发送无人接听消息关闭对话框
        sendAVCallMsg(new MsgCallO(CallType.no_answer[0], msgId), msgType, getToUserId(sendUserId, toUserId));
    }, 30 * 1000);
}


/**
 * 发送挂断通话信息
 */
function hangup(callType, msgType) {
    let {msgId, sendUserId, toUserId} = getMsgIdAndSendUidByRemoteCallDom(msgType);
    logger.info("取消/挂断通话,msgType:", msgType, "msgId:", msgId, "sendUserId:", sendUserId, "toUserId:", toUserId);
    //发送挂断信息
    sendAVCallMsg(new MsgCallO(callType, msgId), msgType, getToUserId(sendUserId, toUserId));
}

function hideCallBtnAndDialog() {
    // 清除计时器
    clearCallTimer();
    clearHideRemoteBtnTimer();
    clearWaitingCallTimer();
    // 关闭对话框
    $(".call-container").addClass("hide");//隐藏通话对话框
    $(".call-remote").addClass("hide");//隐藏准备接通的按钮
    $(".call-remote-accept").addClass("hide");//隐藏接通后的按钮
    $(".call-local").removeClass("hide");//显示取消按钮

    $(".call-camera-off").removeClass("call-camera-off").addClass("call-camera-on"); //摄像头默认打开
    $(".call-microphone-off").removeClass("call-microphone-off").addClass("call-microphone-on");//麦克风默认打开
    // 恢复通话对话框
    recoveryCallDialog(null);

    $(localVideoDom).addClass("hide").addClass("call-local-video-mirror");
    //清除消息id
    $(remoteVideoDom).removeAttr("msg-id").removeAttr("to-user-id").removeAttr("send-user-id").addClass("hide");
    $(remoteAudioDom).removeAttr("msg-id").removeAttr("to-user-id").removeAttr("send-user-id").addClass("hide");

    // 停止播放铃声
    pauseCallRingtone();
    //播放挂断铃声
    plaCallHangupRingtone();
}

function getToUserId(sendUserId, toUserId) {
    return chatUser.id === parseInt(sendUserId) ? toUserId : sendUserId;
}

/**
 * 麦克风开关
 */
function microphoneTurnedOnOff(_this) {
    let isOff = false;
    let $this = $(_this);
    if ($this.hasClass("call-microphone-on")) {
        $this.removeClass("call-microphone-on").addClass("call-microphone-off");
        $this.parent().find("div").html("麦克风已关");
        isOff = true;
    } else {
        $this.removeClass("call-microphone-off").addClass("call-microphone-on");
        $this.parent().find("div").html("麦克风已开");
        isOff = false;
    }
    if (isOff) { //关闭麦克风
        localMediaStream.getAudioTracks().forEach(track => track.enabled = false);
        logger.info("关闭麦克风");
    } else { //打开麦克风
        logger.info("打开麦克风");
        localMediaStream.getAudioTracks().forEach(track => track.enabled = true);
    }
}

/**
 * 摄像头开关
 */
function cameraTurnedOnOff(_this) {
    let isOff = false;
    let $this = $(_this);
    if ($this.hasClass("call-camera-on")) {
        $this.removeClass("call-camera-on").addClass("call-camera-off");
        $this.parent().find("div").html("摄像头已关");
        isOff = true;
    } else {
        $this.removeClass("call-camera-off").addClass("call-camera-on");
        $this.parent().find("div").html("摄像头已开");
        isOff = false;
    }
    if (isOff) { //关闭摄像头
        logger.info("关闭摄像头");
        localMediaStream.getVideoTracks().forEach(track => track.enabled = false);
    } else { //打开摄像头
        logger.info("打开摄像头");
        localMediaStream.getVideoTracks().forEach(track => track.enabled = true);
    }
}

/**
 * 翻转摄像头
 */
function flipCamera(_this) {
    let facingMode = $(_this).attr("facing-mode");
    if (facingMode === "environment") {
        facingMode = "user";
        $(localVideoDom).addClass("call-local-video-mirror");
    } else {
        facingMode = "environment";
        $(localVideoDom).removeClass("call-local-video-mirror");
    }
    $(_this).attr("facing-mode", facingMode);

    let constraints = getCurrLocalConstraints(MsgType.VIDEO_CALL, facingMode);

    // 关闭本地视频流
    localMediaStream.getVideoTracks().forEach(track => track.stop());
    // 打开本地媒体
    openLocalMedia(constraints, (stream) => {
        // 显示本地视频流
        setVideoDomStream(localVideoDom, stream);

        const [videoTrack] = stream.getVideoTracks();
        const sender = rtcPeerConnection.getSenders().find((s) => s.track.kind === videoTrack.kind);
        sender.replaceTrack(videoTrack).then(r => logger.info("翻转摄像头成功:", r, new Date().toLocaleString()));

    }, () => hangup(CallType.dropped[0], MsgType.VIDEO_CALL));
}


function getCurrLocalConstraints(msgType, facingMode) {
    let constraints = {
        audio: localMediaStream.getAudioTracks()[0].getConstraints()
    }
    if (msgType === MsgType.VIDEO_CALL) {
        constraints.video = localMediaStream.getVideoTracks()[0].getConstraints();
        constraints.video.facingMode = facingMode;
    }
    return constraints;
}

function switchMinimize(_this) {
    logger.info("切换最小化窗口");
    let callDialog = $(_this).parent();
    callDialog.addClass("call-container-min");
    callDialog.draggable({containment: "parent"});
    $(".call-dialog-min-ico").addClass("hide");
    if (callState === CallType.invite[0]) {//邀请中显示描述
        $(".call-dialog-min-label").removeClass("hide");
    }

    $(".call-remote").addClass("hide");//隐藏准备接通的按钮
    $(".call-remote-accept").addClass("hide");//隐藏接通后的按钮
    $(".call-local").addClass("hide");//显示取消按钮
    setTimeout(function () {
        logger.info("添加点击事件");
        callDialog.on('click', () => recoveryCallDialog(callDialog))
    }, 500);
}

function recoveryCallDialog(callDialog) {
    logger.info("恢复窗口");
    if (!mobile) {
        $(".call-container").css("top", "").css("left", "");
        return;
    }
    if (callDialog == null || callDialog.length === 0) {
        return;
    }
    callDialog.off("click");
    callDialog.removeClass("call-container-min").css("top", "").css("left", "");
    callDialog.draggable("destroy");
    $(".call-dialog-min-ico").removeClass("hide");
    $(".call-dialog-min-label").addClass("hide");

    if (callState === CallType.invite[0] + '-my') {
        $(".call-local").removeClass("hide");//显示取消按钮
    } else if (callState === CallType.invite[0]) {
        $(".call-remote").removeClass("hide");//隐藏准备接通的按钮
    } else {
        $(".call-remote-accept").removeClass("hide");//隐藏接通后的按钮
    }
}

/**
 * 检查是否有未接通通话
 */
function checkAndOpenCallDialog() {
    let url = `${MSG_URL_PREFIX}/chat/msg/getLastCallMsg`;
    ajaxRequest(url, "get", {}, null, function (res) {
        if (res.code !== 200) {
            logger.info("获取最近语音消息失败,res:", res);
            myAlert('', res.message, "err");
            return;
        }
        if (!res.data) {
            return;
        }
        getUserAndOpenAVCall(res.data);
    });
}