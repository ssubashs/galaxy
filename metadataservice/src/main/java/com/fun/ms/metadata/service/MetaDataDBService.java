package com.fun.ms.metadata.service;

import com.atlassian.fugue.Option;
import com.atlassian.fugue.Unit;
import com.fun.ms.common.service.IMetaDataService;
import com.fun.ms.common.service.MetaDataRequest;
import com.fun.ms.common.service.MetaDataResponse;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.serviceproxy.ServiceException;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class MetaDataDBService implements IMetaDataService {

    private final String dbName;
    int NO_NAME_ERROR = 2;
    int BAD_NAME_ERROR = 3;

    public MetaDataDBService(String dbName){
        this.dbName=dbName;
    }


    @Override
    public void process(MetaDataRequest request, Handler<AsyncResult<MetaDataResponse>> resultHandler) {
        log.info("DB Service for metadata request {}",request);

        if (request.getId() == null || Strings.isNullOrEmpty(request.getId())) {
            resultHandler.handle(ServiceException.fail(NO_NAME_ERROR, "No id for metadata conversion"));
        } else if ("bad".equalsIgnoreCase(request.getId())) {
            resultHandler.handle(ServiceException.fail(BAD_NAME_ERROR, "Bad id requested "));
        } else {
           String id = request.getId();
           Map<String,String> data = Data.LocalDB.get(request.getType());
            Option.option(data.get(id)).fold(()->{
                resultHandler.handle(ServiceException.fail(500,"id not found."));
                return Unit.Unit();
            },label->{
                resultHandler.handle(Future.succeededFuture(new MetaDataResponse(request.getType(),id,label)));
                return Unit.Unit();
            });

        }
    }



    private static class Data{
        static final Map<String,String> AccountMap = ImmutableMap.of("1","insurance","2","auto","3","healthcare","4","IT services");
        static final Map<String,String> UserMap = ImmutableMap.of("1","john","2","smith","3","bob","4","god");
        static final Map<String,String> CountryMap = ImmutableMap.of("1","USA","2","England","3","Canada","4","Mexico");
        static final Map<MetaDataRequest.Type,Map<String,String>> LocalDB
                = ImmutableMap.of(MetaDataRequest.Type.ACCOUNT,AccountMap, MetaDataRequest.Type.COUNTRY,CountryMap,MetaDataRequest.Type.USER,UserMap);

    }


}
