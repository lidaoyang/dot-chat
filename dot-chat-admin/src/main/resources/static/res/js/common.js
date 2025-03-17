let env = "dev";// dev:开发环境,test:测试环境,prod:生产环境
if (location.host !== "dot-chat.jrmall.cn") {
    env = "local"
}

let HOST, BASE_URL;
switch (env) {
    case "dev":
        HOST = "dot-chat.jrmall.cn";
        BASE_URL = "https://" + HOST + "/";
        break;
    case "prod":
        HOST = "dot-chat.jrmall.cn";
        BASE_URL = "https://" + HOST + "/";
        break;
    default:
        HOST = "dev.dot.cn";
        BASE_URL = "https://" + HOST + ":9089/";
}
// 定义常量对象
const SYS_API_PREFIX = "api/sys"; // 接口前缀
const MSG_API_PREFIX = "api/chat"; // 接口前缀
const SYS_URL_PREFIX = BASE_URL + SYS_API_PREFIX; // url前缀
const MSG_URL_PREFIX = BASE_URL + MSG_API_PREFIX; // url前缀
const TOKEN_KEY = "Authorization";
const USER_KEY = "user";
const LAST_ACCESS_TIME_KEY = "lastAccessedTime";
const EXPIRES_IN = "expiresIn";
const CONTENT_TYPE_JSON = "application/json";

const METHOD = {
    GET: "GET",
    POST: "POST",
    PUT: "PUT",
    DELETE: "DELETE"
};

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
                console.error("token过期", dateNow());
                deleteUserCookie();
                return;
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
    if (!token && url !== `${SYS_URL_PREFIX}/auth/admin/login`) {
        console.error("token过期", dateNow());
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
                console.error("token过期", dateNow());
                deleteUserCookie();
                return;
            }
            if (res.code !== 200) {
                console.error("操作失败,data:", data, "res:", res);
                alertError(res.message);
                return;
            }
            successFn(res);
        },
        error: function (err) {
            console.error(err);
            alertError(err);
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
    if (!token && url !== `${SYS_URL_PREFIX}/auth/admin/login`) {
        console.error("token过期", dateNow());
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
                console.error("token过期", dateNow());
                deleteUserCookie();
                return;
            }
            if (res.code !== 200) {
                console.error("获取数据失败,data:", data, "res:", res);
                alertError(res.message);
                return;
            }
            successFn(res);
        },
        error: function (err) {
            console.error(err);
            alertError(err);
            // alert("请求错误,请稍后重试,msg:" + err.statusText);
        }
    });
}


function logout() {
    let url = `${SYS_URL_PREFIX}/auth/admin/logout`;
    ajaxRequest(url, METHOD.GET, null, null, function (res) {
        if (res.code !== 200) {
            console.error("退出失败", dateNow());
            alertError(res.message);
            return;
        }
        deleteUserCookie();
    });
}

function deleteUserCookie() {
    deleteCookie(TOKEN_KEY);
    deleteCookie(LAST_ACCESS_TIME_KEY);
    deleteCookie(EXPIRES_IN);
    localStorage.clear();
    toLogin();
}

function toLogin() {
    if (window.parent !== window) {
        window.parent.location.href = "/login.html";
    } else {
        location.href = "/login.html";
    }
}

let autoRefreshTokenTimer = 0;

function autoRefreshToken() {
    if (autoRefreshTokenTimer === 0) {
        console.info("添加自动刷新token timer", dateNow());
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
    console.info("刷新token", dateNow());
    let url = `${SYS_URL_PREFIX}/auth/admin/refreshToken`;
    ajaxRequest(url, METHOD.PUT, {}, null, function (res) {
        if (res.code !== 200) {
            console.error("刷新失败", dateNow());
            deleteUserCookie();
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

function dateNow() {
    let date = new Date();
    return date.toLocaleDateString() + " " + date.toLocaleTimeString();
}

function alertInfo(msg, tit, callback) {
    myAlert("info", msg, tit, callback)
}

function alertWarn(msg, tit, callback) {
    myAlert("warning", msg, tit ? tit : "警告提示!", callback)
}

function alertError(msg, tit) {
    myAlert("error", msg, tit ? tit : "错误提示!")
}

function myAlert(type, msg, tit, callback) {
    mini.showMessageBox({
        showHeader: true,
        width: 280,
        title: tit ? tit : "提示!",
        buttons: ["关闭"],
        message: msg,
        iconCls: "mini-messagebox-" + type,
        callback: function (action) {
            if (callback) {
                callback(action);
            }
            this.close();
        }
    });
}

function showTipsSuccess(msg) {
    showTips("success", "成功!", msg)
}

function showTipsInfo(msg, tit) {
    showTips("info", tit, msg)
}

function showTipsWarn(msg, tit) {
    showTips("warning", tit, msg)
}

function showTipsDanger(msg, tit) {
    showTips("danger", tit, msg)
}

function showTips(state, tit, msg) {
    mini.showTips({
        content: `<b>${tit}</b> <br/>${msg}`,
        state: state,
        x: "center",
        y: "top",
        timeout: 3000
    });
}

/**
 * 状态开关渲染器
 * @param e
 * @returns {string}
 */
function onStatusRenderer(e) {
    // console.log("onStatusRenderer", e);
    if (e.value) {
        return `<span id="${e.record.id}" name="grid-status-sw" class="switch-on" themeColor="#6d9eeb" style="zoom:0.45;"></span>`
    }
    return `<span id="${e.record.id}" name="grid-status-sw" class="switch-off" themeColor="#6d9eeb" style="zoom:0.45;"></span>`
}

/**
 * 头像渲染器
 * @param e
 * @returns {string}
 */
function onAvatarRenderer(e) {
    if (e.value) {
        return `<img id="${e.record.id}" class="grid-avatar" src="${e.value}" alt="头像"/>`
    }
    return `<img id="${e.record.id}" class="grid-avatar" src="../images/user.jpg" alt="默认头像" />`
}

function switchInitForGrid(apiType, grid) {
    honeySwitch.init("span[name='grid-status-sw']");
    // console.log("switchInit");
    switchEvent("span[name='grid-status-sw']",
        function (ele) {
            // console.log("on", $(ele).attr("id"));
            updateStatus($(ele).attr("id"), true);
        },
        function (ele) {
            // console.log("off", $(ele).attr("id"));
            updateStatus($(ele).attr("id"), false);
        });

    function updateStatus(id, status) {
        let url = `${SYS_URL_PREFIX}/auth/${apiType}/modifyStatus?id=${id}&status=${status}`;
        ajaxRequest(url, METHOD.PUT, null, null, function (res) {
            showTipsSuccess("修改成功!");
            grid.reload();
        });
    }
}

function switchInitForChatGrid(apiType, grid) {
    honeySwitch.init("span[name='grid-status-sw']");
    // console.log("switchInit");
    switchEvent("span[name='grid-status-sw']",
        function (ele) {
            // console.log("on", $(ele).attr("id"));
            updateStatus($(ele).attr("id"), true);
        },
        function (ele) {
            // console.log("off", $(ele).attr("id"));
            updateStatus($(ele).attr("id"), false);
        });

    function updateStatus(id, status) {
        let url = `${MSG_URL_PREFIX}/${apiType}/modifyStatus?id=${id}&status=${status}`;
        ajaxRequest(url, METHOD.PUT, null, null, function (res) {
            showTipsSuccess("修改成功!");
            grid.reload();
        });
    }
}

function switchInitForForm() {
    honeySwitch.init("span[name='form-status-sw']");

    switchEvent("span[name='form-status-sw']",
        function (ele) {
            // $(ele).children("input").val(true);
            mini.getChildControls(ele)[0].setValue(true);
        },
        function (ele) {
            // $(ele).children("input").val(false);
            mini.getChildControls(ele)[0].setValue(false);
        });
}

function setSwitch(idName, status) {
    if (status === true) {
        honeySwitch.showOn(idName)
    } else {
        honeySwitch.showOff(idName)
    }
}

/**
 * 表格加载失败触发方法
 * @param e
 */
function onLoadError(e) {
    // console.log("load",e);
    if (e.result.code === 401) {
        console.error("token过期", dateNow());
        deleteUserCookie();
        return;
    }
    if (e.result.code !== 200) {
        console.error("获取数据失败", "result:", e.result);
        alertError(e.result.message);
    }
}
