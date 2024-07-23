package com.info7255.medicalplanapi.service;

import com.info7255.medicalplanapi.service.KafkaConsumer_ESService;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;


@Service
public class KafkaConsumerService {

    @Autowired
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
        System.out.println("Consumed message: " + record.key().toString());

        String operation = record.key().toString();
        String body = record.value().toString();

        JSONObject jsonBody = new JSONObject(body);
        String id = jsonBody.getString("objectId");
        switch (operation) {
            case "SAVE": {
                System.out.println("SAVE");
                service.postDocument(jsonBody, id);
                break;
            }
            case "DELETE": {
                service.deleteDocument(jsonBody, id);
                break;
            }
        }
    }


}
