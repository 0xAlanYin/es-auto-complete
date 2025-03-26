package com.example.esautocomplete.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.index.PutTemplateRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import com.example.esautocomplete.model.Token;
import com.example.esautocomplete.service.TokenService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ElasticsearchIndexInitializer {

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;
    
    @Autowired
    private TokenService tokenService;
    
//    @EventListener(ApplicationReadyEvent.class)
    public void initIndices() {
        try {
            log.info("正在检查并初始化Elasticsearch索引...");

            // 删除索引
             elasticsearchOperations.indexOps(Token.class).delete();

            // 检查索引是否存在
            boolean indexExists = elasticsearchOperations.indexOps(Token.class).exists();

            if (!indexExists) {
                log.info("索引不存在，开始创建token索引...");
                createTokenIndex();

                // 初始化示例数据
                log.info("开始初始化样本数据...");
                tokenService.initSampleData();

                log.info("token索引创建完成");
            } else {
                log.info("token索引已存在，执行数据重新索引以确保大小写不敏感匹配生效");
                tokenService.reindexAllTokens();
            }

        } catch (Exception e) {
            log.error("初始化Elasticsearch索引时发生错误", e);
        }
    }
    
    private void createTokenIndex() throws IOException {
        try {
            // 使用Spring Data Elasticsearch提供的方法创建索引
            elasticsearchOperations.indexOps(Token.class).createWithMapping();
            log.info("索引创建成功，使用了@Document和@Field注解配置");
        } catch (Exception e) {
            log.error("创建索引时发生错误", e);
            throw e;
        }
    }
} 