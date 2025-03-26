package com.example.esautocomplete.service;

import java.util.List;

import com.example.esautocomplete.model.Token;

public interface TokenService {
    
    /**
     * 保存Token
     * @param token 待保存的Token对象
     * @return 保存后的Token对象
     */
    Token saveToken(Token token);
    
    /**
     * 批量保存Token
     * @param tokens 待保存的Token对象列表
     */
    void saveTokens(List<Token> tokens);
    
    /**
     * 获取自动补全建议
     * @param prefix 前缀
     * @param size 结果数量
     * @return 补全建议列表
     */
    List<String> getSuggestions(String prefix, int size);
    
    /**
     * 获取拼写纠错建议
     * @param text 待纠错文本
     * @param size 结果数量
     * @return 纠错建议列表
     */
    List<String> getCorrections(String text, int size);
    
    /**
     * 初始化示例数据
     */
    void initSampleData();
    
    /**
     * 重新索引所有Token数据，确保大小写不敏感匹配生效
     */
    void reindexAllTokens();
    
    /**
     * 使用 Phrase Suggester 获取短语纠错建议
     * @param text 待纠错文本
     * @param size 结果数量
     * @return 纠错建议列表
     */
    List<String> getPhraseCorrections(String text, int size);
} 