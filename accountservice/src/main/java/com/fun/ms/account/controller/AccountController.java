package com.fun.ms.account.controller;

import com.atlassian.fugue.Option;
import com.atlassian.fugue.Unit;
import com.fun.ms.account.Account;
import com.fun.ms.account.service.AccountService;
import com.fun.ms.common.service.ErrorResponse;
import com.fun.ms.common.service.IMetaDataService;
import com.fun.ms.common.service.MetaDataRequest;
import com.fun.ms.common.service.MetaDataResponse;
import io.vertx.core.Vertx;
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
public class AccountController {
    @GET
    @Path("/{id}")
    @Produces({ MediaType.APPLICATION_JSON})
    public void doGet(
            @Suspended final AsyncResponse asyncResponse,
            @Context Vertx vertx, @PathParam("id") Integer id) {

        log.info("Account request for id {}.",id);



            if (id == null || id == 0) {
                asyncResponse.resume(Response.status(422).entity(new ErrorResponse(422, "invalid id.")).build());
            }

           Option<Account> maybeAccount =  AccountService.getAccount(id);
        maybeAccount.fold(()->{
               asyncResponse.resume(Response.status(400).entity(new ErrorResponse(400, "Account not found.")).build());
               return Unit.Unit();
           }, acc -> {
               IMetaDataService metaDataService = IMetaDataService.createProxy(vertx);
               metaDataService.process(new MetaDataRequest(acc.getTypeId(), MetaDataRequest.Type.ACCOUNT, Long.valueOf(1l)), asyncResponseHandler -> {
                   if (asyncResponseHandler.failed()) {
                       log.error("internal server error while calling metadata service.", asyncResponseHandler.cause());
                       asyncResponse.resume(Response.status(200).entity(acc).build());
                   } else {
                       MetaDataResponse accountTypeLabel  = asyncResponseHandler.result();
                       Account accountWithLabel = Account.builder()
                               .id(acc.getId())
                               .owningCompany(acc.getOwningCompany())
                               .status(acc.getStatus())
                               .typeId(acc.getTypeId())
                               .typeDescription(accountTypeLabel.getLabel()).build();
                       asyncResponse.resume(Response.status(200).entity(accountWithLabel).build());
                   }
               });
               return Unit.Unit();
           });



    }


}
