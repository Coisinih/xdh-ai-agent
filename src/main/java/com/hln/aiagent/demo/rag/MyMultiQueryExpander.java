package com.hln.aiagent.demo.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 自定义多查询扩展器
 */
@Component
public class MyMultiQueryExpander {

    @Resource
    private ChatModel dashscopeChatModel;

    public List<Query> expand(String query) {
        MultiQueryExpander queryExpander = MultiQueryExpander.builder()
                .numberOfQueries(3) // 设置需要扩展的prompt数量
                .chatClientBuilder(ChatClient.builder(dashscopeChatModel))
                .build();
        return queryExpander.expand(new Query(query));
    }
}
