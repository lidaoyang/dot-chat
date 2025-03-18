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


#初始化数据
#管理员 sys_admin
INSERT INTO sys_admin (account, name, pwd, avatar, roles, status, last_ip, last_time, create_time, update_time) VALUES ('15158152796', 'daoyang', 'YvKaeeOSWB8e9Gedm1mhTA==', '', '1', 1, '127.0.0.1', '2025-03-18 11:14:36', '2025-03-10 15:04:53', '2025-03-18 11:14:55');
INSERT INTO sys_admin (account, name, pwd, avatar, roles, status, last_ip, last_time, create_time, update_time) VALUES ('13000000000', '演示用户', '1NAG1ju9yQiJRnO7Avc8Aw==', null, '2', 1, '127.0.0.1', '2025-03-15 17:27:38', '2025-03-12 17:04:28', '2025-03-17 09:34:27');

# 菜单 sys_menu
INSERT INTO sys_menu (name, pid, icon_cls, type, link_url, level, path, status, sort, create_time, update_time) VALUES ('权限管理', 0, 'fa fa-cog', 0, '', 1, '', 1, 100, '2025-03-10 14:16:09', '2025-03-17 10:35:12');
INSERT INTO sys_menu (name, pid, icon_cls, type, link_url, level, path, status, sort, create_time, update_time) VALUES ('权限菜单', 1, 'fa fa-list', 2, 'pages/auth/menu.html', 2, '1', 1, 0, '2025-03-10 14:21:57', '2025-03-13 12:31:13');
INSERT INTO sys_menu (name, pid, icon_cls, type, link_url, level, path, status, sort, create_time, update_time) VALUES ('角色', 1, 'fa fa-users', 2, 'pages/auth/role.html', 2, '1', 1, 3, '2025-03-10 14:41:46', '2025-03-17 10:36:58');
INSERT INTO sys_menu (name, pid, icon_cls, type, link_url, level, path, status, sort, create_time, update_time) VALUES ('管理员', 1, 'fa fa-user', 2, 'pages/auth/admin.html', 2, '1', 1, 4, '2025-03-10 14:42:49', '2025-03-17 10:48:11');
INSERT INTO sys_menu (name, pid, icon_cls, type, link_url, level, path, status, sort, create_time, update_time) VALUES ('添加菜单', 2, '', 1, 'api/sys/auth/menu/add', 3, '1,2', 0, 0, '2025-03-10 14:45:01', '2025-03-10 16:59:44');
INSERT INTO sys_menu (name, pid, icon_cls, type, link_url, level, path, status, sort, create_time, update_time) VALUES ('编辑菜单', 2, '', 1, 'api/sys/auth/menu/edit', 3, '1,2', 0, 0, '2025-03-10 14:45:37', '2025-03-10 16:59:44');
INSERT INTO sys_menu (name, pid, icon_cls, type, link_url, level, path, status, sort, create_time, update_time) VALUES ('删除菜单', 2, '', 1, 'api/sys/auth/menu/delete', 3, '1,2', 0, 0, '2025-03-10 14:45:58', '2025-03-10 16:59:44');
INSERT INTO sys_menu (name, pid, icon_cls, type, link_url, level, path, status, sort, create_time, update_time) VALUES ('修改菜单状态', 2, '', 1, 'api/sys/auth/menu/modifyStatus', 3, '1,2', 0, 0, '2025-03-10 14:46:43', '2025-03-10 16:59:44');
INSERT INTO sys_menu (name, pid, icon_cls, type, link_url, level, path, status, sort, create_time, update_time) VALUES ('菜单列表', 2, '', 1, 'api/sys/auth/menu/list', 3, '1,2', 0, 0, '2025-03-10 14:47:03', '2025-03-10 16:59:44');
INSERT INTO sys_menu (name, pid, icon_cls, type, link_url, level, path, status, sort, create_time, update_time) VALUES ('菜单列表-精简', 2, '', 1, 'api/sys/auth/menu/simlist', 3, '1,2', 0, 0, '2025-03-10 14:47:25', '2025-03-10 16:59:44');
INSERT INTO sys_menu (name, pid, icon_cls, type, link_url, level, path, status, sort, create_time, update_time) VALUES ('添加角色', 3, '', 1, 'api/sys/auth/role/add', 3, '1,3', 0, 0, '2025-03-10 14:49:06', '2025-03-17 10:37:23');
INSERT INTO sys_menu (name, pid, icon_cls, type, link_url, level, path, status, sort, create_time, update_time) VALUES ('编辑角色', 3, '', 1, 'api/sys/auth/role/edit', 3, '1,3', 0, 0, '2025-03-10 14:49:23', '2025-03-10 16:59:44');
INSERT INTO sys_menu (name, pid, icon_cls, type, link_url, level, path, status, sort, create_time, update_time) VALUES ('角色列表', 3, '', 1, 'api/sys/auth/role/list', 3, '1,3', 0, 0, '2025-03-10 14:49:56', '2025-03-10 16:59:44');
INSERT INTO sys_menu (name, pid, icon_cls, type, link_url, level, path, status, sort, create_time, update_time) VALUES ('角色列表-精简', 3, '', 1, 'api/sys/auth/role/simlist', 3, '1,3', 0, 0, '2025-03-10 14:50:13', '2025-03-10 16:59:44');
INSERT INTO sys_menu (name, pid, icon_cls, type, link_url, level, path, status, sort, create_time, update_time) VALUES ('修改角色状态', 3, '', 1, 'api/sys/auth/role/modifyStatus', 3, '1,3', 0, 0, '2025-03-10 14:50:34', '2025-03-10 16:59:44');
INSERT INTO sys_menu (name, pid, icon_cls, type, link_url, level, path, status, sort, create_time, update_time) VALUES ('添加管理员', 4, '', 1, 'api/sys/auth/admin/add', 3, '1,4', 0, 0, '2025-03-10 14:51:05', '2025-03-17 10:36:41');
INSERT INTO sys_menu (name, pid, icon_cls, type, link_url, level, path, status, sort, create_time, update_time) VALUES ('编辑管理员', 4, '', 1, 'api/sys/auth/admin/edit', 3, '1,4', 0, 0, '2025-03-10 14:51:14', '2025-03-12 16:16:30');
INSERT INTO sys_menu (name, pid, icon_cls, type, link_url, level, path, status, sort, create_time, update_time) VALUES ('管理员列表', 4, '', 1, 'api/sys/auth/admin/list', 3, '1,4', 0, 0, '2025-03-10 14:51:32', '2025-03-10 16:59:44');
INSERT INTO sys_menu (name, pid, icon_cls, type, link_url, level, path, status, sort, create_time, update_time) VALUES ('首页', 0, 'fa fa-home', 2, 'pages/index.html', 1, '', 1, 9999, '2025-03-10 16:29:19', '2025-03-10 16:29:19');
INSERT INTO sys_menu (name, pid, icon_cls, type, link_url, level, path, status, sort, create_time, update_time) VALUES ('角色详情', 3, '', 1, 'api/sys/auth/role/info', 3, '1,3', 0, 0, '2025-03-10 16:47:17', '2025-03-10 16:47:17');
INSERT INTO sys_menu (name, pid, icon_cls, type, link_url, level, path, status, sort, create_time, update_time) VALUES ('修改管理员状态', 4, '', 1, 'api/sys/auth/admin/modifyStatus', 3, '1,4', 0, 0, '2025-03-13 11:44:52', '2025-03-17 11:18:23');
INSERT INTO sys_menu (name, pid, icon_cls, type, link_url, level, path, status, sort, create_time, update_time) VALUES ('目录菜单列表-精简', 2, '', 1, 'api/sys/auth/menu/dirlist', 3, '1,2', 1, 0, '2025-03-13 18:57:25', '2025-03-13 18:59:31');
INSERT INTO sys_menu (name, pid, icon_cls, type, link_url, level, path, status, sort, create_time, update_time) VALUES ('角色菜单Ids', 3, '', 1, 'api/sys/auth/role/menuIds', 3, '1,3', 0, 0, '2025-03-15 15:23:11', '2025-03-15 15:23:11');
INSERT INTO sys_menu (name, pid, icon_cls, type, link_url, level, path, status, sort, create_time, update_time) VALUES ('修改角色权限菜单', 3, '', 1, 'api/sys/auth/role/modifyMenu', 3, '1,3', 0, 0, '2025-03-15 16:35:55', '2025-03-15 16:36:24');
INSERT INTO sys_menu (name, pid, icon_cls, type, link_url, level, path, status, sort, create_time, update_time) VALUES ('IM聊天室', 0, 'fa fa-comments', 0, '', 1, '', 1, 0, '2025-03-17 10:07:42', '2025-03-17 10:07:42');
INSERT INTO sys_menu (name, pid, icon_cls, type, link_url, level, path, status, sort, create_time, update_time) VALUES ('用户管理', 27, 'fa fa-user', 2, 'pages/chat/user.html', 2, '27', 1, 0, '2025-03-17 11:33:36', '2025-03-17 11:48:20');
INSERT INTO sys_menu (name, pid, icon_cls, type, link_url, level, path, status, sort, create_time, update_time) VALUES ('用户列表', 28, '', 1, 'api/chat/user/list', 3, '27,28', 0, 0, '2025-03-17 11:37:17', '2025-03-17 11:38:22');
INSERT INTO sys_menu (name, pid, icon_cls, type, link_url, level, path, status, sort, create_time, update_time) VALUES ('修改用户状态', 28, '', 1, 'api/chat/user/modifyStatus', 3, '27,28', 0, 0, '2025-03-17 11:38:11', '2025-03-17 11:38:11');
INSERT INTO sys_menu (name, pid, icon_cls, type, link_url, level, path, status, sort, create_time, update_time) VALUES ('注册统计', 27, 'fa fa-bar-chart', 2, 'pages/chat/register-count.html', 2, '27', 1, 0, '2025-03-17 11:45:06', '2025-03-18 10:06:13');
INSERT INTO sys_menu (name, pid, icon_cls, type, link_url, level, path, status, sort, create_time, update_time) VALUES ('注册统计', 31, '', 1, 'api/chat/user/registerCount', 3, '27,31', 0, 0, '2025-03-17 11:46:26', '2025-03-17 11:46:26');
INSERT INTO sys_menu (name, pid, icon_cls, type, link_url, level, path, status, sort, create_time, update_time) VALUES ('每日用户列表', 31, '', 1, 'api/chat/user/listbydate', 3, '27,31', 0, 0, '2025-03-17 11:47:05', '2025-03-17 11:47:05');
INSERT INTO sys_menu (name, pid, icon_cls, type, link_url, level, path, status, sort, create_time, update_time) VALUES ('单聊消息', 27, 'fa fa-comment', 2, 'pages/chat/msg-single.html', 2, '27', 1, 0, '2025-03-17 15:43:42', '2025-03-18 09:55:02');
INSERT INTO sys_menu (name, pid, icon_cls, type, link_url, level, path, status, sort, create_time, update_time) VALUES ('单聊消息列表', 34, '', 1, 'api/chat/msg/single/list', 3, '27,34', 0, 0, '2025-03-17 15:44:25', '2025-03-17 15:44:25');
INSERT INTO sys_menu (name, pid, icon_cls, type, link_url, level, path, status, sort, create_time, update_time) VALUES ('群聊消息', 27, 'fa fa-commenting', 2, 'pages/chat/msg-group.html', 2, '27', 1, 0, '2025-03-17 15:45:07', '2025-03-18 09:55:13');
INSERT INTO sys_menu (name, pid, icon_cls, type, link_url, level, path, status, sort, create_time, update_time) VALUES ('群里消息列表', 36, '', 1, 'api/chat/msg/group/list', 3, '27,36', 0, 0, '2025-03-17 15:45:37', '2025-03-17 15:45:37');
INSERT INTO sys_menu (name, pid, icon_cls, type, link_url, level, path, status, sort, create_time, update_time) VALUES ('修改管理密码', 4, '', 1, 'api/sys/auth/admin/modifyPwd', 3, '1,4', 0, 0, '2025-03-18 11:06:25', '2025-03-18 11:06:49');

# 角色 sys_role
INSERT INTO sys_role (name, status, type, create_time, update_time) VALUES ('超级管理员', 1, 0, '2025-03-10 14:59:35', '2025-03-10 14:59:35');
INSERT INTO sys_role (name, status, type, create_time, update_time) VALUES ('演示管理员', 1, 2, '2025-03-10 16:18:45', '2025-03-15 16:23:17');
INSERT INTO sys_role (name, status, type, create_time, update_time) VALUES ('测试角色', 1, 1, '2025-03-15 16:24:59', '2025-03-15 16:34:21');

# 角色详情 sys_role_detail
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (1, 1, '', '2025-03-10 14:59:35', '2025-03-10 14:59:35');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (1, 16, 'api/sys/auth/admin/add', '2025-03-10 14:59:35', '2025-03-10 16:15:16');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (1, 17, 'api/sys/auth/admin/edit', '2025-03-10 14:59:35', '2025-03-12 16:17:18');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (1, 18, 'api/sys/auth/admin/list', '2025-03-10 14:59:35', '2025-03-10 16:15:16');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (1, 5, 'api/sys/auth/menu/add', '2025-03-10 14:59:35', '2025-03-10 16:15:16');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (1, 7, 'api/sys/auth/menu/delete', '2025-03-10 14:59:35', '2025-03-10 16:15:16');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (1, 6, 'api/sys/auth/menu/edit', '2025-03-10 14:59:35', '2025-03-10 16:15:16');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (1, 9, 'api/sys/auth/menu/list', '2025-03-10 14:59:35', '2025-03-10 16:15:16');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (1, 8, 'api/sys/auth/menu/modifyStatus', '2025-03-10 14:59:35', '2025-03-10 16:15:16');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (1, 10, 'api/sys/auth/menu/simlist', '2025-03-10 14:59:35', '2025-03-10 16:15:16');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (1, 11, 'api/sys/auth/role/add', '2025-03-10 14:59:35', '2025-03-10 16:15:16');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (1, 12, 'api/sys/auth/role/edit', '2025-03-10 14:59:35', '2025-03-10 16:15:16');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (1, 13, 'api/sys/auth/role/list', '2025-03-10 14:59:35', '2025-03-10 16:15:16');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (1, 15, 'api/sys/auth/role/modifyStatus', '2025-03-10 14:59:35', '2025-03-10 16:15:16');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (1, 14, 'api/sys/auth/role/simlist', '2025-03-10 14:59:35', '2025-03-10 16:15:16');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (1, 4, 'pages/admin.html', '2025-03-10 14:59:35', '2025-03-10 14:59:35');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (1, 2, 'pages/menu.html', '2025-03-10 14:59:35', '2025-03-10 14:59:35');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (1, 3, 'pages/role.html', '2025-03-10 14:59:35', '2025-03-10 14:59:35');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (2, 1, '', '2025-03-10 16:18:45', '2025-03-10 16:18:45');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (2, 16, 'api/sys/auth/admin/add', '2025-03-10 16:18:45', '2025-03-10 16:18:45');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (2, 17, 'api/sys/auth/admin/add', '2025-03-10 16:18:45', '2025-03-10 16:18:45');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (2, 18, 'api/sys/auth/admin/list', '2025-03-10 16:18:45', '2025-03-10 16:18:45');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (2, 5, 'api/sys/auth/menu/add', '2025-03-10 16:18:45', '2025-03-10 16:18:45');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (2, 7, 'api/sys/auth/menu/delete', '2025-03-10 16:18:45', '2025-03-10 16:18:45');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (2, 6, 'api/sys/auth/menu/edit', '2025-03-10 16:18:45', '2025-03-10 16:18:45');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (2, 9, 'api/sys/auth/menu/list', '2025-03-10 16:18:45', '2025-03-10 16:18:45');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (2, 8, 'api/sys/auth/menu/modifyStatus', '2025-03-10 16:18:45', '2025-03-10 16:18:45');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (2, 10, 'api/sys/auth/menu/simlist', '2025-03-10 16:18:45', '2025-03-10 16:18:45');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (2, 11, 'api/sys/auth/role/add', '2025-03-10 16:18:45', '2025-03-10 16:18:45');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (2, 12, 'api/sys/auth/role/edit', '2025-03-10 16:18:45', '2025-03-10 16:18:45');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (2, 13, 'api/sys/auth/role/list', '2025-03-10 16:18:45', '2025-03-10 16:18:45');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (2, 15, 'api/sys/auth/role/modifyStatus', '2025-03-10 16:18:45', '2025-03-10 16:18:45');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (2, 14, 'api/sys/auth/role/simlist', '2025-03-10 16:18:45', '2025-03-10 16:18:45');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (2, 4, 'pages/admin.html', '2025-03-10 16:18:45', '2025-03-10 16:18:45');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (2, 2, 'pages/menu.html', '2025-03-10 16:18:45', '2025-03-10 16:18:45');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (2, 3, 'pages/role.html', '2025-03-10 16:18:45', '2025-03-10 16:18:45');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (1, 19, 'pages/index.html', '2025-03-10 16:43:32', '2025-03-10 16:43:32');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (2, 19, 'pages/index.html', '2025-03-10 16:44:01', '2025-03-10 16:44:01');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (2, 20, 'api/sys/auth/role/info', '2025-03-13 11:46:16', '2025-03-13 11:46:16');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (2, 21, 'api/sys/auth/admin/modifyStatus', '2025-03-13 11:46:16', '2025-03-13 11:46:16');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (1, 20, 'api/sys/auth/role/info', '2025-03-13 11:46:49', '2025-03-13 11:46:49');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (1, 21, 'api/sys/auth/admin/modifyStatus', '2025-03-13 11:46:49', '2025-03-13 11:46:49');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (1, 22, 'api/sys/auth/menu/dirlist', '2025-03-13 18:58:12', '2025-03-13 18:59:53');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (2, 22, 'api/sys/auth/menu/dirlist', '2025-03-13 18:58:56', '2025-03-13 18:59:53');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (2, 25, 'api/sys/auth/role/menuIds', '2025-03-15 15:25:31', '2025-03-15 15:25:31');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (1, 25, 'api/sys/auth/role/menuIds', '2025-03-15 15:26:03', '2025-03-15 15:26:03');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (1, 26, 'api/sys/auth/role/modifyMenu', '2025-03-15 16:54:53', '2025-03-15 16:54:53');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (2, 26, 'api/sys/auth/role/modifyMenu', '2025-03-15 16:55:51', '2025-03-15 16:55:51');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (3, 1, '', '2025-03-15 16:57:00', '2025-03-15 16:57:00');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (3, 16, 'api/sys/auth/admin/add', '2025-03-15 16:57:00', '2025-03-15 16:57:00');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (3, 17, 'api/sys/auth/admin/edit', '2025-03-15 16:57:00', '2025-03-15 16:57:00');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (3, 18, 'api/sys/auth/admin/list', '2025-03-15 16:57:00', '2025-03-15 16:57:00');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (3, 21, 'api/sys/auth/admin/modifyStatus', '2025-03-15 16:57:00', '2025-03-15 16:57:00');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (3, 4, 'pages/auth/admin.html', '2025-03-15 16:57:00', '2025-03-15 16:57:00');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (3, 3, 'pages/auth/role.html', '2025-03-15 16:57:00', '2025-03-15 16:57:00');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (3, 13, 'api/sys/auth/role/list', '2025-03-15 16:57:11', '2025-03-15 16:57:11');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (3, 14, 'api/sys/auth/role/simlist', '2025-03-15 16:57:11', '2025-03-15 16:57:11');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (3, 19, 'pages/index.html', '2025-03-15 16:57:22', '2025-03-15 16:57:22');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (1, 27, '', '2025-03-17 11:35:41', '2025-03-17 11:35:41');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (1, 28, 'pages/chat/user.html', '2025-03-17 11:35:41', '2025-03-17 11:35:41');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (2, 27, '', '2025-03-17 11:35:46', '2025-03-17 11:35:46');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (2, 28, 'pages/chat/user.html', '2025-03-17 11:35:46', '2025-03-17 11:35:46');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (1, 29, 'api/chat/user/list', '2025-03-17 11:47:28', '2025-03-17 11:47:28');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (1, 30, 'api/chat/user/modifyStatus', '2025-03-17 11:47:28', '2025-03-17 11:47:28');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (1, 31, 'pages/chat/register-count.html', '2025-03-17 11:47:28', '2025-03-17 11:47:28');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (1, 32, 'api/chat/user/registerCount', '2025-03-17 11:47:28', '2025-03-17 11:47:28');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (1, 33, 'api/chat/user/listbydate', '2025-03-17 11:47:28', '2025-03-17 11:47:28');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (2, 29, 'api/chat/user/list', '2025-03-17 11:47:33', '2025-03-17 11:47:33');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (2, 30, 'api/chat/user/modifyStatus', '2025-03-17 11:47:33', '2025-03-17 11:47:33');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (2, 31, 'pages/chat/register-count.html', '2025-03-17 11:47:33', '2025-03-17 11:47:33');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (2, 32, 'api/chat/user/registerCount', '2025-03-17 11:47:33', '2025-03-17 11:47:33');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (2, 33, 'api/chat/user/listbydate', '2025-03-17 11:47:33', '2025-03-17 11:47:33');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (1, 34, 'pages/chat/msg-single.html', '2025-03-17 15:45:53', '2025-03-18 09:55:02');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (1, 35, 'api/chat/msg/single/list', '2025-03-17 15:45:53', '2025-03-17 15:45:53');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (1, 36, 'pages/chat/msg-group.html', '2025-03-17 15:45:53', '2025-03-18 09:55:13');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (1, 37, 'api/chat/msg/group/list', '2025-03-17 15:45:53', '2025-03-17 15:45:53');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (2, 34, 'pages/chat/msg-single.html', '2025-03-17 15:45:58', '2025-03-18 09:55:02');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (2, 35, 'api/chat/msg/single/list', '2025-03-17 15:45:58', '2025-03-17 15:45:58');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (2, 36, 'pages/chat/msg-group.html', '2025-03-17 15:45:58', '2025-03-18 09:55:13');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (2, 37, 'api/chat/msg/group/list', '2025-03-17 15:45:58', '2025-03-17 15:45:58');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (1, 38, 'api/sys/auth/admin/modifyPwd', '2025-03-18 11:07:02', '2025-03-18 11:07:02');
INSERT INTO sys_role_detail (role_id, menu_id, menu_url, create_time, update_time) VALUES (2, 38, 'api/sys/auth/admin/modifyPwd', '2025-03-18 11:07:06', '2025-03-18 11:07:06');
