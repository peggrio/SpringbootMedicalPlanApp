package com.info7255.medicalplanapi.service;

import com.info7255.medicalplanapi.model.ErrorResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import com.info7255.medicalplanapi.errorHandler.*;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Value;
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

    public void postDocument(JSONObject plan, String id) throws IOException {
        if (!idExists(INDEX_NAME, id)) {
            IndexRequest request = new IndexRequest(INDEX_NAME, "_doc", id);
            request.source(plan, XContentType.JSON);
            try {
                System.out.println("Hello again!");
                restHighLevelClient.index(request, RequestOptions.DEFAULT);
            } catch (IOException e) {
                System.out.println("postDocument error:"+e.getMessage());
            }
        }else{
            throw new BadRequestException("POST: index already existed");
        }
    }

    public boolean idExists(String INDEX_NAME, String id) throws IOException {
        GetRequest getRequest = new GetRequest(INDEX_NAME, id);
        return restHighLevelClient.exists(getRequest, RequestOptions.DEFAULT);
    }

    public void deleteDocument(JSONObject plan, String id) throws IOException {
        if (!idExists(INDEX_NAME, id)) {
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
