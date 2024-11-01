package com.dot.msg.chat.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.dot.comm.constants.CommConstant;
import com.dot.comm.em.PageFlippingTypeEm;
import com.dot.msg.chat.tio.em.MsgTypeEm;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;

/**
 * 消息记录搜索请求对象
 *
 * @author Dao-yang
 * @date: 2024-01-10 09:56:44
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ChatMsgSearchRequest", description = "消息记录搜索请求对象")
public class ChatMsgSearchRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1725781086462862292L;

    /**
     * 会话id
     */
    @Schema(description = "会话id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "会话id不能为空")
    private String chatId;

    /**
     * 消息类型(TEXT:文本;PRODUCT:商品;IMAGE:图片;VIDEO:视频;FILE:文件;BIZ_CARD:个人名片;GROUP_BIZ_CARD:群名片;)
     */
    @Schema(description = "消息类型(PRODUCT:商品;IMAGE:图片;VIDEO:视频;FILE:文件;)")
    private MsgTypeEm msgType;

    /**
     * 聊天信息ID,下一页或定位聊天信息ID
     */
    @Schema(description = "聊天信息ID,下一页或定位聊天信息ID")
    private Integer msgId;

    /**
     * 翻页类型(PULL_UP:上拉翻页;PULL_DOWN:下拉翻页;LOCATE:定位翻页;FIRST:首次翻页;)
     */
    @Schema(description = "翻页类型(PULL_UP:上拉翻页;PULL_DOWN:下拉翻页;LOCATE:定位翻页;FIRST:首次翻页;)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "翻页类型不能为空")
    private PageFlippingTypeEm pageFlippingType;

    /**
     * 发送用户id
     */
    @Schema(description = "发送用户id")
    private Integer sendUserId;

    /**
     * 发送用户id
     */
    @Schema(description = "发送日期")
    private String sendDate;

    public String getSendDateStart() {
        if (sendDate != null) {
            return sendDate + " 00:00:00";
        }
        return null;
    }

    public String getSendDateEnd() {
        if (sendDate != null) {
            return sendDate + " 23:59:59";
        }
        return null;
    }

    /**
     * 搜索关键词
     */
    @Schema(description = "搜索关键词")
    private String keywords;

    /**
     * 每页大小
     */
    @Schema(description = "每页大小")
    private int limit = CommConstant.DEFAULT_LIMIT;
}
