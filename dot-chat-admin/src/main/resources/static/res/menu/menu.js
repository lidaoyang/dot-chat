//{ id, text, parentId?, href?, hrefTarget?, icon, iconCls, cls, expanded, children }

let Menu_Id = 1;

let Menu = function (element, options) {
    this.element = $(element);
    this.options = $.extend(true, {}, this.options, options);
    this.init();
}

Menu.prototype = {

    options: {
        data: null,
        itemclick: null
    },

    loadData: function (data) {
        this.options.data = data || [];
        this.refresh();
    },

    refresh: function () {
        this._render();
    },

    init: function () {
        let me = this,
            opt = me.options,
            el = me.element;
        console.log('init', me);
        me.loadData(opt.data);

        el.on('click', '.menu-title', function (event) {
            let el = $(event.currentTarget);

            let li = el.parent();
            if (li.hasClass("has-children")) {
                li.toggleClass('open');
            }
            let item = me.getItemByEvent(event);
            if (opt.itemclick) opt.itemclick.call(me, item);

        });

    },

    _render: function () {
        let data = this.options.data || [];
        let html = this._renderItems(data, null);
        this.element.html(html);
    },

    _renderItems: function (items, parent) {
        let s = '<ul class="' + (parent ? "menu-submenu" : "menu") + '">';
        for (let i = 0, l = items.length; i < l; i++) {
            let item = items[i];
            s += this._renderItem(item);
        }
        s += '</ul>';
        return s;
    },

    _renderItem: function (item) {

        let me = this,
            hasChildren = item.children && item.children.length > 0;

        let s = '<li class="' + (hasChildren ? 'has-children' : '') + '">';        //class="menu-item" open, expanded?

        s += '<a class="menu-title" data-id="' + item.id + '" ';
        //        if (item.href) {
        //            s += 'href="' + item.href + '" target="' + (item.hrefTarget || '') + '"';
        //        }
        s += '>';

        s += '<i class="menu-icon fa ' + item.iconCls + '"></i>';
        s += '<span class="menu-text">' + item.text + '</span>';

        if (hasChildren) {
            s += '<span class="menu-arrow fa"></span>';
        }

        s += '</a>';

        if (hasChildren) {
            s += me._renderItems(item.children, item);
        }

        s += '</li>';
        return s;
    },

    getItemByEvent: function (event) {
        let el = $(event.target).closest('.menu-title');
        let id = el.attr("data-id");
        return this.getItemById(id);
    },

    getItemById: function (id) {
        let me = this,
            idHash = me._idHash;
        if (!idHash) {
            idHash = me._idHash = {};

            function each(items) {
                for (let i = 0, l = items.length; i < l; i++) {
                    let item = items[i];
                    item.childNum = 0;
                    if (item.children) {
                        each(item.children);
                        item.childNum = item.children.length
                        delete item.children;
                    }
                    idHash[item.id] = item;
                }
            }

            each(me.options.data);
        }

        return me._idHash[id];
    }

};
