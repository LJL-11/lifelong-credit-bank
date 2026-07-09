package org.csu.creditbank.ai.config;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
public class EmbeddingStoreConfig {

    private final InMemoryEmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();

    /** 从 application.yml 注入通义千问 EmbeddingModel；Key 无效时 Bean 可能不存在 */
    @Autowired(required = false)
    private EmbeddingModel embeddingModel;

    @Bean
    EmbeddingStore<TextSegment> embeddingStore() {
        return store;
    }

    @Bean
    ContentRetriever contentRetriever() {
        if (embeddingModel == null) {
            log.warn("[AI知识库] EmbeddingModel 未就绪，ContentRetriever 返回空");
            return query -> new ArrayList<>();
        }
        return EmbeddingStoreContentRetriever.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(store)
                .maxResults(3)
                .minScore(0.6)
                .build();
    }

    @PostConstruct
    public void ingestOnStartup() {
        if (embeddingModel == null) {
            log.info("[AI知识库] EmbeddingModel 未就绪，跳过向量化。配置 DASHSCOPE_API_KEY 后自动启用。");
            return;
        }
        try {
            List<Document> docs = loadKnowledgeDocs();
            if (docs.isEmpty()) {
                log.info("[AI知识库] knowledge/ 目录为空，跳过向量化");
                return;
            }
            EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                    .embeddingStore(store)
                    .embeddingModel(embeddingModel)
                    .build();
            ingestor.ingest(docs);
            log.info("[AI知识库] 已加载 {} 篇文档，向量化入库完成", docs.size());
        } catch (Exception e) {
            log.error("[AI知识库] 文档向量化失败: {}", e.getMessage(), e);
        }
    }

    private List<Document> loadKnowledgeDocs() throws IOException {
        List<Document> docs = new ArrayList<>();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:knowledge/*.{txt,md}");
        DocumentParser parser = new TextDocumentParser();
        for (Resource res : resources) {
            try {
                Document doc = parser.parse(res.getInputStream());
                doc.metadata().put("source", res.getFilename());
                docs.add(doc);
                log.info("[AI知识库] 加载文档: {}", res.getFilename());
            } catch (Exception e) {
                log.warn("[AI知识库] 跳过文档 {}: {}", res.getFilename(), e.getMessage());
            }
        }
        return docs;
    }
}
