package com.hln.aiagent.agent;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.itextpdf.styledxmlparser.jsoup.internal.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Slf4j
@Data
public class ToolCallAgent extends ReActAgent {

    // 可用的工具类
    private final ToolCallback[] toolCallback;

    // 工具调用管理器
    private final ToolCallingManager toolCallingManager;

    // 工具调用结果
    private ChatResponse toolCallingResponse;

    // 工具调用参数，禁用内置的工具调用机制，自己维护上下文
    private ChatOptions chatOptions;

    public ToolCallAgent(ToolCallback[] toolCallback) {
        super();
        this.toolCallback = toolCallback;
        this.toolCallingManager = ToolCallingManager.builder().build();
        this.chatOptions = DashScopeChatOptions.builder()
                .withProxyToolCalls(true)   // 是否启用内置的工具调用机制，自主实现工具调用，不将工具调用托管给 Spring AI
                .build();
    }


    /**
     * 处理当前状态，并决定下一步行动
     *
     * @return 是否需要行动
     */
    @Override
    public boolean think() {
        // 如果下一步提示词不为空，就将它加入到消息上下文中
        if (!StringUtil.isBlank(getNextStepsPrompt())) {
            UserMessage userMessage = new UserMessage(getNextStepsPrompt());
            getMessagesList().add(userMessage);
        }

        try {
            // 调用大模型，获取响应
            Prompt prompt = new Prompt(getMessagesList(), chatOptions);
            this.toolCallingResponse = getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
                    .tools(toolCallback)
                    .call().chatResponse();

            AssistantMessage assistantMessage = this.toolCallingResponse.getResult().getOutput();
            String result = assistantMessage.getText();
            List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();
            log.info(getName() + "的思考：" + result);
            log.info(getName() + "选择了 " + toolCallList.size() + " 个工具进行调用。");

            String toolCallingInfo = toolCallList.stream()
                    .map(toolCall -> String.format("工具名称 ：%s，工具参数：%s", toolCall.name(), toolCall.arguments()))
                    .collect(Collectors.joining("\n"));
            log.info(toolCallingInfo);

            // 只有无需调用工具时，才需要记录助手消息
            if (toolCallList.isEmpty()) {
                getMessagesList().add(assistantMessage);
                return false;
            }
            // 需要调用工具时，无需记录助手消息，因为工具调用时会自动记录
            return true;
        } catch (Exception e) {
            log.error("{}思考时遇到了错误：{}", getName(), e.getMessage());
            getMessagesList().add(new AssistantMessage("处理时遇到错误：" + e.getMessage()));
            return false;
        }
    }


    /**
     * 执行决定的行动
     *
     * @return 行动执行结果
     */
    @Override
    public String act() {
        if (!this.toolCallingResponse.hasToolCalls()) {
            return "没有工具需要调用。";
        }

        // 调用工具
        Prompt prompt = new Prompt(getMessagesList(), chatOptions);
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, this.toolCallingResponse);

        // 记录消息上下文，无需追加，conversationHistory 已经包含了助手消息和工具调用返回的结果
        setMessagesList(toolExecutionResult.conversationHistory());

        // 获取工具调用的结果
        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());
        String results = toolResponseMessage.getResponses().stream()
                .map(response -> "工具 " + response.name() + "调用完成，结果：" + response.responseData())
                .collect(Collectors.joining("\n"));
        log.info(results);

        // 判断是否调用了终止工具，如果调用了，则将执行状态改为已完成
        boolean terminateToolCalled = toolResponseMessage.getResponses().stream().anyMatch(response -> "doTerminate".equals(response.name()));
        if(terminateToolCalled) setState(AgentState.FINISHED);

        return results;
    }
}
