package com.info7255.medicalplanapi.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.http.HttpStatus;

import java.util.Date;

public class ErrorResponse {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy, hh:mm:ss")
    private final Date timestamp;
    private final String message;
    private final int httpStatus;
    private final String error;

    public Date getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getError() {
        return error;
    }

    public ErrorResponse(String message, int httpStatus, Date timestamp, String error){
        this.message = message;
        this.httpStatus = httpStatus;
        this.timestamp = timestamp;
        this.error = error;
    }


}
