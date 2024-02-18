package com.info7255.medicalplanapi.service;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import redis.clients.jedis.Jedis;

@Service
public class PlanService {

    //define redis object
    private final Jedis jedis;
    private final ETagService eTagService;

    public PlanService(Jedis jedis, ETagService eTagService){
        this.jedis = jedis;
        this.eTagService = eTagService;
    }
    public boolean isKeyPresent(String key){
        Map<String, String> value = jedis.hgetAll(key);
        jedis.close();
        return !(value == null || value.isEmpty());
    }

    public Map<String, Map<String, Object>> jsonToMap(JSONObject jsonObject){
        Map<String, Map<String, Object>> map = new HashMap<>();
        Map<String, Object> contentMap = new HashMap<>();

        for(String key : jsonObject.keySet()){
            String redisKey = jsonObject.get("objectType") + ":" + jsonObject.get("objectId");
            Object value = jsonObject.get(key);
            jedis.hset(redisKey, key, value.toString());
            contentMap.put(key, value);
            map.put(redisKey, contentMap);
        }
        return map;
    }

    public String createPlan(JSONObject plan, String key){
        jsonToMap(plan);
        return setETag(key, plan);
    }

    public String setETag(String key, JSONObject jsonObject){
        String eTag = eTagService.getETag(jsonObject);
        jedis.hset(key, "eTag", eTag);
        return eTag;
    }

    public String getETag(String key){
        return jedis.hget(key, "eTag");
    }

    public Map<String, Object> getPlan(String redisKey){
        Map<String, Object> result = new HashMap<>();
        Set<String> keys = jedis.keys(redisKey);
        for(String key: keys){
            System.out.println(key);
            Map<String, String> value = jedis.hgetAll(key);
            result.put(redisKey,value);
        }
        return result;
    }

    public Map<String, Object> deletePlan(String redisKey) {
        Set<String> keys = jedis.keys(redisKey);
        Map<String, Object> result = new HashMap<>();
        for (String key : keys) {
            System.out.println(key);
            Map<String, String> value = jedis.hgetAll(key);
            result.put(redisKey,value);
        }
        jedis.del(redisKey);
        return result;
    }

    public boolean isInteger(String s){
        try{
            Integer.parseInt(s);
        }catch(Exception e){
            return false;
        }
        return true;
    }
}
