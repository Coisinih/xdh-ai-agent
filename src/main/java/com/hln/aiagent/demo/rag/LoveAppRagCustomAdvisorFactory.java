package com.hln.aiagent.demo.rag;

import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;

/**
 * 创建 文档检索过滤增强器 的工厂
 */
public class LoveAppRagCustomAdvisorFactory {

    public static Advisor createAdvisor(VectorStore loveAppVectorStore, String status) {
        Filter.Expression expression = new FilterExpressionBuilder()
                .eq("status", status)
                .build();
        DocumentRetriever retriever = VectorStoreDocumentRetriever.builder()
                .filterExpression(expression)   // 设置过滤条件
                .similarityThreshold(0.5)   // 相似度阈值
                .topK(2)    // 返回最相关的2个文档
                .vectorStore(loveAppVectorStore)
                .build();
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(retriever)
                .queryAugmenter(LoveAppContextualQueryAugmenterFactory.createInstance())
                .build();
    }
}
