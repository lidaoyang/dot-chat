package com.dot.msg.chat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dot.comm.em.UserTypeEm;
import com.dot.msg.chat.em.GroupSourceEm;
import com.dot.msg.chat.model.ChatGroup;
import com.dot.msg.chat.response.ChatGroupMemberSimResponse;

import java.util.List;

/**
 * 聊天室群组表服务接口
 *
 * @author Dao-yang
 * @date: 2024-01-10 09:56:44
 */
public interface ChatGroupService extends IService<ChatGroup> {

    List<ChatGroup> getList(List<Integer> groupIds);

    List<ChatGroupMemberSimResponse> getChatGroupMemberSimList(UserTypeEm userType, Integer groupId, String keywords);

    /**
     * 创建群组
     *
     * @param userType 用户类型
     * @param members  群成员
     * @return 聊天室ID
     */
    String createGroup(UserTypeEm userType, List<Integer> members);


    /**
     * 更新群昵称
     *
     * @param userType 用户类型
     * @param groupId  群ID
     * @param nickname 群昵称
     * @return boolean
     */
    Boolean updateGroupNickname(UserTypeEm userType, Integer groupId, String nickname);

    /**
     * 更新群名称
     *
     * @param userType  用户类型
     * @param groupId   群ID
     * @param groupName 群名称
     * @return boolean
     */
    Boolean updateGroupName(UserTypeEm userType, Integer groupId, String groupName);

    /**
     * 更新群公告
     *
     * @param userType 用户类型
     * @param groupId  群ID
     * @param notice   群公告
     * @return boolean
     */
    Boolean updateGroupNotice(UserTypeEm userType, Integer groupId, String notice);

    /**
     * 移除群成员
     *
     * @param userType 用户类型
     * @param groupId  群ID
     * @param userIds  用户ID
     * @return boolean
     */
    Boolean removeGroupMember(UserTypeEm userType, Integer groupId, List<Integer> userIds);

    /**
     * 退出群聊
     *
     * @param userType 用户类型
     * @param groupId  群组ID
     * @return boolean
     */
    Boolean logoutGroup(UserTypeEm userType, Integer groupId);

    /**
     * 添加群成员
     *
     * @param userType 用户类型
     * @param groupId  群ID
     * @param userIds  用户ID集合
     * @param source   来源
     * @return boolean
     */
    Boolean addGroupMember(UserTypeEm userType, Integer groupId, List<Integer> userIds, GroupSourceEm source);

    /**
     * 申请入群
     *
     * @param userType 用户类型
     * @param groupId  群ID
     * @param source   来源
     * @return boolean
     */
    Boolean applyGroup(UserTypeEm userType, Integer groupId, GroupSourceEm source);

    /**
     * 同意入群申请
     *
     * @param userType 用户类型
     * @param applyId  群ID
     * @return boolean
     */
    Boolean agreeGroupApply(UserTypeEm userType, Integer applyId);

    /**
     * 获取群二维码
     *
     * @param userType 用户类型
     * @param groupId  群ID
     * @return 群二维码
     */
    String getGroupQrcode(UserTypeEm userType, Integer groupId);

    /**
     * 群聊邀请确认标志更新
     *
     * @param userType 用户类型
     * @param groupId  群ID
     * @param flag     是否需要确认
     * @return true:更新成功;false:更新失败
     */
    Boolean updateInviteCfm(UserTypeEm userType, Integer groupId, Boolean flag);

    /**
     * 群主转让
     *
     * @param userType 用户类型
     * @param groupId  群ID
     * @param userId   用户ID
     * @return true:变更成功;false:变更失败
     */
    Boolean groupLeaderTransfer(UserTypeEm userType, Integer groupId, Integer userId);

    /**
     * 解散群聊
     *
     * @param userType 用户类型
     * @param groupId  群ID
     * @return true:变更成功;false:变更失败
     */
    Boolean dissolveGroup(UserTypeEm userType, Integer groupId);

    /**
     * 移除群管理员
     *
     * @param userType 用户类型
     * @param groupId  群ID
     * @param userId   用户ID
     * @return bool
     */
    Boolean removeGroupManager(UserTypeEm userType, Integer groupId, Integer userId);

    /**
     * 添加群管理员
     *
     * @param userType 用户类型
     * @param groupId  群ID
     * @param userId  用户ID
     * @return bool
     */
    Boolean addGroupManager(UserTypeEm userType, Integer groupId, Integer userId);

}
