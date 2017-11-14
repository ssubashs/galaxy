package com.fun.ms.account;

import com.fun.ms.account.controller.AccountController;
import com.fun.ms.common.verticle.BaseVerticle;
import io.vertx.core.Future;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.plugins.server.vertx.VertxResteasyDeployment;


@Slf4j
public class AccountServer extends BaseVerticle {


  private static final Integer DEFAULT_PORT = 8090 ;
  private static String DEFAULT_HOST = "localhost";
  private static final String SERVICE_NAME = "account-api";

  @Override
  public void start(Future<Void> future) throws Exception {
    Future<Void> superFuture = Future.future();
    super.start(superFuture);

    // Build the Jax-RS hello world deployment
    VertxResteasyDeployment deployment = new VertxResteasyDeployment();
    deployment.start();
    deployment.getRegistry().addPerInstanceResource(AccountController.class);

    superFuture.compose(discoverFuture->
                      createListenerOnPortForRestDeployment(SERVICE_NAME,config().getInteger("account.http.port",DEFAULT_PORT),deployment))
            .compose(actualport ->
                    publishHttpEndpoint(SERVICE_NAME, config().getString("account.http.address",DEFAULT_HOST),actualport))
            .compose(some -> deployverticles())
            .setHandler(future.completer());

  }

  private  Future<Void> deployverticles() {
    Future<Void> result = Future.future();
    vertx.deployVerticle(new AccountVerticle(),handler->{
                  if(handler.failed()){
                      result.fail("Failed to deploy verticle ");
                  }else{
                    log.info("Deployed account verticle with id {}",handler.result());
                  }

            });

    return result;
  }
}
