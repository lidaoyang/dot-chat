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

    function activeTab(item) {
        let tab = tabs.getTab(item.id);
        if (!tab) {
            tab = {name: item.id, pid: item.pid, title: item.text, url: item.url, iconCls: item.iconCls};
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
        $.ajax({
            url: "data/menu.txt",
            success: function (text) {
                let data = mini.decode(text);
                menu.loadData(data);

                data[0].home = true;
                activeTab(data[0]);
            }
        });
    }

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

});