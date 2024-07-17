package com.info7255.medicalplanapi.service;

import com.fasterxml.jackson.databind.JsonNode;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.xcontent.XContentBuilder;
import org.elasticsearch.xcontent.XContentFactory;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

@Service
public class KafkaConsumerService {

    private KafkaConsumer_ESService service;
    private static LinkedHashMap<String, Map<String, Object>> MapOfDocuments = new LinkedHashMap<>();
    private static ArrayList<String> listOfKeys = new ArrayList<>();

    @Value("${spring.kafka.topic.name}")
    private String topicName;

    @Value("${spring.elasticsearch.index-name}")
    private String INDEX_NAME;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupID;

    @KafkaListener(topics = "${spring.kafka.topic.name}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(ConsumerRecord<String, String> record) throws IOException {
        System.out.println("Consumed message: Key" + record.key().toString());

        String operation = record.key().toString();
        String body = record.value().toString();

        JSONObject jsonBody = new JSONObject(body);

        switch (operation) {
            case "SAVE": {
                service.postDocument(jsonBody);
                break;
            }
            case "DELETE": {
                service.deleteDocument(jsonBody);
                break;
            }
        }
    }


}
