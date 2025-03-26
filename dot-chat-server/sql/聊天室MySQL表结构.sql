create table chat_friend
(
    id          int auto_increment
        primary key,
    user_id     int                                  not null comment '用户ID',
    friend_id   int                                  not null comment '好友ID',
    remark      varchar(128)                         null comment '好友备注',
    is_top      tinyint(1) default 0                 null comment '是否置顶',
    label       varchar(256)                         null comment '标签(多个标签用英文逗号分割)',
    initial     varchar(1) default '#'               not null comment '昵称或备注首字母',
    source      varchar(64)                          null comment '来源',
    create_time datetime   default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime   default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint user_id_friend_id_uq
        unique (user_id, friend_id)
)
    comment '聊天室好友表';

create index is_top_initial_idx
    on chat_friend (is_top desc, initial asc);

create index user_id_idx
    on chat_friend (user_id);

create table chat_friend_apply
(
    id            int auto_increment
        primary key,
    apply_user_id int                                not null comment '申请用户ID',
    friend_id     int                                not null comment '好友ID',
    status        tinyint                            null comment '申请状态(0:申请中;1:同意;)',
    source        varchar(64)                        null comment '来源',
    apply_reason  varchar(128)                       null comment '申请理由',
    apply_reply   varchar(128)                       null comment '申请回复',
    create_time   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint apply_user_id_friend_id_idx
        unique (apply_user_id, friend_id)
)
    comment '聊天室新好友申请表';

create index apply_user_id_idx
    on chat_friend_apply (apply_user_id);

create index status_idx
    on chat_friend_apply (status);

create table chat_friend_apply_user_rel
(
    id           int auto_increment
        primary key,
    apply_id     int                                  not null comment '申请ID',
    user_id      int                                  not null comment '用户ID',
    friend_id    int                                  not null comment '好友ID',
    remark       varchar(32)                          null comment '好友备注',
    label        varchar(128)                         null comment '标签',
    unread_count tinyint(1) default 0                 not null comment '未读数',
    create_time  datetime   default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time  datetime   default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '聊天室新好友申请和用户关联表';

create index apply_id_idx
    on chat_friend_apply_user_rel (apply_id);

create index friend_id_idx
    on chat_friend_apply_user_rel (friend_id);

create index unread_count_apply_id_user_id_idx
    on chat_friend_apply_user_rel (apply_id, user_id, unread_count);

create index user_id_is_read_idx
    on chat_friend_apply_user_rel (user_id, unread_count);

create table chat_group
(
    id              int auto_increment
        primary key,
    name            varchar(128)                         null comment '群名称',
    avatar          varchar(256)                         null comment '群头像',
    member_count    int        default 0                 null comment '群成员数',
    invite_cfm      tinyint(1) default 0                 null comment '邀请进群是否需要群主或管理员确认(true:需要,false:不需要)',
    group_leader_id int                                  not null comment '群主用户ID',
    managers        varchar(128)                         null comment '群管理员用户ID,多个用逗号分割,最多3个',
    remark          varchar(256)                         null comment '备注',
    notice          text                                 null comment '群公告',
    is_dissolve     tinyint(1) default 0                 null comment '是否解散',
    dissolve_time   datetime                             null comment '解散时间',
    create_time     datetime   default CURRENT_TIMESTAMP null comment '创建时间',
    update_time     datetime   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '聊天室群组表';

create index group_leader_id_idx
    on chat_group (group_leader_id);

create table chat_group_apply
(
    id              int auto_increment
        primary key,
    group_id        int                                not null comment '群组ID',
    apply_user_ids  varchar(128)                       not null comment '申请人用户ID集合,用逗号分割',
    invite_user_id  int                                null comment '邀请人用户ID',
    approve_user_id int                                null comment '通过人用户ID',
    status          tinyint                            null comment '申请状态(0:申请中;1:同意;)',
    source          varchar(64)                        null comment '来源',
    apply_reason    varchar(128)                       null comment '邀请原因',
    create_time     datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time     datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '聊天室邀请入群申请表';

create index apply_user_id_idx
    on chat_group_apply (apply_user_ids);

create index group_id_idx
    on chat_group_apply (group_id);

create index invite_user_id_idx
    on chat_group_apply (invite_user_id);

create index status_idx
    on chat_group_apply (status);

create table chat_group_apply_user_rel
(
    id          int auto_increment
        primary key,
    user_id     int                                  not null comment '用户ID',
    apply_id    int                                  not null comment '申请ID',
    is_read     tinyint(1) default 0                 null comment '是否已读',
    create_time datetime   default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime   default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '聊天室邀请好友进群申请和用户关联表';

create index apply_id_idx
    on chat_group_apply_user_rel (apply_id);

create index user_id_is_read_idx
    on chat_group_apply_user_rel (user_id, is_read);

create table chat_group_member
(
    id               int auto_increment
        primary key,
    group_id         int                                  null comment '群组ID',
    user_id          int                                  null comment '群成员用户ID',
    nickname         varchar(64)                          null comment '群成员昵称',
    is_group_leader  tinyint(1) default 0                 null comment '是否是群主',
    is_group_manager tinyint(1) default 0                 null comment '是否是群管理员',
    invite_user_id   int                                  null comment '邀请人用户ID(邀请入群时有值)',
    source           varchar(128)                         null comment '来源',
    create_time      datetime   default CURRENT_TIMESTAMP null comment '创建时间',
    update_time      datetime   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint group_id_user_id_uq
        unique (group_id, user_id)
)
    comment '聊天室群成员表';

create index group_id_idx
    on chat_group_member (group_id);

create index is_group_leader_is_group_manager_index
    on chat_group_member (is_group_leader desc, is_group_manager desc);

create table chat_msg
(
    id           int unsigned auto_increment comment '消息id'
        primary key,
    chat_id      varchar(16)                        not null comment '会话id',
    chat_type    varchar(16)                        not null comment '聊天室类型(SINGLE:单聊,GROUP:群聊)',
    group_id     int      default 0                 null comment '群组ID,群聊时groupId有值',
    send_user_id int                                null comment '发送用户ID',
    to_user_id   int                                null comment '接收用户ID',
    msg_type     varchar(16)                        not null comment '消息类型(TEXT:文本;PRODUCT:商品;IMAGE:图片;VIDEO:视频;FILE:文件;BIZ_CARD:个人名片;GROUP_BIZ_CARD:群名片;)',
    msg          text                               not null comment '消息内容',
    send_time    datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '发送时间',
    timestamp    bigint                             null comment '时间戳'
)
    comment '聊天室消息记录表';

create index chat_id_idx
    on chat_msg (chat_id);

create index send_user_id_to_user_id_msg_type_index
    on chat_msg (send_user_id, to_user_id, msg_type);

alter table chat_msg
    add device_type varchar(6) null comment '设备类型(PC,MOBILE)';


create table chat_msg_user_rel
(
    id              int unsigned auto_increment
        primary key,
    chat_id         varchar(16)                          not null comment '聊天室ID',
    user_id         int unsigned                         not null comment '用户id',
    msg_id          int                                  not null comment '聊天信息id',
    is_unread       tinyint(1) default 0                 not null comment '是否未读',
    is_offline      tinyint(1) default 0                 not null comment '是否是离线消息(离线时,未收消息)',
    is_collect      tinyint(1) default 0                 not null comment '是否收藏',
    send_time_stamp bigint                               null comment '发送/接收时间戳',
    create_time     datetime   default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time     datetime   default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint userId_msgId_pk
        unique (user_id, msg_id)
)
    comment '聊天信息和用户关联表';

create index chat_user_id_idx
    on chat_msg_user_rel (chat_id, user_id);

create index user_id_is_offline_idx
    on chat_msg_user_rel (user_id, is_offline);

create table chat_notify_msg
(
    id          int auto_increment comment 'ID'
        primary key,
    linkid      varchar(64) null comment '关联ID(申请ID/业务ID)',
    user_id     int         not null comment '通知用户ID(全局通知类型时为业务ID)',
    notify_type tinyint     not null comment '通知类型(1:个人通知,2:业务通知,3:广播通知)',
    msg_type    varchar(12) null comment '消息类型(SYSTEM:系统消息;EVENT:事件消息;WARNING:预警消息;NOTICE:通知消息)',
    event_type  varchar(16) null comment '事件类型(消息类型为事件消息时有值)',
    msg_content text        null comment '消息内容',
    send_time   datetime    null comment '发送时间'
)
    comment '通知消息';

create index event_type_linkid_msg_type_idx
    on chat_notify_msg (linkid, event_type, msg_type);

create index notify_type_idx
    on chat_notify_msg (notify_type);

create table chat_notify_msg_user_rel
(
    id            int auto_increment comment 'ID'
        primary key,
    linkid        varchar(64)                          not null comment '关联ID(好友申请关联申请ID/业务ID)',
    user_id       int                                  not null comment '用户ID',
    notify_msg_id int                                  not null comment '通知信息ID',
    send_user_id  int        default 0                 not null comment '通知发送用户ID(0为系统发送)',
    to_user_id    int                                  not null comment '通知接收用户ID(全局通知类型时为业务ID)',
    is_offline    tinyint(1) default 0                 null comment '是否是离线消息(广播没有离线消息)',
    is_read       tinyint(1) default 0                 null comment '是否已读',
    create_time   datetime   default CURRENT_TIMESTAMP null comment '创建时间'
)
    comment '通知信息关联用户';

create index linkid_is_read_user_id_idx
    on chat_notify_msg_user_rel (linkid, is_read, user_id);

create index notify_id_idx
    on chat_notify_msg_user_rel (notify_msg_id);

create index notify_msg_id_user_id_idx
    on chat_notify_msg_user_rel (notify_msg_id, user_id);

create index user_id_idx
    on chat_notify_msg_user_rel (to_user_id);

create index user_id_is_offline_idx
    on chat_notify_msg_user_rel (to_user_id asc, is_offline desc);

create index user_id_is_read_idx
    on chat_notify_msg_user_rel (to_user_id, is_read);

create table chat_room
(
    id          int auto_increment comment '主键ID'
        primary key,
    chat_id     varchar(16)                        not null comment '聊天室id',
    chat_type   varchar(16)                        null comment '聊天室类型(SINGLE:单聊,GROUP:群聊)',
    group_id    int      default 0                 null comment '群组ID,群聊时groupId有值',
    timestamp   bigint                             null comment '时间戳',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    constraint chat_id_uq
        unique (chat_id)
)
    comment '用户聊天室表';

create index timestamp_idx
    on chat_room (timestamp desc);

create table chat_room_user_rel
(
    id                int unsigned auto_increment
        primary key,
    chat_id           varchar(16)                          not null comment '聊天室id',
    user_id           int unsigned                         not null comment '用户id',
    is_top            tinyint(1) default 0                 not null comment '是否置顶(true:置顶;false:不置顶)',
    msg_no_disturb    tinyint(1) default 0                 null comment '消息免打扰(true:开启免打扰;false:关闭免打扰)',
    unread_msg_count  int        default 0                 not null comment '未读消息数',
    offline_msg_count int        default 0                 null comment '离线消息数(离线时,未收消息)',
    last_msg_id       int                                  null comment '最新消息ID',
    last_msg          varchar(64)                          null comment '最新消息',
    last_time         datetime                             null comment '最新消息发送时间',
    timestamp         bigint                               null comment '时间戳',
    create_time       datetime   default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time       datetime   default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint chat_user_id_uq
        unique (chat_id, user_id)
)
    comment '聊天室和用户关联表';

create index is_top_idx
    on chat_room_user_rel (id asc, is_top desc);

create index user_id_idx
    on chat_room_user_rel (user_id);

create table chat_user
(
    id              int unsigned auto_increment
        primary key,
    nickname        varchar(64)                            null comment '用户昵称',
    phone           varchar(16)                            null comment '用户电话(登录唯一账号)',
    pwd             varchar(128)                           null comment '密码',
    avatar          varchar(255)                           null comment '头像',
    qrcode          varchar(255)                           null comment '个人二维码',
    sex             tinyint      default 0                 null comment '性别(0:保密,1:男,2:女)',
    status          tinyint(1)   default 1                 null comment '用户状态(1:正常,0:禁用)',
    del_flag        tinyint(1)   default 0                 not null comment '删除标志',
    is_online       tinyint(1)   default 0                 null comment '是否在线',
    signature       varchar(256) default ''                null comment '个性签名',
    last_login_time datetime                               null comment '最后登录时间',
    last_ip         varchar(16)                            null comment '最后登录IP',
    create_time     datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time     datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint phone_uqk
        unique (phone)
)
    comment '聊天室用户表(关联管理员表和企业用户表)';

alter table chat_user
    add create_date date null comment '创建日期' after last_ip;

create index create_date_index
    on chat_user (create_date desc);


create table chat_user_blacklist
(
    id            int auto_increment
        primary key,
    user_id       int                                not null comment '用户ID',
    black_user_id int                                not null comment '拉黑用户ID',
    create_time   datetime default CURRENT_TIMESTAMP not null comment '创建时间'
)
    comment '聊天室用户黑名单表';

create index black_user_id_idx
    on chat_user_blacklist (black_user_id);

create index user_id_idx
    on chat_user_blacklist (user_id);


create table dot_chat.deepseek_req_record
(
    id       int auto_increment comment 'ID'
        primary key,
    chat_id  varchar(16) null comment '会话id',
    user_id  int         not null comment '访问用户ID',
    req_msg  text        null comment '请求信息',
    req_time datetime    null comment '请求时间',
    res_msg  text        null comment '返回信息',
    res_time datetime    null comment '返回时间'
)
    comment 'DeepSeek AI 请求记录';

create index chat_id_index
    on dot_chat.deepseek_req_record (chat_id);

create index user_id_index
    on dot_chat.deepseek_req_record (user_id);



