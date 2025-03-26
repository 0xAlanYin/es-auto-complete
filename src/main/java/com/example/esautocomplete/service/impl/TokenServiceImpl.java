package com.example.esautocomplete.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import co.elastic.clients.elasticsearch.core.search.CompletionSuggest;
import co.elastic.clients.elasticsearch.core.search.CompletionSuggester;
import co.elastic.clients.elasticsearch.core.search.Suggester;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.suggest.Completion;
import org.springframework.data.elasticsearch.core.suggest.response.Suggest;
import org.springframework.stereotype.Service;

import com.example.esautocomplete.model.Token;
import com.example.esautocomplete.repository.TokenRepository;
import com.example.esautocomplete.service.TokenService;

import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TokenServiceImpl implements TokenService {

    private static final String INDEX_NAME = "token";

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Override
    public Token saveToken(Token token) {
        prepareToken(token);
        return tokenRepository.save(token);
    }

    @Override
    public void saveTokens(List<Token> tokens) {
        tokens.forEach(this::prepareToken);
        tokenRepository.saveAll(tokens);
    }

    private void prepareToken(Token token) {
        if (token.getId() == null) {
            token.setId(UUID.randomUUID().toString());
        }

        // 设置name字段的补全建议，添加小写版本以支持大小写不敏感
        String name = token.getName();
        Completion nameCompletion = new Completion(new String[]{name, name.toLowerCase()});
        token.setNameSuggest(nameCompletion);

        // 设置symbol字段的补全建议，添加小写版本以支持大小写不敏感
        String symbol = token.getSymbol();
        Completion symbolCompletion = new Completion(new String[]{symbol, symbol.toLowerCase()});
        token.setSymbolSuggest(symbolCompletion);
    }

    @Override
    public List<String> getSuggestions(String prefix, int size) {
        if (prefix == null || prefix.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            List<String> suggestions = new ArrayList<>();
            String lowerPrefix = prefix.toLowerCase();

            // 使用通配符查询实现大小写不敏感的前缀匹配
            Query namePrefixQuery = new Query.Builder()
                    .wildcard(w -> w.field("name").value(lowerPrefix + "*"))
                    .build();

            Query symbolPrefixQuery = new Query.Builder()
                    .wildcard(w -> w.field("symbol").value(lowerPrefix + "*"))
                    .build();

            Query boolQuery = new Query.Builder()
                    .bool(b -> b.should(namePrefixQuery).should(symbolPrefixQuery))
                    .build();

            NativeQuery searchQuery = NativeQuery.builder()
                    .withQuery(boolQuery)
                    .build();

            SearchHits<Token> searchHits = elasticsearchOperations.search(searchQuery, Token.class);

            // 提取结果
            searchHits.getSearchHits().forEach(hit -> {
                Token token = hit.getContent();
                if (token.getName() != null && token.getName().toLowerCase().contains(lowerPrefix)) {
                    suggestions.add(token.getName());
                }
                if (token.getSymbol() != null && token.getSymbol().toLowerCase().contains(lowerPrefix)) {
                    suggestions.add(token.getSymbol());
                }
            });

            return suggestions.stream().distinct().limit(size).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取自动补全建议时发生错误", e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<String> getCorrections(String text, int size) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            List<String> corrections = new ArrayList<>();
            String lowerText = text.toLowerCase();

            // 首先尝试精确匹配小写文本
            Query exactLowerMatchName = new Query.Builder()
                    .match(m -> m.field("name").query(lowerText))
                    .build();

            Query exactLowerMatchSymbol = new Query.Builder()
                    .match(m -> m.field("symbol").query(lowerText))
                    .build();

            // 然后使用fuzzy查询实现纠错功能
            Query fuzzyNameQuery = new Query.Builder()
                    .fuzzy(f -> f
                            .field("name")
                            .value(lowerText)
                            .fuzziness("AUTO")
                            .prefixLength(1)
                            .maxExpansions(50))
                    .build();

            Query fuzzySymbolQuery = new Query.Builder()
                    .fuzzy(f -> f
                            .field("symbol")
                            .value(lowerText)
                            .fuzziness("AUTO")
                            .prefixLength(1)
                            .maxExpansions(50))
                    .build();

            Query boolQuery = new Query.Builder()
                    .bool(b -> b
                            .should(exactLowerMatchName)
                            .should(exactLowerMatchSymbol)
                            .should(fuzzyNameQuery)
                            .should(fuzzySymbolQuery))
                    .build();

            NativeQuery searchQuery = NativeQuery.builder()
                    .withQuery(boolQuery)
                    .build();

            SearchHits<Token> searchHits = elasticsearchOperations.search(searchQuery, Token.class);

            // 提取结果
            searchHits.getSearchHits().forEach(hit -> {
                Token token = hit.getContent();
                if (token.getName() != null) {
                    corrections.add(token.getName());
                }
                if (token.getSymbol() != null) {
                    corrections.add(token.getSymbol());
                }
            });

            return corrections.stream().distinct().limit(size).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取纠错建议时发生错误", e);
            return Collections.emptyList();
        }
    }

    @Override
    public void initSampleData() {
        try {
            log.info("开始初始化示例数据...");

            List<Token> tokens = new ArrayList<>();

            // 主流加密货币
            tokens.add(Token.builder().name("Bitcoin").symbol("BTC").build());
            tokens.add(Token.builder().name("Ethereum").symbol("ETH").build());
            tokens.add(Token.builder().name("Binance Coin").symbol("BNB").build());
            tokens.add(Token.builder().name("Solana").symbol("SOL").build());
            tokens.add(Token.builder().name("Cardano").symbol("ADA").build());
            tokens.add(Token.builder().name("XRP").symbol("XRP").build());
            tokens.add(Token.builder().name("Polkadot").symbol("DOT").build());
            tokens.add(Token.builder().name("Dogecoin").symbol("DOGE").build());
            tokens.add(Token.builder().name("Avalanche").symbol("AVAX").build());
            tokens.add(Token.builder().name("Shiba Inu").symbol("SHIB").build());
            tokens.add(Token.builder().name("Polygon").symbol("MATIC").build());
            tokens.add(Token.builder().name("Litecoin").symbol("LTC").build());
            tokens.add(Token.builder().name("Chainlink").symbol("LINK").build());
            tokens.add(Token.builder().name("Uniswap").symbol("UNI").build());
            tokens.add(Token.builder().name("Bitcoin Cash").symbol("BCH").build());
            tokens.add(Token.builder().name("Stellar").symbol("XLM").build());
            tokens.add(Token.builder().name("Cosmos").symbol("ATOM").build());
            tokens.add(Token.builder().name("Monero").symbol("XMR").build());
            tokens.add(Token.builder().name("Ethereum Classic").symbol("ETC").build());
            tokens.add(Token.builder().name("Filecoin").symbol("FIL").build());

            // DeFi 代币
            tokens.add(Token.builder().name("Compound").symbol("COMP").build());
            tokens.add(Token.builder().name("Aave").symbol("AAVE").build());
            tokens.add(Token.builder().name("Maker").symbol("MKR").build());
            tokens.add(Token.builder().name("PancakeSwap").symbol("CAKE").build());
            tokens.add(Token.builder().name("SushiSwap").symbol("SUSHI").build());
            tokens.add(Token.builder().name("Curve DAO Token").symbol("CRV").build());
            tokens.add(Token.builder().name("Synthetix").symbol("SNX").build());
            tokens.add(Token.builder().name("yearn.finance").symbol("YFI").build());
            tokens.add(Token.builder().name("Balancer").symbol("BAL").build());
            tokens.add(Token.builder().name("1inch").symbol("1INCH").build());

            // 稳定币
            tokens.add(Token.builder().name("Tether").symbol("USDT").build());
            tokens.add(Token.builder().name("USD Coin").symbol("USDC").build());
            tokens.add(Token.builder().name("Binance USD").symbol("BUSD").build());
            tokens.add(Token.builder().name("Dai").symbol("DAI").build());
            tokens.add(Token.builder().name("TrueUSD").symbol("TUSD").build());

            // 其他知名代币
            tokens.add(Token.builder().name("Tron").symbol("TRX").build());
            tokens.add(Token.builder().name("The Sandbox").symbol("SAND").build());
            tokens.add(Token.builder().name("Decentraland").symbol("MANA").build());
            tokens.add(Token.builder().name("Axie Infinity").symbol("AXS").build());
            tokens.add(Token.builder().name("Basic Attention Token").symbol("BAT").build());

            saveTokens(tokens);

            log.info("示例数据初始化完成，共添加 {} 条记录", tokens.size());
        } catch (Exception e) {
            log.error("初始化示例数据时发生错误", e);
        }
    }

    /**
     * 重新索引所有Token数据，确保大小写不敏感匹配生效
     */
    @Override
    public void reindexAllTokens() {
        try {
            log.info("开始重新索引所有Token数据...");

            // 获取所有Token
            Iterable<Token> allTokens = tokenRepository.findAll();
            List<Token> tokenList = new ArrayList<>();
            allTokens.forEach(tokenList::add);

            if (tokenList.isEmpty()) {
                log.info("没有找到Token数据，无需重新索引");
                return;
            }

            // 重新准备并保存所有Token
            tokenList.forEach(this::prepareToken);
            tokenRepository.saveAll(tokenList);

            log.info("重新索引完成，共处理 {} 条数据", tokenList.size());
        } catch (Exception e) {
            log.error("重新索引Token数据时发生错误", e);
        }
    }

    @Override
    public List<String> getPhraseCorrections(String text, int size) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            List<String> corrections = new ArrayList<>();
            String lowerText = text.toLowerCase();

            // 构建 Phrase Suggester 查询
            NativeQuery searchQuery = NativeQuery.builder()
                    .withQuery(new Query.Builder()
                            .match(m -> m
                                    .field("name")
                                    .query(lowerText)
                                    .fuzziness("AUTO")
                                    .prefixLength(1)
                                    .operator(co.elastic.clients.elasticsearch._types.query_dsl.Operator.And))
                            .build())
                    .withQuery(new Query.Builder()
                            .match(m -> m
                                    .field("symbol")
                                    .query(lowerText)
                                    .fuzziness("AUTO")
                                    .prefixLength(1)
                                    .operator(co.elastic.clients.elasticsearch._types.query_dsl.Operator.And))
                            .build())
                    .build();

            SearchHits<Token> searchHits = elasticsearchOperations.search(searchQuery, Token.class);

            // 提取结果
            searchHits.getSearchHits().forEach(hit -> {
                Token token = hit.getContent();
                if (token.getName() != null) {
                    corrections.add(token.getName());
                }
                if (token.getSymbol() != null) {
                    corrections.add(token.getSymbol());
                }
            });

            return corrections.stream().distinct().limit(size).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取短语纠错建议时发生错误", e);
            return Collections.emptyList();
        }
    }

} 