package com.fun.ms.cluster;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClusterManager extends AbstractVerticle{

    @Override
    public void start(Future<Void> starter){

        vertx.setTimer(10000, handler -> {
           log.info("Cluster running ..");
        });
      log.info("Starting the cluster manager...");
    }
}
