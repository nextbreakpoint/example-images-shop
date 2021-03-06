package com.nextbreakpoint.shop.designs.persistence;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.utils.UUIDs;
import com.google.common.util.concurrent.ListenableFuture;
import com.nextbreakpoint.shop.common.model.DesignChange;
import com.nextbreakpoint.shop.common.model.commands.DeleteDesignCommand;
import com.nextbreakpoint.shop.common.model.commands.InsertDesignCommand;
import com.nextbreakpoint.shop.common.model.commands.UpdateDesignCommand;
import com.nextbreakpoint.shop.common.model.events.DesignChangedEvent;
import com.nextbreakpoint.shop.designs.Store;
import com.nextbreakpoint.shop.designs.model.PersistenceResult;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import rx.Single;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class CassandraStore implements Store {
    private final Logger logger = LoggerFactory.getLogger(CassandraStore.class.getName());

    private static final String ERROR_INSERT_DESIGN = "An error occurred while inserting a design";
    private static final String ERROR_UPDATE_DESIGN = "An error occurred while updating a design";
    private static final String ERROR_DELETE_DESIGN = "An error occurred while deleting a design";

    private static final String INSERT_DESIGN = "INSERT INTO DESIGNS (DESIGN_UUID, DESIGN_JSON, DESIGN_STATUS, DESIGN_CHECKSUM, EVENT_TIMESTAMP) VALUES (?, ?, ?, ?, ?)";
    private static final String SELECT_DESIGN = "SELECT * FROM DESIGNS WHERE DESIGN_UUID = ?";
    private static final String INSERT_DESIGN_VIEW = "INSERT INTO DESIGNS_VIEW (DESIGN_UUID, DESIGN_JSON, DESIGN_CHECKSUM, DESIGN_TIMESTAMP) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_DESIGN_VIEW = "UPDATE DESIGNS_VIEW SET DESIGN_JSON=?, DESIGN_CHECKSUM=?, DESIGN_TIMESTAMP=? WHERE DESIGN_UUID=?";
    private static final String DELETE_DESIGN_VIEW = "DELETE FROM DESIGNS_VIEW WHERE DESIGN_UUID=?";

    private static final int EXECUTE_TIMEOUT = 10;

    private final Supplier<Session> supplier;

    private Session session;

    private ListenableFuture<PreparedStatement> insertDesign;
    private ListenableFuture<PreparedStatement> selectDesign;
    private ListenableFuture<PreparedStatement> insertDesignView;
    private ListenableFuture<PreparedStatement> updateDesignView;
    private ListenableFuture<PreparedStatement> deleteDesignView;

    public CassandraStore(Supplier<Session> supplier) {
        this.supplier = Objects.requireNonNull(supplier);
    }

    @Override
    public Single<PersistenceResult> insertDesign(InsertDesignCommand command) {
        return withSession()
                .flatMap(session -> appendDesignEvent(session, command.getUuid(), makeInsertParams(command)))
                .doOnError(err -> handleError(ERROR_INSERT_DESIGN, err));
    }

    @Override
    public Single<PersistenceResult> updateDesign(UpdateDesignCommand command) {
        return withSession()
                .flatMap(session -> appendDesignEvent(session, command.getUuid(), makeUpdateParams(command)))
                .doOnError(err -> handleError(ERROR_UPDATE_DESIGN, err));
    }

    @Override
    public Single<PersistenceResult> deleteDesign(DeleteDesignCommand command) {
        return withSession()
                .flatMap(session -> appendDesignEvent(session, command.getUuid(), makeDeleteParams(command)))
                .doOnError(err -> handleError(ERROR_DELETE_DESIGN, err));
    }

    public Single<PersistenceResult> updateDesign(DesignChangedEvent event) {
        return withSession()
                .flatMap(session -> updateDesignView(session, event.getUuid()))
                .doOnError(err -> handleError(ERROR_INSERT_DESIGN, err));
    }

    private Single<Session> withSession() {
        if (session == null) {
            session = supplier.get();
            if (session == null) {
                return Single.error(new RuntimeException("Cannot create session"));
            }
            insertDesign = session.prepareAsync(INSERT_DESIGN);
            selectDesign = session.prepareAsync(SELECT_DESIGN);
            insertDesignView = session.prepareAsync(INSERT_DESIGN_VIEW);
            updateDesignView = session.prepareAsync(UPDATE_DESIGN_VIEW);
            deleteDesignView = session.prepareAsync(DELETE_DESIGN_VIEW);
        }
        return Single.just(session);
    }

    private Single<ResultSet> getResultSet(ResultSetFuture future) {
        return Single.fromCallable(() -> future.getUninterruptibly(EXECUTE_TIMEOUT, TimeUnit.SECONDS));
    }

    private Single<PersistenceResult> appendDesignEvent(Session session, UUID uuid, Object[] values) {
        return Single.from(insertDesign)
                .map(pst -> pst.bind(values))
                .map(session::executeAsync)
                .flatMap(this::getResultSet)
                .map(rs -> new PersistenceResult(uuid));
    }

    private Single<PersistenceResult> updateDesignView(Session session, UUID uuid) {
        return Single.from(selectDesign)
                .map(pst -> pst.bind(new Object[] { uuid }))
                .map(session::executeAsync)
                .flatMap(this::getResultSet)
                .map(this::toDesignChanges)
                .map(changes -> changes.stream().reduce(this::mergeChanges))
                .flatMap(maybeChange -> maybeChange.map(change -> updateDesignView(session, change)).orElseGet(() -> Single.just(new PersistenceResult(uuid))));
    }

    private List<DesignChange> toDesignChanges(ResultSet rs) {
        final List<DesignChange> changes = new ArrayList<>();
        final Iterator<Row> iter = rs.iterator();
        while (iter.hasNext()) {
            if (rs.getAvailableWithoutFetching() >= 100 && !rs.isFullyFetched()) {
                rs.fetchMoreResults();
            }
            final Row row = iter.next();
            changes.add(getDesignChange(row));
        }
        return changes;
    }

    private Single<PersistenceResult> updateDesignView(Session session, DesignChange change) {
        switch (change.getStatus().toLowerCase()) {
            case "created": {
                return Single.from(insertDesignView)
                        .map(pst -> pst.bind(makeInsertViewParams(change)))
                        .map(session::executeAsync)
                        .flatMap(this::getResultSet)
                        .map(rs -> new PersistenceResult(change.getUuid()));
            }
            case "updated": {
                return Single.from(updateDesignView)
                        .map(pst -> pst.bind(makeUpdateViewParams(change)))
                        .map(session::executeAsync)
                        .flatMap(this::getResultSet)
                        .map(rs -> new PersistenceResult(change.getUuid()));
            }
            case "deleted": {
                return Single.from(deleteDesignView)
                        .map(pst -> pst.bind(makeDeleteViewParams(change)))
                        .map(session::executeAsync)
                        .flatMap(this::getResultSet)
                        .map(rs -> new PersistenceResult(change.getUuid()));
            }
        }
        throw new IllegalStateException("Unknown status: " + change.getStatus());
    }

    private DesignChange mergeChanges(DesignChange designDocument1, DesignChange designDocument2) {
        if (designDocument2.getStatus().equalsIgnoreCase("deleted")) {
            return new DesignChange(designDocument1.getUuid(), designDocument1.getJson(), designDocument2.getStatus(), designDocument1.getChecksum(), designDocument2.getModified());
        } else {
            return new DesignChange(designDocument1.getUuid(), designDocument2.getJson(), designDocument2.getStatus(), designDocument2.getChecksum(), designDocument2.getModified());
        }
    }

    private DesignChange getDesignChange(Row row) {
        final UUID uuid = row.getUUID("DESIGN_UUID");
        final String json = row.getString("DESIGN_JSON");
        final String status = row.getString("DESIGN_STATUS");
        final String checksum = row.getString("DESIGN_CHECKSUM");
        final Date modified = new Date(UUIDs.unixTimestamp(row.getUUID("EVENT_TIMESTAMP")));
        return new DesignChange(uuid, json, status, checksum, modified);
    }

    private Object[] makeInsertParams(InsertDesignCommand command) {
        return new Object[] { command.getUuid(), command.getJson(), "CREATED", computeChecksum(command.getJson()), command.getTimestamp() };
    }

    private Object[] makeUpdateParams(UpdateDesignCommand command) {
        return new Object[] { command.getUuid(), command.getJson(), "UPDATED", computeChecksum(command.getJson()), command.getTimestamp() };
    }

    private Object[] makeDeleteParams(DeleteDesignCommand command) {
        return new Object[] { command.getUuid(), null, "DELETED", null, command.getTimestamp() };
    }

    private Object[] makeInsertViewParams(DesignChange change) {
        return new Object[] { change.getUuid(), change.getJson(), computeChecksum(change.getJson()), change.getModified() };
    }

    private Object[] makeUpdateViewParams(DesignChange change) {
        return new Object[] { change.getJson(), computeChecksum(change.getJson()), change.getModified(), change.getUuid() };
    }

    private Object[] makeDeleteViewParams(DesignChange change) {
        return new Object[] { change.getUuid() };
    }

    private String computeChecksum(String json) {
        try {
            final byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            final MessageDigest md = MessageDigest.getInstance("MD5");
            return Base64.getEncoder().encodeToString(md.digest(bytes));
        } catch (Exception e) {
            throw new RuntimeException("Cannot compute checksum", e);
        }
    }

    private void handleError(String message, Throwable err) {
        logger.error(message, err);
    }
}
