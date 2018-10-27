package com.nextbreakpoint.shop.gateway.handlers;

import com.nextbreakpoint.shop.common.model.DesignDocument;
import com.nextbreakpoint.shop.common.model.DesignResource;
import com.nextbreakpoint.shop.common.model.Failure;
import com.nextbreakpoint.shop.common.vertx.Authentication;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.buffer.Buffer;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.rxjava.ext.web.client.HttpRequest;
import io.vertx.rxjava.ext.web.client.HttpResponse;
import io.vertx.rxjava.ext.web.client.WebClient;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.nextbreakpoint.shop.common.model.ContentType.APPLICATION_JSON;
import static com.nextbreakpoint.shop.common.model.Headers.ACCEPT;
import static com.nextbreakpoint.shop.common.model.Headers.AUTHORIZATION;

public class DesignsLoadHandler implements Handler<RoutingContext> {
    private final WebClient client;
    private final String webUrl;
    private final String designsUrl;

    public DesignsLoadHandler(WebClient client, JsonObject config) {
        this.client = client;
        this.webUrl = config.getString("client_web_url");
        this.designsUrl = config.getString("client_designs_query_url");
    }

    public void handle(RoutingContext routingContext) {
        try {
            final String token = Authentication.getToken(routingContext);

            final HttpRequest<Buffer> request = client.get(designsUrl);

            if (token != null) {
                request.putHeader(AUTHORIZATION, token);
            }

            request.putHeader(ACCEPT, APPLICATION_JSON).rxSend()
                    .subscribe(response -> handleDesigns(routingContext, response), e -> routingContext.fail(Failure.requestFailed(e)));
        } catch (Exception e) {
            routingContext.fail(Failure.requestFailed(e));
        }
    }

    private void handleDesigns(RoutingContext routingContext, HttpResponse<Buffer> response) {
        try {
            if (response.statusCode() == 200) {
                final List<DesignResource> resources = Stream.of(response.bodyAsJson(DesignDocument[].class))
                        .map(this::makeDesign).collect(Collectors.toList());

                routingContext.put("designs", resources);
                routingContext.put("timestamp", System.currentTimeMillis());

                routingContext.next();
            } else {
                routingContext.put("designs", Collections.EMPTY_LIST);
                routingContext.put("timestamp", System.currentTimeMillis());

                routingContext.next();
            }
        } catch (Exception e) {
            routingContext.fail(Failure.requestFailed(e));
        }
    }

    private DesignResource makeDesign(DesignDocument document) {
        return new DesignResource(document.getUuid(), document.getChecksum(), webUrl + "/content/designs/" + document.getUuid() + ".html", designsUrl + "/" + document.getUuid() + "/0/0/0/256.png", "", "", "", "");
    }

    public static DesignsLoadHandler create(WebClient client, JsonObject config) {
        return new DesignsLoadHandler(client, config);
    }
}