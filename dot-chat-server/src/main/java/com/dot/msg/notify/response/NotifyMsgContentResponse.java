package com.dot.msg.notify.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.io.Serial;
import java.io.Serializable;

/**
 * 通知消息内容请求对象
 *
 * @author Dao-yang
 * @date: 2024-01-30 14:44:18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "EntNotifyMsgContentResponse", description = "通知消息内容请求对象")
public class NotifyMsgContentResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 4993582823092430571L;

    /**
     * 关联ID(订单号/发货单号)
     */
    @Schema(description = "关联ID(订单号/发货单号)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "关联ID不能为空")
    private String linkid;

    /**
     * 内容类型(1:采购订单,2:发货订单,3:询价单)
     */
    @Schema(description = "内容类型(1:采购订单,2:发货订单,3:询价单)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "内容类型不能为空")
    private Integer type;

    /**
     * 订单状态(-1:驳回,1:待确认,2:待支付定金,3:待发货,4:待收货,5:待支付尾款,6:已完成)
     */
    @Schema(description = "订单状态(-1:驳回,1:待确认,2:待支付定金,3:待发货,4:待收货,5:待支付尾款,6:已完成)")
    private Integer status;

    /**
     * 通知内容
     */
    @Schema(description = "通知内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "通知内容不能为空")
    private String content;

}
