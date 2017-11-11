package com.fun.ms.metadata.controller;

import com.fun.ms.common.service.ErrorResponse;
import com.fun.ms.common.service.IMetaDataService;
import com.fun.ms.common.service.MetaDataRequest;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
@Slf4j
public class MetadataController {



    @GET
    @Path("/{type}")
    @Produces({ MediaType.APPLICATION_JSON})
    public void doGet(
            @Suspended final AsyncResponse asyncResponse,
            @Context Vertx vertx, @QueryParam("id") String id, @PathParam("type") String type) {

        MetaDataRequest.Type requestType = MetaDataRequest.Type.fromString(type.toUpperCase());
        log.info("metadata request for type {} and id {}.",requestType, id);

        if(requestType == MetaDataRequest.Type.BADTYPE){
            asyncResponse.resume( Response.status(400).entity(new ErrorResponse(400,"metadata type not found.")).build());
        }else {

            if (id == null || id.isEmpty()) {
                asyncResponse.resume(Response.status(422).entity(new ErrorResponse(422, "invalid id param.")).build());
            }
            IMetaDataService metaDataService = IMetaDataService.createProxy(vertx);
            metaDataService.process(new MetaDataRequest(id, requestType, Long.valueOf(1l)), asyncResponseHandler -> {
                if (asyncResponseHandler.failed()) {
                    log.error("internal server error while calling metadata service.", asyncResponseHandler.cause());
                    asyncResponse.resume(Response.status(500).entity(new ErrorResponse(500, "internal server error.")).build());
                } else {
                    asyncResponse.resume(Response.status(200).entity(asyncResponseHandler.result()).build());
                }
            });
        }

    }




}
