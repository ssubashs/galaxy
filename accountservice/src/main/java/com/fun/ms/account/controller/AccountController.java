package com.fun.ms.account.controller;

import com.fun.ms.account.Account;
import com.fun.ms.account.AccountVerticle;
import com.fun.ms.common.service.ErrorResponse;
import com.fun.ms.common.service.Util;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/info")
@Slf4j
public class AccountController{


    @GET
    @Path("/{id}")
    @Produces({ MediaType.APPLICATION_JSON})
    public void doGet(
            @Suspended final AsyncResponse asyncResponse,
            @Context Vertx vertx, @PathParam("id") Integer id) {
        log.info("Account request for id {}.",id);

        if (id == null || id == 0) {
            asyncResponse.resume(
                    Response.status(422).entity(new ErrorResponse(422, "invalid id.")).build());
        }
       else {
            vertx.eventBus().<JsonObject>send(AccountVerticle.address, new JsonObject().put("accountId", id), handler -> {
                if (handler.failed()) {
                    asyncResponse.resume(Util.convertExceptionToApiResponse(handler.cause()));
                } else {
                    log.info("Response from verticle {}   \n account type object {}", handler.result().body().encodePrettily(),Account.fromJson(handler.result().body()));
                    asyncResponse.resume(Response.status(200)
                            .entity(Account.fromJson(handler.result().body())).build());
                }
            });
        }

    }

}


