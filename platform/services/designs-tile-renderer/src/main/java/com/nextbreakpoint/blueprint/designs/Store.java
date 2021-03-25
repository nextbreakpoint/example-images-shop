package com.nextbreakpoint.blueprint.designs;

import com.nextbreakpoint.blueprint.designs.model.PersistenceResult;
import rx.Single;

import java.util.UUID;

public interface Store {
    Single<PersistenceResult<Void>> publishTile(UUID version, short level, short x, short y);
}
