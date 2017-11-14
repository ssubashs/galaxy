package com.fun.ms.account;

import io.vertx.core.json.JsonObject;
import org.testng.annotations.Test;

public class AccountTest {

    @Test
    public void testJsonConversion(){
        try {
            JsonObject json = Account.builder().id(1).owningCompany("test company").status("active").typeId("4").typeDescription("type def").build().toJson();
            System.out.println(" Account json is \n " +json.encodePrettily());
            System.out.println("Back to type \n"+Account.fromJson(json));

        }catch(Exception ex){
            ex.printStackTrace();
            System.out.println(ex);
        }

    }
}
