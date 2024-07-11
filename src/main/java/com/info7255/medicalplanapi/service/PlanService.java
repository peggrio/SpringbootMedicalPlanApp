package com.info7255.medicalplanapi.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.*;

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
        //store result
        Map<String, Object> resultMap = new HashMap<>();
        Set<String> keys = jedis.keys(redisKey + ":*");//find all sub objects under this key
        keys.add(redisKey);//add itself

        for(String key: keys){
            if(key.equals(redisKey)){
                Map<String, String> contentMap = jedis.hgetAll(key);
                for(String subKeys: contentMap.keySet()){
                    if(!subKeys.equalsIgnoreCase("eTag")){
                        resultMap.put(subKeys, isInteger(contentMap.get(subKeys))? Integer.parseInt(contentMap.get(subKeys)):contentMap.get(subKeys));//convert to Int if needed
                    }
                }
            }else{//they are sub objects, like plan:12xvxc345ssdsds-501:planCostShares
                String newKey = key.substring((redisKey+":").length());//it extracts a substring from the string key, starting from the position equal to the length of the [redisKey:]
                if(!jedis.type(key).equals("string")){// for set type in redis, like plan:12xvxc345ssdsds-501:linkedPlanServices
                    Set<String> members = jedis.smembers(key);
                    List<Object> objects = new ArrayList<>();
                    for(String member: members){
                        objects.add(getPlan(member));
                    }
                    resultMap.put(newKey, objects);
                }else{//for string type in redis, like plan:12xvxc345ssdsds-501:planCostShares
                    //get the only element from set [members]
                    //Iterator<String> iterator = members.iterator();
                    Map<String, String> object = jedis.hgetAll(jedis.get(key));
                    Map<String, Object> nestedMap = new HashMap<>();
                    for(String subKey:object.keySet()){
                        nestedMap.put(subKey, isInteger(object.get(subKey))?Integer.parseInt(object.get(subKey)):object.get(subKey));
                    }

                    resultMap.put(newKey, nestedMap);
                }
            }
        }
        return resultMap;
    }

    public void deletePlan(String redisKey) {
        Set<String> keys = jedis.keys(redisKey + ":*");
        keys.add(redisKey);

        for (String key : keys) {
            System.out.println("print all:"+key);
            if(key.equals(redisKey)){
                jedis.del(new String[]{key});
            }else{
                if(!jedis.type(key).equals("string")){// for set type in redis, like plan:12xvxc345ssdsds-501:linkedPlanServices
                    Set<String> members = jedis.smembers(key);
                    for(String member: members){
                        deletePlan(member);
                    }
                }else{
                    jedis.del(jedis.get(key));
                }
                jedis.del(new String[]{key});
            }
        }
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
