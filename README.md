# 基于Elasticsearch的DEX搜索自动补全和纠错系统

本项目使用Java17和Spring Boot 3实现了基于Elasticsearch的自动补全和纠错功能，可用于Web3 DEX项目的搜索框实现。

## 功能特点

1. **自动补全**：用户在输入框输入文本时，自动提供补全建议
2. **拼写纠错**：当搜索无结果时，可提供纠错建议
3. **完善的示例数据**：预置了多种加密货币的名称和符号数据

## 技术栈

- Java 17
- Spring Boot 3
- Spring Data Elasticsearch
- Elasticsearch 8.x

## 实现方式

1. **Query Completion**：使用Elasticsearch的Prefix查询功能实现自动补全
   - 创建了支持completion类型的索引字段
   - 基于前缀匹配快速查询符合条件的代币名称和符号

2. **Phrase纠错**：使用Elasticsearch的Fuzzy查询功能实现拼写纠错
   - 设置模糊匹配参数提高纠错准确性
   - 根据查询结果分数过滤最相关的纠错结果

## API接口

### 1. 获取自动补全建议

```
GET /api/tokens/suggest?query={查询文本}&size={结果数量}
```

参数说明：
- `query`: 用户输入的查询文本
- `size`: 返回的建议数量，默认为5

返回格式：
```json
{
  "suggestions": ["建议1", "建议2", "建议3"]
}
```

### 2. 获取拼写纠错建议

```
GET /api/tokens/correct?query={查询文本}&size={结果数量}
```

参数说明：
- `query`: 用户输入的查询文本
- `size`: 返回的建议数量，默认为5

返回格式：
```json
{
  "suggestions": ["纠错建议1", "纠错建议2", "纠错建议3"]
}
```

## 运行说明

1. 确保本地已安装并运行Elasticsearch 8.x
2. 克隆本项目并构建
3. 运行应用程序，将自动初始化索引结构和示例数据
4. 访问API进行测试

## Elasticsearch索引结构

索引名称：token

字段说明：
- `id`: 唯一标识符
- `name`: 代币名称
- `symbol`: 代币符号
- `nameSuggest`: 名称自动补全字段（completion类型）
- `symbolSuggest`: 符号自动补全字段（completion类型）

## 示例数据

系统预置了多种加密货币数据，包括：
- 主流加密货币（Bitcoin、Ethereum等）
- DeFi代币（Uniswap、Aave等）
- 稳定币（USDT、USDC等）
- 其他知名代币

## 使用场景

- 用户在DEX平台搜索框输入代币名称或符号时提供智能补全
- 用户输入错误的代币名称或符号时提供纠错建议 