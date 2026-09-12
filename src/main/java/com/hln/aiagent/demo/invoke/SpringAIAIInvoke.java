package com.hln.aiagent.demo.invoke;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 通过 Spring AI api 调用千问大模型
 */
@Component
public class SpringAIAIInvoke implements CommandLineRunner {
    @Resource
    private ChatModel dashScopeChatModel;

    @Override
    public void run(String... args) {
        AssistantMessage output = dashScopeChatModel.call(new Prompt("你是谁？")).getResult().getOutput();
        System.out.println(output.toString());
    }
}
