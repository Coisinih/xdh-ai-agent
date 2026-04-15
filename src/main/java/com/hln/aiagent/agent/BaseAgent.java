package com.hln.aiagent.agent;


import com.hln.aiagent.constant.AgentState;
import io.micrometer.common.util.StringUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;


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

            if (this.currentStep >= this.maxSteps) {
                this.state = AgentState.FINISHED;
                log.info("reached max steps: {}", this.maxSteps);
            }
        } catch (Exception e) {
            this.state = AgentState.ERROR;
            log.info("error occurred: {}", e.getMessage());
        } finally {
            // 4.清空资源
            this.cleanup();
        }
        return String.join("\n", results);
    }

    public SseEmitter runStream(String userPrompt) {
        SseEmitter emitter = new SseEmitter(300000L);   // 设置五分钟超时时间

        // 使用线程异步处理，避免阻塞主线程
        // 如果没有异步处理的话，要等到执行完后才能返回 SseEmitter，效果和同步返回是一样的
        CompletableFuture.runAsync(() -> {
            // 1.参数校验
            try {
                if (this.state != AgentState.IDLE) {
                    emitter.send("智能体在该状态无法运行：" + this.state);
                    emitter.complete();
                    return;
                }
                if (StringUtils.isBlank(userPrompt)) {
                    emitter.send("智能体无法处理空提示词");
                    emitter.complete();
                    return;
                }
            } catch (IOException e) {
                log.error("error occurred: {}", e.getMessage());
                emitter.completeWithError(e);
            }

            try {
                // 2.执行步骤
                // 将状态修改为运行中
                this.state = AgentState.RUNNING;
                // 将用户消息加入到上下文中
                messagesList.add(new UserMessage(userPrompt));
                // 存放每一步 大模型 的返回结果
                for (int i = 0; i < maxSteps && this.state == AgentState.RUNNING; i++) {
                    this.currentStep = i + 1;
                    log.info("current step: {}/{}", currentStep, this.maxSteps);

                    // 调用子类实现的 step 方法
                    String result = this.step();
                    log.info("step{} result: {}", this.currentStep, result);

                    // 发送每一步结果
                    emitter.send(String.format("step%d result: %s", this.currentStep, result));
                }

                // 检查是否超出步骤限制
                if (this.currentStep >= this.maxSteps) {
                    this.state = AgentState.FINISHED;
                    log.info("reached max steps: {}", this.maxSteps);
                    emitter.send("运行达到最大步骤：" + this.maxSteps);
                }

                // 正常完成
                emitter.complete();
            } catch (Exception e) {
                this.state = AgentState.ERROR;
                emitter.completeWithError(e);
                log.info("error occurred: {}", e.getMessage());
            } finally {
                // 4.清空资源
                this.cleanup();
            }
        });

        // 超时回调
        emitter.onTimeout(() -> {
            this.state = AgentState.ERROR;
            this.cleanup();
            log.warn("Sse connection timeout occurred");
        });

        // 完成时回调
        emitter.onCompletion(() -> {
            if (this.state == AgentState.RUNNING) {
                this.state = AgentState.FINISHED;
            }
            this.cleanup();
            log.info("Sse connection completed");
        });

        return emitter;
    }

    /**
     * 每一步的执行逻辑，由子类实现
     *
     * @return 每一步的执行结果
     */
    public abstract String step();

    protected void cleanup() {
        // 清理资源，由子类实现
    }

}
