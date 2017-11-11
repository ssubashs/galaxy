package com.fun.ms.common.service;

import io.vertx.core.json.JsonObject;
import org.testng.annotations.Test;

public class TestJsonConversion {

    @Test
    public void testRequest(){
            String jsonString = " {\"id\" : \"bad\",\n" +
                    "  \"type\" : \"ACCOUNT\",\n" +
                    "  \"limit\" : 1\n}";
        JsonObject jsonObject = new JsonObject(jsonString);
        MetaDataRequest metadata = new MetaDataRequest(jsonObject);
        System.out.println(metadata);
    }
}
