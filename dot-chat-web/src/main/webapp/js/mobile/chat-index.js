$(function () {
    if (!mobile) {
        location.href = "/index.html";
    }
    //登录用户存在时去登录
    if (!chatUser) {
        location.href = "/login.html";
    }
    //自动刷新token
    autoRefreshToken();
    //加载聊天室
    loadChatRoom();
    //注册添加好友按钮点击事件
    registerAddOperationClick();
    //初始化创建群聊对话框
    initCreateGroupDialog();
    //初始化添加好友对话框
    initAddFriendDialog();
    //初始化修改我的信息对话框
    initModifyMyInfoDialog();
    //初始化修改头像对话框
    initModifyAvatarDialog();
    //初始化修改密码对话框
    initModifyPwdDialog();
    // 初始化toast
    initToasts();
    //添加返回按钮监听事件
    addPopstateEventListener();
    //欢迎对话框
    welcomeAlert();
});

/**
 * 添加popstate事件监听器，用于处理浏览器历史记录变化时的情况
 * 添加后,如果用户没有对页面进行操作(点击或其他操作),则不会触发popstate
 * 特别是关注用户在手机上返回时的状态管理
 */
function addPopstateEventListener() {
    // 监听popstate事件，当浏览器历史记录变化时触发
    window.addEventListener("popstate", function (e) {
        // 记录用户通过手机返回时的历史状态
        logger.info("点击了手机的返回,history.state:", history.state, e);
        // 获取页面上的返回按钮元素
        let $leftArrow = $(".header .left-arrow");
        // 获取返回操作类型
        let action = getBackAction($leftArrow);
        // 如果返回操作类型为空，提示用户再次点击返回将离开聊天室
        if (action === null) {
            toastr.info("再次点击返回将会离开聊天室");
            return;
        }
        // 如果返回操作类型为消息列表，更新聊天对象
        if (action === "msg-list") {
            chatToUser = history.state.chatToUser;
        }
        // 重新设置返回按钮的操作类型
        $leftArrow.attr("action", action);
        // 执行返回操作
        goBack();
    }, false);
}

/**
 * 获取返回操作类型
 * @param $leftArrow {jQuery Object} 返回按钮的jQuery对象
 * @return {string|null} 返回操作类型，如果没有定义则返回null
 */
function getBackAction($leftArrow) {
    // 如果历史状态为空，从返回按钮上获取操作类型
    if (history.state == null) {
        let action = $leftArrow.attr("action");
        if (action !== undefined) {
            return action;
        } else {
            return null;
        }
    } else {
        // 如果历史状态不为空，从历史状态中获取标题作为操作类型
        return history.state.tit;
    }
}


/**
 * 向浏览器历史记录中添加一个新的状态对象。
 *
 * 添加后,如果用户没有对页面进行操作(点击或其他操作),则不会触发popstate
 * 该函数使用window.history.pushState方法向浏览器历史栈中添加一个新的状态对象，
 * 从而实现不刷新页面的情况下改变浏览器的地址栏显示。常用于单页面应用中，以增强用户体验。
 *
 */
function pushHistory(state) {
    // logger.info("开始向览器历史记录中添加一个新的状态对象,state:", state, ",history.state:", history.state);
    if (history.state != null && history.state.tit === state.tit) {
        return;
    }
    // 使用pushState方法将状态对象添加到浏览器历史记录中
    window.history.pushState(state, null, "#");
    // logger.info("向览器历史记录中添加一个新的状态对象,state:", state, ",history.state:", history.state);
}


function bottomMsgBtnClick() {
    $("#bottom-msg-btn").addClass("bottom-btn-click").addClass("bottom-msg-btn-click");
    $("#bottom-friend-btn").removeClass("bottom-friend-btn-click").removeClass("bottom-btn-click");
    $("#bottom-my-btn").removeClass("bottom-my-btn-click").removeClass("bottom-btn-click");
    $(".header .nickname").text("消息");
    $("#add-operation").removeClass("hide").addClass("show");
    loadChatRoom();
}

function bottomFriendBtnClick() {
    $("#bottom-friend-btn").addClass("bottom-btn-click").addClass("bottom-friend-btn-click");
    $("#bottom-msg-btn").removeClass("bottom-msg-btn-click").removeClass("bottom-btn-click");
    $("#bottom-my-btn").removeClass("bottom-my-btn-click").removeClass("bottom-btn-click");
    $(".header .nickname").text("好友");
    $("#add-operation").removeClass("hide").addClass("show");
    loadChatFriend();
}

function bottomMyBtnClick() {
    $("#bottom-my-btn").addClass("bottom-btn-click").addClass("bottom-my-btn-click");
    $("#bottom-msg-btn").removeClass("bottom-msg-btn-click").removeClass("bottom-btn-click");
    $("#bottom-friend-btn").removeClass("bottom-friend-btn-click").removeClass("bottom-btn-click");
    $(".header .nickname").text("我");
    $("#add-operation").addClass("hide").removeClass("show");
    gotoMy();
}

function gotoMy() {
    let userTypeO = {};
    if (USER_TYPE === USER_TYPE_O.ENT_USER.val) {
        userTypeO = USER_TYPE_O.ENT_USER;
    } else if (USER_TYPE === USER_TYPE_O.ENTERPRISE.val) {
        userTypeO = USER_TYPE_O.ENTERPRISE;
    } else if (USER_TYPE === USER_TYPE_O.SUPPLIER.val) {
        userTypeO = USER_TYPE_O.SUPPLIER;
    } else if (USER_TYPE === USER_TYPE_O.PL_ADMIN.val) {
        userTypeO = USER_TYPE_O.PL_ADMIN;
    }
    let sexDom = ``;
    if (chatUser.sex !== null && chatUser.sex !== undefined) {
        if (chatUser.sex === 1) {
            sexDom = `<span class="my-sex my-sex-male"></span>`;
        } else if (chatUser.sex === 2) {
            sexDom = `<span class="my-sex my-sex-female"></span>`;
        }
    }
    let infoDom = `
            <div class="my-info" >
                <div class="my-head">
                    <img class="my-avatar" src="${chatUser.avatar}" alt="头像" onclick="modifyAvatarOpen()"/>
                     <div class="my-name-info">
                        <span class="my-name" onclick="modifyMyInfoOpen()">
                            <label>${chatUser.nickname}</label> 
                            <i class="my-edit"></i>
                        </span>
                        <span class="my-id">ID: ${chatUser.id}</span>
                        <span class="my-label ${userTypeO.cls}">${userTypeO.desc}</span>
                        ${sexDom}
                    </div>
                </div>
                <div class="more">
                    <div class="more-info">
                    <label>账号</label><span>${chatUser.phone}</span>
                    </div>
                    <div class="more-info">
                    <label>密码</label><span>****** <i class="my-edit" onclick="modifyPwdOpen()"></i></span>
                    </div>
                    <div class="more-info">
                        <label>公司</label><span>${chatUser.enterpriseName}</span>
                    </div>
                </div>
                <div class="my-bottom">
                    <button class="logout" onclick="logout()">退出登录</button>
                </div>
            </div>
        `;
    $("#container").html("").append(infoDom);
    //自定义移动端返回事件
    pushHistory({tit: "my"});
}

/**
 * 初始化修改我的头像对话框
 */
function initModifyAvatarDialog() {
    $("#modify-avatar-dialog").dialog({
        title: "修改头像",
        autoOpen: false,   // 是否自动弹出窗口
        modal: true,    // 设置为模态对话框
        resizable: false,
        width: 330,   //弹出框宽度
        height: 450,   //弹出框高度
        close: function (event, ui) {
            $("#tailoringImg").cropper('destroy').removeAttr("src");
            $("#chooseImg").val("");
        }
    });
}


function modifyAvatarOpen() {
    $("#modify-avatar-dialog").dialog("open");
}

function cropImageConfirmClick() {
    let $tailoringImg = $('#tailoringImg');
    if ($tailoringImg.attr("src") === undefined || $("#chooseImg").val() === "") {
        myAlert('', '请先选择图片', 'tip')
        return;
    }
    let cropper = $tailoringImg.cropper('getCroppedCanvas');
    let compressQuality = 0.7; // 压缩质量
    let file = base64ToFile(cropper.toDataURL(uploadedImageType), uploadedImageName);
    uploadChatUserAvatar(file, function (res) {
        $(".my-avatar").attr("src", res);
        $("#modify-avatar-dialog").dialog("close");
    });
}

function cropperImg() {
    $('#tailoringImg').cropper({
        aspectRatio: 1,
        viewMode: 2,
        preview: '.img-preview',
        // data: {
        //     width: 300,
        //     height: 300
        // },
        crop: function (e) {
            $("#dataWidth").text(Math.round(e.detail.width) + "px");
            $("#dataHeight").text(Math.round(e.detail.height) + "px");
        }
    });
}

function modifyAvatarClick() {
    $("#chooseImg").click();
}

let uploadedImageName = 'cropper-avatar.jpg';
let uploadedImageType = 'image/jpeg';
let uploadedImageURL;

function modifyAvatarFileChange(_this) {
    let files = _this.files;
    if (files && files.length) {
        let file = files[0];
        if (/^image\/\w+$/.test(file.type)) {
            uploadedImageName = file.name;
            uploadedImageType = file.type;

            if (uploadedImageURL) {
                URL.revokeObjectURL(uploadedImageURL);
            }

            uploadedImageURL = URL.createObjectURL(file);
            $('#tailoringImg').cropper('destroy').attr('src', uploadedImageURL);
            cropperImg();
        } else {
            alert('Please choose an image file.');
        }
    }
}

function cropperOper(_this) {
    let $this = $(_this);
    let data = $this.data();
    switch (data.method) {
        case 'rotate':
            break;
        case 'scaleX':
        case 'scaleY':
            $this.data('option', -data.option);
            break;
    }
    let $tailoringImg = $('#tailoringImg');
    if ($tailoringImg.attr("src") !== undefined) {
        $tailoringImg.cropper(data.method, data.option);
    }
}

/**
 * 初始化修改我的信息对话框
 */
function initModifyMyInfoDialog() {
    $("#modify-myinfo-dialog").dialog({
        title: "修改个人信息",
        autoOpen: false,   // 是否自动弹出窗口
        modal: true,    // 设置为模态对话框
        resizable: false,
        width: 300,   //弹出框宽度
        height: 330,   //弹出框高度
        close: function (event, ui) {

        }
    });
}

function modifyMyInfoOpen() {
    $("#modify-avatar").attr("src", chatUser.avatar);
    $("#modify-nick").text(chatUser.nickname).focus();
    $("#nickname").val(chatUser.nickname);
    $("#modify-area").text(chatUser.enterpriseName);
    if (chatUser.sex !== null && chatUser.sex !== undefined) {
        $("#gender").val(chatUser.sex);
    }
    $("#modify-myinfo-dialog").dialog("open");
}

function saveMyInfoClick() {
    let nickname = $("#nickname").val();
    let sex = $("#gender").val();
    sex = sex ? parseInt(sex) : 0;
    let url = `${MSG_URL_PREFIX}/chat/user/updateNickname`;
    ajaxRequest(url, "post", {userType: USER_TYPE, nickname: nickname, sex: sex}, null, function (res) {
        if (res.code !== 200) {
            logger.info("昵称更新失败,res:", res);
            myAlert('', res.message, "err");
            return;
        }
        chatUser.nickname = nickname;
        chatUser.sex = sex;
        localStorage.setItem(CHAT_USER_KEY, JSON.stringify(chatUser));
        $(".my-name label").text(nickname);
        let sexDom = ``;
        if (chatUser.sex === 1) {
            sexDom = `<span class="my-sex my-sex-male"></span>`;
        } else if (chatUser.sex === 2) {
            sexDom = `<span class="my-sex my-sex-female"></span>`;
        }
        $(".my-name-info .my-sex").remove();
        $(".my-name-info").append(sexDom);

        $("#modify-myinfo-dialog").dialog("close");
    });
}

/**
 * 初始化修改我的信息对话框
 */
function initModifyPwdDialog() {
    $("#modify-pwd-dialog").dialog({
        title: "修改密码",
        autoOpen: false,   // 是否自动弹出窗口
        modal: true,    // 设置为模态对话框
        resizable: false,
        width: 300,   //弹出框宽度
        height: 340,   //弹出框高度
        close: function (event, ui) {
            $("#oldPwd").val("");
            $("#newPwd").val("");
            $("#validNewPwd").val("");
        }
    });
}

function modifyPwdOpen() {
    $(".modify-greet").focus();
    $("#modify-pwd-dialog").dialog("open");
}

function modifyPwdClick() {
    let oldPwd = $("#oldPwd").val();
    if (oldPwd === "") {
        myAlert('', "请输入原密码", "err");
        return;
    }
    let newPwd = $("#newPwd").val();
    let validNewPwd = $("#validNewPwd").val();
    if (newPwd === "") {
        myAlert('', "请输入新密码", "err");
        return;
    }
    if (newPwd.length < 6 || newPwd.length > 20) {
        myAlert('', "新密码长度为6-20位", "err");
        // return;
    }
    if (validNewPwd === "") {
        myAlert('', "请输入确认密码", "err");
        return;
    }
    if (newPwd !== validNewPwd) {
        myAlert('', "两次密码输入不一致", "err");
        return;
    }
    let url = `${SYS_URL_PREFIX}/user/updatePassword`;
    ajaxRequest(url, "post", {
        userType: USER_TYPE,
        oldPwd: oldPwd,
        newPwd: newPwd,
        validNewPwd: validNewPwd
    }, null, function (res) {
        if (res.code !== 200) {
            logger.info("密码更新失败,res:", res);
            myAlert('', res.message, "err");
            return;
        }
        $("#modify-pwd-dialog").dialog("close");
        myConfirm('修改成功', "密码修改成功,是否重新登录?", function () {
            deleteUserCookie();
        });
    });
}

/**
 * 加载聊天室
 */
function loadChatRoom() {
    $("#container").load("/mobile/chat-room.html", function (responseTxt, statusTxt, xhr) {
        if (statusTxt === "success") {
            logger.info("外部HTML[chat-room.html]加载成功！");
            //清空未读聊天室ID集合
            unreadChatIdSet.clear();
            //获取聊天室列表
            getChatRoomList();
            //获取聊天总未读数
            getChatTotalUnreadCount();
            //注册聊天室列表右键菜单
            registerChatRoomListContextMenu();
        } else {
            console.error("外部HTML[chat-room.html]加载失败！");
        }
    });
}


/**
 * 加载好友列表
 */
function loadChatFriend() {
    $("#container").load("/mobile/chat-friend.html", function (responseTxt, statusTxt, xhr) {
        if (statusTxt === "success") {
            logger.info("外部HTML[chat-friend.html]加载成功！");
            //获取好友申请列表
            getFriendApplyList();
            //注册好友申请消息回复按钮点击事件
            registerFriendApplyMsgReplayClick();
            //初始化同意好友申请弹出框
            initAgreeFriendApplyDialog();
            //获取好友列表
            getFriendList();
            $(".friend .friend-btn").click();
            //自定义移动端返回事件
            pushHistory({tit: "friend"});
        } else {
            console.error("外部HTML[chat-friend.html]加载失败！");
        }
    });
}


/**
 * 获取聊天列表
 */
function getChatRoomList() {
    let url = `${MSG_URL_PREFIX}/chat/room/list`;
    ajaxRequest(url, "get", {userType: USER_TYPE}, null, function (res) {
        if (res.code !== 200) {
            logger.info("获取聊天列表失败");
            myAlert('', res.message, "err");
            return;
        }
        chatRoomListCache = res.data;
        generateChatRoomDom(res.data);
        let $select = $("#chat-list .select");
        if ($select.length === 0) {
            //自定义移动端返回事件
            pushHistory({tit: "msg"});
        }
        $select.click();
    });
}


function generateChatRoomDom(chatRoomList) {
    let prevChatId = $.cookie(PREV_CHAT_ID_KEY);
    //清空未读消息集合
    unreadChatIdSet.clear();
    let chatListDom = $("#chat-list");
    chatListDom.html("");
    for (let i = 0; i < chatRoomList.length; i++) {
        let chatRoom = chatRoomList[i];
        let toUser = chatRoom.toUser;
        let unreadCountHideClass = "";
        if (chatRoom.unreadMsgCount === 0) {
            unreadCountHideClass = "hide";
        } else {
            unreadChatIdSet.add(chatRoom.chatId);
        }
        let className;
        if (prevChatId && prevChatId === chatRoom.chatId) {
            className = "list-box select";
        } else {
            className = "list-box";
        }
        if (chatRoom.isTop) {
            className = className + " chat-top";
        }

        let chatRoomDom = `
                <div class="${className}" chat-id="${chatRoom.chatId}" group-id="${chatRoom.groupId}" to-user-id="${toUser.userId}" is-dissolve="${toUser.isDissolve}" to-user-name="${toUser.nickname}" to-user-avatar="${toUser.avatar}" onclick="gotoChatMsgPage(this)">
                    <div class="chat-avatar">
                        <img class="chat-head" src="${toUser.avatar}" alt=""/>
                        <span class="chat-unread ${unreadCountHideClass}" unread-count="${chatRoom.unreadMsgCount}">${chatRoom.unreadMsgCount}</span>
                    </div>
                    <div class="chat-rig">
                        <div class="chat-title">
                            <span class="chat-name">${toUser.nickname}</span>
                            <span class="chat-last-time">${chatRoom.lastTime ? chatRoom.lastTime : ""}</span>
                        </div>
                        <div class="chat-bottom" last-msg-id="${chatRoom.lastMsgId ? chatRoom.lastMsgId : ''}">
                            <span class="chat-last-msg">${chatRoom.lastMsg ? chatRoom.lastMsg : ""}</span>
                        </div>
                    </div>
                </div>
                `;
        chatListDom.append(chatRoomDom);
    }
}

function gotoChatMsgPage(_this) {
    //缓存聊天对象信息
    setChatToUser(_this);
    gotoFirstChatMsg();
}

function goBack() {
    $(".footer").removeClass("hide").addClass("show");
    let goBackArrow = $(".header .left-arrow").removeClass("show").addClass("hide");
    let action = goBackArrow.attr("action");
    goBackArrow.removeAttr("action");
    if (action === 'msg') {
        bottomMsgBtnClick();
        clearChatUnreadCount();
        chatToUser = null;
    } else if (action === 'msg-list') {
        loadChatMsg();
    } else if (action === 'msg-more-info') {
        loadMsgInfoPage();
    } else if (action === 'msg-history') {
        loadChatMsgHistory();
    } else if (action === 'friend') {
        bottomFriendBtnClick();
    } else if (action === 'friend-info') {
        getFriendInfo(history.state.friendId);
    } else if (action === 'friend-apply-info') {
        getFriendApplyInfo(history.state.applyId);
    } else if (action === 'my') {
        bottomMyBtnClick();
    } else {
        logger.info("goBack:action is null");
        return;
    }
    $("#add-operation").addClass("show").removeClass("hide");
    $("#msg-more").addClass("hide");
    $(".group-member-num").addClass("hide").text("");
    deleteCookie(PREV_CHAT_ID_KEY);
}

function clearChatUnreadCount() {
    let chatListDom = $("#chat-list .list-box");
    for (let i = 0; i < chatListDom.length; i++) {
        let chatDom = chatListDom[i];
        let prevChatId = $.cookie(PREV_CHAT_ID_KEY);
        if (prevChatId === $(chatDom).attr("chat-id")) {
            let unreadCountDom = $(chatDom).find(".chat-unread");
            let unreadCount = unreadCountDom.attr("unread-count");
            if (unreadCount !== '0') {
                unreadCountDom.addClass("hide").attr('unread-count', 0).text("0");
            }
        }
    }
}

//---------------------------------------------------------聊天室-----------------------------------------------------------
/**
 * 加载聊天室
 */
function loadChatMsg() {
    $("#container").load("/mobile/chat-msg.html", function (responseTxt, statusTxt, xhr) {
        if (statusTxt === "success") {
            logger.info("外部HTML[chat-msg.html]加载成功！");
            //获取聊天信息列表
            getChatMsgList();
            if (chatToUser.isDissolve === 'true') {
                $("#content-input").attr("placeholder", "当前群聊已解散").attr("disabled", true);
            } else {
                //设置表情包hover效果
                setEmojiHover();
                //注册表情包点击事件
                registerNavFaceClick();
                //注册图片点击事件
                registerChatMsgPictureChange();
                //注册文件点击事件
                registerChatMsgFileChange();
            }
            initRelayMsgUserDialog();
            initRelayUserTabs();
            //注册右键菜单
            registerChatMsgListContextMenu();
            $(".footer").removeClass("show").addClass("hide");
            $(".header .left-arrow").removeClass("hide").addClass("show").attr("action", "msg");
            $("#add-operation").addClass("hide").removeClass("show");
            $("#msg-more").removeClass("hide");
            $("#send-msg-btn").focus();
            //添加历史记录
            pushHistory({tit: "msg-list", chatToUser: chatToUser});
        } else {
            console.error("外部HTML[chat-msg.html]加载失败！");
        }
    });
}

function gotoFirstChatMsg() {
    chatToUser.pageFlippingType = PAGE_FLIPPING_TYPE.FIRST;
    loadChatMsg();
}

function setChatToUser(chatDom) {
    chatToUser = new ChatToUser($(chatDom).attr("chat-id"), $(chatDom).attr("to-user-id"), $(chatDom).attr("group-id"), $(chatDom).attr("to-user-name"), $(chatDom).attr("to-user-avatar"));
    chatToUser.isDissolve = $(chatDom).attr("is-dissolve");
}

//聊天记录分页参数
let msgPage, msgUpPage, hisMsgPage;


function getChatMsgList() {
    if (!chatToUser) {
        myAlert('', "聊天用户不存在", "err");
        return;
    }
    //设置聊天室标题
    $(".header .nickname").text(chatToUser.nickname);

    if (chatToUser.pageFlippingType === PAGE_FLIPPING_TYPE.FIRST) {
        getChatMsgPageFirstList();
    } else if (chatToUser.pageFlippingType === PAGE_FLIPPING_TYPE.LOCATE) {
        let lastMsgId = msgUpPage.lastMsgId;
        getChatMsgPageUpList();
        setTimeout(function () {
            let msgContainer = $(".container");
            let msgOffsetTop = $(".msg-text-outer .msg-text[msg-id=" + lastMsgId + "]").offset().top;
            let msgContainerOffsetTop = msgContainer.offset().top;
            let msgContainerScrollTop = msgContainer.scrollTop();
            // logger.info("msgOffsetTop:", msgOffsetTop, "lastMsgId:", lastMsgId, "msgContainerOffsetTop:", msgContainerOffsetTop,
            //     "msgContainerScrollTop:", msgContainerScrollTop, "scroll-to:", (msgOffsetTop - msgContainerOffsetTop + msgContainerScrollTop - 20));
            msgContainer.animate({
                scrollTop: msgOffsetTop - msgContainerOffsetTop + msgContainerScrollTop - 20
            }, 450);//450毫秒秒滑动到指定位置
        }, 250);
    }
}

/**
 * 获取聊天消息列表
 */
function getChatMsgPageFirstList() {
    //下拉分页参数
    msgPage = new Page(15, 0, false);
    //上拉分页参数
    msgUpPage = new Page(15, 0, false);

    let chatId = chatToUser.chatId;
    //缓存1天聊天id
    setCookieDay(PREV_CHAT_ID_KEY, chatId, 1);
    let url = `${MSG_URL_PREFIX}/chat/msg/list`;
    let data = {
        userType: USER_TYPE,
        chatId: chatId,
        pageFlippingType: PAGE_FLIPPING_TYPE.FIRST,
        limit: msgPage.limit
    };
    ajaxRequest(url, "get", data, null, function (res) {
        if (res.code !== 200) {
            logger.info("获取聊天记录列表失败,chatId:", chatId, "res:", res);
            myAlert('', res.message, "err");
            return;
        }
        //删除未读集合中的聊天id
        unreadChatIdSet.delete(chatId);
        //群聊获取群成员信息
        if (chatToUser.groupId && chatToUser.groupId > 0) {
            //缓存群组成员列表
            getChatGroupMemberList(chatToUser.groupId);
            if (!isGroupMember()) {
                $("#content-input").attr("placeholder", "你被移除当前群聊").attr("disabled", true);
                $("#msg-more").addClass("hide");
            }
            $(".send-attachment-ul .call").addClass("hide"); //隐藏视频通话按钮
        } else {
            $(".send-attachment-ul .call").removeClass("hide"); //显示视频通话按钮
        }
        let chatMsgListDom = $("#chat-msg-list");
        chatMsgListDom.empty();
        let chatMsgList = res.data;
        for (let i = 0; i < chatMsgList.length; i++) {
            let chatMsg = chatMsgList[i];
            let user = getChatMsgUser(chatMsg);
            let chatMsgDom = generateChatMsgDom(user, chatMsg);
            chatMsgListDom.append(chatMsgDom);
        }
        //启动上传进度条
        startUploadProgressBar();
        //设置分页参数
        setMsgPageLastMsgId(chatMsgList, msgUpPage);
        setMsgPageUpLastMsgId(chatMsgList, msgPage);
        msgUpPage.isLastPage = true;
        //清除聊天消息未读数
        cleanChatMsgUnreadCount();
        //缓存1天聊天id
        setCookieDay(PREV_CHAT_ID_KEY, chatId, 1);
        //延时200毫秒,图片加载完成后滚动到底部
        setTimeout(function () {
            let msgContainer = document.getElementsByClassName("container")[0];
            if (!msgContainer || !msgContainer.scrollHeight) {
                return;
            }
            $(msgContainer).animate({
                scrollTop: msgContainer.scrollHeight - Math.round($(msgContainer).height()) - 30
            }, 450);//450毫秒秒滑动到指定位置
        }, 250);
        refreshContextMenu();
    });
}

/**
 * 获取聊天消息列表
 */
function getChatMsgPageUpList() {
    let chatId = chatToUser.chatId;
    let url = `${MSG_URL_PREFIX}/chat/msg/list`;
    let data = {
        userType: USER_TYPE,
        chatId: chatId,
        msgId: msgUpPage.lastMsgId,
        pageFlippingType: chatToUser.pageFlippingType,
        limit: msgUpPage.limit
    };
    ajaxSyncRequest(url, "get", data, null, function (res) {
        if (res.code !== 200) {
            logger.info("获取聊天记录列表失败,chatId:", chatId, "res:", res);
            myAlert('', res.message, "err");
            return;
        }
        //群聊获取群成员信息
        if (chatToUser.groupId && chatToUser.groupId > 0) {
            //缓存群组成员列表
            getChatGroupMemberList(chatToUser.groupId);
            if (!isGroupMember()) {
                $("#content-input").attr("placeholder", "你被移除当前群聊").attr("disabled", true);
            }
        }
        let chatMsgListDom = $("#chat-msg-list");
        if (chatToUser.pageFlippingType === PAGE_FLIPPING_TYPE.LOCATE) {
            chatMsgListDom.empty();
        }
        let chatMsgList = res.data;
        for (let i = 0; i < chatMsgList.length; i++) {
            let chatMsg = chatMsgList[i];
            let user = getChatMsgUser(chatMsg);
            let chatMsgDom = generateChatMsgDom(user, chatMsg);
            chatMsgListDom.append(chatMsgDom);
        }
        setMsgPageLastMsgId(chatMsgList, msgUpPage);
        if (chatToUser.pageFlippingType === PAGE_FLIPPING_TYPE.LOCATE) {
            setMsgPageUpLastMsgId(chatMsgList, msgPage);
            let msgContainer = document.getElementsByClassName("container")[0];
            if (msgContainer.scrollHeight === Math.round($(msgContainer).height()) || msgUpPage.isLastPage) {
                msgPage.isLastPage = false;
                nextPageMsgRecord(msgContainer);
            }
        }
        //启动上传进度条
        startUploadProgressBar();
        refreshContextMenu();
    });
}


function nextPageMsgRecord(_this) {
    let msgContainer = document.getElementsByClassName("container")[0];
    if (!msgContainer) {
        return;
    }
    let scrollTop = $(_this).scrollTop();
    let subHeight = (Math.round($(msgContainer).height()) + Math.round(scrollTop)) - msgContainer.scrollHeight;
    // logger.info("height:", Math.round($(msgContainer).height()), $(msgContainer).height(), "scrollTop:", Math.round(scrollTop), scrollTop,
    //     "scrollHeight:", msgContainer.scrollHeight, "subHeight", subHeight);
    if (scrollTop === 0) {
        if (msgPage.isLastPage) {
            logger.info("下拉加载已经是最后一页了");
            return;
        }
        pageDownMsgRecord();
    } else if (subHeight < 3 && subHeight > -2) {
        if (msgUpPage.isLastPage) {
            logger.info("上拉加载已经是最后一页了");
            return;
        }
        chatToUser.pageFlippingType = PAGE_FLIPPING_TYPE.PULL_UP;
        getChatMsgPageUpList();
        $(msgContainer).animate({
            scrollTop: scrollTop + 50
        }, 450);//450毫秒秒滑动到指定位置
    }
    refreshContextMenu();
}


function pageDownMsgRecord() {
    let msgContainer = document.getElementsByClassName("container")[0];
    let scrollHeight = msgContainer.scrollHeight;
    let chatId = chatToUser.chatId
    let url = `${MSG_URL_PREFIX}/chat/msg/list`;
    let data = {
        userType: USER_TYPE,
        chatId: chatId,
        pageFlippingType: PAGE_FLIPPING_TYPE.PULL_DOWN,
        msgId: msgPage.lastMsgId,
        limit: msgPage.limit
    };
    ajaxRequest(url, "get", data, null, function (res) {
        if (res.code !== 200) {
            logger.info("获取聊天记录列表失败,msgPage:", msgPage, "chatId:", chatId, "res:", res);
            myAlert('', res.message, "err");
            return;
        }
        let chatMsgListDom = $("#chat-msg-list");
        let chatMsgList = res.data;
        for (let i = 0; i < chatMsgList.length; i++) {
            let chatMsg = chatMsgList[i];
            let user = getChatMsgUser(chatMsg);
            let chatMsgDom = generateChatMsgDom(user, chatMsg);
            chatMsgListDom.prepend(chatMsgDom);
            setMsgPageLastMsgId(chatMsgList, msgPage);
        }

        //启动上传进度条
        startUploadProgressBar();

        msgContainer.scrollTop = msgContainer.scrollHeight - scrollHeight - 30
        // $(msgContainer).animate({
        //     scrollTop: msgContainer.scrollHeight - scrollHeight - 30
        // }, 450);//450毫秒秒滑动到指定位置
    });
}

function cleanChatMsgUnreadCount() {
    let totalUnreadCountDom = $("#top-chat-unread");
    let totalUnreadCount = parseInt(totalUnreadCountDom.attr("unread-count"));
    if (totalUnreadCount > 0 && unreadChatIdSet.size === 0) {
        totalUnreadCountDom.addClass("hide");
        totalUnreadCount = unreadChatIdSet.size;
    }
    totalUnreadCountDom.attr("unread-count", totalUnreadCount).text(totalUnreadCount);
}

/**
 * 生成信息dom
 * @param user
 * @param chatMsg
 * @returns {string}
 */
function generateChatMsgDom(user, chatMsg) {
    if (chatMsg.msgType === MsgType.SYSTEM || chatMsg.msgType === MsgType.EVENT) {
        return '<li><div class="sysinfo">' + chatMsg.msg + '</div></li>';
    }
    let msgDom = getMsgDom(chatMsg);
    if (msgDom === '') {
        return '';
    }
    let msgObj = msgParseJson(chatMsg);
    return `
            <li class="${user.msgClass}">
                <div class="avatar" user-id="${user.id}" action="msg-list" onclick="getChatUserInfoDom(this)" ><img src="${user.avatar}" alt=""></div>
                <div class="msg">
                    <span class="msg-name">${user.nickname}</span>
                    <div class="msg-text-outer">
                        <span class="msg-arrow"></span>
                        <span class="msg-text" msg-id="${chatMsg.id}" msg-type="${chatMsg.msgType}" file-name="${msgObj.fileName}" new-file-name="${msgObj.newFileName}" file-url="${msgObj.fileUrl}" >${msgDom}</span>
                    </div>
                    <time class="msg-time">${chatMsg.sendTime}</time>
                </div>
            </li>
        `;
}

function getMsgDom(chatMsg) {
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
        msgDom = getCallMsgDom(chatMsg);
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
    let msgDom = `<img class="msg-img" src="${msgObj.fileUrl}" alt="" onclick="chatMsgImgZoomDom(this)" onerror="this.src='../../ico/pic_error.png'">`;
    if (msgObj.status === 0) {
        msgDom = `<img class="msg-img-loading" src="../../ico/loading.gif" alt="">`;
        let progressBarDom = `<div class="progress-bar" msg-id="${chatMsg.id}" file-url="${msgObj.fileUrl}"></div>`;
        msgDom = msgDom + progressBarDom;
        progressBarMsgIdSet.add(chatMsg.id);
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
                               <img class="msg-video-cover" src="${msgObj.coverUrl}" alt="" onerror="this.src='../../ico/pic_error.png'">
                                <i class="msg-video-play-icon" data-url="${msgObj.fileUrl}" onclick="playVideo(this)" ></i>
                         </div>`;
    if (msgObj.status === 0) {
        msgDom = `<div class="msg-video-outer">
                        <img class="msg-img-loading" src="../../ico/loading.gif" alt="">
                        <i class="msg-video-play-icon hide" data-url="${msgObj.fileUrl}" onclick="playVideo(this)" ></i>
                  </div>`;
        let progressBarDom = `<div class="progress-bar" msg-id="${chatMsg.id}" file-url="${msgObj.coverUrl}"></div>`;
        msgDom = msgDom + progressBarDom;
        progressBarMsgIdSet.add(chatMsg.id);
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
    if (msgObj.status === 0) {
        progressBarMsgIdSet.add(chatMsg.id);
        let progressBarDom = `<div class="progress-bar" msg-id="${chatMsg.id}" file-url="${msgObj.fileUrl}"></div>`;
        msgDom = msgDom + progressBarDom;
    }
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
    return `<div class="msg-card-wrap" user-id="${msgObj.userId}" msg-type="${chatMsg.msgType}" action="msg-list" onclick="bizCardClick(this)" >
                    <div class="msg-card-name-wrap">
                        <img class="msg-card-avatar" src="${msgObj.avatar}" alt="头像" />
                        <span class="msg-card-nickname">${msgObj.nickname}</span>
                    </div>
                    <div class="msg-card-lab"> ${cardLeb}</div>
               </div>`;
}


/**
 * 生成语音视频信息dom
 * @param chatMsg
 * @returns {string}
 */
function getCallMsgDom(chatMsg) {
    let msgObj = JSON.parse(chatMsg.msg);
    if (msgObj.callType === CallType.invite[0]) {
        logger.info('收到通话邀请');
        getUserAndOpenAVCall(chatMsg);
        return '';
    }
    let isMy = chatMsg.sendUserId === chatUser.id;
    let bgCls = '';
    if (chatMsg.msgType === MsgType.AUDIO_CALL) {
        bgCls = "call-audio-msg-icon";
    } else {
        bgCls = isMy ? "call-video-msg-icon-right" : "call-video-msg-icon-left"
    }
    let text = getCallTypeDesc(chatMsg.sendUserId, msgObj.callType);
    if (msgObj.callType === CallType.hangup[0] || msgObj.callType === CallType.dropped[0]) {
        text = text + " " + formatCallTime(msgObj.duration);
    }
    let textIcoDom = ``;
    if (isMy) {
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

function contentInputChange(_this) {
    let contentInputDom = $(_this);
    let content = contentInputDom.val();
    if (content === "") {
        contentInputDom.addClass("content-input");
        $("#send-msg-btn").addClass("hide").attr("disabled", true);
        $("#nav-operation").removeClass("hide");
    } else {
        contentInputDom.removeClass("content-input");
        $("#send-msg-btn").removeClass("hide").removeAttr("disabled");
        $("#nav-operation").addClass("hide");
    }
}

/**
 * 当内容输入框获得焦点时移除只读属性
 * 该函数用于处理内容输入框的焦点事件，通过延迟200毫秒来移除输入框的只读属性，
 * 以便用户能够在此输入框中输入内容。
 * @param {Object} _this - 输入框元素对象，通过此参数来操作具体的输入框元素。
 */
function contentInputFocus(_this) {
    // 使用setTimeout延迟执行，避免立即操作导致的潜在问题
    setTimeout(function () {
        // 移除输入框的只读属性，使其可以被编辑
        $(_this).removeAttr("readonly");
    }, 200);
}


/**
 * 当内容输入框失去焦点时的处理函数。
 *
 * 此函数旨在将输入框设置为只读状态，防止后续编辑。通过传递事件对象的this引用来指定目标元素。
 *
 * @param {Object} _this - 失去焦点的输入框元素。使用jQuery包装此元素以便于操作。
 */
function contentInputBlur(_this) {
    $(_this).attr("readonly", true);
}


/**
 * 设置表情包hover效果
 */
function setEmojiHover() {
    $('.emoji-box-bottom img').hover(
        function () {
            if (!$(this).attr("class")) {
                $(this).addClass('emojiHover');
            }
        },
        function () {
            $(this).removeClass('emojiHover');
        }
    );
}

/**
 * 设置表情包
 * @param _this
 * @param typeKey
 */
function selectEmojis(_this, typeKey) {
    let emojiUl = $('#emoji-ul');
    emojiUl.empty();
    emojiMap[typeKey].emojis.forEach(function (emoji) {
        emojiUl.append('<li class="emoji no-select" data-emoji="' + emoji + '" onclick="emojiClick(this)">' + emoji + '</li>');
    });
    if (_this) {
        $(".emojiSelected").removeClass("emojiSelected");
        $(_this).addClass("emojiSelected").removeClass("emojiHover");
    } else {
        $(".emoji-box-bottom img:first").addClass("emojiSelected").removeClass("emojiHover");
    }
}

/**
 * 注册表情包点击事件
 */
function registerNavFaceClick() {
    $("#nav-face").on("click", function (e) {
        let emojiBoxOuter = $(".emoji-box-outer");
        if (emojiBoxOuter.hasClass("hide")) {
            emojiBoxOuter.removeClass("hide");
            selectEmojis(null, 'SmileysEmotion');
            $(".container").addClass("container-more-emoji").removeClass("container-more");
            $(".send-attachment").addClass("hide");
        } else {
            emojiBoxOuter.addClass("hide");
            $(document).off("click");
            $(".container").removeClass("container-more-emoji");
        }

        $(document).on("click", function (event) {
            let $eTarget = $(event.target);
            if ($eTarget.hasClass("emojiSelected") || $eTarget.hasClass("emoji-box-bottom") || $eTarget.hasClass("emoji") || $eTarget.hasClass("emoji-box-outer")
                || $eTarget.hasClass("emoji-ul") || $eTarget.hasClass("emoji-box-inner") || $eTarget.hasClass("emoji-remove")) {
                return;
            }
            emojiBoxOuter.addClass("hide");
            $(".emoji-box-bottom img").removeClass("emojiSelected");
            $(".container").removeClass("container-more-emoji");
            $(document).off("click");
        });
        e.stopPropagation();
    });

    $("#nav-operation").on("click", function (e) {
        let sendAttachment = $(".send-attachment");
        let msgContainer = $(".container");
        if (msgContainer.hasClass("container-more")) {
            msgContainer.removeClass("container-more");
            sendAttachment.addClass("hide");
        } else {
            msgContainer.addClass("container-more").removeClass("container-more-emoji");
            sendAttachment.removeClass("hide");
            $(".emoji-box-outer").addClass("hide");
        }
        $(document).on("click", function (event) {
            let claName = "." + event.target.className;
            logger.info("className:", claName);
            if ("send-attachment" === claName || sendAttachment.find(claName).length > 0) {
                return;
            }
            msgContainer.removeClass("container-more");
            sendAttachment.addClass("hide");
            $(document).off("click");
        });
        e.stopPropagation();
    });
}

/**
 * 当表情图标被点击时，这个函数被调用。
 * 它将被点击的表情图标所代表的文本插入到内容输入框中，并触发内容输入框的变化事件。
 *
 * @param {HTMLElement} _this - 被点击的表情图标元素。
 */
function emojiClick(_this) {
    // 获取内容输入框的jQuery对象
    let contentInput = $("#content-input");
    // 将被点击的表情图标的数据-emoji属性值插入到内容输入框的光标位置
    contentInput.insertAtCaret($(_this).attr("data-emoji"));
    // 触发输入框的失去焦点事件，以更新可能的输入建议或预览
    contentInput.blur();
    // 触发内容输入框变化的处理函数，以处理输入后的逻辑，如更新界面显示等
    contentInputChange(contentInput);
}


function removeInputEmoji() {
    let $contentInput = $("#content-input");
    let content = $contentInput.val();
    // 获取文本的长度
    let length = content.length;
    if (length === 0) {
        return;
    }
    // 获取最后一个字符的emoji长度
    let emojiLengthToDelete = emojiLength(content.charAt(length - 1));
    logger.info("emojiLengthToDelete:", emojiLengthToDelete);
    // 如果最后一个字符是emoji，则删除它
    if (emojiLengthToDelete > 0) {
        $contentInput.val(content.slice(0, -2)); //删除2个字符的emoji,旗帜的emoji大于2个字符(3-4)会有问题
    } else {
        $contentInput.val(content.substring(0, content.length - 1));
    }
}


function emojiLength(emoji) {
    // 用于正确计算包含surrogate对的emoji的长度
    return emoji.replace(/[\uD800-\uDBFF][\uDC00-\uDFFF]/g, '').length;
}

function getChatMsgUser(chatMsg) {
    let user = {
        nickname: chatMsg.nickname,
        avatar: chatMsg.avatar,
        id: chatMsg.sendUserId
    };
    if (chatMsg.sendUserId === chatUser.id) {
        user.msgClass = "self";
    } else {
        user.msgClass = "other";
    }
    return user;
}

function openPicDialog() {
    $('#picFileInput').click();
}

function openVideoDialog() {
    $('#videoFileInput').click();
}

function openCameraDialog() {
    $('#cameraFileInput').click();
}

function openFileDialog() {
    $('#fileInput').click();
}

function openCardDialog() {
    logger.info("打开名片");
}

/**
 * 注册聊天图片上传
 */
function registerChatMsgPictureChange() {
    $('#picFileInput').on('change', function (e) {
        let file = e.target.files[0];
        if (!file) {
            return;
        }
        uploadFileAndSendImg(file);
    });
    $('#videoFileInput').on('change', function (e) {
        let file = e.target.files[0];
        if (!file) {
            return;
        }
        uploadFileAndSendImg(file);
    });
    /*$('#cameraFileInput').on('change', function (e) {
        let file = e.target.files[0];
        if (!file) {
            return;
        }
        uploadFileAndSendImg(file);
    });*/
}


/**
 * 注册聊天文件上传
 */
function registerChatMsgFileChange() {
    $('#fileInput').on('change', function (e) {
        let file = e.target.files[0];
        if (!file) {
            return;
        }
        uploadFileAndSendImg(file);
    });
}

function uploadFileAndSendImg(file) {
    let fileType = file.type;
    if (fileType.startsWith('image/')) {
        logger.info('选中的文件是图片文件,fileType:', fileType);
        uploadAndSendImageFile(file);
    } else if (fileType.startsWith('video/')) {
        logger.info('选中的文件是视频文件,fileType:', fileType);
        uploadAndSendVideoFile(file);
    } else {
        logger.info('选中的文件,fileType:', fileType);
        uploadAndSendFile(file);
    }
}


//------------------------------------------------------------聊天信息详情页---------------------------------------------------------
/**
 * 加载聊天信息页面
 */
function loadMsgInfoPage() {
    $("#container").load("/mobile/chat-group-msginfo.html", function (responseTxt, statusTxt, xhr) {
        if (statusTxt === "success") {
            logger.info("外部HTML[chat-group-msginfo.html]加载成功！");
            getChatRoomMsgInfo();
            initAddSubGroupMemberDialog();
            if (parseInt(chatToUser.groupId) > 0) {
                initGroupQrcodeDialog();
                initGroupNameDialog();
                initAddRemoveGroupManagerDialog();
                initGroupLeaderTransferDialog();
            } else {
                gotoChatMsgInfo();
                $("#group-base-info").addClass("hide");
                $(".logout-group").addClass("hide");

            }
            let container = $("#container");
            if (chatToUser.isDissolve === "true") {
                container.append(`<div class="ui-widget-overlay ui-front dissolve-mask" style="z-index: 100;">群聊已解散</div>`);
            }
            $(".header .nickname").text("聊天信息");
            $(".footer").removeClass("show").addClass("hide");
            $(".header .left-arrow").removeClass("hide").addClass("show").attr("action", "msg-list");
            $("#msg-more").addClass("hide");
            //添加历史记录
            pushHistory({tit: "msg-more-info"});
        } else {
            console.error("外部HTML[chat-group-msginfo.html]加载失败！");
        }
    });
}

function gotoChatMsgInfo() {
    let groupMDom = `
                     <li user-id="${chatToUser.id}" action="msg-more-info" onclick="getChatUserInfoDom(this)">
                        <div class="member-head">
                            <img class="member-avatar" user-id="${chatToUser.id}" src="${chatToUser.avatar}" alt=""/>
                        </div>
                        <div class="member-nickname">${chatToUser.nickname}</div>
                    </li>
                    <li onclick="openCreateGroupDialog()">
                    <div class="member-head">
                        <div class="member-operation">+</div>
                    </div>
                </li>`;
    $(".group-member").append(groupMDom);
}

/**
 * 跳转到聊天信息页面
 */
function getChatRoomMsgInfo() {
    let url = `${MSG_URL_PREFIX}/chat/room/getChatRoomMsgInfo`;
    ajaxRequest(url, "get", {userType: USER_TYPE, chatId: chatToUser.chatId}, null, function (res) {
        if (res.code !== 200) {
            logger.info("获取聊天室详情失败,chatId:", chatToUser.chatId, "res:", res);
            myAlert('', res.message, "err");
            return;
        }
        chatToUser.chatInfo = res.data;
        // 设置免打扰
        if (chatToUser.chatInfo.msgNoDisturb) {
            $("#no-disturb").removeClass("switch-off").addClass("switch-on");
        } else {
            $("#no-disturb").removeClass("switch-on").addClass("switch-off");
        }
        //设置置顶
        if (chatToUser.chatInfo.isTop) {
            $("#is-top").removeClass("switch-off").addClass("switch-on");
        } else {
            $("#is-top").removeClass("switch-on").addClass("switch-off");
        }

        if (chatToUser.chatInfo.chatType === ChatType.GROUP) {
            $("#group-name").text(res.data.groupInfo.name);
            if (res.data.groupInfo.notice) {
                let noticeJson = JSON.parse(res.data.groupInfo.notice);
                let newLab = $("<p>").html(noticeJson.content);
                $("#group-notice").text(newLab.text());
            }
            $("#group-nickname").text(res.data.groupInfo.nickname);
            $(".group-member-num").removeClass("hide").text("(" + res.data.groupInfo.memberCount + ")");
            // 群成员列表
            getGroupMemberList();
            if (!chatToUser.chatInfo.groupInfo.isGroupLeader) {
                $("#group-manage-label").addClass("hide");
            }
        }
    });
}

/**
 * 获取群成员列表
 */
function getGroupMemberList() {
    let groupMemberList = getLocalGroupMemberList();
    if (groupMemberList) {
        generateGroupMemberDom(groupMemberList);
        return;
    }
    setCurrentRemoteGroupMemberListToLocalStore();
    groupMemberList = getLocalGroupMemberList();
    if (groupMemberList) {
        generateGroupMemberDom(groupMemberList);
    }
}

function generateGroupMemberDom(groupMemberList) {
    let groupMemberDom = $(".group-member");
    if (!groupMemberDom) {
        return;
    }
    for (let i = 0; i < groupMemberList.length; i++) {
        let groupMember = groupMemberList[i];
        let className = "top";
        if (i > 18) { // 超过18个隐藏
            className = "more hide";
            $(".group-member-more").removeClass("hide");
        }
        let groupMDom = `
                 <li class="${className}" user-id="${groupMember.userId}" action="msg-more-info" onclick="getChatUserInfoDom(this)">
                    <div class="member-head">
                        <img class="member-avatar" src="${groupMember.avatar}" alt=""/>
                    </div>
                    <div class="member-nickname">${groupMember.nickname}</div>
                </li>
            `;
        groupMemberDom.append(groupMDom);
    }
    let groupMDom = `
            <li onclick="openAddSubGroupMemberDialog('add')">
                <div class="member-head">
                    <div class="member-operation">+</div>
                </div>
            </li>
        `;
    groupMemberDom.append(groupMDom);
    if (chatToUser.chatInfo.groupInfo.isGroupManager || chatToUser.chatInfo.groupInfo.isGroupLeader) {
        groupMDom = `
                <li onclick="openAddSubGroupMemberDialog('remove')">
                <div class="member-head">
                    <div class="member-operation">-</div>
                </div>
            </li>
            `;
        groupMemberDom.append(groupMDom);
    }
}

function bizCardClick(_this) {
    let msgType = $(_this).attr("msg-type");
    if (MsgType.BIZ_CARD === msgType) {
        getChatUserInfoDom(_this);
    } else {
        getGroupCardInfoDom(_this);
    }
}

function getChatUserInfoDom(_this) {
    let userId = $(_this).attr("user-id");
    getChatUserInfo(userId, function (userInfo) {
        let groupNicknameDom = ``;
        // 设置群昵称
        if (parseInt(chatToUser.groupId) > 0 && chatToUser.chatInfo
            && (chatToUser.chatInfo.groupInfo.isGroupLeader || chatToUser.chatInfo.groupInfo.isGroupManager)
            || parseInt(chatToUser.groupId) > 0 && userInfo.userId === chatUser.id) {
            let groupMemberList = getLocalGroupMemberList();
            for (let i = 0; i < groupMemberList.length; i++) {
                let groupMember = groupMemberList[i];
                if (groupMember.userId === userInfo.userId) {
                    if (groupMember.groupNickname && userInfo.remark !== groupMember.groupNickname) {
                        groupNicknameDom = `
                        <div class="more-info">
                                <label>群昵称:</label><span>${groupMember.groupNickname}</span>
                            </div>
                            `;
                    }
                    groupNicknameDom = groupNicknameDom + `
                            <div class="more-info">
                                <label>群来源:</label><span>${groupMember.sourceDesc ? groupMember.sourceDesc : '-'}</span>
                            </div>`;
                    break;
                }
            }
        }
        let sourceDom = ``;
        let bottomDom = ``;
        if (userInfo.isFriend) {
            if (userInfo.userId === chatUser.id) {
                if (parseInt(chatToUser.groupId) > 0) {
                    sourceDom = `<div class="friend-source"> ${groupNicknameDom}</div>`;
                }
            } else {
                sourceDom = `
                <div class="friend-source">
                    ${groupNicknameDom}
                    <div class="more-info"><label>备注:</label><span class="friend-remark"><input type="text" friend-id="${userInfo.userId}" remark="${userInfo.remark}" value="${userInfo.remark ? userInfo.remark : ''}" placeholder="设置备注" onclick="remarkClick(this)" onfocusin="remarkClick(this)" onfocusout="modifyFriendRemark(this)"></span></div>
                    <div class="more-info">
                        <label>好友来源:</label><span>${userInfo.sourceDesc ? userInfo.sourceDesc : '-'}</span>
                    </div>
                </div>`;
                bottomDom = `<span class="send-msg-btn" user-id="${userInfo.userId}" nickname="${userInfo.nickname}" avatar="${userInfo.avatar}" onclick="sendMsgBtnClick(this)">发消息</span>`;
            }
        } else {
            sourceDom = `
                <div class="friend-source">
                    ${groupNicknameDom}
                    <div class="more-info">
                        <label>好友来源:</label><span>-</span>
                    </div>
                </div>`;
            bottomDom = `<span class="send-msg-btn" user-id="${userInfo.userId}" nickname="${userInfo.nickname}" avatar="${userInfo.avatar}" enterprise-name="${userInfo.enterpriseName}" onclick="addFriendApplyClick(this,'${FRIEND_SOURCE.GROUP}')">添加为好友</span>`;
        }

        let infoDom = `
            <div class="friend-apply-info" >
                <div class="friend-head">
                    <img class="friend-avatar" src="${userInfo.avatar}" alt="头像"/>
                     <div class="friend-name-info">
                        <span class="friend-name">${userInfo.remark ? userInfo.remark : userInfo.nickname}</span>
                        <span class="friend-remark">昵称: ${userInfo.nickname}</span>
                        <span class="friend-remark">公司: ${userInfo.enterpriseName}</span>
                    </div>
                </div>
                ${sourceDom}
                <div class="friend-bottom">
                    ${bottomDom}
                </div>
            </div>
        `;

        $("#container").html("").append(infoDom);
        $(".footer").removeClass("show").addClass("hide");
        $(".header .left-arrow").removeClass("hide").addClass("show").attr("action", $(_this).attr("action"));
        $(".header .nickname").text("好友");
        $("#add-operation").addClass("hide").removeClass("show");
    });
}


function getGroupCardInfoDom(_this) {
    let chatId = $(_this).attr("user-id");
    let isMyChat = isMyChatRoom(chatId);
    let bottomDom = ``;
    if (isMyChat) {
        bottomDom = `<span class="send-msg-btn" onclick="gotoGroupMsg('${chatId}')">发消息</span>`;
    } else {
        bottomDom = `<span class="send-msg-btn" onclick="applyGroup('${chatId}')">申请入群</span>`;
    }
    let infoDom = `
            <div class="friend-apply-info" >
                <div class="friend-head">
                    <img class="friend-avatar" src="${$(_this).find('.msg-card-avatar').attr('src')}" alt="头像"/>
                     <div class="friend-name-info">
                        <span class="friend-name">${$(_this).find('.msg-card-nickname').text()}</span>
                        <span class="friend-remark">群ID: ${chatId}</span>
                    </div>
                </div>
                <div class="friend-bottom">
                    ${bottomDom}
                </div>
            </div>
        `;

    $("#container").html("").append(infoDom);
    $(".footer").removeClass("show").addClass("hide");
    $(".header .left-arrow").removeClass("hide").addClass("show").attr("action", $(_this).attr("action"));
    $(".header .nickname").text("群聊");
    $("#add-operation").addClass("hide").removeClass("show");
}

function gotoGroupMsg(chatId) {
    setCookieDay(PREV_CHAT_ID_KEY, chatId, 1);
    loadChatRoom();
}


function applyGroup(chatId) {
    myConfirm("申请入群", "确认要申请进入该群聊吗?", function () {
        let groupId = chatId.replace("G_", "");
        let url = `${MSG_URL_PREFIX}/chat/group/applyJoinGroup`;
        ajaxRequest(url, "post", {
            userType: USER_TYPE,
            groupId: groupId,
            source: GROUP_SOURCE.CARD
        }, null, function (res) {
            if (res.code !== 200) {
                logger.info("申请入群失败,chatId:", chatId);
                myAlert('', res.message, "err");
                return;
            }
            logger.info("申请成功,chatId:", chatId);
        });
    });
}

function msgInfoSwitch(_this) {
    let flag = false;
    let className = $(_this).attr("class");
    if (className === "switch-on") {
        $(_this).removeClass("switch-on").addClass("switch-off");
        flag = false;
    } else {
        $(_this).removeClass("switch-off").addClass("switch-on");
        flag = true;
    }

    let url;
    let id = _this.id;
    if (id === "no-disturb") {
        url = `${MSG_URL_PREFIX}/chat/room/updateMsgNoDisturb`;
    } else if (id === "is-top") {
        url = `${MSG_URL_PREFIX}/chat/room/updateIsTop`;
    } else if (id === "invite-cfm") {
        url = `${MSG_URL_PREFIX}/chat/group/updateInviteCfm`;
    }
    ajaxRequest(url, "post", {
        userType: USER_TYPE,
        chatId: chatToUser.chatId,
        groupId: chatToUser.groupId,
        flag: flag
    }, null, function (res) {
        if (res.code !== 200) {
            logger.info("修改标志失败,chatId:", chatToUser.chatId, "res:", res);
            myAlert('', res.message, "err");
            return;
        }
        if (id === "no-disturb") {
            chatToUser.chatInfo.msgNoDisturb = flag;
        } else if (id === "is-top") {
            chatToUser.chatInfo.isTop = flag;
        } else if (id === "invite-cfm") {
            chatToUser.chatInfo.groupInfo.inviteCfm = flag;
        }
    });
}

function moreGroupMember(_this) {
    if ($(_this).find(".info-title").text() === "展开更多") {
        $(_this).find(".info-title").text("收起");
        $(_this).find(".arrow-down").removeClass("arrow-down").addClass("arrow-up");
        $(".group-member li.more").removeClass("hide");
    } else {
        $(_this).find(".info-title").text("展开更多");
        $(_this).find(".arrow-up").removeClass("arrow-up").addClass("arrow-down");
        $(".group-member li.more").addClass("hide");
    }
}

/**
 * 群二维码弹框打开
 */
function groupQrcodeDialogOpen() {
    let url = `${MSG_URL_PREFIX}/chat/group/getGroupQrcode`;
    ajaxRequest(url, "get", {userType: USER_TYPE, groupId: chatToUser.groupId}, null, function (res) {
        if (res.code !== 200) {
            logger.info("获取群二维码失败,groupId:", chatToUser.groupId, "res:", res);
            myAlert('', res.message, "err");
            return;
        }
        $("#group-qrcode-img").attr("src", res.data);
        $("#group-qrcode-dialog").dialog("open");
    });
}

/**
 * 初始化群二维码弹框
 */
function initGroupQrcodeDialog() {
    $("#group-qrcode-dialog").dialog({
        title: "群二维码",
        autoOpen: false,   // 是否自动弹出窗口
        modal: true,    // 设置为模态对话框
        resizable: false,
        width: 320,   //弹出框宽度
        height: 405,   //弹出框高度
        close: function (event, ui) {

        }
    });
}

/**
 * 群名称修改弹框打开
 */
function groupNameDialogOpen(type) {
    if (type === "groupName") {
        $("#modify-group-name-input").val(chatToUser.chatInfo.groupInfo.name);
        $("#modify-group-avatar").attr("src", chatToUser.avatar);
        $(".modify-group-name-btn .primarybtn").attr("modify-type", "groupName");
        $(".modify-group-desc").text("修改群名称后，将在群内通知其他成员。");
        $("#modify-group-name-dialog").dialog({title: "修改群名称"}).dialog("open");
    } else if (type === "groupNickname") {
        $("#modify-group-name-input").val(chatToUser.chatInfo.groupInfo.nickname);
        $("#modify-group-avatar").attr("src", chatUser.avatar);
        $(".modify-group-name-btn .primarybtn").attr("modify-type", "groupNickname");
        $(".modify-group-desc").text("昵称修改后，只会在此群内显示，群内成员都可见。");
        $("#modify-group-name-dialog").dialog({title: "修改我在群里的昵称"}).dialog("open");
    }
}

/**
 * 初始化修改群名称弹框
 */
function initGroupNameDialog() {
    $("#modify-group-name-dialog").dialog({
        autoOpen: false,   // 是否自动弹出窗口
        modal: true,    // 设置为模态对话框
        resizable: false,
        width: 320,   //弹出框宽度
        height: 235,   //弹出框高度
        close: function (event, ui) {
            $("#modify-group-name-input").val("");
            $(".modify-group-name-btn .primarybtn").attr("disabled", true);
        }
    });
}

function groupNameInputChange(_this) {
    let ngroupName = $(_this).val();
    let btnDom = $(".modify-group-name-btn .primarybtn");
    if (ngroupName.length === 0) {
        btnDom.attr("disabled", true);
        return;
    }
    let modifyType = btnDom.attr("modify-type");
    let oldName = "";
    if (modifyType === "groupName") {
        oldName = chatToUser.chatInfo.groupInfo.name;
    } else if (modifyType === "groupNickname") {
        oldName = chatToUser.chatInfo.groupInfo.nickname;
    }
    if (ngroupName === oldName) {
        btnDom.attr("disabled", true);
        return;
    }
    btnDom.removeAttr("disabled");
}

function clearGroupNameInput() {
    $("#modify-group-name-input").val("");
    $(".modify-group-name-btn .primarybtn").attr("disabled", true);
}

function modifyGroupName(_this) {
    let modifyType = $(_this).attr("modify-type");
    let name = $("#modify-group-name-input").val();
    let url = "";
    let msg = "";
    if (modifyType === "groupName") {
        msg = "请输入新的群名称";
        url = `${MSG_URL_PREFIX}/chat/group/updateGroupName`;
    } else if (modifyType === "groupNickname") {
        msg = "请输入新的群昵称";
        url = `${MSG_URL_PREFIX}/chat/group/updateGroupNickname`;
    }
    if (name.length === 0 || name === chatToUser.chatInfo.groupInfo.nickname) {
        myAlert('', msg, "err");
        return;
    }
    ajaxRequest(url, "post", {
        userType: USER_TYPE,
        groupId: chatToUser.groupId,
        name: name
    }, null, function (res) {
        if (res.code !== 200) {
            logger.info("修改名称失败,groupId:", chatToUser.groupId, "res:", res);
            myAlert('', res.message, "err");
            return;
        }
        $("#modify-group-name-dialog").dialog("close");
        localStorage.removeItem(GROUP_MEMBER_PREFIX + chatToUser.groupId);
        loadMsgInfoPage();
    });
}

function loadMsgGroupNoticeTextbox() {
    $("#container").load("/mobile/textboxio.html", function (responseTxt, statusTxt, xhr) {
        if (statusTxt === "success") {
            logger.info("外部HTML[textboxio.html]加载成功！");
            let noticeContent = '';
            if (chatToUser.chatInfo.groupInfo.notice) {
                let noticeJson = JSON.parse(chatToUser.chatInfo.groupInfo.notice);
                noticeContent = noticeJson.content;
                let userId = noticeJson.userId;
                let groupMemberList = getLocalGroupMemberList();
                for (let i = 0; i < groupMemberList.length; i++) {
                    if (groupMemberList[i].userId === userId) {
                        $(".group-notice-avatar").attr("src", groupMemberList[i].avatar);
                        $(".group-notice-nickname-text").text(groupMemberList[i].nickname);
                        break;
                    }
                }
                $(".group-notice-nickname-time").text(noticeJson.noticeTime);
            } else {
                $(".group-notice-user").addClass("hide");
            }
            instantiateTextbox(noticeContent);

            $(".header .nickname").text("群公告");
            $(".footer").removeClass("show").addClass("hide");
            $(".header .left-arrow").removeClass("hide").addClass("show").attr("action", "msg-more-info");
            $("#add-operation").addClass("hide").removeClass("show");
        } else {
            console.error("外部HTML[textboxio.html]加载失败！");
        }
    });
}


/**
 * 富文本编辑器对象
 */
let noticeEditor;

/**
 * 初始化富文本编辑器
 */
function instantiateTextbox(noticeContent) {
    textboxio.replace('#textbox-notice', {
        paste: {
            style: 'clean'
        },
        css: {
            stylesheets: ['../textboxio/example.css']
        },
        images: {
            upload: {
                url: `${SYS_URL_PREFIX}/upload/img`,
                basePath: 'https://oss.pinmallzj.com/'
            }
        }
    });
    let editors = textboxio.get('#textbox-notice');
    noticeEditor = editors[0];
    noticeEditor.content.set(noticeContent);
    noticeEditor.events.loaded.addListener(function () {
        // 禁止编辑
        $($("#ephox_textbox-notice iframe")[0].contentWindow.document).find("body").attr("contenteditable", "false");
        //隐藏编辑按钮
        if (!chatToUser.chatInfo.groupInfo.isGroupLeader && !chatToUser.chatInfo.groupInfo.isGroupManager) {
            $("#ephox_textbox-notice .ephox-polish-disabled-mask").addClass("hide");
            $(".group-notice-btn .primarybtn").addClass("hide");
        }
    });
    noticeEditor.events.dirty.addListener(function () {
        logger.info('Editor content now dirty.');
        bindTextboxBackClickEvent(noticeContent);
    });
    noticeEditor.events.focus.addListener(function () {
        logger.info('this editor has focus');
        if (noticeEditor.content.isDirty()) {
            bindTextboxBackClickEvent(noticeContent);
        }
    });
}

let textboxBlankContent = "<i>空</i>";

function bindTextboxBackClickEvent(noticeContent) {
    $(".ephox-chameleon-toolbar-group .ephox-pastry-button[aria-label='返回']").unbind("click").bind("click", function () {
        logger.info("点击了返回按钮");
        let blankContent = "<p>\n  <br>\n</p>";
        let content = getNoticeContent();
        if (blankContent === content) {
            logger.info("内容为空");
            content = "";
            noticeEditor.content.set("<i>空</i>");
        }
        if (content === noticeContent) {
            logger.info("内容没有变化");
            noticeEditor.content.setDirty(false);
        } else {
            noticeEditor.content.setDirty(true);
        }
        if (noticeEditor.content.isDirty()) {
            $(".group-notice-btn .primarybtn").removeAttr("disabled");
        }
    });
}

function getNoticeContent() {
    return noticeEditor.content.get();
}

function saveGroupNotice() {
    let noticeContent = getNoticeContent();
    if (textboxBlankContent === noticeContent) {
        logger.info("内容为空");
        noticeContent = "";
        myConfirm("清空群公告?", "确定要清空群公告吗?", function () {
            saveGroupNoticeContent(noticeContent);
        });
    } else {
        myConfirm("发布群公告?", "该公告会通知全部群成员,确认发布吗?", function () {
            saveGroupNoticeContent(noticeContent);
        });
    }
}

function saveGroupNoticeContent(noticeContent) {
    logger.info("群公告内容", noticeContent);
    let url = `${MSG_URL_PREFIX}/chat/group/updateGroupNotice`;
    ajaxRequest(url, "post", {
        userType: USER_TYPE,
        groupId: chatToUser.groupId,
        notice: noticeContent
    }, null, function (res) {
        if (res.code !== 200) {
            logger.info("更新公告失败,groupId:", chatToUser.groupId, "res:", res);
            myAlert('', res.message, "err");
            return;
        }
        loadMsgInfoPage();
    });
}

/**
 * 添加或异常群成员按钮点击事件
 */
function openAddSubGroupMemberDialog(type) {
    $(".user-search .primarybtn").attr("oper-type", type);
    $(".search-user-list").focus();
    if (type === "add") {
        $(".add-remove-group-member .create-group-btn").text("完成");
        $("#add-remove-group-member-dialog").dialog({title: "添加群成员"}).dialog("open");
    } else if (type === "remove") {
        $(".add-remove-group-member .create-group-btn").text("删除");
        $("#add-remove-group-member-dialog").dialog({title: "移除群成员"}).dialog("open");
    } else if (type === "transfer") {
        $(".add-remove-group-member .create-group-btn").addClass("hide");
        $("#add-remove-group-member-dialog").dialog({title: "选择新群主"}).dialog("open");
    }
    searchUserClick();
    $("#search-u-input").on('keydown', function (event) {
        //监听特定的按键
        if (event.keyCode === 13) {
            searchUserClick();
        }
        //防止按键的默认行为
        if (event.keyCode === 32) {
            event.preventDefault();
        }
    });
}

/**
 * 初始化添加或移除群成员对话框
 */
function initAddSubGroupMemberDialog() {
    $("#add-remove-group-member-dialog").dialog({
        title: "添加群成员",
        autoOpen: false,   // 是否自动弹出窗口
        modal: true,    // 设置为模态对话框
        resizable: false,
        width: 300,   //弹出框宽度
        height: 440,   //弹出框高度
        close: function (event, ui) {
            $("#search-u-input").val("");
            $("#groupmember-list").html("");
        }
    });
}


/**
 * 搜索好友按钮点击事件
 */
function searchUserClick() {
    selectedFriendIdSet.clear();
    let keyword = $("#search-u-input").val();
    let type = $(".user-search .primarybtn").attr("oper-type");
    if (type === "add") {
        getUserFriendSearchList(keyword);
    } else if (type === "remove") {
        getGroupMemberSearchList(keyword);
    } else if (type === "transfer") {
        getGroupLeaderTranGroupMemberList(keyword);
    }
}

function getGroupMemberSearchList(keyword) {
    if (keyword === "") {
        let groupMembers = getLocalGroupMemberList();
        generateSearchUserListDom(groupMembers, "remove");
        return;
    }
    let url = `${MSG_URL_PREFIX}/chat/group/member/list`;
    ajaxSyncRequest(url, "get", {
        userType: USER_TYPE,
        groupId: chatToUser.groupId,
        keywords: keyword
    }, null, function (res) {
        if (res.code !== 200) {
            logger.info("获取群成员失败,groupId:", chatToUser.groupId, "res:", res);
            myAlert('', res.message, "err");
            return;
        }
        generateSearchUserListDom(res.data, "remove");
    });
}

function getUserFriendSearchList(keyword) {
    userFriendListSearch(keyword, chatToUser.groupId, (data) => generateSearchUserListDom(data, "add"));
}

function generateSearchUserListDom(searchUserList, type) {
    let groupmemberListDom = $("#groupmember-list");
    groupmemberListDom.html("");
    for (let i = 0; i < searchUserList.length; i++) {
        let userId;
        let serUser = searchUserList[i];
        if (type === "add") {
            userId = serUser.friendId;
        } else if (type === "remove") {
            userId = serUser.userId;
        }
        let userDom = `
                <li class="search-row">
                    <div class="el-image search-avatar">
                        <img src="${serUser.avatar}" class="el-image-inner" alt="头像"/>
                    </div>
                    <div class="search-center">
                        <p class="search-nick">${serUser.nickname}</p> 
                    </div>
                    <span class="checkbox-group" user-id="${userId}" nickname="${serUser.nickname}" avatar="${serUser.avatar}"  onclick="checkboxChange(this)"></span>
                </li>
        `;
        groupmemberListDom.append(userDom).focus();
    }
}

function addRemoveGroupMember(_this) {
    let type = $(".user-search .primarybtn").attr("oper-type");
    if (type === "add") {
        addGroupMember();
    } else if (type === "remove") {
        removeGroupMember();
    }
}

function addGroupMember() {
    if (selectedFriendIdSet.size === 0) {
        myAlert('', "请选择要添加的群成员", "err");
        return;
    }
    let selectedFriendIdList = Array.from(selectedFriendIdSet);
    let url = `${MSG_URL_PREFIX}/chat/group/addGroupMember`;
    ajaxRequest(url, "post", {
        userType: USER_TYPE,
        groupId: chatToUser.groupId,
        userIds: selectedFriendIdList.join(','),
        source: GROUP_SOURCE.INVITE
    }, null, function (res) {
        if (res.code !== 200) {
            logger.info("添加群成员失败,selectedFriendIdSet:", selectedFriendIdSet);
            myAlert('', res.message, "err");
            return;
        }
        logger.info("添加群成员成功,groupId:", chatToUser.groupId);
        localStorage.removeItem(GROUP_MEMBER_PREFIX + chatToUser.groupId);
        // 关闭对话框
        $("#add-remove-group-member-dialog").dialog("close");
        //跳转到聊天信息界面
        loadMsgInfoPage();
    });
}

function removeGroupMember() {
    if (selectedFriendIdSet.size === 0) {
        myAlert('', "请选择要移除的群成员", "err");
        return;
    }
    let selectedFriendIdList = Array.from(selectedFriendIdSet);
    let url = `${MSG_URL_PREFIX}/chat/group/removeGroupMember`;
    ajaxRequest(url, "post", {
        userType: USER_TYPE,
        groupId: chatToUser.groupId,
        userIds: selectedFriendIdList.join(',')
    }, null, function (res) {
        if (res.code !== 200) {
            logger.info("移除群成员失败,selectedFriendIdSet:", selectedFriendIdSet);
            myAlert('', res.message, "err");
            return;
        }
        logger.info("移除群成员成功,groupId:", chatToUser.groupId);
        localStorage.removeItem(GROUP_MEMBER_PREFIX + chatToUser.groupId);
        // 关闭对话框
        $("#add-remove-group-member-dialog").dialog("close");
        //跳转到聊天信息界面
        loadMsgInfoPage();
    });
}

function goGroupManagePage() {
    let inviteSwitch = "switch-off";
    //设置群聊邀请确认标志
    if (chatToUser.chatInfo.groupInfo.inviteCfm) {
        inviteSwitch = "switch-on";
    }
    let managersDom = generateGroupManagerDom();
    let groupManageDom = `
            <div class="chat-msg-info-set">
                <div class="group-info">
                    <label class="info-title">群聊邀请确认
                    <br/><span class="group-invite-desc">启用后，群成员需群主或群管理员确认才能邀请朋友进群。</span></label>
                    <div class="base-info-right">
                        <span id="invite-cfm" class="${inviteSwitch}" onclick="msgInfoSwitch(this)"></span>
                    </div>
                </div>
            </div>
            <div class="chat-msg-info-set">
                <div class="group-info" onclick="openAddSubGroupMemberDialog('transfer')">
                    <label class="info-title">群主管理权限转让</label>
                    <div class="base-info-right">
                        <span class="right-arrow"></span>
                    </div>
                </div>
                <div class="group-info" onclick="groupManagerDialogOpen()">
                    <label class="info-title">群管理员</label>
                    <div class="base-info-right">
                        <div class="group-manager-low">
                            <ul class="group-managers">
                            ${managersDom}
                            </ul>
                        </div>
                        <span class="right-arrow"></span>
                    </div>
                </div>
            </div>
            <div class="chat-msg-info-set margin-bottom50" onclick="dissolveGroup()">
                <span class="logout-group">解散该群聊</span>
            </div>
        `;

    $("#container").html("").append(groupManageDom);
    $(".footer").removeClass("show").addClass("hide");
    $(".header .left-arrow").removeClass("hide").addClass("show").attr("action", "msg-more-info");
    $(".group-member-num").addClass("hide").text("");
    $(".header .nickname").text("群管理");
    $("#add-operation").addClass("hide").removeClass("show");
}

function generateGroupManagerDom() {
    let managersDom = ``;
    let managerIds = chatToUser.chatInfo.groupInfo.managers;
    if (managerIds) {
        let groupMemberList = getLocalGroupMemberList();
        let managerIdList = managerIds.split(",");
        for (let i = 0; i < managerIdList.length; i++) {
            let managerId = parseInt(managerIdList[i]);
            for (let j = 0; j < groupMemberList.length; j++) {
                let groupMember = groupMemberList[j];
                if (managerId === groupMember.userId) {
                    managersDom = managersDom + `
                     <li class="top" user-id="${groupMember.userId}">
                        <img class="manager-avatar" user-id="${groupMember.userId}" src="${groupMember.avatar}" alt=""/>
                        <div class="manager-nickname" user-id="${groupMember.userId}" >${groupMember.nickname}</div>
                        <span class="manager-remove" style="display: none;" user-id="${groupMember.userId}" nickname="${groupMember.nickname}" >移除</span>
                    </li>
                    `;
                }
            }
        }
    }
    return managersDom;
}

/**
 * 退出群聊
 */
function logoutGroup() {
    myConfirm('确定退出群聊?', "退出群聊后将会清空聊天记录,确定退出吗", function () {
        let url = `${MSG_URL_PREFIX}/chat/group/logoutGroup`;
        ajaxRequest(url, "post", {
            userType: USER_TYPE,
            groupId: chatToUser.groupId
        }, null, function (res) {
            if (res.code !== 200) {
                logger.info("退出群聊失败,groupId:", chatToUser.groupId);
                myAlert('', res.message, "err");
                return;
            }
            //跳转到聊天信息界面
            $(".header .left-arrow").attr("action", "msg");
            goBack();
        });
    });
}

/**
 * 初始化群主转让对话框
 */
function initGroupLeaderTransferDialog() {
    $("#add-remove-group-member-dialog").dialog({
        title: "选择新群主",
        autoOpen: false,   // 是否自动弹出窗口
        modal: true,    // 设置为模态对话框
        resizable: false,
        width: 300,   //弹出框宽度
        height: 450,   //弹出框高度
        close: function (event, ui) {
            $("#search-u-input").val("");
            $("#groupmember-list").html("");
            $(".add-remove-group-member .create-group-btn").removeClass("hide");
        }
    });
}


function getGroupLeaderTranGroupMemberList(keyword) {
    if (keyword === "") {
        let groupMembers = getLocalGroupMemberList();
        generateSearchMemberListDom(groupMembers);
        return;
    }
    let url = `${MSG_URL_PREFIX}/chat/group/member/list`;
    ajaxSyncRequest(url, "get", {
        userType: USER_TYPE,
        groupId: chatToUser.groupId,
        keywords: keyword
    }, null, function (res) {
        if (res.code !== 200) {
            logger.info("获取群成员失败,groupId:", chatToUser.groupId, "res:", res);
            myAlert('', res.message, "err");
            return;
        }
        generateSearchMemberListDom(res.data);
    });
}

function generateSearchMemberListDom(searchUserList) {
    let groupmemberListDom = $("#groupmember-list");
    groupmemberListDom.html("");
    for (let i = 0; i < searchUserList.length; i++) {
        let serUser = searchUserList[i];
        let userId = serUser.userId;
        if (userId === chatUser.id) {
            continue;
        }
        let userDom = `
                <li class="search-row" user-id="${userId}" nickname="${serUser.nickname}" onclick="groupLeaderTransfer(this)">
                    <div class="el-image search-avatar">
                        <img src="${serUser.avatar}" class="el-image-inner" alt="头像"/>
                    </div>
                    <div class="search-center">
                        <p class="search-nick">${serUser.nickname}</p> 
                    </div>
                </li>
        `;
        groupmemberListDom.append(userDom).focus();
    }
}


/**
 * 退出群聊
 */
function groupLeaderTransfer(_this) {
    let userId = $(_this).attr("user-id");
    if (userId === chatUser.id) {
        myAlert('', "不能转让给自己", "err");
        return;
    }
    let nickname = $(_this).attr("nickname");
    myConfirm('确定转让群主?', "确定选择 " + nickname + " 为新群主，你将自动放弃群主身份。", function () {
        let url = `${MSG_URL_PREFIX}/chat/group/groupLeaderTransfer`;
        ajaxRequest(url, "post", {
            userType: USER_TYPE,
            groupId: chatToUser.groupId,
            userId: userId
        }, null, function (res) {
            if (res.code !== 200) {
                logger.info("转让群主失败,groupId:", chatToUser.groupId, "userId:", userId);
                myAlert('', res.message, "err");
                return;
            }
            logger.info("转让群主成功,groupId:", chatToUser.groupId, "userId:", userId);
            $("#add-remove-group-member-dialog").dialog("close");
            //跳转到聊天信息界面
            goBack();
        });
    });
}

/**
 * 初始化添加或移除群成员对话框
 */
function initAddRemoveGroupManagerDialog() {
    $("#add-remove-group-manager-dialog").dialog({
        title: "添加/移除群管理员",
        autoOpen: false,   // 是否自动弹出窗口
        modal: true,    // 设置为模态对话框
        resizable: false,
        width: 300,   //弹出框宽度
        height: 570,   //弹出框高度
        close: function (event, ui) {
            $("#search-group-input").val("");
            $("#group-members").html("");
            goGroupManagePage();
        }
    });
}

function groupManagerDialogOpen() {
    searchGroupMemberClick();
    let managersDom = generateGroupManagerDom();
    if (managersDom === "") {
        $("#group-managers").addClass("hide");
    } else {
        $("#group-managers").html("").append(managersDom).focus();
    }
    $("#add-remove-group-manager-dialog").dialog("open");

    registerGroupManagerTouchEvent();
}

/**
 * 搜索群成员点击事件
 */
function searchGroupMemberClick() {
    let keyword = $("#search-group-input").val();
    if (keyword === "") {
        generateSearchGroupMemberListDom(getLocalGroupMemberList());
        return;
    }
    getSearchGroupMemberList(keyword);
}

function getSearchGroupMemberList(keyword) {
    let url = `${MSG_URL_PREFIX}/chat/group/member/list`;
    ajaxSyncRequest(url, "get", {
        userType: USER_TYPE,
        groupId: chatToUser.groupId,
        keywords: keyword
    }, null, function (res) {
        if (res.code !== 200) {
            logger.info("获取群成员失败,groupId:", chatToUser.groupId, "res:", res);
            myAlert('', res.message, "err");
            return;
        }
        generateSearchGroupMemberListDom(res.data);
    });
}


function generateSearchGroupMemberListDom(searchUserList) {
    let groupmemberListDom = $("#group-members");
    groupmemberListDom.html("");
    for (let i = 0; i < searchUserList.length; i++) {
        let serUser = searchUserList[i];
        if (chatUser.id === serUser.userId || isManager(serUser.userId)) {
            continue;
        }
        let userDom = `
                <li class="search-row">
                    <div class="el-image search-avatar">
                        <img src="${serUser.avatar}" class="el-image-inner" alt="头像"/>
                    </div>
                    <div class="search-center">
                        <p class="search-nick">${serUser.nickname}</p> 
                    </div>
                    <span class="button-group-add" user-id="${serUser.userId}" nickname="${serUser.nickname}" avatar="${serUser.avatar}"  onclick="addGroupManager(this)">添加</span>
                </li>
        `;
        groupmemberListDom.append(userDom);
    }
}

function isManager(userId) {
    let managerIds = chatToUser.chatInfo.groupInfo.managers;
    if (!managerIds) {
        return false;
    }
    let managerIdList = managerIds.split(",");
    for (let j = 0; j < managerIdList.length; j++) {
        let managerId = parseInt(managerIdList[j]);
        if (userId === managerId) {
            return true;
        }
    }
    return false;
}

function registerGroupManagerTouchEvent() {
    let timeOutEvent = 0;
    let touchDom;
    $("#group-managers li").on({
        touchstart: function (e) {
            //阻止默认事件的默认操作
            e.preventDefault();
            touchDom = $(e.target);
            //长按触发事件
            timeOutEvent = setTimeout(function () {
                timeOutEvent = 0;
                logger.info("你长按了", $(e.target).attr("class"), $(e.target).attr("user-id"));
                let liDom;
                let classname = $(e.target).attr("class");
                if (classname === "top") {
                    liDom = touchDom.find(".manager-remove");
                } else {
                    liDom = touchDom.parent().find(".manager-remove");
                }
                //显示
                liDom.slideDown(500);
                setTimeout(function () {
                    //隐藏
                    liDom.slideUp(500);
                }, 2500);
            }, 700)
        },
        touchend: function () {
            clearTimeout(timeOutEvent);
            if (timeOutEvent !== 0) {
                logger.info("你点击了", touchDom.attr("class"), touchDom.attr("user-id"));
                if (touchDom.attr("class") === "manager-remove") {
                    removeGroupManager(touchDom);
                }
            }
            return false;
        }
    });
}

function removeGroupManager(_this) {
    let nickname = $(_this).attr("nickname");
    let userId = $(_this).attr("user-id");
    myConfirm('确定移除群管理员吗?', "确定要移除“" + nickname + "”管理员吗?", function () {
        let url = `${MSG_URL_PREFIX}/chat/group/removeGroupManager`;
        ajaxRequest(url, "post", {
            userType: USER_TYPE,
            groupId: chatToUser.groupId,
            userId: userId
        }, null, function (res) {
            if (res.code !== 200) {
                logger.info("移除群管理员失败,groupId:", chatToUser.groupId);
                myAlert('', res.message, "err");
                return;
            }
            $(_this).parent().remove();
            setCurrentRemoteGroupMemberListToLocalStore();
            setGroupManagers(userId);
            searchGroupMemberClick();
        });
    });
}

function setGroupManagers(userId) {
    let newManagers = [];
    let managerIds = chatToUser.chatInfo.groupInfo.managers.split(",");
    for (let i = 0; i < managerIds.length; i++) {
        if (managerIds[i] === userId) {
            continue
        }
        newManagers.push(managerIds[i]);
    }
    if (newManagers.length === 0) {
        $("#group-managers").addClass("hide");
        chatToUser.chatInfo.groupInfo.managers = "";
    } else {
        chatToUser.chatInfo.groupInfo.managers = newManagers.join(",");
    }
}

function addGroupManager(_this) {
    if (chatToUser.chatInfo.groupInfo.managers) {
        let managerCount = chatToUser.chatInfo.groupInfo.managers.split(",").length;
        if (managerCount >= 3) {
            myAlert('', "群管理员最多3人", "err");
            return;
        }
    }
    let nickname = $(_this).attr("nickname");
    let userId = $(_this).attr("user-id");
    myConfirm('确定添加群管理员吗?', "确定要添加“" + nickname + "”为管理员吗?", function () {
        let url = `${MSG_URL_PREFIX}/chat/group/addGroupManager`;
        ajaxRequest(url, "post", {
            userType: USER_TYPE,
            groupId: chatToUser.groupId,
            userId: userId
        }, null, function (res) {
            if (res.code !== 200) {
                logger.info("添加群管理员失败,groupId:", chatToUser.groupId);
                myAlert('', res.message, "err");
                return;
            }
            if (chatToUser.chatInfo.groupInfo.managers) {
                chatToUser.chatInfo.groupInfo.managers += "," + userId;
            } else {
                chatToUser.chatInfo.groupInfo.managers = userId;
            }
            setCurrentRemoteGroupMemberListToLocalStore();
            let managersDom = generateGroupManagerDom();
            $("#group-managers").html("").append(managersDom).removeClass("hide").focus();
            registerGroupManagerTouchEvent();
            $(_this).parent().remove();
        });
    });
}

function dissolveGroup() {
    myConfirm('解散群聊', "解散群聊后，群成员和群主都将被移出群聊，确认解散吗？", function () {
        let url = `${MSG_URL_PREFIX}/chat/group/dissolveGroup`;
        ajaxRequest(url, "post", {
            userType: USER_TYPE,
            groupId: chatToUser.groupId
        }, null, function (res) {
            if (res.code !== 200) {
                logger.info("解散群聊失败,groupId:", chatToUser.groupId);
                myAlert('', res.message, "err");
                return;
            }
            //跳转到聊天信息界面
            $(".header .left-arrow").attr("action", "msg");
            goBack();
        });
    });
}

function cleanMsgList() {
    let nickname = "和" + chatToUser.nickname;
    if (parseInt(chatToUser.groupId) > 0) {
        nickname = "群"
    }
    myConfirm('清空聊天记录', "确定删除" + nickname + "的聊天记录吗?", function () {
        let url = `${MSG_URL_PREFIX}/chat/room/cleanMsgList`;
        ajaxRequest(url, "post", {
            userType: USER_TYPE,
            chatId: chatToUser.chatId,
        }, null, function (res) {
            if (res.code !== 200) {
                logger.info("清空聊天记录失败,groupId:", chatToUser.groupId);
                myAlert('', res.message, "err");
                return;
            }
            logger.info("清空聊天记录成功,chatId:", chatToUser.chatId);
        });
    });
}

/**
 * 加载聊天室
 */
function loadChatMsgHistory() {
    $("#container").load("/mobile/chat-msg-history.html", function (responseTxt, statusTxt, xhr) {
        if (statusTxt === "success") {
            logger.info("外部HTML[chat-msg-history.html]加载成功！");
            if (parseInt(chatToUser.groupId) > 0) {
                $(".header .nickname").text("“" + chatToUser.nickname + "”的聊天记录");
                initGroupMemberSelectOption();
            } else {
                $(".header .nickname").text("与“" + chatToUser.nickname + "”的聊天记录");
                $("#msg-group-member-select").parent().remove();
            }
            //初始化日期选择器
            initDatepicker();
            searchMsgTypeClick($(".active"));
            registerChatMsgListHisContextMenu();
            $(".footer").removeClass("show").addClass("hide");
            $(".header .left-arrow").removeClass("hide").addClass("show").attr("action", "msg-more-info");
            $("#add-operation").addClass("hide").removeClass("show");
            //添加历史记录
            pushHistory({tit: "msg-history"});
        } else {
            console.error("外部HTML[chat-msg-history.html]加载失败！");
        }
    });
}

function initDatepicker() {
    $("#msg-datepicker").datepicker({
        autoSize: true,
        changeMonth: true,
        changeYear: true,
        onSelect: function (dateText, inst) {
            $(".msg-datepicker-label").text(dateText);
            //搜索消息记录
            searchMsgTypeClick($("#msg-datepicker").parent());
        }
    });
}

function initGroupMemberSelectOption() {
    $.widget("custom.iconselectmenu", $.ui.selectmenu, {
        _renderItem: function (ul, item) {
            let li = $("<li>"),
                wrapper = $("<div>", {text: item.label});
            if (item.disabled) {
                li.addClass("ui-state-disabled");
            }
            $("<span>", {
                style: item.element.attr("data-style"),
                "class": "ui-icon " + item.element.attr("data-class")
            }).appendTo(wrapper);
            return li.append(wrapper).appendTo(ul);
        }
    });

    let msgGroupMemberSelect = $("#msg-group-member-select");
    let groupMemberList = getLocalGroupMemberList();
    if (groupMemberList) {
        for (let i = 0; i < groupMemberList.length; i++) {
            let member = groupMemberList[i];
            let avatar = member.avatar;
            let optionHtml = `<option value="${member.userId}" data-class="avatar" data-style="background-image: url('${member.avatar}'); background-size: 100%;">
                            ${member.nickname}
                        </option>`
            msgGroupMemberSelect.append(optionHtml);
        }
    }

    msgGroupMemberSelect
        .iconselectmenu({
            width: 120,
            change: function (event, data) {
                $(".msg-type-item").removeClass("active");
                msgGroupMemberSelect.parent().addClass("active");

                let msgGroupMemberSelectDom = $("#msg-group-member-select");
                msgGroupMemberSelectDom.val(data.item.value);
                //搜索消息记录
                searchMsgTypeClick(msgGroupMemberSelectDom.parent());
            }
        })
        .iconselectmenu("menuWidget")
        .addClass("ui-menu-icons avatar");
}

function searchMsgTypeClick(_this) {
    $(".msg-type-item").removeClass("active");
    $(_this).addClass("active");

    searchMsgHistoryClick();
}

function searchMsgHistoryClick() {
    let searchMsgListDom = $("#search-msg-list");
    searchMsgListDom.html("");
    //重置分页参数
    hisMsgPage = new Page(20, 0, false);
    searchMsgHistoryPage();
}


function searchMsgHistoryPage() {
    let searchMsgListDom = $("#search-msg-list");

    let msgDatepickerDom = $("#msg-datepicker");
    let msgGroupMemberSelectDom = $("#msg-group-member-select");
    let activeDom = $(".search-msg-type .active");
    let msgType = activeDom.attr("msg-type");
    if (activeDom.attr("name") !== "date" && activeDom.attr("name") !== "member") {
        $(".msg-datepicker-label").text("日期");
        msgDatepickerDom.val("");
        msgGroupMemberSelectDom.val("0");
        $(".ui-selectmenu-text").text("选择群成员");
    }
    let sendUserId = parseInt(msgGroupMemberSelectDom.val());
    sendUserId = sendUserId > 0 ? sendUserId : null;
    let sendDate = msgDatepickerDom.val();
    let keywords = $("#search-msg-input").val();

    let url = `${MSG_URL_PREFIX}/chat/msg/list`;
    let data = {
        userType: USER_TYPE,
        chatId: chatToUser.chatId,
        msgType: msgType,
        sendUserId: sendUserId,
        sendDate: sendDate,
        keywords: keywords,
        pageFlippingType: PAGE_FLIPPING_TYPE.HIS_PULL_UP,
        msgId: hisMsgPage.lastMsgId,
        limit: hisMsgPage.limit
    };
    ajaxSyncRequest(url, "get", data, null, function (res) {
        if (res.code !== 200) {
            logger.info("获取聊天记录列表失败,chatId:", chatToUser.chatId, "res:", res);
            myAlert('', res.message, "err");
            return;
        }
        //缓存总页数
        let chatMsgList = res.data;
        setMsgPageLastMsgId(chatMsgList, hisMsgPage);
        generateMsgHistoryDom(searchMsgListDom, chatMsgList);
        //注册缩放事件
        refreshHisContextMenu();
        //启动上传进度条
        startUploadProgressBar();
    });
}

function generateMsgHistoryDom(searchMsgListDom, chatMsgList) {
    for (let i = 0; i < chatMsgList.length; i++) {
        let chatMsg = chatMsgList[i];
        let msgDom = getMsgDom(chatMsg);
        if (msgDom === '') {
            continue;
        }
        let msgObj = msgParseJson(chatMsg);
        let chatMsgDom = `
             <li class="search-msg-item" >
                <div class="search-msg-item-left">
                    <div class="search-msg-item-avatar">
                        <img class="avatar" src="${chatMsg.avatar}" alt="头像"/>
                    </div>
                </div>
                <div class="search-msg-item-right" msg-id="${chatMsg.id}" msg-type="${chatMsg.msgType}" file-name="${msgObj.fileName}" file-url="${msgObj.fileUrl}" >
                    <div class="search-msg-item-nickname">
                        <span class="nickname">${chatMsg.nickname}</span>
                        <span class="date">${chatMsg.sendTime.length > 10 ? chatMsg.sendTime.substring(0, 10) : chatMsg.sendTime}</span>
                    </div>
                    <div class="search-msg-item-msg">
                        <span class="msg-text" msg-id="${chatMsg.id}" msg-type="${chatMsg.msgType}" file-name="${msgObj.fileName}" new-file-name="${msgObj.newFileName}" file-url="${msgObj.fileUrl}" >${msgDom}</span>
                    </div>
                </div>
            </li>
            `;
        searchMsgListDom.append(chatMsgDom);
    }
}

function msgHistLocate(msgId) {
    msgUpPage.lastMsgId = msgId;
    chatToUser.pageFlippingType = PAGE_FLIPPING_TYPE.LOCATE;
    loadChatMsg();
}

function searchDateClick(_this) {
    $(".msg-type-item").removeClass("active");
    $(_this).addClass("active");

    $("#msg-datepicker").datepicker("show");
}

function searchMsgHistoryListScroll(_this) {
    let scrollTop = $(_this).scrollTop();
    let msgContainer = document.getElementById("search-msg-list");
    let subHeight = (Math.round($(msgContainer).height()) + Math.round(scrollTop)) - msgContainer.scrollHeight;
    // logger.info("height:", Math.round($(msgContainer).height()), $(msgContainer).height(), "scrollTop:", Math.round(scrollTop), scrollTop,
    //     "scrollHeight:", msgContainer.scrollHeight, "subHeight", subHeight);
    if (subHeight < 3 && subHeight > -2) {
        if (hisMsgPage.isLastPage) {
            logger.info("已经是最后一页了");
            return;
        }
        searchMsgHistoryPage();
        $(msgContainer).animate({
            scrollTop: scrollTop + 30
        }, 450);//450毫秒秒滑动到指定位置
    }
}

function initRelayUserTabs() {
    $("#relay-user-tabs").tabs({
        beforeActivate: function (event, ui) {
            // logger.info("beforeActivate event:", event, ",id:", event.currentTarget.hash);
            searchRelayUserList1(event.currentTarget.hash.replace("#", ""));
        },
        create: function (event, ui) {
            // logger.info("create event:", event, ",id:", event.target.id);
            // getRelayChatRoomList();
        }
    });
}

function searchRelayUserList() {
    let tabId = $("#relay-user-tabs li[aria-selected='true']").attr("aria-controls");
    searchRelayUserList1(tabId);
}

function searchRelayUserList1(tabId) {
    let keyword = $("#relay-keyword-input").val();
    // logger.info("searchRelayUserList tabId:", tabId);
    if (tabId === "relay-user-list") {
        getRelayUserFriendList(keyword);
    } else {
        let chatRoomList = chatRoomListSearch(keyword);
        generateRelayUserListDom(chatRoomList, "chat");
    }
}

/**
 * 获取最近聊天列表
 */
function getRelayChatRoomList() {
    if (chatRoomListCache.length > 0) {
        generateRelayUserListDom(chatRoomListCache, "chat");
        return;
    }
    selectedFriendIdSet.clear();
    refreshChatRoomList((data) => generateRelayUserListDom(data, "chat"));
}

/**
 * 获取好友列表
 * @param keyword
 */
function getRelayUserFriendList(keyword) {
    selectedFriendIdSet.clear();
    userFriendListSearch(keyword, null, (data) => generateRelayUserListDom(data, "friend"));
}

function generateRelayUserListDom(searchUserList, type) {
    logger.info("type:", type)
    let listDom;
    if (type === "chat") {
        listDom = $("#relay-chat-list ul");
    } else {
        listDom = $("#relay-user-list ul");
    }
    listDom.html("");
    for (let i = 0; i < searchUserList.length; i++) {
        let userId;
        let serUser = searchUserList[i];
        if (type === "chat") {
            if ($(".replay-msg-btn").attr("action") === "card" && serUser.chatType === ChatType.SINGLE) {
                continue;
            }
            userId = serUser.chatId;
            serUser = serUser.toUser;
            if (serUser.isDissolve) {
                continue;
            }
        } else if (type === "friend") {
            userId = serUser.friendId;
        }
        let userDom = `
                <li class="search-row">
                    <div class="el-image search-avatar">
                        <img src="${serUser.avatar}" class="el-image-inner" alt="头像"/>
                    </div>
                    <div class="search-center">
                        <p class="search-nick">${serUser.nickname}</p> 
                    </div>
                    <span class="checkbox-group" user-id="${userId}" nickname="${serUser.nickname}" avatar="${serUser.avatar}"  onclick="checkboxChange(this)"></span>
                </li>
        `;
        listDom.append(userDom).focus();
    }
}


function openRelayMsgUserDialog(msgId) {
    getRelayChatRoomList();
    $(".replay-msg-btn").attr("msg-id", msgId).attr("action", "relay");
    $("#relay-msg-user-dialog").dialog({title: "转发消息"}).dialog("open");
}

function openSendCardMsgDialog() {
    chatRoomListCache = [];
    getRelayChatRoomList();
    $(".replay-msg-btn").attr("action", "card");
    $("#relay-msg-user-dialog").dialog({title: "发送名片"}).dialog("open");
}


function relayMsgBtnClick(_this) {
    let action = $(_this).attr("action");
    if (action === "card") { // 发送名片
        sendCardMsg();
        return;
    }
    // 转发消息
    let msgIds = [];
    msgIds.push($(_this).attr("msg-id"));
    relayMsg(msgIds);
}

function sendCardMsg() {
    let msgType;
    let tabId = $("#relay-user-tabs li[aria-selected='true']").attr("aria-controls");
    if (tabId === "relay-user-list") {
        msgType = MsgType.BIZ_CARD;
    } else if (tabId === "relay-chat-list") {
        msgType = MsgType.GROUP_BIZ_CARD;
    }
    let selected = $(".checkbox-group-checked");
    if (selected.length === 0) {
        myAlert('', "请选择要转发的用户", "err");
        return;
    }
    let selOne = selected.eq(0);
    let msgCardO = new MsgCardO(selOne.attr("user-id"), selOne.attr("nickname"), selOne.attr("avatar"));
    //发送消息
    sendObjMsg(msgCardO, msgType);
    $("#relay-msg-user-dialog").dialog("close");
}

function relayMsg(msgIds) {
    let chatIds = [];
    let toUserIds = [];
    let tabId = $("#relay-user-tabs li[aria-selected='true']").attr("aria-controls");
    if (tabId === "relay-user-list") {
        toUserIds = Array.from(selectedFriendIdSet).join(",");
    } else if (tabId === "relay-chat-list") {
        chatIds = Array.from(selectedFriendIdSet).join(",");
    }
    let url = `${MSG_URL_PREFIX}/chat/msg/relay`;
    ajaxRequest(url, "post", {
        userType: USER_TYPE,
        msgIds: msgIds.join(","),
        chatIds: chatIds,
        toUserIds: toUserIds
    }, null, function (res) {
        if (res.code !== 200) {
            logger.info("转发消息失败,msgIds:", msgIds);
            myAlert('', res.message, "err");
            return;
        }
        $("#relay-msg-user-dialog").dialog("close");
        $("#chat-msg-history-dialog").dialog("close");
    });
}

//------------------------------------------------------------------------------好友------------------------------------------------------------------
// 初始化同意好友申请对话框
function initAgreeFriendApplyDialog() {
    $("#agree-friend-apply-dialog").dialog({
        title: "同意好友申请",
        autoOpen: false,   // 是否自动弹出窗口
        modal: true,    // 设置为模态对话框
        resizable: false,
        width: 300,   //弹出框宽度
        height: 320,   //弹出框高度
        close: function (event, ui) {
            // 关闭弹出框
            $("#remark-agree").val("");
            $("#label-agree").val("");
        }
    });
}

function registerAgreeFriendApplyDialogClick() {
    $(".friend-title .agree-btn").on("click", function (event) {
        agreeFriendApplyDialogOpen(this);
        event.stopPropagation();
    });
}

/**
 * 添加好友按钮点击事件
 */
function agreeFriendApplyDialogOpen(_this) {
    let applyId = $(_this).attr("apply-id");
    let nickname = $(_this).attr("nickname");
    let avatar = $(_this).attr("avatar");
    let enterpriseName = $(_this).attr("enterprise-name");
    $("#apply-id-agree").val(applyId).focus();
    $("#add-avatar-agree").attr("src", avatar);
    $("#add-nick-agree").text(nickname);
    $("#add-area-agree").text(enterpriseName);

    $("#remark-agree").attr("placeholder", nickname);
    chatToUser = new ChatToUser('', $(_this).attr("to-user-id"), 0, nickname, avatar);
    $("#agree-friend-apply-dialog").dialog("open");
}

function friendBtnClick(_this, hideId) {
    let id = "#" + hideId;
    let className = $(_this).children().eq(0).attr("class");
    if (className === "friend-arrow-right") {
        $(_this).children().eq(0).removeClass("friend-arrow-right").addClass("friend-arrow-down");
        $(id).removeClass("hide");
    } else {
        $(_this).children().eq(0).removeClass("friend-arrow-down").addClass("friend-arrow-right");
        $(id).addClass("hide");
    }
}

/**
 * 获取好友申请列表
 */
function getFriendApplyList() {
    //清空未读好友申请ID集合
    unreadFriendApplyIdSet.clear();
    let url = `${MSG_URL_PREFIX}/chat/friend/apply/list`;
    ajaxRequest(url, "get", {userType: USER_TYPE}, null, function (res) {
        if (res.code !== 200) {
            logger.info("获取好友申请列表失败,chatId:", chatId, "res:", res);
            myAlert('', res.message, "err");
            return;
        }
        let friendApplyListDom = $("#friend-apply-list");
        friendApplyListDom.html("");
        let friendApplyList = res.data;
        for (let i = 0; i < friendApplyList.length; i++) {
            let friendApply = friendApplyList[i];
            let unreadHideClass = "";
            if (friendApply.unreadCount === 0) {
                unreadHideClass = "hide";
            } else {
                //添加到未读集合
                unreadFriendApplyIdSet.add(friendApply.id.toString());
            }
            let toUserId = friendApply.applyUserId === chatUser.id ? friendApply.friendId : friendApply.applyUserId;
            let statusObj = getStatusInfo(friendApply, '');
            let friendApplyDom = `
                <div class="apply-info" apply-id="${friendApply.id}" to-user-id="${toUserId}" nickname="${friendApply.nickname}" avatar="${friendApply.avatar}" onclick="goFriendApplyInfoPage(this);">
                    <div class="friend-head">
                        <img class="friend-avatar" src="${friendApply.avatar}" alt=""/>
                        <span class="friend-unread ${unreadHideClass}" unread-count="${friendApply.unreadCount}">${friendApply.unreadCount}</span>
                    </div>
                    <div class="friend-right">
                        <div class="friend-title">
                            <span class="friend-name">${friendApply.nickname}</span>
                            <span class="apply-status ${statusObj.statusHideClass}">${statusObj.statusDesc}</span>
                            <span class="agree-btn ${statusObj.agreeBtnClass}" apply-id="${friendApply.id}" nickname="${friendApply.nickname}" avatar="${friendApply.avatar}" enterprise-name="${friendApply.enterpriseName}" >接受</span>
                        </div>
                        <div class="friend-bottom">
                            <span class="apply-msg">${friendApply.reason}</span>
                        </div>
                    </div>
                </div>
            `;
            friendApplyListDom.append(friendApplyDom);
        }
        let unreadCount = unreadFriendApplyIdSet.size;
        if (unreadCount > 0) {
            $(".friend-apply .top-unread").removeClass("hide").attr("unread-count", unreadCount).text(unreadCount);
        }
        //注册同意好友申请按钮点击事件
        registerAgreeFriendApplyDialogClick();
    });
}

function goFriendApplyInfoPage(_this) {
    let applyId = $(_this).attr("apply-id");
    getFriendApplyInfo(applyId);
    //隐藏未读消息
    updateTotalUnreadCount(_this);
}

function getFriendApplyInfo(applyId) {
    let url = `${MSG_URL_PREFIX}/chat/friend/apply/info`;
    ajaxRequest(url, "get", {userType: USER_TYPE, applyId: applyId}, null, function (res) {
        if (res.code !== 200) {
            logger.info("获取好友申请详情失败,applyId:", applyId, "res:", res);
            myAlert('', res.message, "err");
            return;
        }
        //删除未读集合中的申请ID
        unreadFriendApplyIdSet.delete(applyId);
        let applyInfo = res.data;
        let msgListDom = ``;
        let applyMsgList = applyInfo.notifyMsgList;
        for (let i = 0; i < applyMsgList.length; i++) {
            let applyMsg = applyMsgList[i];
            msgListDom = msgListDom + `<li>${applyMsg.nickname}: ${applyMsg.msgContent}</li>`;
        }
        let remarkDom = ``;
        if (applyInfo.remark) {
            remarkDom = `<div class="more-info"><label>备注</label><span class="friend-remark">${applyInfo.remark}</span></div>`;
        }
        let toUserId = applyInfo.applyUserId === chatUser.id ? applyInfo.friendId : applyInfo.applyUserId;
        let statusObj = getStatusInfo(applyInfo, '对方');
        let infoDom = `
            <div class="friend-apply-info" >
                <div class="friend-head">
                    <img class="friend-avatar" src="${applyInfo.avatar}" alt="头像"/>
                     <div class="friend-name-info">
                        <span class="friend-name">${applyInfo.remark ? applyInfo.remark : applyInfo.nickname}</span>
                        <span class="friend-remark">昵称: ${applyInfo.nickname}</span>
                        <span class="friend-remark">公司: ${applyInfo.enterpriseName}</span>
                    </div>
                </div>
                <div class="friend-apply-msg">
                    <ul id="friend-apply-msg-list" apply-id="${applyInfo.id}" nickname="${applyInfo.nickname}">
                       ${msgListDom}
                    </ul>
                    <div class="apply-replay-input ${statusObj.replayHideClass}">
                        <div class="friend-apply-replay">回复</div>
                        <div class="friend-apply-replay-content hide">
                            <input id="friend-apply-id" type="hidden" value="${applyInfo.id}">
                            <textarea id="apply-replay-content" cols="40" rows="3" placeholder="请输入回复内容" onkeyup="replayContentChange(this); "></textarea>
                            <button class="send-btn"  disabled onclick="replayMsgSend()">发送</button>
                        </div>
                    </div>
                </div>
                <div class="friend-source">
                    ${remarkDom}
                    <div class="more-info">
                        <label>来源</label><span>${applyInfo.sourceDesc}</span>
                    </div>
                </div>
                <div class="friend-bottom">
                    <span class="apply-status ${statusObj.statusHideClass}">${statusObj.statusDesc}</span>
                    <span class="agree-btn ${statusObj.agreeBtnClass}" apply-id="${applyInfo.id}" to-user-id="${toUserId}" nickname="${applyInfo.nickname}" avatar="${applyInfo.avatar}" enterprise-name="${applyInfo.enterpriseName}" onclick="agreeFriendApplyDialogOpen(this)" >通过好友验证</span>
                </div>
            </div>
        `;
        $("#container").html("").append(infoDom);
        $(".footer").removeClass("show").addClass("hide");
        $(".header .left-arrow").removeClass("hide").addClass("show").attr("action", "friend");
        $(".header .nickname").text("好友申请");
        $("#add-operation").addClass("hide").removeClass("show");
        //注册好友申请消息回复按钮点击事件
        registerFriendApplyMsgReplayClick();
        //注册好友申请消息回复输入框keydown事件
        applyReplayContentKeydown();
        //自定义移动端返回事件
        pushHistory({tit: "friend-apply-info", applyId: applyId});
    });
}

function getStatusInfo(applyInfo, oh) {
    let statusO = {agreeBtnClass: '', statusHideClass: '', replayHideClass: ''};
    if (applyInfo.status === 0) {
        statusO.statusDesc = "等待" + oh + "验证";
        if (applyInfo.applyUserId === chatUser.id) {
            statusO.agreeBtnClass = "hide";
        } else {
            statusO.statusHideClass = "hide";
        }
    } else {
        statusO.agreeBtnClass = "hide";
        statusO.replayHideClass = "hide";
        statusO.statusDesc = "已添加";
    }
    return statusO;
}

function updateTotalUnreadCount(_this) {
    let unreadCountDom = $(_this).find(".friend-unread");
    let unreadCount = parseInt(unreadCountDom.attr("unread-count"));
    if (unreadCount > 0) {
        unreadCountDom.attr("unread-count", 0).addClass("hide").text("0");
    }
    let totalUnreadDom = $("#top-friend-unread");
    let totalUnreadDom2 = $(".friend-apply .top-unread");
    let totalUnreadCount = parseInt(totalUnreadDom.attr("unread-count"));
    if (totalUnreadCount > 0 && unreadFriendApplyIdSet.size === 0) {
        totalUnreadDom.addClass("hide");
        totalUnreadDom2.addClass("hide");
        totalUnreadCount = unreadFriendApplyIdSet.size;
    }
    totalUnreadDom.attr("unread-count", totalUnreadCount).text(totalUnreadCount);
    totalUnreadDom2.attr("unread-count", totalUnreadCount).text(totalUnreadCount);
}

/**
 * 注册好友申请消息回复按钮点击事件
 */
function registerFriendApplyMsgReplayClick() {
    $(".friend-apply-replay").on("click", function (e) {
        $(".friend-apply-replay").removeClass("show").addClass("hide");
        $(".friend-apply-replay-content").removeClass("hide").addClass("show");
        $(".friend-apply-msg").on("click", function (event) {
            if (event.target.className.indexOf("friend-apply-replay-content") !== -1 || event.target.id === "apply-replay-content") {
                return;
            }
            $(".friend-apply-replay").removeClass("hide").addClass("show");
            $(".friend-apply-replay-content").removeClass("show").addClass("hide");
        });
        e.stopPropagation();
    });
}

function replayContentChange(_this) {
    let content = $(_this).val();
    if (content === "") {
        $(".send-btn").attr("disabled", true);
    } else {
        $(".send-btn").removeAttr("disabled");
    }
}

function replayMsgSend() {
    let applyId = $("#friend-apply-id").val();
    if (applyId === "") {
        myAlert('', '请先选择好友申请记录', "err");
        return;
    }
    let content = $("#apply-replay-content").val();
    if (content === "") {
        myAlert('', '请输入回复内容', "err");
        return;
    }
    let data = JSON.stringify({userType: USER_TYPE, applyId: applyId, replayContent: content});
    let url = `${MSG_URL_PREFIX}/chat/friend/apply/replay`;
    ajaxRequest(url, "post", data, CONTENT_TYPE_JSON, function (res) {
        if (res.code !== 200) {
            logger.info("回复失败,applyId:", applyId, "res:", res);
            myAlert('', res.message, "err");
            return;
        }
        $("#friend-apply-list .select").find(".apply-msg").text("我: " + content);
        $("#friend-apply-msg-list").append(`<li>我: ${content}</li>`);
        $("#apply-replay-content").val("");
    });
}

function applyReplayContentKeydown() {
    $("#apply-replay-content").on('keydown', function (event) {
        //监听特定的按键
        if (event.keyCode === 13) {
            replayMsgSend();
            $(".friend-apply-replay").removeClass("hide").addClass("show");
            $(".friend-apply-replay-content").removeClass("show").addClass("hide");
        }
        //防止按键的默认行为
        if (event.keyCode === 32) {
            event.preventDefault();
        }
    });
}


/**
 * 同意好友申请
 */
function agreeFriendApply() {
    let applyId = $("#apply-id-agree").val();
    let remark = $("#remark-agree").val();
    let label = $("#label-agree").val();
    let url = `${MSG_URL_PREFIX}/chat/friend/apply/agree`;
    let data = JSON.stringify({
        userType: USER_TYPE,
        applyId: applyId,
        remark: remark,
        label: label
    });
    ajaxRequest(url, "post", data, CONTENT_TYPE_JSON, function (res) {
        if (res.code !== 200) {
            logger.info("好友添加失败,res:", res);
            return;
        }
        $("#agree-friend-apply-dialog").dialog("close");
        chatToUser.chatId = res.data;
        gotoFirstChatMsg();
    });
}


/**
 * 获取好友列表
 */
function getFriendList() {
    let url = `${MSG_URL_PREFIX}/chat/friend/list`;
    ajaxRequest(url, "get", {userType: USER_TYPE}, null, function (res) {
        if (res.code !== 200) {
            logger.info("获取好友列表失败,", "res:", res);
            myAlert('', res.message, "err");
            return;
        }
        let friendListDom = $("#friend-list");
        friendListDom.html("");
        let friendList = res.data;
        let initialSet = new Set();
        for (let i = 0; i < friendList.length; i++) {
            let friend = friendList[i];
            if (!initialSet.has(friend.initial)) {
                let initialDom = `<label class="friend-initial">${friend.initial}</label>`;
                initialSet.add(friend.initial);
                friendListDom.append(initialDom);
            }
            let friendDom = `
                <div class="friend-info" friend-id="${friend.friendId}" onclick="goFriendInfoPage(this);">
                    <div class="friend-head">
                        <img class="friend-avatar" src="${friend.avatar}" alt=""/>
                    </div>
                    <div class="friend-right">
                        <div class="friend-title">
                            <span class="friend-name">${friend.nickname}</span>
                        </div>
                    </div>
                </div>
             `;
            friendListDom.append(friendDom);
        }
    });
}

function goFriendInfoPage(_this) {
    let friendId = $(_this).attr("friend-id");
    getFriendInfo(friendId);
}

function getFriendInfo(friendId) {
    let url = `${MSG_URL_PREFIX}/chat/friend/info`;
    ajaxRequest(url, "get", {userType: USER_TYPE, friendId: friendId}, null, function (res) {
        if (res.code !== 200) {
            logger.info("获取好友详情失败,friendId:", friendId, "res:", res);
            myAlert('', res.message, "err");
            return;
        }
        let friendInfo = res.data;
        let infoDom = `
            <div class="friend-apply-info" >
                <div class="friend-head">
                    <img class="friend-avatar" src="${friendInfo.avatar}" alt="头像"/>
                     <div class="friend-name-info">
                        <span class="friend-name">${friendInfo.remark ? friendInfo.remark : friendInfo.nickname}</span>
                        <span class="friend-remark">昵称: ${friendInfo.nickname}</span>
                        <span class="friend-remark">公司: ${friendInfo.enterpriseName}</span>
                    </div>
                </div>
                <div class="friend-source">
                    <div class="more-info"><label>备注</label><span class="friend-remark"><input type="text" friend-id="${friendInfo.friendId}" remark="${friendInfo.remark}" value="${friendInfo.remark}" placeholder="设置备注" onclick="remarkClick(this)" onfocusin="remarkClick(this)" onfocusout="modifyFriendRemark(this)"></span></div>
                    <div class="more-info">
                        <label>来源</label><span>${friendInfo.sourceDesc}</span>
                    </div>
                </div>
                <div class="friend-bottom">
                    <span class="send-msg-btn" user-id="${friendId}" nickname="${friendInfo.nickname}" avatar="${friendInfo.avatar}" onclick="sendMsgBtnClick(this)">发消息</span>
                </div>
            </div>
        `;
        chatToUser = new ChatToUser('', friendId, '0', friendInfo.nickname, friendInfo.avatar)
        $("#container").html("").append(infoDom);
        $(".footer").removeClass("show").addClass("hide");
        $(".header .left-arrow").removeClass("hide").addClass("show").attr("action", "friend");
        $(".header .nickname").text("好友");
        $("#add-operation").addClass("hide").removeClass("show");
        //自定义移动端返回事件
        pushHistory({tit: "friend-info", friendId: friendId});
    });
}

function sendMsgBtnClick(_this) {
    let userId = $(_this).attr("user-id");
    let url = `${MSG_URL_PREFIX}/chat/room/gotoSendMsg`;
    ajaxRequest(url, "get", {userType: USER_TYPE, friendId: userId}, null, function (res) {
        if (res.code !== 200) {
            logger.info("去聊天室失败,friendId:", userId, "res:", res);
            myAlert('', res.message, "err");
            return;
        }
        let nickname = $(_this).attr("nickname");
        let avatar = $(_this).attr("avatar");
        chatToUser = new ChatToUser('', userId, '0', nickname, avatar);

        let chatId = res.data;
        chatToUser.chatId = chatId;
        //缓存1天聊天id
        setCookieDay(PREV_CHAT_ID_KEY, chatId, 1);
        gotoFirstChatMsg();
    });
}


function modifyFriendRemark(_this) {
    $(_this).removeClass("i-click");
    let friendId = $(_this).attr("friend-id");
    let oldRemark = $(_this).attr("remark");
    let remark = $(_this).val();
    if (oldRemark === remark) {
        return;
    }
    let url = `${MSG_URL_PREFIX}/chat/friend/modifyRemark`;
    ajaxRequest(url, "post", {userType: USER_TYPE, friendId: friendId, remark: remark}, null, function (res) {
        if (res.code !== 200) {
            logger.info("修改好友备注失败,friendId:", friendId, "res:", res);
            myAlert('', res.message, "err");
            return;
        }
        $(_this).attr("remark", remark);
        let nickname = res.data;
        $(".friend-name-info .friend-name").text(nickname).focus();
        //获取好友申请列表
        getFriendApplyList();
        //获取好友列表
        getFriendList();
    });
}

function remarkClick(_this) {
    $(_this).addClass("i-click");
}


/**
 * 获取聊天总未读数
 */
function getChatTotalUnreadCount() {
    let url = `${MSG_URL_PREFIX}/chat/room/getUnreadCount`;
    ajaxRequest(url, "get", {userType: USER_TYPE}, null, function (res) {
        if (res.code !== 200) {
            logger.info("获取总未读数失败,res:", res);
            myAlert('', res.message, "err");
            return;
        }
        if (res.data.chatUnreadCount > 0) {
            $("#top-chat-unread").removeClass("hide").attr("unread-count", res.data.chatUnreadCount).text(res.data.chatUnreadCount);
        }
        if (res.data.friendUnreadCount > 0) {
            $("#top-friend-unread").removeClass("hide").attr("unread-count", res.data.friendUnreadCount).text(res.data.friendUnreadCount);
        }
    });
}

/**
 * 创建群聊按钮点击事件
 */
function openCreateGroupDialog() {
    searchFriendClick();
    $("#create-group-dialog").dialog("open");
    $("#search-friend-input").on('keydown', function (event) {
        //监听特定的按键
        if (event.keyCode === 13) {
            searchFriendClick();
        }
        //防止按键的默认行为
        if (event.keyCode === 32) {
            event.preventDefault();
        }
    });
}

/**
 * 初始化添加好友对话框
 */
function initCreateGroupDialog() {
    $("#create-group-dialog").dialog({
        title: "创建群聊",
        autoOpen: false,   // 是否自动弹出窗口
        modal: true,    // 设置为模态对话框
        resizable: false,
        width: 300,   //弹出框宽度
        height: 440,   //弹出框高度
        close: function (event, ui) {
            $("#search-friend-input").val("");
            $("#search-friend-list").html("");
            $(".create-group-btn").attr("disabled", true);
        }
    });
}


/**
 * 创建群聊搜索好友按钮点击事件
 */
function searchFriendClick() {
    selectedFriendIdSet.clear();
    let keyword = $("#search-friend-input").val();
    userFriendListSearch(keyword, null, (data) => getSearchUserListDom(data));
}

function getSearchUserListDom(searchUserList) {
    let searchUserListDom = $("#search-friend-list");
    searchUserListDom.html("");
    for (let i = 0; i < searchUserList.length; i++) {
        let serUser = searchUserList[i];
        let userDom = `
                <li class="search-row">
                    <div class="el-image search-avatar">
                        <img src="${serUser.avatar}" class="el-image-inner" alt="头像"/>
                    </div>
                    <div class="search-center">
                        <p class="search-nick">${serUser.nickname}</p> 
                    </div>
                    <span class="checkbox-group" user-id="${serUser.friendId}" nickname="${serUser.nickname}" avatar="${serUser.avatar}"  onclick="checkboxChange(this)"></span>
                </li>
        `;
        searchUserListDom.append(userDom);
    }
}

let selectedFriendIdSet = new Set();

function checkboxChange(_this) {
    if ($(_this).hasClass("checkbox-group-checked")) {
        $(_this).removeClass("checkbox-group-checked").addClass("checkbox-group");
        selectedFriendIdSet.delete($(_this).attr("user-id"));
    } else {
        let replayMsgBtn = $(".replay-msg-btn");
        if (replayMsgBtn.length > 0 && replayMsgBtn.attr("action") === "card") { //发送名片时单选
            selectedFriendIdSet.clear();
            $(".checkbox-group-checked").removeClass("checkbox-group-checked").addClass("checkbox-group");
        }
        $(_this).addClass("checkbox-group-checked").removeClass("checkbox-group");
        selectedFriendIdSet.add($(_this).attr("user-id"));
    }
    if (selectedFriendIdSet.size > 0) {
        $(".create-group-btn").removeAttr("disabled");
        $(".replay-msg-btn").removeAttr("disabled");
    } else {
        $(".create-group-btn").attr("disabled", true);
        $(".replay-msg-btn").attr("disabled", true);
    }
}

function createGroup() {
    if (selectedFriendIdSet.size === 0) {
        myAlert('', "请先选择好友", "err");
        return;
    }
    let selectedFriendIdList = Array.from(selectedFriendIdSet);
    let url = `${MSG_URL_PREFIX}/chat/group/create`;
    ajaxRequest(url, "post", {userType: USER_TYPE, members: selectedFriendIdList.join(',')}, null, function (res) {
        if (res.code !== 200) {
            logger.info("创建群聊失败,selectedFriendIdSet:", selectedFriendIdSet);
            myAlert('', res.message, "err");
            return;
        }
        let chatId = res.data;
        logger.info("创建群聊成功,chatId:", chatId);
        //缓存1天聊天id
        setCookieDay(PREV_CHAT_ID_KEY, chatId, 1);
        // 关闭对话框
        $("#create-group-dialog").dialog("close");
        //跳转到聊天界面
        loadChatRoom();
    });
}

/**
 * 添加好友按钮点击事件
 */
function addFriendClick() {
    $("#add-friend-dialog").dialog("open");
    $("#search-input").on('keydown', function (event) {
        //监听特定的按键
        if (event.keyCode === 13) {
            addFriendSearchClick();
        }
        //防止按键的默认行为
        if (event.keyCode === 32) {
            event.preventDefault();
        }
    });
}

/**
 * 初始化添加好友对话框
 */
function initAddFriendDialog() {
    $("#add-friend-dialog").dialog({
        title: "添加好友",
        autoOpen: false,   // 是否自动弹出窗口
        modal: true,    // 设置为模态对话框
        resizable: false,
        width: 300,   //弹出框宽度
        height: 400,   //弹出框高度
        close: function (event, ui) {
            $("#search-input").val("");
            $("#search-user-list").html("");
        }
    });
    $("#add-friend-apply-dialog").dialog({
        title: "申请添加好友",
        autoOpen: false,   // 是否自动弹出窗口
        modal: true,    // 设置为模态对话框
        resizable: false,
        width: 300,   //弹出框宽度
        height: 400,   //弹出框高度
        close: function (event, ui) {
            // 关闭弹出框
            $("#remark").val("");
            $("#label").val("");
        }
    });
}

/**
 * 注册添加好友操作按钮点击事件
 */
function registerAddOperationClick() {
    $("#add-operation").on("click", function (e) {
        let oper = $("#more-operation");
        if (oper.hasClass("show")) {
            oper.removeClass("show");
            oper.addClass("hide");
            return;
        }
        oper.removeClass("hide");
        oper.addClass("show");
        $(document).on("click", function (event) {
            oper.removeClass("show");
            oper.addClass("hide");
            $(document).off("click");
        });
        e.stopPropagation();
    });
}

/**
 * 添加好友搜索按钮点击事件
 */
function addFriendSearchClick() {
    let searchUserListDom = $("#search-user-list");
    searchUserListDom.html("");
    let keyword = $("#search-input").val();
    if (keyword === "") {
        return;
    }
    let url = `${MSG_URL_PREFIX}/chat/user/getSearchList`;
    ajaxRequest(url, "get", {userType: USER_TYPE, keyword: keyword}, null, function (res) {
        if (res.code !== 200) {
            logger.info("搜索用户列表失败,keyword:", keyword);
            myAlert('', res.message, "err");
            return;
        }
        let searchUserList = res.data;
        for (let i = 0; i < searchUserList.length; i++) {
            let serUser = searchUserList[i];
            let userDom = `
                <li class="search-row">
                    <div class="el-image search-avatar">
                        <img src="${serUser.avatar}" class="el-image-inner" alt="头像"/>
                    </div>
                    <div class="search-center">
                        <p class="search-nick">${serUser.nickname}</p>
                        <p class="search-city">${serUser.enterpriseName}</p>
                    </div>
                    <span class="apply-add" user-id="${serUser.id}" nickname="${serUser.nickname}" avatar="${serUser.avatar}" enterprise-name="${serUser.enterpriseName}" onclick="addFriendApplyClick(this,'${FRIEND_SOURCE.SEARCH}')">加好友</span>
                </li>
        `;
            searchUserListDom.append(userDom);
        }
    });
}

/**
 * 添加好友按钮点击事件
 */
function addFriendApplyClick(_this, source) {
    let userId = $(_this).attr("user-id");
    let nickname = $(_this).attr("nickname");
    let avatar = $(_this).attr("avatar");
    let enterpriseName = $(_this).attr("enterprise-name");
    $("#add-avatar").attr("src", avatar);
    $("#add-nick").text(nickname);
    $("#add-area").text(enterpriseName);

    $("#friend-id").val(userId).focus();
    $("#source").val(source);
    $("#remark").attr("placeholder", nickname);
    $("#applyReason").val("我是" + chatUser.nickname);

    $("#add-friend-apply-dialog").dialog("open");
}

function sendAddFriendApplyClick() {
    let friendId = $("#friend-id").val();
    let source = $("#source").val();
    let remark = $("#remark").val();
    let label = $("#label").val();
    let applyReason = $("#applyReason").val();
    if (friendId === '') {
        myAlert('', "好友ID为空", "err");
        return
    }
    if (source === '') {
        myAlert('', "来源为空", "err");
        return;
    }
    if (applyReason === '') {
        myAlert('', "申请理由为空", "err");
        return;
    }
    let data = JSON.stringify({
        userType: USER_TYPE,
        friendId: friendId,
        source: source,
        remark: remark,
        label: label,
        applyReason: applyReason
    });
    let url = `${MSG_URL_PREFIX}/chat/friend/apply/add`;
    ajaxRequest(url, "post", data, CONTENT_TYPE_JSON, function (res) {
        if (res.code !== 200) {
            logger.info("添加好友申请失败,data:", data);
            myAlert('', res.message, 'err');
            return;
        }
        $("#add-friend-apply-dialog").dialog("close");
        myAlert('', "添加好友申请成功", 'success');
        loadChatFriend();
    });
}

function remarkFocus(_this) {
    let remark = $(_this).val();
    if (remark === '') {
        $(_this).val($(_this).attr("placeholder"));
    }
}


