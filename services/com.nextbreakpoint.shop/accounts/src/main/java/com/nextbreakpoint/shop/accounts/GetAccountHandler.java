package com.nextbreakpoint.shop.accounts;

import com.nextbreakpoint.shop.common.Failure;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.ext.web.RoutingContext;

import java.util.UUID;

import static com.nextbreakpoint.shop.common.ContentType.APPLICATION_JSON;
import static com.nextbreakpoint.shop.common.Headers.CONTENT_TYPE;

public class GetAccountHandler implements Handler<RoutingContext> {
    private Store store;

    public GetAccountHandler(Store store) {
        this.store = store;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        try {
            processGetAccount(routingContext);
        } catch (Exception e) {
            e.printStackTrace();

            routingContext.fail(Failure.requestFailed(e));
        }
    }

    private void processGetAccount(RoutingContext routingContext) {
        final UUID uuid = UUID.fromString(routingContext.request().getParam("param0"));

        store.loadAccount(uuid).subscribe(result -> emitGetAccountResponse(routingContext, result.orElse(null)), err -> routingContext.fail(Failure.databaseError(err)));
    }

    private void emitGetAccountResponse(RoutingContext routingContext, JsonObject result) {
        if (result == null) {
            routingContext.response().setStatusCode(404).end();
        } else {
            final String uuid = result.getString("UUID");
            final String name = result.getString("NAME");
            final String role = result.getString("ROLE");
            routingContext.response().putHeader(CONTENT_TYPE, APPLICATION_JSON).setStatusCode(200).end(createAccountResponseObject(UUID.fromString(uuid), name, role).encode());
        }
    }

    private JsonObject createAccountResponseObject(UUID uuid, String name, String role) {
        final JsonObject json = new JsonObject();
        json.put("uuid", uuid.toString());
        json.put("name", name);
        json.put("role", role);
        return json;
    }
}