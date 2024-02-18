package com.info7255.medicalplanapi.service;

import org.json.JSONObject;
import org.springframework.data.redis.connection.ReactiveStringCommands;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.BasicPermission;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
public class ETagService {
    public String getETag(JSONObject jsonObject){
        // Generate the ETag of json object
        String encoded = null;
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(jsonObject.toString().getBytes(StandardCharsets.UTF_8));
            encoded = Base64.getEncoder().encodeToString(hash);
        }catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        return "\""+encoded+"\"";
    }
}
