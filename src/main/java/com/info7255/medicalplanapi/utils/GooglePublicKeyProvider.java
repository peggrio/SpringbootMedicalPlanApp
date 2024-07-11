package com.info7255.medicalplanapi.utils;

import com.auth0.jwt.interfaces.RSAKeyProvider;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GooglePublicKeyProvider implements RSAKeyProvider {
    private static final String GOOGLE_CERTS_URL = "https://www.googleapis.com/oauth2/v3/certs";
    private final Map<String, RSAPublicKey> publicKeys = new ConcurrentHashMap<>();

    public GooglePublicKeyProvider() throws IOException {
        fetchGooglePublicKeys();
    }

    private void fetchGooglePublicKeys() throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(GOOGLE_CERTS_URL);
        CloseableHttpResponse response = httpClient.execute(httpGet);
        String jsonResponse = EntityUtils.toString(response.getEntity());

        // Parse the JSON response to extract the keys (omitted for brevity)
        // You can use a library like Jackson or Gson to parse the JSON response

        // Add the parsed keys to the publicKeys map
        // publicKeys.put("key_id", parsedRSAPublicKey);
    }

    @Override
    public RSAPublicKey getPublicKeyById(String keyId) {
        return publicKeys.get(keyId);
    }

    @Override
    public RSAPrivateKey getPrivateKey() {
        return null;
    }

    @Override
    public String getPrivateKeyId() {
        return null;
    }
}
