$(function () {
    mini.parse();
    //menu 新建
    let menu = new Menu("#mainMenu", {
        itemclick: function (item) {
            if (item.childNum === 0) {
                activeTab(item);
            }
        }
    });

    let tabs = mini.get("mainTabs");

    // tabs 激活时触发
    registerTabsActivechangedEvent();

    // 滚动条自定义
    $(".sidebar").mCustomScrollbar({autoHideScrollbar: true});

    // 菜单提示
    new MenuTip(menu);

    loadMenuData();

    //toggle 侧边栏显示隐藏
    registerToggleClickEvent();

    // 个人信息点击事件
    registerMyInfoClickEvent();

    // 自动刷新token
    autoRefreshToken();
    // 设置用户头像
    setUserAvatar();
    // 修改密码点击事件
    registerModifyPwdClickEvent();

    function activeTab(item) {
        // console.log('activeTab', item);
        let tab = tabs.getTab(item.id.toString());// 获取tab int根据index,String根据name获取
        if (!tab) {
            tab = {name: item.id.toString(), pid: item.pid, title: item.text, url: item.url, iconCls: item.iconCls};
            if (!item.home) {
                tab.showCloseButton = true;
            }
            tab = tabs.addTab(tab);
        }
        tabs.activeTab(tab);
    }

    function registerTabsActivechangedEvent() {
        tabs.on("activechanged", function (e) {
            // console.log('activechanged', tabs.getActiveTab(), e);
            let tab = e.tab;
            if (!tab) {
                tab = tabs.getActiveTab();
            }
            // 添加面包屑
            addBreadcrumb(tab.name, tab.pid);
            // 移除菜单选中
            $(".menu-selected").removeClass("menu-selected");
            let $menuTitle = $(".menu-title[data-id='" + tab.name + "']");
            // 添加选中样式
            $menuTitle.addClass("menu-selected");
            // 打开父级菜单
            $menuTitle.parents(".has-children").addClass("open");
        });

        $("#tabsMenu .menu-refresh").on("click", function (e) {
            tabs.reloadTab(tabs.getActiveTab());
        });

        $("#tabsMenu .menu-close").on("click", function (e) {
            let activeTab = tabs.getActiveTab();
            let tab0 = tabs.getTab(0);
            let excludeTabs = [activeTab];
            if (activeTab.name !== tab0.name) {
                excludeTabs.push(tab0);
            }
            tabs.removeAll(excludeTabs);
        });
    }


    // 面包屑
    function addBreadcrumb(id, pid) {
        let $breadcrumb = $(".breadcrumb");
        $breadcrumb.html("");
        let breadcrumbHtml = generateAllBreadcrumbHtml(id, pid);
        $breadcrumb.append(breadcrumbHtml);
    }

    // 生成面包屑 html
    function generateAllBreadcrumbHtml(id, pid) {
        let hList = [];
        // 递归生成父级面包屑
        generateParentBreadcrumbHtmlList(hList, pid);

        let breadcrumbHtml = ``;
        for (let i = hList.length - 1; i >= 0; i--) {
            breadcrumbHtml = breadcrumbHtml + hList[i];
        }
        // 生成当前面包屑
        let item = menu.getItemById(id);
        breadcrumbHtml = breadcrumbHtml + `<label>${item.text}</label>`;
        return breadcrumbHtml;
    }

    // 生成父级面包屑 html
    function generateParentBreadcrumbHtmlList(hList, pid) {
        if (pid === 0) {
            return;
        }
        let parent = menu.getItemById(pid);
        if (parent) {
            hList.push(generateBreadcrumbHtml(parent));
            generateParentBreadcrumbHtmlList(hList, parent.pid)
        }
    }

    // 生成面包屑 html
    function generateBreadcrumbHtml(item) {
        return `<a href='#' id='${item.id}' pid='${item.pid}' >${item.text}</a><span>&nbsp;&nbsp;/&nbsp;&nbsp;</span>`;
    }

    function loadMenuData() {
        let url = `${SYS_API_PREFIX}/auth/role/menu/list`;
        ajaxRequest(url, "get", {}, null, function (res) {
            if (res.code !== 200) {
                console.error("获取菜单失败", dateNow());
                mini.alert(res.message);
                return;
            }
            let data = mini.decode(res.data);
            menu.loadData(data);

            data[0].home = true;
            activeTab(data[0]);
        });

    }


});


function registerToggleClickEvent() {
    $("#toggle").click(function () {
        let $body = $('body');
        $body.toggleClass('compact');
        if ($body.hasClass('compact')) { // 切换为隐藏
            $("#toggle span").removeClass("fa-dedent").addClass("fa-indent");
        } else { // 切换为显示
            $("#toggle span").removeClass("fa-indent").addClass("fa-dedent");
        }
        // 重绘
        mini.layout();
    });
}

function registerMyInfoClickEvent() {
    $(".dropdown-toggle").click(function (event) {
        $(this).parent().addClass("open");
        return false;
    });

    $(document).click(function (event) {
        $(".dropdown").removeClass("open");
    });
}

function setUserAvatar() {
    let user = JSON.parse(localStorage.getItem("user"));
    let avatar = getUserAvatarOrDefault(user.avatar)
    $(".userinfo .user-img").attr("src", avatar);
}

function getUserAvatarOrDefault(avatar) {
    let defAvatar = "res/images/admin-avatar.jpg"
    if (avatar) {
        return avatar;
    }
    return defAvatar;
}

function registerModifyPwdClickEvent() {
    $("#modify-pwd").click(function () {
        openModifyPwdDialog();
    });
}

function openModifyPwdDialog() {
    let form = new mini.Form("#edit-form");
    form.clear();
    mini.get("modify-pwd-dialog").show();
}

function modifyPwd() {
    let form = new mini.Form("#edit-form");
    if (!form.validate()) {
        alertWarn(form.getErrorTexts()[0]);
        return;
    }
    let oldPwd = mini.get("old-pwd").getValue();
    let newPwd = mini.get("new-pwd").getValue();
    let reNewPwd = mini.get("re-new-pwd").getValue();
    if (oldPwd === newPwd) {
        alertWarn("新旧密码不能相同");
        return;
    }
    if (newPwd !== reNewPwd) {
        alertWarn("两次密码不一致");
        return;
    }
    let url = `${SYS_API_PREFIX}/auth/admin/modifyPwd`;
    ajaxRequest(url, METHOD.PUT, {
        oldPwd: oldPwd,
        newPwd: newPwd
    }, null, function (res) {
        showTipsSuccess("密码修改成功!");
        mini.confirm("密码修改成功,是否重新登录？", "重新登录？",
            function (action) {
                if (action === "ok") {
                    logout();
                } else {
                    mini.get("modify-pwd-dialog").hide();
                }
            }
        );
    });
}

function openUserInfoDialog() {
    let user = JSON.parse(localStorage.getItem("user"));
    let avatar = getUserAvatarOrDefault(user.avatar)
    let form = new mini.Form("#user-info-form");
    form.setData({
        id: user.id,
        name: user.name,
        account: user.account
    });
    $("#user-info-form .user-avatar").attr("src", avatar);
    mini.get("user-info-dialog").show();
}
