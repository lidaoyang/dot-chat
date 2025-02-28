package com.dot.deepseek.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * DeepSeek AI 请求记录实体
 *
 * @author Dao-yang
 * @date: 2025-02-18 09:44:24
 */
@Data
@TableName("deepseek_req_record")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "DeepseekReqRecord", description = "DeepSeek AI 请求记录")
public class DeepseekReqRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 6251806582062907016L;

    /**
     * ID
     */
    @Schema(description = "ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 访问用户ID
     */
    @Schema(description = "访问用户ID")
    private Integer userId;

    /**
     * 请求信息
     */
    @Schema(description = "请求信息")
    private String reqMsg;

    /**
     * 请求时间
     */
    @Schema(description = "请求时间")
    private String reqTime;

    /**
     * 返回信息
     */
    @Schema(description = "返回信息")
    private String resMsg;

    /**
     * 返回时间
     */
    @Schema(description = "返回时间")
    private String resTime;

}
