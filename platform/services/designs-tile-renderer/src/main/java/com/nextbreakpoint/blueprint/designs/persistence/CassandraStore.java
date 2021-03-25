package com.nextbreakpoint.blueprint.designs.persistence;

import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.nextbreakpoint.blueprint.designs.Store;
import com.nextbreakpoint.blueprint.designs.model.PersistenceResult;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.rxjava.cassandra.CassandraClient;
import rx.Single;

import java.time.Instant;
import java.util.*;
import java.util.function.Supplier;

public class CassandraStore implements Store {
    private final Logger logger = LoggerFactory.getLogger(CassandraStore.class.getName());

    private static final String ERROR_PUBLISH_TILE = "An error occurred while publishing a tile";

    private static final String UPDATE_TILE = "UPDATE TILE_ENTITY SET TILE_COMPLETED = ? WHERE VERSION_UUID = ? AND TILE_LEVEL = ? AND TILE_ROW = ? AND TILE_COL = ?";

    private final Supplier<CassandraClient> supplier;

    private CassandraClient session;

    private Single<PreparedStatement> updateTile;

    public CassandraStore(Supplier<CassandraClient> supplier) {
        this.supplier = Objects.requireNonNull(supplier);
    }

    @Override
    public Single<PersistenceResult<Void>> publishTile(UUID version, short level, short x, short y) {
        return withSession()
                .flatMap(session -> publishTile(session, version, level, x, y))
                .doOnError(err -> handleError(ERROR_PUBLISH_TILE, err));
    }

    private Single<PersistenceResult<Void>> publishTile(CassandraClient session, UUID version, short level, short x, short y) {
        return updateTile
                .map(pst -> pst.bind(Instant.now(), version, level, x, y))
                .flatMap(session::rxExecute)
                .map(rs -> new PersistenceResult<>(version, null));
    }

    private Single<CassandraClient> withSession() {
        if (session == null) {
            session = supplier.get();
            if (session == null) {
                return Single.error(new RuntimeException("Cannot create session"));
            }
            updateTile = session.rxPrepare(UPDATE_TILE);
        }
        return Single.just(session);
    }

    private void handleError(String message, Throwable err) {
        logger.error(message, err);
    }
}
