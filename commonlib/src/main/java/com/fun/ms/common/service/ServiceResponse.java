package com.fun.ms.common.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import static com.fun.ms.common.service.ServiceResponse.ERROR;
import static com.fun.ms.common.service.ServiceResponse.META_DATA;

@DataObject
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME,property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ErrorResponse.class,name = ERROR),
        @JsonSubTypes.Type(value = MetaDataResponse.class,name = META_DATA)
})
@Slf4j
public abstract class  ServiceResponse {
    public abstract String getType();
    protected static final String ERROR = "error";
    protected static final String META_DATA = "metadata";
    protected static final com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .registerModule(new GuavaModule())
            .registerModule(new JodaModule());
    public  JsonObject toJson(){
        try {
            return  new JsonObject(mapper.writeValueAsString(this));
        } catch (IOException e) {
            log.error("failed to convert type {} to JsonObject",this);
            throw new IllegalStateException("JsonObject conversion failed. ");
        }
    }
    public abstract  ServiceResponse newInstance(JsonObject jsonObject);

}
