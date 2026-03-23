package com.hln.aiagent.rag;

import com.hln.aiagent.demo.rag.MyKeywordEnricher;
import com.hln.aiagent.demo.rag.MyTokenTextSplitter;
import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

/**
 * 基于内存的向量数据库
 */
@Configuration
public class LoveAppVecStoreConfig {
    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;
    @Resource
    private MyTokenTextSplitter myTokenTextSplitter;
    @Resource
    private MyKeywordEnricher myKeywordEnricher;

    @Bean
    @Primary
    VectorStore loveAppVectorStore(EmbeddingModel dashscopeEmbeddingStore) {
        // 加载文档
        List<Document> documents = loveAppDocumentLoader.loadDocuments();

        // 创建基于内存的向量数据库，并将本地文档存储到向量数据库中
        SimpleVectorStore simpleVectorStoreBuilder = SimpleVectorStore.builder(dashscopeEmbeddingStore).build();

        // 使用自定义的文本拆分器，将文档拆分成更小的片段
//        List<Document> splitCustomized = myTokenTextSplitter.splitCustomized(documents);

        // 使用自定义的元信息关键词提取增强器，提取文档中的关键词
        List<Document> keywordEnrich = myKeywordEnricher.keywordEnrich(documents);
        simpleVectorStoreBuilder.add(keywordEnrich);
        return simpleVectorStoreBuilder;
    }
}
