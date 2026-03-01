package com.hln.aiagent.demo.invoke;

import dev.langchain4j.community.model.dashscope.QwenChatModel;

public class LangChain4JAiInvoke {
    public static void main(String[] args) {
        QwenChatModel qwenModel = QwenChatModel.builder()
                .apiKey(TestApiKey.API_KEY)
                .modelName("qwen-plus")
                .build();
        String answer = qwenModel.chat("你是谁？");
        System.out.println(answer);
    }
}

