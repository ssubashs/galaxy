package com.fun.ms.common.service;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;


@Value
@DataObject
@Slf4j
public class MetaDataResponse extends ServiceResponse{
    @JsonIgnore
    private final String type = META_DATA;
    private final MetaDataRequest.Type reqType;
    private final String label;
    private final String id;

    public MetaDataResponse(JsonObject jsonObject){
        try {
            MetaDataResponse request =  mapper.readValue(jsonObject.encode(), MetaDataResponse.class);
            this.id = request.getId();
            this.reqType = request.reqType;
            this.label = request.label;
        }catch(Exception exception){
            log.error("failed to convert JsonObject [ {} ] to type.",jsonObject.encodePrettily());
            throw new IllegalStateException("Type conversion failed. ");
        }
    }

    @JsonCreator
    public MetaDataResponse(@JsonProperty("type") MetaDataRequest.Type type,@JsonProperty("id") String id,@JsonProperty("label") String label) {
        super();
        this.reqType = type;
        this.id = id;
        this.label = label;
    }

    @Override
    public ServiceResponse newInstance(JsonObject jsonObject) {
        return new MetaDataResponse(jsonObject);
    }
}
