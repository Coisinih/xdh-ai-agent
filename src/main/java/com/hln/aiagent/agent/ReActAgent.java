package com.hln.aiagent.agent;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * ReAct(Reasoning and Acting) 模式的代理抽象类
 * 实现了思考-行动的循环模式
 */
@EqualsAndHashCode(callSuper = true)
@Slf4j
@Data
public abstract class ReActAgent extends BaseAgent {
    /**
     * 当前步骤的思考内容，由 think() 填充
     */
    private String currentThought = "";

    protected void setCurrentThought(String currentThought) {
        this.currentThought = currentThought;
    }

    /**
     * 处理当前状态，并决定下一步行动
     * @return  是否需要行动
     */
    public abstract boolean think();

    /**
     * 执行决定的行动
     * @return  行动执行结果
     */
    public abstract String act();


    @Override
    public String step() {
        try {
            if (!think()) {
                return "思考完成，无需行动。";
            }
            return act();
        } catch (Exception e) {
            log.info("Agent {} 步骤执行异常", this.getName(), e);
            return "Agent 步骤执行异常: " + e.getMessage();
        }
    }
}
