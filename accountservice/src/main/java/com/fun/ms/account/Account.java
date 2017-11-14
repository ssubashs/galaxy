package com.fun.ms.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.vertx.core.json.JsonObject;
import lombok.Value;
import lombok.experimental.Builder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Value
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = Account.Builder.class)
@Slf4j
public class Account {
    private final Integer id;
    private final String typeId;
    private final String owningCompany;
    private final String status;
    private final String typeDescription;
    private static final ObjectMapper mapper = new ObjectMapper();
    public  Account copyWithNewTypeDescription(String newTypeDescription){
        return new Account(this.id,this.typeId,this.owningCompany,this.status,newTypeDescription);
    }

    /**
     * Jackson doesn't want prefix
     */
    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {

    }

    public JsonObject toJson(){

//        return new JsonObject()
//                .put("id",id)
//                .put("typeId",typeId)
//                .put("owningCompany",owningCompany)
//                .put("status",status)
//                .put("typeDescription",typeDescription);
        try {

            return  new JsonObject(mapper.writeValueAsString(this));
        } catch (IOException e) {
            log.error("failed to convert type {} to JsonObject , {} ",this,e);
            throw new IllegalStateException("JsonObject conversion failed. ");
        }
    }

    public static Account fromJson(JsonObject jsonObject){
//        return Account.builder()
//                .id(jsonObject.getInteger("id"))
//                .owningCompany(jsonObject.getString("owningCompany"))
//                .status(jsonObject.getString("status"))
//                .typeId(jsonObject.getString("typeId"))
//                .typeDescription(jsonObject.getString("typeDescription"))
//                .build();
        try {
            return mapper.readValue(jsonObject.encode(),Account.class);
        } catch (IOException e) {
            log.error("failed to convert {} to account.{}",jsonObject,e);
            throw new IllegalArgumentException("Invalid jsonobject for account type conversion.");
        }
    }
}
