package com.nextbreakpoint.blueprint.accounts.controllers.load;

import com.nextbreakpoint.blueprint.accounts.model.LoadAccountResponse;
import com.nextbreakpoint.blueprint.common.core.Mapper;
import io.vertx.core.json.JsonObject;

import java.util.Optional;

public class LoadAccountResponseMapper implements Mapper<LoadAccountResponse, Optional<String>> {
    @Override
    public Optional<String> transform(LoadAccountResponse response) {
        final Optional<String> json = response.getAccount()
                .map(Account -> new JsonObject()
                        .put("uuid", Account.getUuid())
                        .put("name", Account.getName())
                        .put("role", Account.getAuthorities())
                        .encode());

        return json;
    }
}
