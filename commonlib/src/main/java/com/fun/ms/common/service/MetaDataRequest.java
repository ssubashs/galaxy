package com.fun.ms.common.service;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Value
@DataObject
@Slf4j
public class MetaDataRequest {
    private final String id;
    private final Type type;
    private final Long limit;

    private final static ObjectMapper mapper = new ObjectMapper();

    public MetaDataRequest(JsonObject jsonObject){
        try {
            MetaDataRequest request =  mapper.readValue(jsonObject.encode(), MetaDataRequest.class);
            this.id = request.getId();
            this.type = request.type;
            this.limit = request.limit;
        }catch(Exception exception){
            log.error("failed to convert JsonObject [ {} ] to type.{}",jsonObject.encodePrettily(),exception);
            throw new IllegalStateException("Type conversion failed. ");
        }
    }

    @JsonCreator
    public MetaDataRequest(@JsonProperty("id") String id,@JsonProperty("type") Type type,@JsonProperty("limit") Long limit) {
        this.id = id;
        this.type = type;
        this.limit = limit;
    }

    public JsonObject toJson(){
        try {
            return  new JsonObject(mapper.writeValueAsString(this));
        } catch (IOException e) {
            log.error("failed to convert type {} to JsonObject",this);
            throw new IllegalStateException("JsonObject conversion failed. ");
        }
    }

    public  enum Type{
        USER,
        ACCOUNT,
        COUNTRY,
        BADTYPE;

        public static Type fromString(String stringType){
         return Util.exceptionSafe(()-> Type.valueOf(stringType)) .getOrElse(BADTYPE);
        }
    }
}


