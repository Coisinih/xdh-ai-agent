package com.hln.aiagent.demo.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.KeywordMetadataEnricher;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 自定义元信息关键词提取增强器
 */
@Component
public class MyKeywordEnricher {

    @Resource
    private ChatModel dashscopeChatModel;

    public List<Document> keywordEnrich(List<Document> documents) {
        KeywordMetadataEnricher metadataEnricher = new KeywordMetadataEnricher(dashscopeChatModel, 3);
        return metadataEnricher.apply(documents);
    }
}
