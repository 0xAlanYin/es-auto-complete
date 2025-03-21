package com.example.esautocomplete.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import com.example.esautocomplete.model.Token;

@Repository
public interface TokenRepository extends ElasticsearchRepository<Token, String> {
} 