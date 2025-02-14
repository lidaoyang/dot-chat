function sendAiMsg() {
    let content = $("#ai-output").text();
    if (content === '' || content.trim() === '') {
        myAlert('', "内容不能为空", 'warn');
        return;
    }
    sendObjMsg(content, MsgType.TEXT);
    $(".ai-msg-dialog").hide();
    $('.ai-btn').attr('disabled', true);
}

function editAiMsg() {
    let content = $("#ai-output").text();
    if (content === '' || content.trim() === '') {
        myAlert('', "内容不能为空", 'warn');
        return;
    }
    let $contentInput = $("#content-input");
    $contentInput.val(content);
    if (mobile) {
        // 触发内容输入框变化的处理函数，以处理输入后的逻辑，如更新界面显示等
        contentInputChange($contentInput);
    }
    $(".ai-msg-dialog").hide();
    $('.ai-btn').attr('disabled', true);
}


/**
 * 设置AI消息列表
 */
function getDeepSeekAiMsgList() {
    // 清空
    let deepSeekAiMsgList = [];
    deepSeekAiMsgList.push(new DSAiMessage(chatUser.nickname + chatUser.id, '你是当前聊天中的一员,你来简单的回答一下对方', 'system'));
    let msgListDom = $("#chat-msg-list").children();
    let lastThreeDom = msgListDom.length <= 3 ? msgListDom : msgListDom.slice(-3);
    for (let i = 0; i < lastThreeDom.length; i++) {
        let $msgDom = $(lastThreeDom[i]);
        if ($msgDom.attr("class") === "sys") {
            continue;
        }
        let msgType = $msgDom.find(".msg-text").attr("msg-type");
        if (msgType !== MsgType.TEXT) {
            continue;
        }
        let uid = $msgDom.find(".avatar").attr("user-id");
        let name = $msgDom.find(".msg-name").text();
        let content = $msgDom.find(".msg-text-wrap").text();
        deepSeekAiMsgList.push(new DSAiMessage(name + uid, content));
    }
    // logger.info("AI消息列表:", deepSeekAiMsgList);
    return deepSeekAiMsgList;
}

/**
 * 注册AI消息点击事件
 */
function registerNavAiMsgClick() {
    $("#nav-ai-reply-msg").on("click", function (e) {
        if (!checkToUser()) {
            return;
        }
        // 显示AI消息框
        $(".ai-msg-dialog").show();
        // 注册document点击事件
        registerDocumentClickForAi();

        // 设置AI消息列表
        let deepSeekAiMsgList = getDeepSeekAiMsgList();
        // 清空输出内容
        let $aiOutput = $("#ai-output");
        $aiOutput.html('');

        if (deepSeekAiMsgList.length <= 1) {
            $aiOutput.html('还没有聊天上下文,请先联系对方,等对方回复后,我在参与吧!');
            return;
        }

        let data = {
            messages: deepSeekAiMsgList
        }
        // 显示加载中
        $('.ai-loading').show();
        // 开始sse接收AI数据
        startSSE(data);
        e.stopPropagation();
    });
}

function registerDocumentClickForAi() {
    $(document).on("click", function (event) {
        let clsName = event.target.className;
        if (clsName.startsWith("ai-")) {
            return;
        }
        $(".ai-msg-dialog").hide();
        $('.ai-btn').attr('disabled', true);
        // 停止sse
        stopSSE();
        // 移除点击事件
        $(document).off("click");
    });
}

let sseClient = null;

function startSSE(messages) {
    let requestBody = JSON.stringify(messages);
    let url = `${DEEPSEEK_URL_PREFIX}/completions/stream`;
    let token = $.cookie(TOKEN_KEY);
    if (!token) {
        logger.error("token过期");
        deleteUserCookie();
        return;
    }
    // 配置 SSE 连接
    sseClient = new SSE(url, {
        headers: {
            'Authorization': token, // 设置请求头
            'Content-Type': 'application/json'
        },
        payload: requestBody, // 设置请求体（仅对 POST 有效）
        method: 'POST' // 默认是 GET，如需 POST 可修改
    });

    // 监听消息事件
    sseClient.addEventListener('message', (event) => {
        const data = event.data;
        // logger.info('Data: ', data);
        document.getElementById('ai-output').innerHTML += data;
    });

    // 监听自定义事件（需后端指定事件名称）
    // sseClient.addEventListener('customEvent', (event) => {
    //     logger.info('Custom Event:', event.data);
    // });
    sseClient.addEventListener('readystatechange', (event) => {
        // logger.info('readystatechange event:', event);
        if (event.readyState === 1) {
            $('.ai-loading').hide();
        }
        if (event.readyState === 2) {
            $('.ai-loading').hide();
            let $aiOutput = $("#ai-output");
            if ($aiOutput.text() === '') {
                $aiOutput.html('服务器繁忙，请稍后再试。');
            } else {
                $('.ai-btn').removeAttr('disabled');
            }
            closeSse();
        }
    });
    // 错误处理
    sseClient.onerror = (error) => {
        logger.error('SSE Error:', error);
    };

    // 启动连接
    sseClient.stream();
}

function stopSSE() {
    if (sseClient) {
        // 关闭服务端sse连接
        closeServiceSse();
        closeSse();
    }
}

function closeSse() {
    if (sseClient) {
        sseClient.close(); // 关闭连接
        sseClient = null;
    }
}

/**
 * 关闭服务端sse连接
 */
function closeServiceSse() {
    let url = `${DEEPSEEK_URL_PREFIX}/closeSse`;
    let token = $.cookie(TOKEN_KEY);
    if (!token) {
        logger.error("token过期");
        deleteUserCookie();
        return;
    }
    ajaxRequest(url, "post", null, null, function (res) {
        if (res.code !== 200) {
            logger.info("关闭SSE失败");
            myAlert('', res.message, "err");
        }
    });
}


/**
 * DeepSeek Ai 信息对象
 *
 * @param name 名称
 * @param content 内容
 * @param role 角色(user/assistant/system)
 * @constructor
 */
function DSAiMessage(name, content, role,) {
    this.name = name;
    this.content = content;
    this.role = role;
}
