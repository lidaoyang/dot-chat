package com.dot.deepseek.entity;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * DeepSeek AI 接口请求Body对象
 *
 * @author: Dao-yang.
 * @date: Created in 2025/2/6 10:15
 */
@Data
public class DSChatRequestBody {

    /**
     * 消息列表
     */
    private List<DSChatRequestMessage> messages;

    /**
     * 模型,使用的模型的 ID。
     * deepseek-chat 模型已全面升级为 DeepSeek-V3，接口不变。 通过指定 model='deepseek-chat' 即可调用 DeepSeek-V3。
     * <p>
     * deepseek-reasoner 是 DeepSeek 最新推出的推理模型 DeepSeek-R1。通过指定 model='deepseek-reasoner'，即可调用 DeepSeek-R1。
     */
    private String model = "deepseek-chat";

    /**
     * 介于 -2.0 和 2.0 之间的数字。如果该值为正，那么新 token 会根据其在已有文本中的出现频率受到相应的惩罚，降低模型重复相同内容的可能性。
     * 默认值为 0。
     */
    private int frequency_penalty;

    /**
     * 介于 1 到 8192 间的整数，限制一次请求中模型生成 completion 的最大 token 数。输入 token 和输出 token 的总长度受模型的上下文长度的限制。
     * <p>
     * 如未指定 max_tokens参数，默认使用 4096。
     */
    private Integer max_tokens;

    /**
     * 介于 -2.0 和 2.0 之间的数字。如果该值为正，那么新 token 会根据其是否已在已有文本中出现受到相应的惩罚，从而增加模型谈论新主题的可能性。
     * 默认值为 0。
     */
    private int presence_penalty;

    /**
     * 一个 object，指定模型必须输出的格式。
     * <p>
     * 设置为 { "type": "json_object" } 以启用 JSON 模式，该模式保证模型生成的消息是有效的 JSON。
     * <p>
     * 注意: 使用 JSON 模式时，你还必须通过系统或用户消息指示模型生成 JSON。否则，模型可能会生成不断的空白字符，直到生成达到令牌限制，从而导致请求长时间运行并显得“卡住”。
     * 此外，如果 finish_reason="length"，这表示生成超过了 max_tokens 或对话超过了最大上下文长度，消息内容可能会被部分截断。
     * Possible values: [text, json_object]
     * Default value: text
     */
    private Map<String, String> response_format = Map.of("type", "text");

    /**
     * 如果设置为 True，将会以 SSE（server-sent events）的形式以流式发送消息增量。消息流以 data: [DONE] 结尾。
     * 默认值为 false。
     */
    private boolean stream;
}
