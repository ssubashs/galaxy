package com.fun.ms.account;

import com.atlassian.fugue.Option;
import com.atlassian.fugue.Pair;
import com.atlassian.fugue.Unit;
import com.fun.ms.account.service.AccountService;
import com.fun.ms.common.service.IMetaDataService;
import com.fun.ms.common.service.MetaDataRequest;
import com.fun.ms.common.service.MetaDataResponse;
import com.fun.ms.common.service.Util;
import com.fun.ms.common.verticle.BaseVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.EventBusService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AccountVerticle extends BaseVerticle {

    public static String address = "handleAccount";

    @Override
    public void start(Future<Void> future) throws Exception {
        super.start(future);
        log.info("Starting Account Verticle");
        vertx.eventBus().<JsonObject>localConsumer(address, handler -> {
            Integer accountId = handler.body().getInteger("accountId");

            Option<Account> maybeAccount = AccountService.getAccount(accountId);
            maybeAccount.fold(() -> {
                handler.fail(404, "account not found.");
                return Unit.Unit();
            },acc -> {
                log.info("account info from db {}",acc);
               Future<JsonObject> jsonResponse =
                       this.circuitBreaker.executeWithFallback(dataFuture -> {
//                    this.circuitBreaker.execute(dataFuture->{
                   EventBusService.getServiceProxy(ServiceDiscovery.create(vertx), record -> record.getName().equalsIgnoreCase(IMetaDataService.SERVICE_NAME),
                           IMetaDataService.class,
                           resulthandler -> {
                               if (resulthandler.failed()) {
                                   log.warn("failed to find the event bus service for metadata. {}", resulthandler.cause());
                                   dataFuture.fail("Service is not found.");
                               } else {
                                   resulthandler.result().process(new MetaDataRequest(acc.getTypeId(), MetaDataRequest.Type.ACCOUNT, Long.valueOf(1l)), asyncResponseHandler -> {
                                       if (asyncResponseHandler.failed()) {
                                           log.error("internal server error while calling metadata service.", asyncResponseHandler.cause());
                                           dataFuture.fail("Remote Service failed.");
                                       } else {
                                           MetaDataResponse accountTypeLabel = asyncResponseHandler.result();
                                           dataFuture.complete(acc.copyWithNewTypeDescription(accountTypeLabel.getLabel()).toJson());
                                       }
                                   });
                               }
                           });
                       }
//                    );
                       ,
                       throwable -> {
                           log.error("Error calling the metadata service.{}",throwable);
                            return acc.copyWithNewTypeDescription("Service is unavailable.").toJson();
                       });

               jsonResponse.setHandler(ar-> {
                   if(ar.failed()){
                       Pair<Integer,String> excepResponse = Util.convertExceptionCodeAndMessage(ar.cause());
                       handler.fail(excepResponse.left(),excepResponse.right());
                   }else{
                       handler.reply(ar.result());
                   }
               });
              return Unit.Unit();
            });

        });
    }


//    EventBusService.getServiceProxy(ServiceDiscovery.create(vertx), record -> record.getName().equalsIgnoreCase(IMetaDataService.SERVICE_NAME),
//    IMetaDataService.class,
//    resulthandler -> {
//        if (resulthandler.failed()) {
//            log.warn("failed to find the event bus service for metadata. {}", resulthandler.cause());
//            handler.reply(acc.copyWithNewTypeDescription("Metadata Service Not Available in Service Discovery").toJson());
//        } else {
//            resulthandler.result().process(new MetaDataRequest(acc.getTypeId(), MetaDataRequest.Type.ACCOUNT, Long.valueOf(1l)), asyncResponseHandler -> {
//                if (asyncResponseHandler.failed()) {
//                    log.error("internal server error while calling metadata service.", asyncResponseHandler.cause());
//                    handler.reply(acc.copyWithNewTypeDescription("Metadata service failed.").toJson());
//                } else {
//                    MetaDataResponse accountTypeLabel = asyncResponseHandler.result();
//                    handler.reply(acc.copyWithNewTypeDescription(accountTypeLabel.getLabel()).toJson());
//                }
//            });
//        }
//    });
//                return Unit.Unit();

}
