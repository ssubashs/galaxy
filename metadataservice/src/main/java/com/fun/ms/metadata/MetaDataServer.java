package com.fun.ms.metadata;

import com.fun.ms.common.service.IMetaDataService;
import com.fun.ms.common.verticle.BaseVerticle;
import com.fun.ms.metadata.controller.MetadataController;
import com.fun.ms.metadata.service.MetaDataDBService;
import io.vertx.core.Future;
import io.vertx.serviceproxy.ProxyHelper;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.plugins.server.vertx.VertxResteasyDeployment;

@Slf4j
public class MetaDataServer extends BaseVerticle {

  private static String DEFAULT_HOST = "localhost";
  private static Integer DEFAULT_PORT = 8190;
  private static final String SERVICE_NAME = "metadata-api";

  @Override
  public void start(Future<Void> future) throws Exception {
    Future<Void> superFuture = Future.future();
    super.start(superFuture);

    // Build the Jax-RS hello world deployment
    VertxResteasyDeployment deployment = new VertxResteasyDeployment();
    deployment.start();
    deployment.getRegistry().addPerInstanceResource(MetadataController.class);

    Integer port = config().getInteger("metadata.http.port",DEFAULT_PORT);
    String host = config().getString("metadata.http.address",DEFAULT_HOST);

    ProxyHelper.registerService(IMetaDataService.class, vertx, new MetaDataDBService("localDB") , IMetaDataService.ADDRESS);

    superFuture.compose(discoverFuture-> publishEventBusService(IMetaDataService.SERVICE_NAME, IMetaDataService.ADDRESS, IMetaDataService.class))
            .compose(servicePublished -> createListenerOnPortForRestDeployment(SERVICE_NAME,port,deployment))
            .compose(actualport -> publishHttpEndpoint(SERVICE_NAME,host,port))
            .setHandler(future.completer());
  }




}
