package com.dot.sys.user.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 企业信息表
 * @author  Dao-yang
 * @date: 2023-02-19 15:08:34
 */

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name="EnterpriseSimResponse", description="企业信息ID name信息")
public class EnterpriseSimResponse implements Serializable {

	@Serial
	private static final long serialVersionUID =  6713740255759208081L;

	/**
	 * 企业id
	 */
	@Schema(description = "企业id")
	@JsonProperty("id")
	private Integer enterpriseId;

	/**
	 * 企业名称
	 */
	@JsonProperty("value")
	@Schema(description = "企业名称")
	private String name;

	/**
	 * 行业分类(1:礼品行业;2:医疗行业)
	 */
	@Schema(description = "行业分类(1:礼品行业;2:医疗行业)")
	private Integer industryType;
}
