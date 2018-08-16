package com.nextbreakpoint.shop.designs;

import com.nextbreakpoint.shop.common.AccessHandler;
import com.nextbreakpoint.shop.common.CORSHandlerFactory;
import com.nextbreakpoint.shop.common.Failure;
import com.nextbreakpoint.shop.common.GraphiteManager;
import com.nextbreakpoint.shop.common.JWTProviderFactory;
import com.nextbreakpoint.shop.common.KafkaClientFactory;
import com.nextbreakpoint.shop.common.ResponseHelper;
import com.nextbreakpoint.shop.common.ServerUtil;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Launcher;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.WorkerExecutor;
import io.vertx.rxjava.core.http.HttpServer;
import io.vertx.rxjava.ext.auth.jwt.JWTAuth;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.rxjava.ext.web.handler.BodyHandler;
import io.vertx.rxjava.ext.web.handler.CookieHandler;
import io.vertx.rxjava.ext.web.handler.CorsHandler;
import io.vertx.rxjava.ext.web.handler.LoggerHandler;
import io.vertx.rxjava.ext.web.handler.TimeoutHandler;
import io.vertx.rxjava.kafka.client.producer.KafkaProducer;
import rx.Single;

import static com.nextbreakpoint.shop.common.Authority.ADMIN;
import static com.nextbreakpoint.shop.common.ContentType.APPLICATION_JSON;
import static com.nextbreakpoint.shop.common.Headers.ACCEPT;
import static com.nextbreakpoint.shop.common.Headers.AUTHORIZATION;
import static com.nextbreakpoint.shop.common.Headers.CONTENT_TYPE;
import static com.nextbreakpoint.shop.common.Headers.X_MODIFIED;
import static com.nextbreakpoint.shop.common.Headers.X_XSRF_TOKEN;
import static com.nextbreakpoint.shop.common.ServerUtil.UUID_REGEXP;
import static com.nextbreakpoint.shop.designs.Factory.createDeleteDesignHandler;
import static com.nextbreakpoint.shop.designs.Factory.createDeleteDesignsHandler;
import static com.nextbreakpoint.shop.designs.Factory.createInsertDesignHandler;
import static com.nextbreakpoint.shop.designs.Factory.createUpdateDesignHandler;
import static java.util.Arrays.asList;

public class Verticle extends AbstractVerticle {
    private WorkerExecutor executor;

    private HttpServer server;

    public static void main(String[] args) {
        System.setProperty("crypto.policy", "unlimited");
        System.setProperty("vertx.metrics.options.enabled", "true");
        System.setProperty("vertx.metrics.options.registryName", "exported");

        Launcher.main(new String[] { "run", Verticle.class.getCanonicalName(), "-conf", args.length > 0 ? args[0] : "config/default.json" });
    }

    @Override
    public void start(Future<Void> startFuture) {
        final JsonObject config = vertx.getOrCreateContext().config();

        executor = createWorkerExecutor(config);

        vertx.<Void>rxExecuteBlocking(future -> initServer(config, future))
                .subscribe(x -> startFuture.complete(), err -> startFuture.fail(err));
    }

    @Override
    public void stop(Future<Void> stopFuture) {
        if (executor != null) {
            executor.close();
        }

        if (server != null) {
            server.rxClose().subscribe(x -> stopFuture.complete(), err -> stopFuture.fail(err));
        } else {
            stopFuture.complete();
        }
    }

    private void initServer(JsonObject config, io.vertx.rxjava.core.Future<Void> future) {
        Single.fromCallable(() -> createServer(config)).subscribe(x -> future.complete(), err -> future.fail(err));
    }

    private Void createServer(JsonObject config) {
        GraphiteManager.configureMetrics(config);

        final Integer port = config.getInteger("host_port");

        final String webUrl = config.getString("client_web_url");

        final String topic = config.getString("events_topic");

        final String messageSource = config.getString("message_source");

        final JWTAuth jwtProvider = JWTProviderFactory.create(vertx, config);

        final KafkaProducer<String, String> producer = KafkaClientFactory.createProducer(vertx, config);

        final Router mainRouter = Router.router(vertx);

        final Router apiRouter = Router.router(vertx);

        mainRouter.route().handler(LoggerHandler.create());
        mainRouter.route().handler(BodyHandler.create());
        mainRouter.route().handler(CookieHandler.create());
        mainRouter.route().handler(TimeoutHandler.create(30000));

        final CorsHandler corsHandler = CORSHandlerFactory.createWithAll(webUrl, asList(AUTHORIZATION, CONTENT_TYPE, ACCEPT, X_XSRF_TOKEN, X_MODIFIED), asList(CONTENT_TYPE, X_XSRF_TOKEN, X_MODIFIED));

        apiRouter.route("/designs/*").handler(corsHandler);

        final Handler<RoutingContext> onAccessDenied = rc -> rc.fail(Failure.accessDenied("Authorisation failed"));

        final Handler insertDesignHandler = new AccessHandler(jwtProvider, createInsertDesignHandler(producer, topic, messageSource), onAccessDenied, asList(ADMIN));

        final Handler updateDesignHandler = new AccessHandler(jwtProvider, createUpdateDesignHandler(producer, topic, messageSource), onAccessDenied, asList(ADMIN));

        final Handler deleteDesignHandler = new AccessHandler(jwtProvider, createDeleteDesignHandler(producer, topic, messageSource), onAccessDenied, asList(ADMIN));

        final Handler deleteDesignsHandler = new AccessHandler(jwtProvider, createDeleteDesignsHandler(producer, topic, messageSource), onAccessDenied, asList(ADMIN));

        apiRouter.post("/designs")
                .produces(APPLICATION_JSON)
                .consumes(APPLICATION_JSON)
                .handler(insertDesignHandler);

        apiRouter.putWithRegex("/designs/" + UUID_REGEXP)
                .produces(APPLICATION_JSON)
                .consumes(APPLICATION_JSON)
                .handler(updateDesignHandler);

        apiRouter.deleteWithRegex("/designs/" + UUID_REGEXP)
                .produces(APPLICATION_JSON)
                .handler(deleteDesignHandler);

        apiRouter.delete("/designs")
                .handler(deleteDesignsHandler);

        apiRouter.options("/designs/*")
                .handler(ResponseHelper::sendNoContent);

        apiRouter.options("/designs")
                .handler(ResponseHelper::sendNoContent);

        mainRouter.route().failureHandler(ResponseHelper::sendFailure);

        mainRouter.mountSubRouter("/api", apiRouter);

        final HttpServerOptions options = ServerUtil.makeServerOptions(config);

        server = vertx.createHttpServer(options)
                .requestHandler(mainRouter::accept)
                .listen(port);

        return null;
    }

    private WorkerExecutor createWorkerExecutor(JsonObject config) {
        final int poolSize = Runtime.getRuntime().availableProcessors();
        final long maxExecuteTime = config.getInteger("max_execution_time_in_millis", 2000) * 1000000L;
        return vertx.createSharedWorkerExecutor("worker", poolSize, maxExecuteTime);
    }
}
