package com.hln.aiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 基于内存的向量数据库
 */
@Configuration
public class LoveAppVecStoreConfig {
    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Bean
    VectorStore loveAppVectorStore(EmbeddingModel dashscopeEmbeddingStore) {
        // 加载文档
        List<Document> documents = loveAppDocumentLoader.loadDocuments();

        // 创建基于内存的向量数据库，并将本地文档存储到向量数据库中
        SimpleVectorStore simpleVectorStoreBuilder = SimpleVectorStore.builder(dashscopeEmbeddingStore).build();
        simpleVectorStoreBuilder.add(documents);
        return simpleVectorStoreBuilder;
    }
}
