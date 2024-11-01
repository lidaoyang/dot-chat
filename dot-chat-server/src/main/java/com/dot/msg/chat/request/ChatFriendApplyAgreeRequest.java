package com.dot.msg.chat.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;

/**
 * 聊天室新同意好友申请请求对象
 *
 * @author Dao-yang
 * @date: 2024-01-10 09:56:44
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ChatFriendApplyAgreeRequest", description = "聊天室新同意好友申请请求对象")
public class ChatFriendApplyAgreeRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 260395165663474068L;

    /**
     * 申请ID
     */
    @Schema(description = "申请ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "申请ID不能为空")
    private Integer applyId;

    /**
     * 好友昵称
     */
    @Schema(description = "好友昵称", hidden = true)
    @JsonIgnore
    private String nickname;

    /**
     * 好友备注
     */
    @Schema(description = "好友备注")
    private String remark;

    /**
     * 标签(多个标签用英文逗号分割)
     */
    @Schema(description = "标签(多个标签用英文逗号分割)")
    private String label;

}
