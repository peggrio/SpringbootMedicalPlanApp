package com.info7255.medicalplanapi.model;

import org.apache.http.HttpHost;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.RestClient;

import java.util.Arrays;


@Configuration
public class ElasticSearchConfig {

    @Value("${spring.elasticsearch.uris}")
    private String[] elasticsearchUris;

    @Value("${spring.elasticsearch.username}")
    private String username;

    @Value("${spring.elasticsearch.password}")
    private String password;

    private RestHighLevelClient restHighLevelClient;

    @Bean
    public RestHighLevelClient buildClient() {
        try {
            restHighLevelClient = new RestHighLevelClient(
                    RestClient.builder(
                            new HttpHost("localhost", 9200, "http"),
                            new HttpHost("localhost", 9201, "http")));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return restHighLevelClient;
    }
}
