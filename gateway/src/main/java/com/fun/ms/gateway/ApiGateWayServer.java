package com.fun.ms.gateway;

import com.fun.ms.common.verticle.BaseVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.HttpEndpoint;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@Slf4j
public class ApiGateWayServer extends BaseVerticle {

  private static String DEFAULT_HOST = "localhost";
  private static Integer DEFAULT_PORT = 8050;
  private static final String SERVICE_NAME = "gateway-api";

  @Override
  public void start(Future<Void> future) throws Exception {
    Future<Void> superFuture = Future.future();
    super.start(superFuture);

    Integer port = config().getInteger("api.http.port",DEFAULT_PORT);
    String host = config().getString("api.http.address",DEFAULT_HOST);

    log.info("Trying to deploy gateway app in {} port {}",host,port);
    Router router = Router.router(vertx);


      // body handler
      router.route().handler(BodyHandler.create());

      // version handler
      router.get("/api/v").handler(this::apiVersion);

      String hostURI = buildHostURI();


      // api dispatcher
      router.route("/api/*").handler(this::dispatchRequests);
      router.route("/test").handler(this::testHandler);

      HttpServerOptions httpServerOptions = new HttpServerOptions();

      // create http server
      vertx.createHttpServer(httpServerOptions)
              .requestHandler(router::accept)
              .listen(port, ar -> {
                  if (ar.succeeded()) {
                      publishApiGateway(host, port);
                      future.complete();
                      log.info("API Gateway is running on port " + port);
                      log.info("gate url  {}",hostURI);

                  } else {
                      future.fail(ar.cause());
                  }
              });

//      vertx.setPeriodic(1000l,timeouthandler->
//              {
//                  IMetaDataService metaDataService = IMetaDataService.createProxy(vertx);
//                  metaDataService.process(new MetaDataRequest("account", MetaDataRequest.Type.ACCOUNT, Long.valueOf(1l)), asyncResponseHandler -> {
//                      if (asyncResponseHandler.failed()) {
//                          log.error("internal server error while calling metadata service.", asyncResponseHandler.cause());
//                      } else {
//                          MetaDataResponse accountTypeLabel  = asyncResponseHandler.result();
//                          log.info("metadata response is {}",accountTypeLabel);
//                      }
//                  });
//              });

     }

    private void testHandler(RoutingContext routingContext) {
        routingContext.response().setStatusCode(200)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("test","successful").encodePrettily());
    }

    private void dispatchRequests(RoutingContext context) {
        int initialOffset = 5; // length of `/api/`
        // run with circuit breaker in order to deal with failure

        circuitBreaker.execute(future -> {
            getAllEndpoints().setHandler(ar -> {
                if (ar.succeeded()) {
                    List<Record> recordList = ar.result();
                    dumpRecordsListToConsole(recordList);
                    // get relative path and retrieve prefix to dispatch client
                    String path = context.request().uri();
                    log.info("Request path for dispatching. ");
                    if (path.length() <= initialOffset) {
                        notFound(context);
                        future.complete();
                        return;
                    }
                    String prefix = (path.substring(initialOffset)
                            .split("/"))[0];
                    // generate new relative path
                    String newPath = path.substring(initialOffset + prefix.length());
                    // get one relevant HTTP client, may not exist
                    log.info("prefix {} and new path {}",prefix,newPath);
                    Optional<Record> client = recordList.stream()
                            .filter(record -> record.getMetadata().getString("api.name") != null)
                            .filter(record -> record.getMetadata().getString("api.name").equals(prefix))
                            .findAny(); // simple load balance

                    if (client.isPresent()) {
                        doDispatch(context, newPath, discovery.getReference(client.get()).get(), future);
                    } else {
                        notFound(context);
                        future.complete();
                    }
                } else {
                    future.fail(ar.cause());
                }
            });
        }).setHandler(ar -> {
            if (ar.failed()) {
                badGateway(ar.cause(), context);
            }
        });
    }


    private void dumpRecordsListToConsole(List<Record> recordList){
      log.info("Service records available");
        recordList.stream().forEach(record -> {
            log.info("name {} \n meta data {} \n",record.getMetadata().encodePrettily(),record.getName());
        });
    }

    /**
     * Dispatch the request to the downstream REST layers.
     *
     * @param context routing context instance
     * @param path    relative path
     * @param client  relevant HTTP client
     */
    private void doDispatch(RoutingContext context, String path, HttpClient client, Future<Object> cbFuture) {
        HttpClientRequest toReq = client
                .request(context.request().method(), path, response -> {
                    response.bodyHandler(body -> {
                        if (response.statusCode() >= 500) { // api endpoint server error, circuit breaker should fail
                            cbFuture.fail(response.statusCode() + ": " + body.toString());
                        } else {
                            HttpServerResponse toRsp = context.response()
                                    .setStatusCode(response.statusCode());
                            response.headers().forEach(header -> {
                                toRsp.putHeader(header.getKey(), header.getValue());
                            });
                            // send response
                            toRsp.end(body);
                            cbFuture.complete();
                        }
                        ServiceDiscovery.releaseServiceObject(discovery, client);
                    });
                });
        // set headers
        context.request().headers().forEach(header -> {
            toReq.putHeader(header.getKey(), header.getValue());
        });
        if (context.user() != null) {
            toReq.putHeader("user-principal", context.user().principal().encode());
        }
        // send request
        if (context.getBody() == null) {
            toReq.end();
        } else {
            toReq.end(context.getBody());
        }
    }

    private void apiVersion(RoutingContext context) {
        context.response()
                .end(new JsonObject().put("version", "v1").encodePrettily());
    }

    /**
     * Get all REST endpoints from the service discovery infrastructure.
     *
     * @return async result
     */
    private Future<List<Record>> getAllEndpoints() {
        Future<List<Record>> future = Future.future();
        discovery.getRecords(record -> record.getType().equals(HttpEndpoint.TYPE),
                future.completer());
        return future;
    }

    // log methods



    private String buildHostURI() {
        int port = config().getInteger("api.gateway.http.port", DEFAULT_PORT);
        final String host = config().getString("api.gateway.http.address.external", "localhost");
        return String.format("http://%s:%d", host, port);
    }

    protected void badRequest(RoutingContext context, Throwable ex) {
        context.response().setStatusCode(400)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("error", ex.getMessage()).encodePrettily());
    }

    protected void notFound(RoutingContext context) {
        context.response().setStatusCode(404)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("message", "not_found").encodePrettily());
    }

    protected void internalError(RoutingContext context, Throwable ex) {
        context.response().setStatusCode(500)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("error", ex.getMessage()).encodePrettily());
    }

    protected void notImplemented(RoutingContext context) {
        context.response().setStatusCode(501)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("message", "not_implemented").encodePrettily());
    }

    protected void badGateway(Throwable ex, RoutingContext context) {
        ex.printStackTrace();
        context.response()
                .setStatusCode(502)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("error", "bad_gateway")
                        //.put("message", ex.getMessage())
                        .encodePrettily());
    }

    protected void serviceUnavailable(RoutingContext context) {
        context.fail(503);
    }

    protected void serviceUnavailable(RoutingContext context, Throwable ex) {
        context.response().setStatusCode(503)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("error", ex.getMessage()).encodePrettily());
    }

    protected void serviceUnavailable(RoutingContext context, String cause) {
        context.response().setStatusCode(503)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("error", cause).encodePrettily());
    }


}
