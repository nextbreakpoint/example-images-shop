package com.nextbreakpoint.shop.designs.controllers.list;

import com.nextbreakpoint.shop.common.model.Content;
import com.nextbreakpoint.shop.common.model.Mapper;
import com.nextbreakpoint.shop.common.model.Metadata;
import com.nextbreakpoint.shop.designs.model.ListDesignsResponse;
import io.vertx.core.json.Json;

import java.util.Set;

import static com.nextbreakpoint.shop.common.model.Metadata.MODIFIED;
import static java.util.Collections.singleton;

public class ListDesignsOutputMapper implements Mapper<ListDesignsResponse, Content> {
    @Override
    public Content transform(ListDesignsResponse response) {
        final String json = Json.encode(response.getUuids());

        final Set<Metadata> metadata = singleton(extractModified(response));

        return new Content(json, metadata);
    }

    protected Metadata extractModified(ListDesignsResponse response) {
        return new Metadata(MODIFIED, String.valueOf(response.getUpdated()));
    }
}
