create table sys_admin
(
    id          int auto_increment comment 'ID'
        primary key,
    account     varchar(16)                          not null comment '账号',
    name        varchar(128)                         null comment '名称',
    pwd         varchar(32)                          null comment '密码',
    avatar      varchar(128)                         null comment '头像',
    roles       varchar(32)                          null comment '角色ID(多个用逗号分隔)',
    status      tinyint(1) default 1                 null comment '状态(1:正常,0:禁用)',
    last_ip     varchar(16)                          null comment '最后登录IP',
    last_time   datetime                             null comment '最后登录时间',
    create_time datetime   default CURRENT_TIMESTAMP null comment '创建时间',
    update_time datetime   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint account_uq
        unique (account)
)
    comment '管理员表';

create index status_index
    on sys_admin (status);

create table sys_menu
(
    id          int auto_increment comment 'ID'
        primary key,
    name        varchar(64)                        not null comment '菜单名称',
    pid         int      default 0                 null comment '父级ID',
    icon_cls    varchar(64)                        null comment '菜单图标CSS类',
    type        int                                null comment '菜单类型(0:目录菜单,1:API菜单,2:页面菜单)',
    link_url    varchar(128)                       null comment '链接地址(页面菜单和API菜单的地址,文件夹菜单为空)',
    level       int      default 0                 null comment '菜单树形层级',
    path        varchar(64)                        null comment '树节点的路径,从根节点到当前节点的父级,用逗号分割',
    status      int      default 1                 null comment '状态(0:隐藏;1:显示),默认1',
    sort        int      default 0                 null comment '排序编号',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '系统菜单';

create index link_url_index
    on sys_menu (link_url);

create index path_index
    on sys_menu (path);

create index pid_index
    on sys_menu (pid);

create index sort_index
    on sys_menu (sort);

create index status_index
    on sys_menu (status);

create index type_index
    on sys_menu (type);

create table sys_role
(
    id          int auto_increment comment 'ID'
        primary key,
    name        varchar(64)                        null comment '角色名称',
    status      tinyint(1)                         null comment '状态(1:显示,0:隐藏)',
    type        tinyint                            not null comment '角色类型(0:超级管理员,1:普通管理员,2:演示管理员)',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '系统角色';

create index status_index
    on sys_role (status);

create index type_index
    on sys_role (type);

create table sys_role_detail
(
    id          int auto_increment comment 'ID'
        primary key,
    role_id     int                                not null comment '角色ID',
    menu_id     int                                not null comment '菜单ID',
    menu_url    varchar(64)                        null comment '菜单URL地址',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '角色明细';

create index menu_id_index
    on sys_role_detail (menu_id);

create index menu_url_index
    on sys_role_detail (menu_url);

create index role_id_index
    on sys_role_detail (role_id);

create index role_id_menu_id_index
    on sys_role_detail (role_id, menu_id);

