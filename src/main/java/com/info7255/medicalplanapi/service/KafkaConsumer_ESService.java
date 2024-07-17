package com.info7255.medicalplanapi.service;

import com.info7255.medicalplanapi.model.ErrorResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import com.info7255.medicalplanapi.errorHandler.*;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;

@Repository
public class KafkaConsumer_ESService {
    private RestHighLevelClient restHighLevelClient;

    @Value("${spring.elasticsearch.index-name}")
    private String INDEX_NAME;

    public KafkaConsumer_ESService(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
    }

    public void postDocument(JSONObject plan) throws IOException {
        if (!indexExists()) {
            IndexRequest request = new IndexRequest(INDEX_NAME, "_doc");
            request.source(plan, XContentType.JSON);
            try {
                restHighLevelClient.index(request, RequestOptions.DEFAULT);
            } catch (IOException e) {
                System.out.println("postDocument error:"+e.getMessage());
            }
        }else{
            throw new BadRequestException("POST: index already existed");
        }
    }

    public boolean indexExists() throws IOException {
        GetIndexRequest request = new GetIndexRequest(INDEX_NAME);
        return restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
    }

    public void deleteDocument(JSONObject plan) throws IOException {
        if (!indexExists()) {
            try {
                DeleteRequest deleteRequest = new DeleteRequest(INDEX_NAME, "_doc");
                restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
            } catch (IOException e) {
                throw new BadRequestException("error when deleting entries in elasticsearch");
            }
        }else{
            throw new BadRequestException("DELETE: index does not existed");
        }
    }
}
