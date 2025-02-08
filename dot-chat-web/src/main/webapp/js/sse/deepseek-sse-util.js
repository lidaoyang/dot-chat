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
    $("#content-input").val(content);
    $(".ai-msg-dialog").hide();
    $('.ai-btn').attr('disabled', true);
}

/**
 * 注册AI消息点击事件
 */
function registerNavAiMsgClick() {
    $("#nav-ai-reply-msg").on("click", function (e) {
        if (!checkToUser()) {
            return;
        }
        $(".ai-msg-dialog").show();
        $('.ai-loading').show();
        $("#ai-output").html('');

        let data = {
            messages: [
                new DSAiMessage('system', '你是当前聊天中的一员,名称叫吉祥,你来帮我继续回答对方的信息吧', 'system'),
                new DSAiMessage('daoyang', '快来玩王者啊，我邀请你。今天想玩哪个英雄？'),
            ]
        }
        // 开始sse接收AI数据
        startSSE(data);
        // let text = "等待对方回复后,AI会根据对方回复的内容生成回复消息";

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
        e.stopPropagation();
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
    sseClient.addEventListener('customEvent', (event) => {
        logger.info('Custom Event:', event.data);
    });
    sseClient.addEventListener('readystatechange', (event) => {
        logger.info('readystatechange event:', event);
        if (event.readyState === 1) {
            $('.ai-loading').hide();
        }
        if (event.readyState === 2) {
            $('.ai-btn').removeAttr('disabled');
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
        sseClient.close(); // 关闭连接
        sseClient = null;
        // 关闭服务端sse连接
        closeServiceSse();
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
