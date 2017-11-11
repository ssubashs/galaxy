package com.fun.ms.gateway;

import com.hazelcast.config.Config;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

@Slf4j
public class Starter {
    public static void main(String[] args){
      log.info("Gateway Starter....");
       deployVerticleInCluster(new ApiGateWayServer(),new DeploymentOptions());

    }

    private static void deployVerticleInCluster(AbstractVerticle verticleInstance, DeploymentOptions deploymentOptions) {
        log.info("trying to deploy java:{}",verticleInstance.getClass().getName());
        VertxOptions vertxOptions = new VertxOptions().setClustered(true);
        Config hazelcastconfig = new Config();
        hazelcastconfig.getNetworkConfig().getJoin().getTcpIpConfig().addMember("172.28.152.25:5701").setEnabled(true);
        hazelcastconfig.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        ClusterManager hazelcastClusterManager = new HazelcastClusterManager(hazelcastconfig);
        vertxOptions.setClusterManager(hazelcastClusterManager);
        Consumer<Vertx> runner = clusteredVertx -> {
            try {
                if (deploymentOptions != null) {
                    clusteredVertx.deployVerticle(verticleInstance, deploymentOptions);
                } else {
                    clusteredVertx.deployVerticle(verticleInstance);
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        };

        Vertx.clusteredVertx(vertxOptions, res -> {
            if (res.succeeded()) {
                Vertx vertx = res.result();
                runner.accept(vertx);
            } else {
                res.cause().printStackTrace();
            }
        });
    }
}
