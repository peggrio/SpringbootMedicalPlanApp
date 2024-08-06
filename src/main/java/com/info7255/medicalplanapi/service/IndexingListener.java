package com.info7255.medicalplanapi.service;

import com.info7255.medicalplanapi.errorHandler.BadRequestException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.xcontent.XContentType;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
//import org.elasticsearch.common.xcontent.XContentBuilder;
//import org.elasticsearch.common.xcontent.XContentFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class IndexingListener {
    private final RestHighLevelClient restHighLevelClient;

    @Value("${spring.elasticsearch.index-name}")
    private String INDEX_NAME;
    private static LinkedHashMap<String, Map<String, Object>> MapOfDocuments = new LinkedHashMap<>();
    private static ArrayList<String> listOfKeys = new ArrayList<>();

    public IndexingListener(RestHighLevelClient restHighLevelClient) {

        this.restHighLevelClient = restHighLevelClient;
    }

    private boolean indexExists() throws IOException {
        GetIndexRequest request = new GetIndexRequest(INDEX_NAME);
        return restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
    }

    protected void postDocument(JSONObject plan, String id) throws IOException {
        if (!indexExists()) {
            createElasticIndex();
        }

        IndexRequest request = new IndexRequest(INDEX_NAME, "_doc", id);
        request.source(plan, XContentType.JSON);
        try {
            restHighLevelClient.index(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            System.out.println("postDocument error:"+e.getMessage());
        }
    }

    public void deleteDocument(String id) throws IOException {
        if (indexExists()) {
            try {
                DeleteRequest deleteRequest = new DeleteRequest(INDEX_NAME, id);
                // Define the listener
                ActionListener<DeleteResponse> listener = new ActionListener<DeleteResponse>() {
                    @Override
                    public void onResponse(DeleteResponse deleteResponse) {
                        // Called when the delete operation is successful
                        System.out.println("Document deleted successfully.");
                    }

                    @Override
                    public void onFailure(Exception e) {
                        // Called when the delete operation fails
                        System.err.println("Delete operation failed.");
                        e.printStackTrace();
                    }
                };

                // Perform the asynchronous delete operation
                restHighLevelClient.deleteAsync(deleteRequest, RequestOptions.DEFAULT, listener);

                // Close the client gracefully on application shutdown
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        restHighLevelClient.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }));

            } catch (Exception e) {
                throw new BadRequestException("error when deleting entries in elasticsearch");
            }
        }else{
            throw new BadRequestException("DELETE: index does not existed");
        }
    }

    private void createElasticIndex() throws IOException {
        CreateIndexRequest request = new CreateIndexRequest(INDEX_NAME);
        request.settings(Settings.builder().put("index.number_of_shards", 1).put("index.number_of_replicas", 1));
        CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);

        boolean acknowledged = createIndexResponse.isAcknowledged();
        System.out.println("Index Creation:" + acknowledged);

    }
}
