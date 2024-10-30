$(function () {
    initChooseEntDialog();
});

/**
 * 初始化选择企业对话框
 */
function initChooseEntDialog() {
    $("#choose-ent-dialog").dialog({
        title: "选择登录企业",
        autoOpen: false,   // 是否自动弹出窗口
        modal: true,    // 设置为模态对话框
        resizable: false,
        width: 270,   //弹出框宽度
        height: 300,   //弹出框高度
        close: function (event, ui) {

        }
    });
}

function goLogin() {
    let userType = $("#userType").val();
    let account = $("#account").val();
    if (userType === "") {
        myAlert("", "请选择用户类型", 'warn');
        return;
    }
    if (account === "") {
        myAlert("", "账号不能为空", 'warn');
        return;
    }
    if (userType === "PL_ADMIN") { // 平台管理员直接登录
        $("#enterpriseId").val("0");
        login();
        return;
    }
    let url = `${SYS_URL_PREFIX}/ent/sim/list`;
    if (userType === "SUPPLIER") {
        url = `${SYS_URL_PREFIX}/supp/sim/list`;
    }
    let data = {userType: userType, account: account};
    ajaxRequestNotAuth(url, 'get', data, null, function (res) {
        if (res.code !== 200) {
            logger.info("获取企业信息失败 ", res);
            myAlert("", res.message, 'err');
            return;
        }
        let entList = res.data;
        if (entList.length === 0) {
            myAlert("", "该账号不存在", 'warn');
            return;
        }
        if (entList.length === 1) { // 登录企业只有一个直接登录
            $("#enterpriseId").val(entList[0].id);
            login();
        } else { // 登录企业有多个，弹出选择企业对话框
            for (let i = 0; i < entList.length; i++) {
                let entDom = `<li class="ent-dialog-item" ent-id="${entList[i].id}" onclick="chooseEnt(this)" >${entList[i].id}-${entList[i].value}</li>`;
                $("#choose-ent-dialog ul").append(entDom);
            }
            $("#choose-ent-dialog").dialog("open");
        }
    });
}

function chooseEnt(_this) {
    $("#enterpriseId").val($(_this).attr("ent-id"));
    login();
    $("#choose-ent-dialog").dialog("close");
}

function login() {
    let userType = $("#userType").val();
    let enterpriseId = $("#enterpriseId").val();
    let account = $("#account").val();
    let password = $("#password").val();
    if (userType === "") {
        myAlert("", "请选择用户类型", 'warn');
        return;
    }
    if (enterpriseId === "") {
        myAlert("", "请选择所属企业", 'warn');
        return;
    }

    if (account === "" || password === "") {
        myAlert("", "请输入账号和密码", 'warn');
        return;
    }
    let url = `${SYS_URL_PREFIX}/user/login`;
    let data = JSON.stringify({
        userType: userType,
        enterpriseId: enterpriseId,
        account: account,
        password: password
    });
    ajaxSyncRequest(url, 'post', data, 'application/json', function (res) {
        if (res.code !== 200) {
            myAlert("登录失败", res.message, 'err');
            return;
        }
        USER_TYPE = userType;
        // 保存token
        saveTokenToCookie(res.data);
        // 自动刷新token
        autoRefreshToken();
        //获取聊天用户信息
        getChatUser();
    });
}
