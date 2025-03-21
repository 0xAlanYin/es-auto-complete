#!/bin/bash

# 检查Elasticsearch
echo "检查Elasticsearch状态..."
ELASTICSEARCH_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:9200)

if [ "$ELASTICSEARCH_STATUS" != "200" ]; then
    echo "Elasticsearch未运行，请先启动Elasticsearch服务"
    exit 1
fi

echo "Elasticsearch已启动，状态正常"

# 编译和运行Spring Boot应用
echo "开始编译项目..."
./mvnw clean package -DskipTests

if [ $? -eq 0 ]; then
    echo "编译成功，开始运行应用..."
    java -jar target/es-auto-complete-0.0.1-SNAPSHOT.jar
else
    echo "编译失败，请检查项目代码"
    exit 1
fi 