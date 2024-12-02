package com.dot.msg.notify.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.dot.msg.notify.em.NotifyBizEm;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 发送企业通知消息请求对象
 *
 * @author Dao-yang
 * @date: 2024-01-30 14:44:18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "EntNotifyMsgSendRequest", description = "发送企业通知消息请求对象")
public class EntNotifyMsgSendRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 4993582823092430571L;

    /**
     * 通知业务(BIZ_ENT_ORDER:企业订单业务;BIZ_SUPP_ORDER:供应商订单业务)
     */
    @Schema(description = "通知业务(BIZ_ENT_ORDER:企业订单业务;BIZ_SUPP_ORDER:供应商订单业务)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "通知业务不能为空")
    private NotifyBizEm notifyBiz;

    /**
     * 通知业务关联企业ID集合
     */
    @Schema(description = "通知业务关联企业ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "通知业务关联企业ID不能为空")
    private Integer enterpriseId;

    /**
     * 通知内容,json格式
     */
    @Schema(description = "通知内容,json格式", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "通知内容不能为空")
    private EntNotifyMsgContentRequest msgContent;

}
