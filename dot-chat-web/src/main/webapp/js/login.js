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

function login() {
    let account = $("#account").val();
    let password = $("#password").val();

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
        autoRefreshToken();
        //获取聊天用户信息
        getChatUser();
    });
}
