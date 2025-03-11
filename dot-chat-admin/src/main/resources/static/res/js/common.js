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
const MSG_API_PREFIX = "api/msg"; // 接口前缀
const SYS_URL_PREFIX = BASE_URL + SYS_API_PREFIX; // url前缀
const MSG_URL_PREFIX = BASE_URL + MSG_API_PREFIX; // url前缀
const TOKEN_KEY = "Authorization";
const USER_KEY = "user";
const LAST_ACCESS_TIME_KEY = "lastAccessedTime";
const EXPIRES_IN = "expiresIn";
const CONTENT_TYPE_JSON = "application/json";

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
            if (res.code !== 200) {
                console.error("获取数据失败,data:", data, "res:", res);
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
            }
            successFn(res);
        },
        error: function (err) {
            console.error(err);
            // alert("请求错误,请稍后重试,msg:" + err.statusText);
        }
    });
}


function logout() {
    let url = `${SYS_URL_PREFIX}/auth/admin/logout`;
    ajaxRequest(url, "post", {}, null, function (res) {
        if (res.code !== 200) {
            console.error("退出失败", dateNow());
            mini.alert(res.message);
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
    location.href = "../login.html";
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
    ajaxRequest(url, "post", {}, null, function (res) {
        if (res.code !== 200) {
            console.error("刷新失败", dateNow());
            mini.alert(res.message);
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