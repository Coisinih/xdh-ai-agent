package com.hln.aiagent.agent;


import com.hln.aiagent.constant.AgentState;
import io.micrometer.common.util.StringUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.ArrayList;
import java.util.List;


/**
 * 抽象基础代理类，用于管理代理状态和执行流程，
 * 提供状态转换、内存管理和基于步骤的执行循环的基础功能。
 * 子类必须实现step方法。
 */
@Data
@Slf4j
public abstract class BaseAgent {
    // 智能体名字
    private String name;

    // 提示词
    private String systemPrompt;
    private String nextStepsPrompt;

    // 执行状态
    private AgentState state = AgentState.IDLE;

    // 流程控制
    private int maxSteps = 10;
    private int currentStep = 0;

    // LLM
    private ChatClient chatClient;

    // 自主维护上下文的会话记忆
    private List<Message> messagesList = new ArrayList<>();

    public String run(String userPrompt) {
        // 1.参数校验
        if (this.state != AgentState.IDLE) {
            throw new RuntimeException("Can not run agent with state: " + this.state);
        }
        if (StringUtils.isBlank(userPrompt)) {
            throw new RuntimeException("Can not run agent with empty userPrompt");
        }
        List<String> results = null;

        try {
            // 2.执行步骤
            // 将状态修改为运行中
            this.state = AgentState.RUNNING;
            // 将用户消息加入到上下文中
            messagesList.add(new UserMessage(userPrompt));
            // 存放每一步 大模型 的返回结果
            results = new ArrayList<>();
            for (int i = 0; i < maxSteps && this.state == AgentState.RUNNING; i++) {
                this.currentStep = i + 1;
                log.info("current step: {}/{}", currentStep, this.maxSteps);
                // 调用子类实现的 step 方法
                String result = this.step();
                log.info("step{} result: {}", this.currentStep, result);
                results.add(result);
            }

            if( this.currentStep >= this.maxSteps){
                this.state = AgentState.FINISHED;
                log.info("reached max steps: {}", this.maxSteps);
            }
        } catch (Exception e) {
            this.state = AgentState.ERROR;
            log.info("error occurred: {}", e.getMessage());
        } finally {
            this.cleanup();
        }

        // 4.清空资源
        return String.join("\n", results);
    }

    /**
     * 每一步的执行逻辑，由子类实现
     * @return  每一步的执行结果
     */
    public abstract String step();

    protected void cleanup() {
        // 清理资源，由子类实现
    }

}
