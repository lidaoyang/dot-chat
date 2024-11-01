$(function () {
    if (mobile) {
        location.href = "/mobile/index.html";
    }
    //登录用户存在时去登录
    if (!chatUser) {
        location.href = "/login.html";
    }
    //自动刷新token
    autoRefreshToken();
    //设置当前用户头像
    setChatUserAvatar();
    //注册添加好友按钮点击事件
    registerAddOperationClick();
    //获取聊天总未读数
    getChatTotalUnreadCount();
    //加载聊天室
    loadChatRoom();
    //注册我的点击事件
    registerMyClick();
    //初始化创建群对话框
    initCreateGroupDialog();
    //初始化添加好友对话框
    initAddFriendDialog();
    //初始化修改我的信息对话框
    initModifyMyInfoDialog();
    //初始化修改头像对话框
    initModifyAvatarDialog();
    //初始化修改密码对话框
    initModifyPwdDialog();
    //注册顶部右侧操作按钮点击事件
    registerTopRightOprClick();
    //注册搜索框点击事件
    registerTopSearchClick();
    //欢迎对话框
    welcomeAlert();
    // 视频通话可拖拽
    $("#call-video-dialog").draggable({containment: "document"});
    $("#call-audio-dialog").draggable({containment: "document"});

});

//聊天记录分页参数
let msgPage, msgUpPage, hisMsgPage;

/**
 * 设置聊天用户头像
 */
function setChatUserAvatar() {
    if (chatUser) {
        $("#curr-chat-head").attr("src", chatUser.avatar);
    }
}

/**
 * 清空搜索框
 */
function clearSearchInput() {
    $("#top-search-input").val("");
}

function registerTopSearchClick() {
    let isTypingPinyin = false;
    $("#top-search-input").on({
        input: function (e) {
            // logger.info("input");
            if (isTypingPinyin) {
                return;
            }
            topSearch();
        },
        focus: function (e) {
            // logger.info("focus");
            topSearch();
        },
        compositionstart: function (e) {
            // 监听中文输入法
            // logger.info("compositionstart");
            isTypingPinyin = true;
        },
        compositionend: function (e) {
            // 监听中文输入法
            // logger.info("compositionend");
            isTypingPinyin = false;
            topSearch();
        }
    });
}

function topSearch() {
    let $searchFriendListDom = $(".search-friend-list");
    $searchFriendListDom.html("");
    let $searchChatRoomListDom = $(".search-chat-room-list");
    $searchChatRoomListDom.html("");

    let keyword = $("#top-search-input").val();
    // logger.info("搜索关键字：", keyword);
    if (keyword === "") {
        $("#top-search-dialog").addClass("hide");
        // 移除点击事件
        $(document).off("click");
        return;
    }

    // 搜索好友
    userFriendListSearch(keyword, null, (dataList) => {
        if (dataList.length === 0) {
            $searchFriendListDom.append(`<li class="search-item"><div class="search-none">未搜索到好友</div></li>`);
            return;
        }
        for (let i = 0; i < dataList.length; i++) {
            let itemDom = topSearchItemDom(dataList[i], 'user');
            $searchFriendListDom.append(itemDom);
        }
    });

    // 搜索群聊
    let chatRoomList = chatRoomListSearch(keyword);
    if (chatRoomList.length === 0) {
        $searchChatRoomListDom.append(`<li class="search-item"><div class="search-none">未搜索到聊天室</div></li>`);
    } else {
        for (let i = 0; i < chatRoomList.length; i++) {
            let chatRoom = chatRoomList[i];
            let itemDom = topSearchItemDom(chatRoom, 'chatroom');
            $searchChatRoomListDom.append(itemDom);
        }
    }

    // 显示搜索界面
    $("#top-search-dialog").removeClass("hide");

    // 点击空白区域关闭搜索框
    $(document).on("click", function (event) {
        let claName = event.target.className;
        // logger.info("className:", claName);
        if ("search-input" === claName) {
            return;
        }
        $("#top-search-dialog").addClass("hide");
        // 移除点击事件
        $(document).off("click");
    });
}

function topSearchItemDom(user, type) {
    let clickStr='';
    if (type === 'user') {
         clickStr = "sendMsgBtnClick("+user.friendId+")";
    } else {
        clickStr = "toSendMsg('"+user.chatId+"')";
        user = user.toUser;
    }
    return `
            <li class="search-item" onclick="${clickStr}">
                <div class="item-avatar">
                    <img class="avatar-a" src="${user.avatar}" alt=""/>
                </div>
                <div class="item-nickname">
                    <div class="nickname">${user.nickname}</div>
                </div>
            </li>
            `;
}

function toSendMsg(chatId){
    //缓存1天聊天id
    setCookieDay(PREV_CHAT_ID_KEY, chatId, 1);
    loadChatRoom();
}

/**
 * 注册好友申请消息回复按钮点击事件
 */
function registerMyClick() {
    $(".top-left .header").on("click", function (e) {
        let myHead = $(".my-info .my-head");
        $(myHead).find(".my-avatar").attr("src", chatUser.avatar);
        $(myHead).find(".my-name-info .my-name").text(chatUser.nickname ? chatUser.nickname : '');
        $(myHead).find(".my-name-info .my-id-v").text(chatUser.id);
        let myMore = $(".top-left .my-more");
        $(myMore).find(".my-more-info .my-phone").text(chatUser.phone);
        $(myMore).find(".my-more-info .my-signature").text(chatUser.signature);
        if (chatUser.sex !== null && chatUser.sex !== undefined) {
            let sexDom = ``;
            if (chatUser.sex === 1) {
                sexDom = `<span class="my-sex my-sex-male"></span>`;
            } else if (chatUser.sex === 2) {
                sexDom = `<span class="my-sex my-sex-female"></span>`;
            }
            $(myHead).find(".my-name-info .my-sex").remove();
            $(myHead).find(".my-name-info").append(sexDom);
        }
        $(".my").removeClass("hide").addClass("show");
        $(document).on("click", function (event) {
            // logger.info(event.target.className);
            if (event.target.className.startsWith("my")) {
                return;
            }
            $(".my").removeClass("show").addClass("hide");
            // 移除点击事件
            $(document).off("click");
        });
        e.stopPropagation();
    });
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
        width: 790,   //弹出框宽度
        height: 630,   //弹出框高度
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
        $("#curr-chat-head").attr("src", res);
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
 * 获取聊天总未读数
 */
function getChatTotalUnreadCount() {
    let url = `${MSG_URL_PREFIX}/chat/room/getUnreadCount`;
    ajaxRequest(url, "get", {}, null, function (res) {
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
        width: 470,   //弹出框宽度
        height: 440,   //弹出框高度
        position: {my: "left top", at: "left bottom", of: "#add-operation"},
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
    ajaxRequest(url, "post", {members: selectedFriendIdList.join(',')}, null, function (res) {
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
        width: 470,   //弹出框宽度
        height: 450,   //弹出框高度
        position: {my: "left top", at: "left bottom", of: "#add-operation"},
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
        width: 375,   //弹出框宽度
        height: 420,   //弹出框高度
        close: function (event, ui) {
            // 关闭弹出框
            $("#remark").val("");
            $("#label").val("");
        }
    });
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
        width: 360,   //弹出框宽度
        height: 400,   //弹出框高度
        close: function (event, ui) {

        }
    });
}

function modifyMyInfoOpen() {
    $("#modify-avatar").attr("src", chatUser.avatar);
    $("#modify-nick").text(chatUser.nickname);
    $("#nickname").val(chatUser.nickname);
    $("#modify-signature").text(chatUser.signature);
    if (chatUser.sex !== null && chatUser.sex !== undefined) {
        $("#gender").val(chatUser.sex);
    }
    $("#modify-myinfo-dialog").dialog("open");
}

function saveMyInfoClick() {
    let nickname = $("#nickname").val();
    let sex = $("#gender").val();
    sex = sex ? parseInt(sex) : null;
    let signature = $("#signature").val();
    let url = `${MSG_URL_PREFIX}/chat/user/updateNickname`;
    ajaxRequest(url, "post", {nickname: nickname, sex: sex, signature: signature}, null, function (res) {
        if (res.code !== 200) {
            logger.info("昵称更新失败,res:", res);
            myAlert('', res.message, "err");
            return;
        }
        chatUser.nickname = nickname;
        chatUser.sex = sex;
        chatUser.signature = signature;
        localStorage.setItem(CHAT_USER_KEY, JSON.stringify(chatUser));
        $("#modify-myinfo-dialog").dialog("close");
        myAlert('', "修改成功", "ok");
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
        width: 360,   //弹出框宽度
        height: 340,   //弹出框高度
        close: function (event, ui) {
            $("#oldPwd").val("");
            $("#newPwd").val("");
            $("#validNewPwd").val("");
        }
    });
}

function openModifyPwdDialog() {
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
 * 注册添加好友操作按钮点击事件
 */
function registerAddOperationClick() {
    $("#add-operation").on("click", function (e) {
        let oper = $("#more-operation");
        oper.removeClass("hide");
        oper.addClass("show");
        $(document).on("click", function (event) {
            oper.removeClass("show");
            oper.addClass("hide");
            // 移除点击事件
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
    ajaxRequest(url, "get", {keyword: keyword}, null, function (res) {
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
                        <p class="search-city">${serUser.enterpriseName}</p></div>
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

function registerTopRightOprClick() {
    // 注册菜单
    registerTopRightOprContextMenu();

    $(".top-right .ic-menu").on("click", function (e) {
        e.preventDefault();
        $('.ic-menu').contextMenu();
    });
}

/**
 * 注册修改信息左键菜单
 */
function registerTopRightOprContextMenu() {
    $.contextMenu({
        selector: '.ic-menu',
        trigger: "none",
        className: "my-context-list",
        callback: function (key, options) {
            let m = "clicked: " + key;
            // logger.info(m, "option:", options) || alert(m);
        },
        items: {
            "modify-phone": {
                name: "修改账号",
                icon: function (opt, $itemElement, itemKey, item) {
                    return "context-menu-icon context-menu-icon-phone"
                },
                callback: function (itemKey, opt, e) {
                    let m = "修改账号 was clicked " + itemKey;
                    // logger.info("itemKey:", itemKey, "option:", opt.$trigger.attr("msg-id"));
                    // openModifyPhoneDialog();
                    myAlert('', "修改账号功能暂未开放", "info");
                }
            },
            "modify-pwd": {
                name: "修改密码",
                icon: function (opt, $itemElement, itemKey, item) {
                    return "context-menu-icon context-menu-icon-pwd"
                },
                callback: function (itemKey, opt, e) {
                    let m = "修改密码 was clicked " + itemKey;
                    // logger.info(m, "option:", opt);
                    openModifyPwdDialog();
                }
            },
            "sep1": "---------",
            "revoke": {
                name: "退出登录",
                icon: function (opt, $itemElement, itemKey, item) {
                    return "context-menu-icon context-menu-icon-logout"
                },
                callback: function (itemKey, opt, e) {
                    let m = "退出登录 was clicked " + itemKey;
                    // logger.info(m, "option:", opt);
                    logout();
                }
            }
        }
    });
}


/**
 * 加载聊天室
 */
function loadChatRoom() {
    $("#chat-box").load("chat-room.html", function (responseTxt, statusTxt, xhr) {
        if (statusTxt === "success") {
            logger.info("外部HTML[chat-room.html]加载成功！");
            //设置默认选中聊天按钮
            setChatButtonSelect();
            //获取聊天室列表
            getChatRoomList();
            //设置表情包hover效果
            setEmojiHover();
            //注册表情包点击事件
            registerNavFaceClick();
            //注册聊天信息页点击事件
            registerNavTopMsgMoreClick();
            //设置内容输入框键盘事件
            contentKeydown();
            //注册聊天信息页粘贴事件
            registerContentOnPaste();
            //注册聊天室图片上传事件
            registerChatMsgPictureChange();
            //注册聊天信息页视频上传事件
            registerChatMsgVideoChange();
            //注册聊天信息页文件上传事件
            registerChatMsgFileChange();
            //初始化聊天信息弹出框
            initChatMsgListDialog();
            //初始化日期选择器
            initDatepicker();
            //初始化转发消息用户弹出框
            initRelayMsgUserDialog();
            //注册聊天室列表右键菜单
            registerChatRoomListContextMenu();
        } else {
            console.error("外部HTML[chat-room.html]加载失败！");
        }
    });
}

/**
 * 设置默认选中聊天按钮
 */
function setChatButtonSelect() {
    $("#file").removeClass("file_select").addClass("file");
    $("#friend").removeClass("friend_select").addClass("friend");
    $("#chat").removeClass("chat").addClass("chat_select");
}

/**
 * 获取聊天列表
 */
function getChatRoomList() {
    let url = `${MSG_URL_PREFIX}/chat/room/list`;
    ajaxRequest(url, "get", {}, null, function (res) {
        if (res.code !== 200) {
            logger.info("获取聊天列表失败");
            myAlert('', res.message, "err");
            return;
        }
        chatRoomListCache = res.data;
        generateChatRoomDom(res.data);
        //初始化转发用户标签页
        initRelayUserTabs();
        $("#chat-list .select").click();
    });
}

function generateChatRoomDom(chatRoomList) {
    let prevChatId = $.cookie(PREV_CHAT_ID_KEY);
    //清空未读消息集合
    unreadChatIdSet.clear();
    let className;
    let chatListDom = $("#chat-list");
    chatListDom.html("");
    for (let i = 0; i < chatRoomList.length; i++) {
        let chatRoom = chatRoomList[i];
        let toUser = chatRoom.toUser;
        if (prevChatId && prevChatId === chatRoom.chatId) {
            className = "list-box select";
        } else {
            className = "list-box";
        }

        if (chatRoom.isTop) {
            className = className + " chat-top";
        }

        let unreadCountHideClass = "";
        if (chatRoom.unreadMsgCount === 0) {
            unreadCountHideClass = "hide";
        } else {//添加到未读消息集合
            unreadChatIdSet.add(chatRoom.chatId);
        }
        let chatRoomDom = `
                <div class="${className}" chat-id="${chatRoom.chatId}" group-id="${chatRoom.groupId}" to-user-id="${toUser.userId}" is-dissolve="${toUser.isDissolve}" to-user-name="${toUser.nickname}" to-user-avatar="${toUser.avatar}" onclick="getChatMsgList(this)">
                    <div class="chat-avatar">
                        <img class="chat-head" src="${toUser.avatar}" alt=""/>
                        <span class="chat-unread ${unreadCountHideClass}" unread-count="${chatRoom.unreadMsgCount}">${chatRoom.unreadMsgCount}</span>
                    </div>
                    <div class="chat-rig">
                        <div class="chat-title">
                            <span class="chat-name">${toUser.nickname ? toUser.nickname : ''}</span>
                            <span class="chat-last-time">${chatRoom.lastTime ? chatRoom.lastTime : ''}</span>
                        </div>
                        <div class="chat-bottom" last-msg-id="${chatRoom.lastMsgId ? chatRoom.lastMsgId : ''}">
                            <span class="chat-last-msg">${chatRoom.lastMsg ? chatRoom.lastMsg : ''}</span>
                        </div>
                    </div>
                </div>
                `;
        chatListDom.append(chatRoomDom);
    }
}

/**
 * 获取聊天消息列表
 * @param _this
 */
function getChatMsgList(_this) {
    let chatId = $(_this).attr("chat-id");
    //缓存1天聊天id
    setCookieDay(PREV_CHAT_ID_KEY, chatId, 1);
    //缓存聊天对象信息
    setChatToUser(_this);
    //下拉分页参数
    msgPage = new Page(15, 0, false);
    //上拉分页参数
    msgUpPage = new Page(15, 0, false);

    let url = `${MSG_URL_PREFIX}/chat/msg/list`;
    let data = {
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
        //缓存总页数
        msgPage.totalPage = res.data;
        //群聊获取群成员信息
        if (chatToUser.groupId && chatToUser.groupId > 0) {
            //缓存群组成员列表
            getChatGroupMemberList(chatToUser.groupId);
            $(".but-nav .call").addClass("hide"); //隐藏视频通话按钮
        } else {
            $(".but-nav .call").removeClass("hide"); //显示视频通话按钮
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
        $(".select").removeClass("select");
        $(_this).addClass("select");
        $("#chat-to-user").html($(_this).attr("to-user-name"));
        //清除聊天消息未读数
        cleanChatMsgUnreadCount(_this);

        if (!avatarTooltipDom) {
            avatarTooltipDom = chatUserAvatarTooltip();
        }
        setIsDissolve();

        setMsgPageLastMsgId(chatMsgList, msgUpPage);
        setMsgPageUpLastMsgId(chatMsgList, msgPage);
        msgUpPage.isLastPage = true;

        // registerChatMsgZoom();
        // 注册聊天消息列表右键菜单
        registerChatMsgListContextMenu();
        //启动上传进度条
        startUploadProgressBar();
        //延时200毫秒,图片加载完成后滚动到底部
        setTimeout(function () {
            let msgContainer = document.getElementsByClassName("container")[0];
            if (!msgContainer || !msgContainer.scrollHeight) {
                return;
            }
            // logger.info("msgContainer.scrollHeight:", msgContainer.scrollHeight, ",height:", $(msgContainer).height());
            $(msgContainer).animate({
                scrollTop: msgContainer.scrollHeight - Math.round($(msgContainer).height()) - 30
            }, 450);//450毫秒秒滑动到指定位置
        }, 250);
    });
}

/**
 * 设置是否解散群聊和是否被移除群聊
 */
function setIsDissolve() {
    if (chatToUser.isDissolve === 'true' || !isGroupMember()) {
        if (chatToUser.isDissolve === 'true') {
            $("#content-input").attr("placeholder", "当前群聊已解散").attr("disabled", true);
            $(".sendto .dissolve-mask").removeClass("hide").text("当前群聊已解散");
        } else {
            $("#content-input").attr("placeholder", "你被移除当前群聊").attr("disabled", true);
            $(".sendto .dissolve-mask").removeClass("hide").text("你被移除当前群聊");
        }
        $("#send-msg-btn").attr("disabled", true);
        $("#msg-more").addClass("hide");
    } else {
        $("#content-input").attr("placeholder", "请输入消息内容，按Enter键或发送按钮发送，按Ctrl+Enter换行").removeAttr("disabled").val("");
        $("#send-msg-btn").removeAttr("disabled");
        $(".sendto .dissolve-mask").addClass("hide");
        $("#msg-more").removeClass("hide");
    }
}

function setChatToUser(chatDom) {
    chatToUser = new ChatToUser($(chatDom).attr("chat-id"), $(chatDom).attr("to-user-id"), $(chatDom).attr("group-id"), $(chatDom).attr("to-user-name"), $(chatDom).attr("to-user-avatar"));
    chatToUser.isDissolve = $(chatDom).attr("is-dissolve");
}


function cleanChatMsgUnreadCount(_this) {
    let unreadCountDom = $(_this).find(".chat-unread");
    unreadCountDom.addClass("hide").attr('unread-count', 0).text("0");

    let totalUnreadCountDom = $("#top-chat-unread");
    if (unreadChatIdSet.size === 0) {
        totalUnreadCountDom.addClass("hide");
    }
    totalUnreadCountDom.attr("unread-count", unreadChatIdSet.size).text(unreadChatIdSet.size);
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
        return "";
    }
    let msgObj = msgParseJson(chatMsg);
    return `
            <li class="${user.msgClass}">
                <div class="avatar" user-id="${user.id}" ><img src="${user.avatar}" alt=""></div>
                <div class="msg">
                    <div class="msg-name">${user.nickname}</div>
                    <div class="msg-text-outer">
                        <span class="msg-arrow"></span>
                        <span class="msg-text" msg-id="${chatMsg.id}" msg-type="${chatMsg.msgType}" file-name="${msgObj.fileName}" new-file-name="${msgObj.newFileName}" file-url="${msgObj.fileUrl}" > ${msgDom}</span>
                    </div>
                    <time>${chatMsg.sendTime}</time>
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
    let msgDom = `<img class="msg-img" src="${msgObj.fileUrl}" alt="" onclick="chatMsgImgZoomDom(this)" onerror="this.src='../ico/pic_error.png'">`;
    if (msgObj.status === 0) {
        msgDom = `<img class="msg-img-loading" src="../ico/loading.gif" alt="">`;
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
                               <img class="msg-video-cover" src="${msgObj.coverUrl}" alt="" onerror="this.src='../ico/pic_error.png'">
                                <i class="msg-video-play-icon" data-url="${msgObj.fileUrl}" onclick="playVideo(this)" ></i>
                         </div>`;
    if (msgObj.status === 0) {
        msgDom = `<div class="msg-video-outer">
                        <img class="msg-img-loading" src="../ico/loading.gif" alt="">
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

let avatarTooltipDom;

function chatUserAvatarTooltip() {
    return $(document).tooltip({
        items: ".avatar,.msg-card-wrap",
        content: function () {
            let msgType = $(this).parent().attr("msg-type");
            if (msgType === MsgType.BIZ_CARD) {
                return getChatUserInfoTooltipDom(this);
            } else if (msgType === MsgType.GROUP_BIZ_CARD) {
                return getGroupCardInfoTooltipDom(this);
            }
            return getChatUserInfoTooltipDom(this);
        },
        hide: {effect: "fade", duration: 1750}
    });
}

function getChatUserInfoTooltipDom(_this) {
    let userId = $(_this).attr("user-id");
    let chatMsgKey = "chat_u_" + chatToUser.chatId + "_" + userId;
    let chatUserInfo = $.cookie(chatMsgKey);
    if (chatUserInfo) {
        let userInfo = JSON.parse(chatUserInfo);
        return generateChatUserInfTooltipDom(userInfo);
    }
    let userInfoDom = ``;
    getChatUserInfo(userId, function (userInfo) {
        //缓存用户信息30分钟
        setCookie(chatMsgKey, JSON.stringify(userInfo), 45);
        userInfoDom = generateChatUserInfTooltipDom(userInfo);
    });
    return userInfoDom;
}

function generateChatUserInfTooltipDom(userInfo) {
    let sourceDom = getSourceDom(userInfo);
    let bottomDom = getUserInfoBottomDom(userInfo);
    return `
            <div class="uset-info-tooltip" >
                <div class="user-head">
                    <img class="user-avatar" src="${userInfo.avatar}" alt="头像"/>
                     <div class="user-name-info">
                        <span class="user-name">${userInfo.remark ? userInfo.remark : userInfo.nickname}</span>
                        <span class="user-remark">昵称: ${userInfo.nickname}</span>
                        <span class="user-remark">公司: ${userInfo.enterpriseName}</span>
                    </div>
                </div>
                ${sourceDom}
                <div class="user-bottom">
                    ${bottomDom}
                </div>
            </div>
        `;
}

function getUserInfoBottomDom(userInfo) {
    let bottomDom = ``;
    if (userInfo.isFriend) {
        bottomDom = `<span class="send-msg-btn" user-id="${userInfo.userId}" onclick="sendMsgBtnClick(${userInfo.userId})">发消息</span>`;
    } else {
        bottomDom = `<span class="send-msg-btn" user-id="${userInfo.userId}" nickname="${userInfo.remark ? userInfo.remark : userInfo.nickname}" avatar="${userInfo.avatar}" enterprise-name="${userInfo.enterpriseName}" onclick="addFriendApplyClick(this,'${FRIEND_SOURCE.GROUP}')">添加好友</span>`;
    }
    return bottomDom;
}

function getSourceDom(userInfo) {
    if (!userInfo.isFriend) {
        return ``;
    }
    let groupNicknameDom = getGroupNicknameDom(userInfo);
    if (userInfo.userId === chatUser.id) {
        return `<div class="friend-source">
                    ${groupNicknameDom}
                </div>`;
    }
    return `
            <div class="friend-source">
                ${groupNicknameDom}
                <div class="more-info"><label>备注:</label><span class="friend-remark">${userInfo.remark ? userInfo.remark : '-'}</span></div>
                <div class="more-info">
                    <label>好友来源:</label><span>${userInfo.sourceDesc ? userInfo.sourceDesc : '-'}</span>
                </div>
            </div>`;
}

function getGroupNicknameDom(userInfo) {
    let groupNicknameDom = ``;
    // 设置群昵称
    if (parseInt(chatToUser.groupId) > 0 && chatToUser.chatInfo
        && (chatToUser.chatInfo.groupInfo.isGroupLeader || chatToUser.chatInfo.groupInfo.isGroupManager)
        || (parseInt(chatToUser.groupId) > 0 && userInfo.userId === chatUser.id)) {
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
                if (groupMember.sourceDesc) {
                    groupNicknameDom = groupNicknameDom + `
                            <div class="more-info">
                                <label>群来源:</label><span>${groupMember.sourceDesc}</span>
                            </div>`;
                }
                break;
            }
        }
    }
    return groupNicknameDom;
}

function getGroupCardInfoTooltipDom(_this) {
    let chatId = $(_this).attr("user-id");
    let isMyChat = isMyChatRoom(chatId);
    let bottomDom = ``;
    if (isMyChat) {
        bottomDom = `<span class="send-msg-btn" onclick="gotoGroupMsg('${chatId}')">发消息</span>`;
    } else {
        bottomDom = `<span class="send-msg-btn" onclick="applyGroup('${chatId}')">申请入群</span>`;
    }
    return `
            <div class="uset-info-tooltip" >
                <div class="user-head">
                    <img class="user-avatar" src="${$(_this).find('.msg-card-avatar').attr('src')}" alt="头像"/>
                     <div class="user-name-info">
                        <span class="user-name">${$(_this).find('.msg-card-nickname').text()}</span>
                        <span class="user-remark">群ID: ${chatId}</span>
                    </div>
                </div>
                <div class="user-bottom">
                    ${bottomDom}
                </div>
            </div>
        `;
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
        emojiUl.append('<li class="emoji" data-emoji="' + emoji + '" onclick="emojiClick(this)">' + emoji + '</li>');
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
        if (!checkToUser()) {
            return;
        }
        selectEmojis(null, 'SmileysEmotion');
        $(".emoji-box-outer").show();
        $(document).on("click", function (event) {
            if (event.target.className === "emojiSelected" || event.target.className === "emoji-box-bottom") {
                return;
            }
            $(".emoji-box-outer").hide();
            $(".emoji-box-bottom img").removeClass("emojiSelected");
            // 移除点击事件
            $(document).off("click");
        });
        e.stopPropagation();
    });
}

function emojiClick(_this) {
    $("#content-input").insertAtCaret($(_this).attr("data-emoji"));
}

function contentKeydown() {
    $("#content-input").on('keydown', function (event) {
        // logger.info("code:", event.keyCode, 'key:', event.key, 'ctrlKey:', event.ctrlKey)
        //监听特定的按键
        if (event.keyCode === 13) {
            if (event.ctrlKey) { // ctrl+Enter 换行
                $(this).insertAtCaret('\n');
            } else { // Enter 发送消息
                sendMsg();
            }
        }
    });
}

/**
 * 注册聊天图片粘贴
 */
function registerContentOnPaste() {
    $("#content-input").on('paste', function (event) {
        let items = (event.clipboardData || event.originalEvent.clipboardData).items;
        for (let index in items) {
            let item = items[index];
            if (item.kind === 'file') {
                let file = item.getAsFile();
                uploadImageFileAsync(file, "chat-msg", function (res) {
                    sendObjMsg(res, MsgType.IMAGE);
                });
            }
        }
    });
}

/**
 * 注册聊天图片上传
 */
function registerChatMsgPictureChange() {
    $('#imageInput').on('change', function (e) {
        let file = e.target.files[0];
        if (!file) {
            return;
        }
        uploadAndSendImageFile(file);
    });
}

/**
 * 注册聊天视频上传
 */
function registerChatMsgVideoChange() {
    $('#videoInput').on('change', function (e) {
        let file = e.target.files[0];
        if (!file) {
            return;
        }
        uploadAndSendVideoFile(file);
    });
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
    });
}

/**
 * 选择发送图片
 */
function chatMsgPictureClick() {
    if (!checkToUser()) {
        return;
    }
    $("#imageInput").click();
}

/**
 * 选择发送视频
 */
function chatMsgVideoClick() {
    if (!checkToUser()) {
        return;
    }
    $("#videoInput").click();
}

/**
 * 选择发送文件
 */
function chatMsgFileClick() {
    if (!checkToUser()) {
        return;
    }
    $("#fileInput").click();
}

/**
 * 开启截屏插件
 */
function changeScreenShot() {
    if (!checkToUser()) {
        return;
    }
    new screenShotPlugin({
        enableWebRtc: true, //是否启用webrtc，值为boolean类型，值为false则使用html2canvas来截图
        clickCutFullScreen: true, //单击截全屏启用状态,值为boolean类型， 默认为false
        wrcWindowMode: false, //是否启用窗口截图模式，值为boolean类型，默认为false，即当前标签页截图。如果标签页截图的内容有滚动条或者底部有空缺，可以考虑启用此模式。
        useRatioArrow: true, //是否使用等比例箭头, 默认为false(递增变粗的箭头)。
        completeCallback: function (shotObj, cutInfo) {
            // logger.info(shotObj.base64, cutInfo);
            let filename = "screen-shot-" + new Date().getTime() + ".png";
            let file = base64ToFile(shotObj.base64, filename);
            uploadAndSendImageFile(file);
        },
        closeCallback: function () {
            logger.info("截图窗口关闭");
        }
    });
}

function checkToUser() {
    if (!chatToUser || chatToUser.isDissolve === "true") {
        return false;
    }
    return true;
}

/**
 * 注册聊天历史消息图片缩放
 */
function registerChatMsgHisZoom() {
    $('.search-msg-item-msg .msg-text .msg-img').off('click').click(function () {
        $(".chat-msg-modal").remove();
        let src = $(this).attr('src');
        let alt = $(this).attr('alt');
        let modal = $('<div class="chat-msg-modal">');
        let modalImg = $('<img class="modal-img">');
        let caption = $('<div class="caption">');

        modalImg.attr('src', src);
        modalImg.attr('alt', alt);

        caption.text(alt);

        modal.append(modalImg);
        modal.append(caption);

        $('body').append(modal);

        modal.click(function () {
            modalImg.draggable("destroy");
            modal.remove();
        });

        imgScale(modalImg);
    });
}

/**
 * 获取聊天消息上拉
 */
function getChatMsgPageUpList() {
    let chatId = chatToUser.chatId;
    let url = `${MSG_URL_PREFIX}/chat/msg/list`;
    let data = {
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
            // 更新菜单
            refreshContextMenu();
            // 关闭聊天记录弹窗
            $("#chat-msg-history-dialog").dialog("close");
        }
        startUploadProgressBar();
    });
}

function nextPageMsgRecord(_this) {
    let msgContainer = document.getElementsByClassName("container")[0];
    let scrollTop = $(_this).scrollTop();
    // logger.info("height:", Math.round($(msgContainer).height()), "scrollTop:", scrollTop, "scrollHeight:", msgContainer.scrollHeight,
    //     (Math.round($(msgContainer).height()) + Math.round(scrollTop) === msgContainer.scrollHeight));

    if (scrollTop === 0) {
        if (msgPage.isLastPage) {
            logger.info("下拉加载已经是最后一页了");
            return;
        }
        pageDownMsgRecord();
    } else if (Math.round($(msgContainer).height()) + Math.round(scrollTop) === msgContainer.scrollHeight) {
        if (msgUpPage.isLastPage) {
            logger.info("上拉加载已经是最后一页了");
            return;
        }
        let lastMsgId = msgUpPage.lastMsgId;
        chatToUser.pageFlippingType = PAGE_FLIPPING_TYPE.PULL_UP;
        getChatMsgPageUpList();
        $(msgContainer).animate({
            scrollTop: scrollTop + 50
        }, 450);//450毫秒秒滑动到指定位置
    }
}


function pageDownMsgRecord() {
    let msgContainer = document.getElementsByClassName("container")[0];
    let scrollHeight = msgContainer.scrollHeight;
    let chatId = chatToUser.chatId
    let url = `${MSG_URL_PREFIX}/chat/msg/list`;
    let data = {
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
        // 启动进度条
        startUploadProgressBar();
        // 更新菜单
        refreshContextMenu();
        msgContainer.scrollTop = msgContainer.scrollHeight - scrollHeight - 30;
        // $(msgContainer).animate({
        //     scrollTop: msgContainer.scrollHeight - scrollHeight - 30
        // }, 450);//450毫秒秒滑动到指定位置
    });
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


//------------------------------------------------------------聊天信息详情页---------------------------------------------------------

/**
 * 注册聊天信息页点击事件
 */
function registerNavTopMsgMoreClick() {
    $("#msg-more").on("click", function (e) {
        let chatMsgInfoDom = $("#chat-msg-info");
        chatMsgInfoDom.removeClass("hide");
        loadMsgInfoPage();
        $(document).on("click", function (event) {
            let className = event.target.className;
            // logger.info("className:", className);
            if (className) {
                let classNames = className.split(" ");
                for (let i = 0; i < classNames.length; i++) {
                    let claName = "." + classNames[i];
                    if (claName === ".ui-widget-overlay") { // 点击遮罩层
                        return;
                    }
                    if (chatMsgInfoDom.find(claName).length > 0 || claName === ".chat-msg-info") {
                        return;
                    }
                    let $modifyGroupNameDia = $("#modify-group-name-dialog");
                    if ($modifyGroupNameDia.find(claName).length > 0 || $modifyGroupNameDia.parent().find(claName).length > 0) {
                        return;
                    }
                    let $groupQrcodeDia = $("#group-qrcode-dialog");
                    if ($groupQrcodeDia.find(claName).length > 0 || $groupQrcodeDia.parent().find(claName).length > 0) {
                        return;
                    }
                    let $groupNoticeTextBoxDia = $("#group-notice-textbox-dialog");
                    if ($groupNoticeTextBoxDia.find(claName).length > 0 || $groupNoticeTextBoxDia.parent().find(claName).length > 0) {
                        return;
                    }
                    let $groupManagersDia = $("#group-managers-dialog");
                    if ($groupManagersDia.find(claName).length > 0 || $groupManagersDia.parent().find(claName).length > 0) {
                        return;
                    }
                    let $addRemoveGroupMemberDia = $("#add-remove-group-member-dialog");
                    if ($addRemoveGroupMemberDia.find(claName).length > 0 || $addRemoveGroupMemberDia.parent().find(claName).length > 0) {
                        return;
                    }
                    let $addRemoveGroupManagerDia = $("#add-remove-group-manager-dialog");
                    if ($addRemoveGroupManagerDia.find(claName).length > 0 || $addRemoveGroupManagerDia.parent().find(claName).length > 0) {
                        return;
                    }
                    let $chatMsgHistoryDia = $("#chat-msg-history-dialog");
                    if ($chatMsgHistoryDia.find(claName).length > 0 || $chatMsgHistoryDia.parent().find(claName).length > 0) {
                        return;
                    }
                    let relayMsgUserDia = $("#relay-msg-user-dialog");
                    if (relayMsgUserDia.find(claName).length > 0 || relayMsgUserDia.parent().find(claName).length > 0) {
                        return;
                    }
                    let $dialogConfirm = $("#dialog-confirm");
                    if ($dialogConfirm.find(claName).length > 0 || $dialogConfirm.parent().find(claName).length > 0) {
                        return;
                    }
                }
            }
            chatMsgInfoDom.addClass("hide");
            $(".group-notice-btn .edit-desc").addClass("hide");
            $("#group-notice").text("");
            $("#group-name").text("");
            $("#group-nickname").text("");
            // 移除点击事件
            $(document).off("click");
        });
        e.stopPropagation();
    });
}

/**
 * 加载聊天信息页面
 */
function loadMsgInfoPage() {
    $(".group-member").html("");
    getChatRoomMsgInfo();
    initAddSubGroupMemberDialog();
    if (parseInt(chatToUser.groupId) > 0) {
        $("#group-base-info").removeClass("hide");
        $(".logout-group").removeClass("hide");
        initGroupQrcodeDialog();
        initGroupNameDialog();
        initAddRemoveGroupManagerDialog();
        initGroupLeaderTransferDialog();
        initGroupNoticeTextboxDialog();
        instantiateTextbox();
        initGroupManagersDialog();
    } else {
        gotoChatMsgInfo();
        $(".group-member-more").addClass("hide");
        $("#group-base-info").addClass("hide");
        $(".logout-group").addClass("hide");
    }
    if (!avatarTooltipDom) {
        avatarTooltipDom = chatUserAvatarTooltip();
    }
}

function gotoChatMsgInfo() {
    let groupMDom = `
                     <li class="avatar" user-id="${chatToUser.id}" action="msg-more-info">
                        <div class="member-head">
                            <img class="member-avatar avatar" user-id="${chatToUser.id}" src="${chatToUser.avatar}" alt=""/>
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
    ajaxSyncRequest(url, "get", {chatId: chatToUser.chatId}, null, function (res) {
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
        let className = "top-member";
        if (i > 15) { // 超过15个隐藏
            className = "more hide";
            $(".group-member-more").removeClass("hide");
        }
        let groupMDom = `
                 <li class="${className} avatar" user-id="${groupMember.userId}" action="msg-more-info">
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
    if ($(_this).find(".text").text() === "展开更多") {
        $(_this).find(".text").text("收起");
        $(_this).find(".arrow-down").removeClass("arrow-down").addClass("arrow-up");
        $(".group-member li.more").removeClass("hide");
    } else {
        $(_this).find(".text").text("展开更多");
        $(_this).find(".arrow-up").removeClass("arrow-up").addClass("arrow-down");
        $(".group-member li.more").addClass("hide");
    }
}

/**
 * 群二维码弹框打开
 */
function groupQrcodeDialogOpen() {
    let url = `${MSG_URL_PREFIX}/chat/group/getGroupQrcode`;
    ajaxRequest(url, "get", {groupId: chatToUser.groupId}, null, function (res) {
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


/**
 * 初始化群公告弹框
 */
function initGroupNoticeTextboxDialog() {
    $("#group-notice-textbox-dialog").dialog({
        title: "群公告",
        autoOpen: false,   // 是否自动弹出窗口
        modal: true,    // 设置为模态对话框
        resizable: false,
        width: 520,   //弹出框宽度
        height: 720,   //弹出框高度
        close: function (event, ui) {
            cancelEditGroupNotice();
        }
    });
}

function groupNoticeTextboxDialogOpen() {
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
        $(".group-notice-content .no-content").removeClass("hide");
    }
    //隐藏编辑按钮
    if (!chatToUser.chatInfo.groupInfo.isGroupLeader && !chatToUser.chatInfo.groupInfo.isGroupManager) {
        $(".group-notice-btn .primarybtn").addClass("hide");
        $(".group-notice-btn .edit-desc").removeClass("hide");
    }
    setNoticeEditorContent(noticeContent);
    $("#group-notice-textbox-dialog").dialog("open");
}

function setNoticeEditorContent(noticeContent) {
    noticeEditor.content.set(noticeContent);
}

function editGroupNotice(_this) {
    $(".group-notice-btn button").removeClass("hide");
    $(_this).addClass("hide");
    $(".ephox-polish-layer-above-editor").removeClass("hide");
    $($("#ephox_textbox-notice iframe")[0].contentWindow.document).find("body").attr("contenteditable", "true");
}

function cancelEditGroupNotice() {
    setNoticeContent();
    $(".group-notice-btn button").addClass("hide");
    $(".group-notice-btn .edit").removeClass("hide");
    $(".group-notice-btn .save").attr("disabled", true);
    $(".ephox-polish-layer-above-editor").addClass("hide");
    $($("#ephox_textbox-notice iframe")[0].contentWindow.document).find("body").attr("contenteditable", "false");
}

function setNoticeContent() {
    let noticeContent = '';
    if (chatToUser.chatInfo.groupInfo.notice) {
        let noticeJson = JSON.parse(chatToUser.chatInfo.groupInfo.notice);
        noticeContent = noticeJson.content;
    }
    setNoticeEditorContent(noticeContent);
}

/**
 * 富文本编辑器对象
 */
let noticeEditor;

/**
 * 初始化富文本编辑器
 */
function instantiateTextbox() {
    if (noticeEditor) {
        return;
    }
    textboxio.replace('#textbox-notice', {
        paste: {
            style: 'clean'
        },
        css: {
            stylesheets: ['../textboxio/example.css']
        },
        codeview: {
            showButton: false	// Hides the code view button, default is true (shown)
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
    noticeEditor.events.loaded.addListener(textboxLoadedEvent);
    noticeEditor.events.dirty.addListener(textboxContentChangeEvent);
    noticeEditor.events.focus.addListener(textboxFocusContentChangeEvent);
}

function textboxLoadedEvent() {
    // logger.info('this editor has loaded.');
    // 禁止编辑
    $($("#ephox_textbox-notice iframe")[0].contentWindow.document).find("body").attr("contenteditable", "false");
    $(".ephox-polish-layer-above-editor").addClass("hide");
}

function textboxFocusContentChangeEvent() {
    // logger.info('this editor has focus.', new Date());
    if (noticeEditor.content.isDirty()) {
        textboxContentChangeEvent();
    }
}

function textboxContentChangeEvent() {
    // logger.info('Editor content now dirty.', new Date());
    let noticeContent = '';
    if (chatToUser.chatInfo.groupInfo.notice) {
        let noticeJson = JSON.parse(chatToUser.chatInfo.groupInfo.notice);
        noticeContent = noticeJson.content;
    }
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
        $(".group-notice-btn .save").attr("disabled", true);
    } else {
        noticeEditor.content.setDirty(true);
    }
    if (noticeEditor.content.isDirty()) {
        $(".group-notice-btn .save").removeAttr("disabled");
    }
}


function getNoticeContent() {
    return noticeEditor.content.get();
}

function saveGroupNotice() {
    let noticeContent = getNoticeContent();
    let textboxBlankContent = "<i>空</i>";
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
    let url = `${MSG_URL_PREFIX}/chat/group/updateGroupNotice`;
    ajaxRequest(url, "post", {
        groupId: chatToUser.groupId,
        notice: noticeContent
    }, null, function (res) {
        if (res.code !== 200) {
            logger.info("更新公告失败,groupId:", chatToUser.groupId, "res:", res);
            myAlert('', res.message, "err");
            return;
        }
        $("#group-notice-textbox-dialog").dialog("close");
    });
}

/**
 * 初始化群管理对话框
 */
function initGroupManagersDialog() {
    $("#group-managers-dialog").dialog({
        title: "群管理",
        autoOpen: false,   // 是否自动弹出窗口
        modal: true,    // 设置为模态对话框
        resizable: false,
        width: 370,   //弹出框宽度
        height: 360,   //弹出框高度
        close: function (event, ui) {

        }
    });
}

/**
 * 打开群管理对话框
 */
function groupManagersDialogOpen() {
    let inviteSwitch = "switch-off";
    //设置群聊邀请确认标志
    if (chatToUser.chatInfo.groupInfo.inviteCfm) {
        inviteSwitch = "switch-on";
    }
    $("#invite-cfm").attr("class", inviteSwitch);

    setGroupManagersHtml();
    $("#group-managers-dialog").dialog("open");
}

function setGroupManagersHtml() {
    let managersDom = generateGroupManagerDom();
    $(".group-manager-low .group-managers").html("").append(managersDom);
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
                     <li class="top" user-id="${groupMember.userId}" onclick="groupManagerClick(this)">
                        <img class="manager-avatar" user-id="${groupMember.userId}" src="${groupMember.avatar}" alt=""/>
                        <div class="manager-nickname" user-id="${groupMember.userId}" >${groupMember.nickname}</div>
                        <span class="manager-remove" style="display: none;" user-id="${groupMember.userId}" nickname="${groupMember.nickname}" onclick="removeGroupManager(this)">移除</span>
                    </li>
                    `;
                }
            }
        }
    }
    return managersDom;
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

function addRemoveGroupMember() {
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

/**
 * 退出群聊
 */
function logoutGroup() {
    myConfirm('确定退出群聊?', "退出群聊后将会清空聊天记录,确定退出吗", function () {
        let url = `${MSG_URL_PREFIX}/chat/group/logoutGroup`;
        ajaxRequest(url, "post", {
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
            setGroupManagersHtml();
        }
    });
}

function groupManagerOperationDialogOpen() {
    searchGroupMemberClick();
    let managersDom = generateGroupManagerDom();
    if (managersDom === "") {
        $("#group-managers").addClass("hide");
    } else {
        $("#group-managers").html("").append(managersDom).focus();
    }
    $("#add-remove-group-manager-dialog").dialog("open");
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

function groupManagerClick(_this) {
    let liDom = $(_this).find(".manager-remove");
    //显示
    liDom.slideDown(500);
    setTimeout(function () {
        //隐藏
        liDom.slideUp(500);
    }, 2500);
}

function removeGroupManager(_this) {
    let nickname = $(_this).attr("nickname");
    let userId = $(_this).attr("user-id");
    myConfirm('确定移除群管理员吗?', "确定要移除“" + nickname + "”管理员吗?", function () {
        let url = `${MSG_URL_PREFIX}/chat/group/removeGroupManager`;
        ajaxRequest(url, "post", {
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
            $(_this).parent().remove();
        });
    });
}

function dissolveGroup() {
    myConfirm('解散群聊', "解散群聊后，群成员和群主都将被移出群聊，确认解散吗？", function () {
        let url = `${MSG_URL_PREFIX}/chat/group/dissolveGroup`;
        ajaxRequest(url, "post", {
            groupId: chatToUser.groupId
        }, null, function (res) {
            if (res.code !== 200) {
                logger.info("解散群聊失败,groupId:", chatToUser.groupId);
                myAlert('', res.message, "err");
                return;
            }
            $("#group-managers-dialog").dialog("close");
            loadChatRoom();
        });
    });
}

/**
 * 清空聊天记录
 */
function cleanMsgList() {
    let nickname = "和" + chatToUser.nickname;
    if (parseInt(chatToUser.groupId) > 0) {
        nickname = "群"
    }
    myConfirm('清空聊天记录', "确定删除" + nickname + "的聊天记录吗?", function () {
        let url = `${MSG_URL_PREFIX}/chat/room/cleanMsgList`;
        ajaxRequest(url, "post", {
            chatId: chatToUser.chatId,
        }, null, function (res) {
            if (res.code !== 200) {
                logger.info("清空聊天记录失败,groupId:", chatToUser.groupId);
                myAlert('', res.message, "err");
                return;
            }
            logger.info("清空聊天记录成功,chatId:", chatToUser.chatId);
            $("#chat-msg-list").html("");
        });
    });
}

/**
 * 初始化群管理对话框
 */
function initChatMsgListDialog() {
    $("#chat-msg-history-dialog").dialog({
        title: "聊天记录",
        autoOpen: false,   // 是否自动弹出窗口
        modal: true,    // 设置为模态对话框
        resizable: false,
        width: 450,   //弹出框宽度
        height: 660,   //弹出框高度
        close: function (event, ui) {
            $(".msg-type-item").removeClass("active").first().addClass("active");
            $('.search-msg-item-msg .msg-text .msg-img').off("click");
            $("#search-msg-list").html("");
        }
    });
}

function openChatMsgHistoryDialog() {
    if (!chatToUser) {
        return;
    }
    let title = "聊天记录";
    if (parseInt(chatToUser.groupId) > 0) {
        title = "“" + chatToUser.nickname + "”的聊天记录"
        $("#msg-group-member-select").parent().removeClass("hide");
        initGroupMemberSelectOption();
    } else {
        title = "与“" + chatToUser.nickname + "”的聊天记录"
        $("#msg-group-member-select").parent().addClass("hide");
    }
    searchMsgTypeClick($(".active"));
    $("#chat-msg-history-dialog").dialog({title: title}).dialog("open");
}

/**
 * 初始化日期选择器
 */
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
    let msgGroupMemberSelect = $("#msg-group-member-select");
    msgGroupMemberSelect.empty();
    msgGroupMemberSelect.append(`<option value="0" data-class="avatar-a">选择群成员</option>`);
    let groupMemberList = getLocalGroupMemberList();
    if (groupMemberList) {
        for (let i = 0; i < groupMemberList.length; i++) {
            let member = groupMemberList[i];
            let avatar = member.avatar;
            let optionHtml = `<option value="${member.userId}" data-class="avatar-a" data-style="background-image: url('${member.avatar}'); background-size: 100%;">
                            ${member.nickname}
                        </option>`
            msgGroupMemberSelect.append(optionHtml);
        }
    }

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
        .addClass("ui-menu-icons avatar-a");
    msgGroupMemberSelect.iconselectmenu("refresh");
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
        chatId: chatToUser.chatId,
        msgType: msgType,
        sendUserId: sendUserId,
        sendDate: sendDate,
        keywords: keywords,
        pageFlippingType: PAGE_FLIPPING_TYPE.HIS_PULL_UP,
        msgId: hisMsgPage.lastMsgId,
        limit: hisMsgPage.limit
    };
    ajaxRequest(url, "get", data, null, function (res) {
        if (res.code !== 200) {
            logger.info("获取聊天记录列表失败,chatId:", chatToUser.chatId, "res:", res);
            myAlert('', res.message, "err");
            return;
        }
        //缓存总页数
        let chatMsgList = res.data;
        setMsgPageLastMsgId(chatMsgList, hisMsgPage);
        generateMsgHistoryDom(searchMsgListDom, chatMsgList);
        registerChatMsgListHisContextMenu();
        registerChatMsgHisZoom();
        startUploadProgressBar();
    });
}

function generateMsgHistoryDom(searchMsgListDom, chatMsgList) {
    for (let i = 0; i < chatMsgList.length; i++) {
        let chatMsg = chatMsgList[i];
        let msgDom = getMsgDom(chatMsg);
        if (msgDom === "") {
            continue;
        }
        let msgObj = msgParseJson(chatMsg);
        let chatMsgDom = `
             <li class="search-msg-item">
                <div class="search-msg-item-left">
                    <div class="search-msg-item-avatar">
                        <img class="avatar-a" user-id="${chatMsg.sendUserId}" src="${chatMsg.avatar}" alt="头像"/>
                    </div>
                </div>
                <div class="search-msg-item-right" msg-id="${chatMsg.id}" msg-type="${chatMsg.msgType}" file-name="${msgObj.fileName}" file-url="${msgObj.fileUrl}">
                    <div class="search-msg-item-nickname">
                        <span class="nickname">${chatMsg.nickname}</span>
                        <span class="date">${chatMsg.sendTime.length > 10 ? chatMsg.sendTime.substring(0, 10) : chatMsg.sendTime}</span>
                    </div>
                    <div class="search-msg-item-msg">
                        <span class="msg-text"  msg-id="${chatMsg.id}" msg-type="${chatMsg.msgType}" file-name="${msgObj.fileName}" new-file-name="${msgObj.newFileName}" file-url="${msgObj.fileUrl}">${msgDom}</span>
                    </div>
                </div>
            </li>
            `;
        searchMsgListDom.append(chatMsgDom);
    }
}

function msgHistLocate(msgId) {
    msgUpPage.lastMsgId = msgId;
    let lastMsgId = msgId;
    chatToUser.pageFlippingType = PAGE_FLIPPING_TYPE.LOCATE;
    getChatMsgPageUpList();
    setTimeout(function () {
        let $msgContainer = $(".container");
        let msgOffsetTop = $(".msg-text-outer .msg-text[msg-id=" + lastMsgId + "]").offset().top;
        let msgContainerOffsetTop = $msgContainer.offset().top;
        let msgContainerScrollTop = $msgContainer.scrollTop();
        // logger.info("msgOffsetTop:", msgOffsetTop, "lastMsgId:", lastMsgId, "msgContainerOffsetTop:", msgContainerOffsetTop,
        //     "msgContainerScrollTop:", msgContainerScrollTop, "scroll-to:", (msgOffsetTop - msgContainerOffsetTop + msgContainerScrollTop - 20));
        $msgContainer.animate({
            scrollTop: msgOffsetTop - msgContainerOffsetTop + msgContainerScrollTop - 20
        }, 450);//450毫秒秒滑动到指定位置
    }, 250);

}

function searchDateClick(_this) {
    $(".msg-type-item").removeClass("active");
    $(_this).addClass("active");

    $("#msg-datepicker").datepicker("show");
}

function searchMsgHistoryListScroll(_this) {
    let scrollTop = $(_this).scrollTop();
    let msgContainer = document.getElementById("search-msg-list");
    // logger.info("height:", $(msgContainer).height(), "scrollTop:", scrollTop, "scrollHeight:", msgContainer.scrollHeight,
    //     (Math.round($(msgContainer).height()) + Math.round(scrollTop) === msgContainer.scrollHeight));

    if (Math.round($(msgContainer).height()) + Math.round(scrollTop) === msgContainer.scrollHeight) {
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
            getRelayChatRoomList();
        }
    });
}

function searchRelayUserList() {
    let tabId = $("#relay-user-tabs li[aria-selected='true']").attr("aria-controls");
    searchRelayUserList1(tabId);
}

function searchRelayUserList1(tabId) {
    // 清空选中
    selectedFriendIdSet.clear();
    $(".replay-msg-btn").attr("disabled", true);

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
    $(".replay-msg-btn").attr("msg-id", msgId).attr("action", "relay").text("转发");
    $("#relay-msg-user-dialog").dialog({title: "转发消息"}).dialog("open");
}

function openSendCardMsgDialog() {
    if (!checkToUser()) {
        return;
    }
    $(".relay-msg-user .primarybtn").click();
    $(".replay-msg-btn").attr("action", "card").text("发送");
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
    // 发送对象消息
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

//-------------------------------------------------------加载好友页面-------------------------------------------------------
/**
 * 加载好友页面
 */
function loadChatFriend() {
    chatToUser = null;
    $("#chat").removeClass("chat_select").addClass("chat");
    $("#file").removeClass("file_select").addClass("file");
    $("#friend").removeClass("friend").addClass("friend_select");
    $("#chat-box").load("chat-friend.html", function (responseTxt, statusTxt, xhr) {
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

        } else {
            console.error("外部HTML[chat-friend.html]加载失败！");
        }
    });
}

function initAgreeFriendApplyDialog() {
    $("#agree-friend-apply-dialog").dialog({
        title: "同意好友申请",
        autoOpen: false,   // 是否自动弹出窗口
        modal: true,    // 设置为模态对话框
        resizable: false,
        width: 375,   //弹出框宽度
        height: 350,   //弹出框高度
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
    ajaxRequest(url, "get", {}, null, function (res) {
        if (res.code !== 200) {
            logger.info("获取好友申请列表失败", "res:", res);
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
            let statusObj = getStatusInfo(friendApply, '');
            let friendApplyDom = `
                <div class="apply-info" apply-id="${friendApply.id}" nickname="${friendApply.nickname}" onclick="getFriendApplyInfo(this);">
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

function getFriendApplyInfo(_this) {
    $(".apply-info").removeClass("select");
    $(".friend-info").removeClass("select");
    $(_this).addClass("select");

    let applyId = $(_this).attr("apply-id");
    let url = `${MSG_URL_PREFIX}/chat/friend/apply/info`;
    ajaxRequest(url, "get", {applyId: applyId}, null, function (res) {
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
                    <span class="agree-btn ${statusObj.agreeBtnClass}" apply-id="${applyInfo.id}" nickname="${applyInfo.nickname}" avatar="${applyInfo.avatar}" enterprise-name="${applyInfo.enterpriseName}" onclick="agreeFriendApplyDialogOpen(this)" >通过好友验证</span>
                </div>
            </div>
        `;
        $("#friend-apply-info").html("").append(infoDom);
        //隐藏未读消息
        updateTotalUnreadCount(_this);
        //注册好友申请消息回复按钮点击事件
        registerFriendApplyMsgReplayClick();
        //注册好友申请消息回复输入框keydown事件
        applyReplayContentKeydown();
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
    let data = JSON.stringify({applyId: applyId, replayContent: content});
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
        let chatId = res.data;
        //缓存1天聊天id
        setCookieDay(PREV_CHAT_ID_KEY, chatId, 1);
        loadChatRoom();
    });
}


/**
 * 获取好友列表
 */
function getFriendList() {
    let url = `${MSG_URL_PREFIX}/chat/friend/list`;
    ajaxRequest(url, "get", {}, null, function (res) {
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
                <div class="friend-info" friend-id="${friend.friendId}" onclick="getFriendInfo(this);">
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


function getFriendInfo(_this) {
    $(".apply-info").removeClass("select");
    $(".friend-info").removeClass("select");
    $(_this).addClass("select");

    let friendId = $(_this).attr("friend-id");
    let url = `${MSG_URL_PREFIX}/chat/friend/info`;
    ajaxRequest(url, "get", {friendId: friendId}, null, function (res) {
        if (res.code !== 200) {
            logger.info("获取好友详情失败,friendId:", friendId, "res:", res);
            myAlert('', res.message, "err");
            return;
        }
        let friendInfo = res.data;
        let remarkDom = ``;
        if (friendInfo.remark) {
            remarkDom = `<div class="more-info"><label>备注</label><span class="friend-remark">${friendInfo.remark}</span></div>`;
        }
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
                    <div class="more-info"><label>备注</label><span class="friend-remark"><input type="text" friend-id="${friendInfo.friendId}" remark="${friendInfo.remark}" value="${friendInfo.remark}" onclick="remarkClick(this)" onfocusin="remarkClick(this)" onfocusout="modifyFriendRemark(this)"></span></div>
                    <div class="more-info">
                        <label>来源</label><span>${friendInfo.sourceDesc}</span>
                    </div>
                </div>
                <div class="friend-bottom">
                    <span class="send-msg-btn" onclick="sendMsgBtnClick(${friendId})">发消息</span>
                </div>
            </div>
        `;
        $("#friend-apply-info").html("").append(infoDom);
    });
}

function sendMsgBtnClick(friendId) {
    let url = `${MSG_URL_PREFIX}/chat/room/gotoSendMsg`;
    ajaxRequest(url, "get", {friendId: friendId}, null, function (res) {
        if (res.code !== 200) {
            logger.info("去聊天室失败,friendId:", friendId, "res:", res);
            myAlert('', res.message, "err");
            return;
        }
        let chatId = res.data;
        //缓存1天聊天id
        setCookieDay(PREV_CHAT_ID_KEY, chatId, 1);
        loadChatRoom();
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
    ajaxRequest(url, "post", {friendId: friendId, remark: remark}, null, function (res) {
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