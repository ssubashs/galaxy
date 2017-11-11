package com.fun.ms.common.service;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.serviceproxy.ProxyHelper;

@ProxyGen
public interface IMetaDataService {

    public static String ADDRESS = "vertx.metadataservice";
    public static String SERVICE_NAME = "metadataservice";
    void process(MetaDataRequest request, Handler<AsyncResult<MetaDataResponse>> resultHandler);

    static IMetaDataService createProxy(Vertx vertx) {
        return ProxyHelper.createProxy(IMetaDataService.class, vertx, ADDRESS);
    }
}
