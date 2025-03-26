package com.example.esautocomplete.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.esautocomplete.dto.SuggestResponse;
import com.example.esautocomplete.service.TokenService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/tokens")
@CrossOrigin(origins = "*")
public class TokenController {

    @Autowired
    private TokenService tokenService;

    /**
     * 获取自动补全建议
     * 
     * @param query 查询前缀
     * @param size 建议数量，默认为5
     * @return 补全建议列表
     */
    @GetMapping("/suggest")
    public ResponseEntity<SuggestResponse> getSuggestions(
            @RequestParam("query") String query,
            @RequestParam(value = "size", defaultValue = "5") int size) {
        
        log.info("接收到自动补全请求: query={}, size={}", query, size);
        
        return ResponseEntity.ok(SuggestResponse.builder()
                .suggestions(tokenService.getSuggestions(query, size))
                .build());
    }

    /**
     * 获取纠错建议
     * 
     * @param query 查询文本
     * @param size 建议数量，默认为5
     * @return 纠错建议列表
     */
    @GetMapping("/correct")
    public ResponseEntity<SuggestResponse> getCorrections(
            @RequestParam("query") String query,
            @RequestParam(value = "size", defaultValue = "5") int size) {
        
        log.info("接收到纠错请求: query={}, size={}", query, size);
        
        return ResponseEntity.ok(SuggestResponse.builder()
                .suggestions(tokenService.getCorrections(query, size))
                .build());
    }

    /**
     * 获取短语纠错建议
     * 
     * @param query 查询文本
     * @param size 建议数量，默认为5
     * @return 纠错建议列表
     */
    @GetMapping("/phrase-correct")
    public ResponseEntity<SuggestResponse> getPhraseCorrections(
            @RequestParam("query") String query,
            @RequestParam(value = "size", defaultValue = "5") int size) {
        
        log.info("接收到短语纠错请求: query={}, size={}", query, size);
        
        return ResponseEntity.ok(SuggestResponse.builder()
                .suggestions(tokenService.getPhraseCorrections(query, size))
                .build());
    }
} 