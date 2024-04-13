package com.info7255.medicalplanapi.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import redis.clients.jedis.Jedis;

import javax.print.attribute.standard.JobKOctets;

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
            Object value = jsonObject.get(key);//traverse all the elements in this jsonObject

            if(value instanceof JSONObject){//it is planCostShares
                Map<String, Map<String, Object>> subMap = jsonToMap((JSONObject) value);//it will return a map
                //subMap structure:
                // key: "objectId"+"objectType"
                // value:
                //    "deductible": 2000,
                //    "_org": "peggy.com",
                //    "copay": 23,
                //    "objectId": "1234vxc2324sdf-501",
                //    "objectType": "membercostshare"
                for(String subKey : subMap.keySet()){// actually it just one key in subMap
                    Map<String, Object> subContentMap = subMap.get(subKey);
                    subKey = subContentMap.get("objectType")+":"+subContentMap.get("objectId");
                    jedis.set(redisKey+":"+key, subKey);//be aware of sadd, hset and set
                }// this part store links: (look like)
                    // key:
                    // objectType: objectId: planCostShares:
                    // value:
                    // deductible,
                    // key:
                    // objectType: objectId: planCostShares:
                    // value:
                    // _org
            }else if(value instanceof JSONArray){//it is linkedPlanServices
                ArrayList<Object> result = jsonToList((JSONArray)value);

                Map<String, Map<String, Object>> resultMap = new HashMap<>();

                // Iterate through the ArrayList and convert each object to Map<String, Map<String, Object>>
                for (Object obj : result) {
                    // Assuming 'obj' is a Map<String, Map<String, Object>>
                    Map<String, Map<String, Object>> subMap = (Map<String, Map<String, Object>>) obj;
                    for(String subKey : subMap.keySet()){// actually it just one key in subMap
                        Map<String, Object> subContentMap = subMap.get(subKey);
                        subKey = subContentMap.get("objectType")+":"+subContentMap.get("objectId");
                        jedis.sadd(redisKey+":"+key, subKey);//be aware of sadd, hset and set
                    }
                }
            }else{//they are "_org", "objectId", "objectType" etc
                jedis.hset(redisKey, key, value.toString());
                contentMap.put(key, value);
                map.put(redisKey, contentMap);
            }
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

    public ArrayList<Object> jsonToList(JSONArray jsonArray){//recursion until all the returns are JSON object type
        ArrayList<Object> result = new ArrayList<>();
        for(Object value:jsonArray){
            if(value instanceof JSONObject){
                result.add(jsonToMap((JSONObject) value));
            }else if(value instanceof JSONArray){
                result.add(jsonToList((JSONArray) value));
            }
        }return result;
    }
}
