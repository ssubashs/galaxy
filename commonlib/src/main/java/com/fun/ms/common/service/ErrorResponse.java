package com.fun.ms.common.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Value
@DataObject
@Slf4j
public class ErrorResponse extends ServiceResponse{
   @JsonIgnore
    private final String type = ERROR;
    private final Integer errorCode;
    private final String errorMessage;

    public ErrorResponse(JsonObject jsonObject){
        try {
            ErrorResponse request =  mapper.readValue(jsonObject.encode(), ErrorResponse.class);
            this.errorCode = request.errorCode;
            this.errorMessage = request.errorMessage;
        }catch(Exception exception){
            log.error("failed to convert JsonObject [ {} ] to type.",jsonObject.encodePrettily());
            throw new IllegalStateException("Type conversion failed. ");
        }
    }

    public ErrorResponse(Integer errorCode,String errorMessage){
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }

    @Override
    public ServiceResponse newInstance(JsonObject jsonObject) {
        return new ErrorResponse(jsonObject);
    }
}
