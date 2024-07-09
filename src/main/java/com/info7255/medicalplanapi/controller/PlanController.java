package com.info7255.medicalplanapi.controller;

import com.info7255.medicalplanapi.model.ErrorResponse;
import com.info7255.medicalplanapi.service.PlanService;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import com.info7255.medicalplanapi.errorHandler.*;
import org.everit.json.schema.Schema;

import java.util.*;

@RestController
public class PlanController {
    @Autowired
    private PlanService planService;

    public PlanController(PlanService planService){
        this.planService = planService;
    }

    @PostMapping(path = "/plan", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createPlan(@RequestBody(required = false) String planObject) throws BadRequestException {
        if(planObject == null ||planObject.isBlank()) {
            throw new BadRequestException("Request body is missing!");
        }
        System.out.println(planObject);

        JSONObject plan = new JSONObject(planObject);
        JSONObject schemaJSON = new JSONObject(new JSONTokener(Objects.requireNonNull(PlanController.class.getResourceAsStream("/validate_schema.json"))));

        Schema schema = SchemaLoader.load(schemaJSON);
        try {
            schema.validate(plan);
        } catch(ValidationException e){
            throw new BadRequestException(e.getMessage());
        }

        String key = plan.getString("objectType")+":" + plan.getString("objectId");
        if(planService.isKeyPresent(key))
            throw new BadRequestException("This object already exist.");

        String eTag = planService.createPlan(plan,key);

        //add etag to header
        HttpHeaders headersToSend = new HttpHeaders();
        headersToSend.setETag(eTag);

        return new ResponseEntity<>("{\"objectId\": \"" + plan.getString("objectId") + "\"}", headersToSend, HttpStatus.CREATED);
    }
    @GetMapping(path = "/{objectType}/{objectId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getPlan(@PathVariable String objectType,
                        @PathVariable String objectId,
                        @RequestHeader HttpHeaders headers){

        String key = objectType + ":" + objectId;
        if(!planService.isKeyPresent(key)){
            throw new ResourceNotFoundException("Object not found");
        }

        //check if the ETag provided is not corrupt
        List<String> ifNoneMatch = null;
        try{
            ifNoneMatch = headers.getIfNoneMatch();//if IfNoneMatch doesn't be selected, it will return none
        }catch(Exception e){
            throw new ETagParseException("ETag value invalid! Make sure ETag value is a string");
        }

        String eTag = planService.getETag(key);

        HttpHeaders headersToSend = new HttpHeaders();
        headersToSend.setETag(eTag);

        if(objectType.equals(objectType) && ifNoneMatch.contains(eTag)){
            return new ResponseEntity<>(null, headersToSend, HttpStatus.NOT_MODIFIED);
        }

        Map<String, Object> result = planService.getPlan(key);
        if(objectType.equals(objectType)){
            return new ResponseEntity<>(result, headersToSend, HttpStatus.OK);
        }
        return new ResponseEntity<>(result,HttpStatus.OK);
    }
    @DeleteMapping(path = "/{objectType}/{objectId}")
    public ResponseEntity<?> deletePlan(@PathVariable String objectType,
                                        @PathVariable String objectId,
                                        @RequestHeader HttpHeaders headers) {
        String key = objectType + ":" + objectId;
        if (!planService.isKeyPresent(key)) {
            throw new ResourceNotFoundException("Plan not found");
        }

        String eTag = planService.getETag(key);
        System.out.println("etag:"+eTag);
        List<String> ifMatch;

        try{
            ifMatch = headers.getIfMatch();
        }catch (Exception e){
            throw new ETagParseException("ETag value is invalid! Make sure the ETag value is a string!");
        }

        if(ifMatch.size() == 0)throw new ETagParseException("ETag is missing in the request!");
        if(!ifMatch.contains(eTag))return preConditionFailed(eTag);

        Map<String, Object> result = planService.getPlan(key);
        planService.deletePlan(key);

        return new ResponseEntity<>(result, HttpStatus.NO_CONTENT);
    }

    @PutMapping(path = "/{objectType}/{objectId}")
    public ResponseEntity<?> updatePlan(@PathVariable String objectType,
                                        @PathVariable String objectId,
                                        @RequestBody(required = false) String planObject,
                                        @RequestHeader HttpHeaders headers){
        if(planObject == null || planObject.isBlank()){
            throw new BadRequestException("Request body is empty!");
        }
        String key = objectType +":"+objectId;
        if (!planService.isKeyPresent(key)) {
            throw new ResourceNotFoundException("Plan not found");
        }

        List<String> ifMatch;
        try {
            ifMatch = headers.getIfMatch();
        }catch (Exception e){
            throw new ETagParseException("ETag value invalid! Make sure ETag value is a string");
        }

        String eTag = planService.getETag(key);
        if(ifMatch.size() == 0){
            throw new ETagParseException("ETag doesn't provided with the request");
        }

        if(!ifMatch.contains(eTag)){
            return preConditionFailed(eTag);
        }

        JSONObject plan = new JSONObject(planObject);

        JSONObject schemaJSON = new JSONObject(new JSONTokener(Objects.requireNonNull(PlanController.class.getResourceAsStream("/validate_schema.json"))));
        Schema schema = SchemaLoader.load(schemaJSON);
        try {
            schema.validate(plan);
        } catch(ValidationException e){
            throw new BadRequestException(e.getMessage());
        }

        if(!plan.get("objectId").equals(objectId)){
            throw new BadRequestException("The objectId in JSON schema is not consistent with request head");
        }

        //delete old plan, create new plan and eTag
        planService.deletePlan(key);

        String updateETag = planService.createPlan(plan, key);

        HttpHeaders headersToSend = new HttpHeaders();
        headersToSend.setETag(updateETag);
        return new ResponseEntity<>("{\"message: Plan updated successfully\"}",
                headersToSend,
                HttpStatus.OK);
    }
    private ResponseEntity preConditionFailed(String eTag){
        HttpHeaders headersToSend = new HttpHeaders();
        headersToSend.setETag(eTag);
        ErrorResponse errorResponse = new ErrorResponse(
                "Plan has been updated",
                HttpStatus.PRECONDITION_FAILED.value(),
                new Date(),
                HttpStatus.PRECONDITION_REQUIRED.getReasonPhrase()
        );
        return new ResponseEntity<>(errorResponse,headersToSend,HttpStatus.PRECONDITION_FAILED);

    }
}
