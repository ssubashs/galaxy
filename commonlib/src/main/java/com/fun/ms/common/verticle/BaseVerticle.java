package com.fun.ms.common.verticle;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import io.vertx.servicediscovery.types.EventBusService;
import io.vertx.servicediscovery.types.HttpEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.plugins.server.vertx.VertxRequestHandler;
import org.jboss.resteasy.plugins.server.vertx.VertxResteasyDeployment;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
public abstract class BaseVerticle extends AbstractVerticle {

    protected ServiceDiscovery discovery;
    protected CircuitBreaker circuitBreaker;
    protected Set<Record> registeredRecords = new ConcurrentHashSet<>();

    @Override
    public void start(Future<Void> future) throws Exception {
        // init service discovery instance
        log.info("starting base verticle ... vertx {},config -{}",vertx,config());
        try {
            discovery = ServiceDiscovery.create(vertx, new ServiceDiscoveryOptions().setBackendConfiguration(config()));

            // init circuit breaker instance
            JsonObject cbOptions = config().getJsonObject("circuit-breaker") != null ?
                    config().getJsonObject("circuit-breaker") : new JsonObject();
            circuitBreaker = CircuitBreaker.create(cbOptions.getString("name", "circuit-breaker"), vertx,
                    new CircuitBreakerOptions()
                            .setMaxFailures(cbOptions.getInteger("max-failures", 5))
                            .setTimeout(cbOptions.getLong("timeout", 10000L))
                            .setFallbackOnFailure(true)
                            .setResetTimeout(cbOptions.getLong("reset-timeout", 10000L))
            );
            log.info("Service discovery and circuit breakers setup.");
            future.complete();
        }
        catch(Exception exception){
            log.error("failed setup service discovery. {} ",exception);
            future.fail(exception);
        }

    }


    protected Future<Void> publishHttpEndpoint(String name, String host, int port) {
        Record record = HttpEndpoint.createRecord(name, host, port, "/",
                new JsonObject().put("api.name", config().getString("api.name", ""))
        );
        log.info("Publishing record for http endpoint with name {} /n metadata {}",record.getName(),record.getMetadata().encodePrettily());
        return publish(record);
    }

    protected Future<Void> publishApiGateway(String host, int port) {
        Record record = HttpEndpoint.createRecord("api-gateway", true, host, port, "/", null)
                .setType("api-gateway");
        return publish(record);
    }

    protected Future<Void> publishEventBusService(String name, String address, Class serviceClass) {
        Record record = EventBusService.createRecord(name, address, serviceClass);
        return publish(record);
    }

    protected Future<Integer> createListenerOnPortForRestDeployment(String servicename, Integer port,VertxResteasyDeployment deployment){
        // Start the front end server using the Jax-RS controller
        Future<Integer> future = Future.future();
        vertx.createHttpServer()
                .requestHandler(new VertxRequestHandler(vertx, deployment))
                .listen(port, ar -> {
                    if(ar.failed()){
                        String message = "failed to deploy "+servicename+ " in port "+port;
                        log.error(message + " because of ",ar.cause());
                        future.fail(message);
                    }else {
                        log.info("Service started on port " + ar.result().actualPort());
                        future.complete(port);
                    }
                });
        return future;
    }

    private Future<Void> publish(Record record) {
        log.info("trying to add service record {} to discovery {}",record,discovery);
        if (discovery == null) {
            try {
                log.info("Starting discovery...");
                start();
            } catch (Exception e) {
                throw new IllegalStateException("Cannot create discovery service");
            }
        }

        Future<Void> future = Future.future();
        // publish the service
        discovery.publish(record, ar -> {
            if (ar.succeeded()) {
                registeredRecords.add(record);
                log.info("Service <" + ar.result().getName() + "> published");
                future.complete();
            } else {
                future.fail(ar.cause());
            }
        });

        return future;
    }

    @Override
    public void stop(Future<Void> future) throws Exception {
        // In current design, the publisher is responsible for removing the service
        List<Future> futures = new ArrayList<>();
        registeredRecords.forEach(record -> {
            Future<Void> cleanupFuture = Future.future();
            futures.add(cleanupFuture);
            discovery.unpublish(record.getRegistration(), cleanupFuture.completer());
        });

        if (futures.isEmpty()) {
            discovery.close();
            future.complete();
        } else {
            CompositeFuture.all(futures)
                    .setHandler(ar -> {
                        discovery.close();
                        if (ar.failed()) {
                            future.fail(ar.cause());
                        } else {
                            future.complete();
                        }
                    });
        }
    }


}
