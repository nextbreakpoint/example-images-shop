CREATE KEYSPACE IF NOT EXISTS designs WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };

CREATE ROLE IF NOT EXISTS admin WITH PASSWORD = 'password' AND LOGIN = true;
CREATE ROLE IF NOT EXISTS verticle WITH PASSWORD = 'password' AND LOGIN = true;

GRANT ALL PERMISSIONS on KEYSPACE designs TO admin;
GRANT ALL PERMISSIONS on KEYSPACE designs TO verticle;

USE designs;

CREATE TABLE IF NOT EXISTS DESIGN_EVENT (
  DESIGN_UUID UUID,
  DESIGN_DATA TEXT,
  DESIGN_STATUS TEXT,
  DESIGN_CHECKSUM TEXT,
  EVENT_TIMESTAMP TIMEUUID,
  EVENT_PUBLISHED TIMESTAMP,
  PRIMARY KEY (DESIGN_UUID, EVENT_TIMESTAMP)
) WITH CLUSTERING ORDER BY (EVENT_TIMESTAMP ASC);

CREATE TABLE IF NOT EXISTS DESIGN_ENTITY (
  DESIGN_UUID UUID,
  DESIGN_DATA TEXT,
  DESIGN_CHECKSUM TEXT,
  DESIGN_CREATED TIMESTAMP,
  DESIGN_UPDATED TIMESTAMP,
  PRIMARY KEY (DESIGN_UUID)
);

CREATE TABLE IF NOT EXISTS VERSION_ENTITY (
    VERSION_UUID UUID,
    VERSION_CREATED TIMESTAMP,
    VERSION_UPDATED TIMESTAMP,
    VERSION_PUBLISHED TIMESTAMP,
    DESIGN_DATA TEXT,
    DESIGN_CHECKSUM TEXT,
    PRIMARY KEY (DESIGN_CHECKSUM)
);

CREATE INDEX IF NOT EXISTS VERSION_UUID_IDX ON VERSION_ENTITY (VERSION_UUID);

CREATE TABLE IF NOT EXISTS RENDER_ENTITY (
    RENDER_UUID UUID,
    RENDER_LEVEL SMALLINT,
    RENDER_ERROR TEXT,
    RENDER_STATUS TEXT,
    RENDER_CREATED TIMESTAMP,
    RENDER_UPDATED TIMESTAMP,
    RENDER_PUBLISHED TIMESTAMP,
    RENDER_COMPLETED TIMESTAMP,
    VERSION_UUID UUID,
    PRIMARY KEY (VERSION_UUID, RENDER_LEVEL)
);

CREATE INDEX IF NOT EXISTS RENDER_UUID_IDX ON RENDER_ENTITY (RENDER_UUID);

CREATE TABLE IF NOT EXISTS TILE_ENTITY (
    TILE_UUID UUID,
    TILE_LEVEL SMALLINT,
    TILE_ROW SMALLINT,
    TILE_COL SMALLINT,
    TILE_ERROR TEXT,
    TILE_STATUS TEXT,
    TILE_LOCATION TEXT,
    TILE_CREATED TIMESTAMP,
    TILE_UPDATED TIMESTAMP,
    TILE_PUBLISHED TIMESTAMP,
    TILE_COMPLETED TIMESTAMP,
    VERSION_UUID UUID,
    PRIMARY KEY (VERSION_UUID, TILE_LEVEL, TILE_ROW, TILE_COL)
);

CREATE INDEX IF NOT EXISTS TILE_UUID_IDX ON TILE_ENTITY (TILE_UUID);

// FIXTURES

TRUNCATE TABLE DESIGN_EVENT;
TRUNCATE TABLE DESIGN_ENTITY;
TRUNCATE TABLE VERSION_ENTITY;
TRUNCATE TABLE RENDER_ENTITY;
TRUNCATE TABLE TILE_ENTITY;

INSERT INTO DESIGN_EVENT (DESIGN_UUID, DESIGN_DATA, DESIGN_STATUS, DESIGN_CHECKSUM, EVENT_TIMESTAMP) VALUES (48f6563e-e095-4d91-b473-4d8afc758c43, '{"metadata":"{\"translation\":{\"x\":0.0,\"y\":0.0,\"z\":1.0,\"w\":0.0},\"rotation\":{\"x\":0.0,\"y\":0.0,\"z\":0.0,\"w\":0.0},\"scale\":{\"x\":1.0,\"y\":1.0,\"z\":1.0,\"w\":1.0},\"point\":{\"x\":0.0,\"y\":0.0},\"julia\":false,\"options\":{\"showPreview\":false,\"showTraps\":false,\"showOrbit\":false,\"showPoint\":false,\"previewOrigin\":{\"x\":0.0,\"y\":0.0},\"previewSize\":{\"x\":0.25,\"y\":0.25}}}","manifest":"{\"pluginId\":\"Mandelbrot\"}","script":"fractal {\norbit [-2.0 - 2.0i,+2.0 + 2.0i] [x,n] {\nloop [0, 200] (mod2(x) > 40) {\nx = x * x + w;\n}\n}\ncolor [#FF000000] {\npalette gradient {\n[#FFFFFFFF > #FF000000, 100];\n[#FF000000 > #FFFFFFFF, 100];\n}\ninit {\nm = 100 * (1 + sin(mod(x) * 0.2 / pi));\n}\nrule (n > 0) [1] {\ngradient[m - 1]\n}\n}\n}\n"}', 'CREATED', '0', now());

INSERT INTO DESIGN_EVENT (DESIGN_UUID, DESIGN_DATA, DESIGN_STATUS, DESIGN_CHECKSUM, EVENT_TIMESTAMP) VALUES (afb0f952-b26a-46de-8e65-40d5e98dc243, '{"metadata":"{\"translation\":{\"x\":0.0,\"y\":0.0,\"z\":1.0,\"w\":0.0},\"rotation\":{\"x\":0.0,\"y\":0.0,\"z\":0.0,\"w\":0.0},\"scale\":{\"x\":1.0,\"y\":1.0,\"z\":1.0,\"w\":1.0},\"point\":{\"x\":0.0,\"y\":0.0},\"julia\":false,\"options\":{\"showPreview\":false,\"showTraps\":false,\"showOrbit\":false,\"showPoint\":false,\"previewOrigin\":{\"x\":0.0,\"y\":0.0},\"previewSize\":{\"x\":0.25,\"y\":0.25}}}","manifest":"{\"pluginId\":\"Mandelbrot\"}","script":"fractal {\norbit [-2.0 - 2.0i,+2.0 + 2.0i] [x,n] {\nloop [0, 100] (mod2(x) > 40) {\nx = x * x + w;\n}\n}\ncolor [#FF000000] {\npalette gradient {\n[#FFFFFFFF > #FF000000, 100];\n[#FF000000 > #FFFFFFFF, 100];\n}\ninit {\nm = 100 * (1 + sin(mod(x) * 0.2 / pi));\n}\nrule (n > 0) [1] {\ngradient[m - 1]\n}\n}\n}\n"}', 'CREATED', '1', now());

INSERT INTO DESIGN_EVENT (DESIGN_UUID, DESIGN_DATA, DESIGN_STATUS, DESIGN_CHECKSUM, EVENT_TIMESTAMP) VALUES (51a0ce28-10a7-4a2e-ac6b-b80f682c0bb7, '{"metadata":"{\"translation\":{\"x\":0.0,\"y\":0.0,\"z\":1.0,\"w\":0.0},\"rotation\":{\"x\":0.0,\"y\":0.0,\"z\":0.0,\"w\":0.0},\"scale\":{\"x\":1.0,\"y\":1.0,\"z\":1.0,\"w\":1.0},\"point\":{\"x\":0.0,\"y\":0.0},\"julia\":false,\"options\":{\"showPreview\":false,\"showTraps\":false,\"showOrbit\":false,\"showPoint\":false,\"previewOrigin\":{\"x\":0.0,\"y\":0.0},\"previewSize\":{\"x\":0.25,\"y\":0.25}}}","manifest":"{\"pluginId\":\"Mandelbrot\"}","script":"fractal {\norbit [-2.0 - 2.0i,+2.0 + 2.0i] [x,n] {\nloop [0, 20] (mod2(x) > 40) {\nx = x * x + w;\n}\n}\ncolor [#FF000000] {\npalette gradient {\n[#FFFFFFFF > #FF000000, 100];\n[#FF000000 > #FFFFFFFF, 100];\n}\ninit {\nm = 100 * (1 + sin(mod(x) * 0.2 / pi));\n}\nrule (n > 0) [1] {\ngradient[m - 1]\n}\n}\n}\n"}', 'CREATED', '2', now());

INSERT INTO DESIGN_EVENT (DESIGN_UUID, DESIGN_DATA, DESIGN_STATUS, DESIGN_CHECKSUM, EVENT_TIMESTAMP) VALUES (2fe33eed-58f7-425b-b10c-2e490c0f248e, '{"metadata":"{\"translation\":{\"x\":0.0,\"y\":0.0,\"z\":1.0,\"w\":0.0},\"rotation\":{\"x\":0.0,\"y\":0.0,\"z\":0.0,\"w\":0.0},\"scale\":{\"x\":1.0,\"y\":1.0,\"z\":1.0,\"w\":1.0},\"point\":{\"x\":0.0,\"y\":0.0},\"julia\":false,\"options\":{\"showPreview\":false,\"showTraps\":false,\"showOrbit\":false,\"showPoint\":false,\"previewOrigin\":{\"x\":0.0,\"y\":0.0},\"previewSize\":{\"x\":0.25,\"y\":0.25}}}","manifest":"{\"pluginId\":\"Mandelbrot\"}","script":"fractal {\norbit [-2.0 - 2.0i,+2.0 + 2.0i] [x,n] {\nloop [0, 10] (mod2(x) > 40) {\nx = x * x + w;\n}\n}\ncolor [#FF000000] {\npalette gradient {\n[#FFFFFFFF > #FF000000, 100];\n[#FF000000 > #FFFFFFFF, 100];\n}\ninit {\nm = 100 * (1 + sin(mod(x) * 0.2 / pi));\n}\nrule (n > 0) [1] {\ngradient[m - 1]\n}\n}\n}\n"}', 'CREATED', '3', now());

INSERT INTO DESIGN_EVENT (DESIGN_UUID, DESIGN_DATA, DESIGN_STATUS, DESIGN_CHECKSUM, EVENT_TIMESTAMP) VALUES (afb0f952-b26a-46de-8e65-40d5e98dc243, null, 'DELETED', null, now());

INSERT INTO DESIGN_EVENT (DESIGN_UUID, DESIGN_DATA, DESIGN_STATUS, DESIGN_CHECKSUM, EVENT_TIMESTAMP) VALUES (2fe33eed-58f7-425b-b10c-2e490c0f248e, '{"metadata":"{\"translation\":{\"x\":0.0,\"y\":0.0,\"z\":1.0,\"w\":0.0},\"rotation\":{\"x\":0.0,\"y\":0.0,\"z\":0.0,\"w\":0.0},\"scale\":{\"x\":1.0,\"y\":1.0,\"z\":1.0,\"w\":1.0},\"point\":{\"x\":0.0,\"y\":0.0},\"julia\":false,\"options\":{\"showPreview\":false,\"showTraps\":false,\"showOrbit\":false,\"showPoint\":false,\"previewOrigin\":{\"x\":0.0,\"y\":0.0},\"previewSize\":{\"x\":0.25,\"y\":0.25}}}","manifest":"{\"pluginId\":\"Mandelbrot\"}","script":"fractal {\norbit [-2.0 - 2.0i,+2.0 + 2.0i] [x,n] {\nloop [0, 10] (mod2(x) > 40) {\nx = x * x + w;\n}\n}\ncolor [#FF000000] {\npalette gradient {\n[#FFFFFFFF > #FF000000, 100];\n[#FF000000 > #FFFFFFFF, 100];\n}\ninit {\nm = 100 * (1 + sin(mod(x) * 0.2 / pi));\n}\nrule (n > 0) [1] {\ngradient[m - 1]\n}\n}\n}\n"}', 'UPDATED', '4', now());

INSERT INTO DESIGN_ENTITY (DESIGN_UUID, DESIGN_DATA, DESIGN_CHECKSUM, DESIGN_CREATED, DESIGN_UPDATED) VALUES (48f6563e-e095-4d91-b473-4d8afc758c43, '{"metadata":"{\"translation\":{\"x\":0.0,\"y\":0.0,\"z\":1.0,\"w\":0.0},\"rotation\":{\"x\":0.0,\"y\":0.0,\"z\":0.0,\"w\":0.0},\"scale\":{\"x\":1.0,\"y\":1.0,\"z\":1.0,\"w\":1.0},\"point\":{\"x\":0.0,\"y\":0.0},\"julia\":false,\"options\":{\"showPreview\":false,\"showTraps\":false,\"showOrbit\":false,\"showPoint\":false,\"previewOrigin\":{\"x\":0.0,\"y\":0.0},\"previewSize\":{\"x\":0.25,\"y\":0.25}}}","manifest":"{\"pluginId\":\"Mandelbrot\"}","script":"fractal {\norbit [-2.0 - 2.0i,+2.0 + 2.0i] [x,n] {\nloop [0, 200] (mod2(x) > 40) {\nx = x * x + w;\n}\n}\ncolor [#FF000000] {\npalette gradient {\n[#FFFFFFFF > #FF000000, 100];\n[#FF000000 > #FFFFFFFF, 100];\n}\ninit {\nm = 100 * (1 + sin(mod(x) * 0.2 / pi));\n}\nrule (n > 0) [1] {\ngradient[m - 1]\n}\n}\n}\n"}', '0', toTimeStamp(now()), toTimeStamp(now()));

INSERT INTO DESIGN_ENTITY (DESIGN_UUID, DESIGN_DATA, DESIGN_CHECKSUM, DESIGN_CREATED, DESIGN_UPDATED) VALUES (2fe33eed-58f7-425b-b10c-2e490c0f248e, '{"metadata":"{\"translation\":{\"x\":0.0,\"y\":0.0,\"z\":1.0,\"w\":0.0},\"rotation\":{\"x\":0.0,\"y\":0.0,\"z\":0.0,\"w\":0.0},\"scale\":{\"x\":1.0,\"y\":1.0,\"z\":1.0,\"w\":1.0},\"point\":{\"x\":0.0,\"y\":0.0},\"julia\":false,\"options\":{\"showPreview\":false,\"showTraps\":false,\"showOrbit\":false,\"showPoint\":false,\"previewOrigin\":{\"x\":0.0,\"y\":0.0},\"previewSize\":{\"x\":0.25,\"y\":0.25}}}","manifest":"{\"pluginId\":\"Mandelbrot\"}","script":"fractal {\norbit [-2.0 - 2.0i,+2.0 + 2.0i] [x,n] {\nloop [0, 10] (mod2(x) > 40) {\nx = x * x + w;\n}\n}\ncolor [#FF000000] {\npalette gradient {\n[#FFFFFFFF > #FF000000, 100];\n[#FF000000 > #FFFFFFFF, 100];\n}\ninit {\nm = 100 * (1 + sin(mod(x) * 0.2 / pi));\n}\nrule (n > 0) [1] {\ngradient[m - 1]\n}\n}\n}\n"}', '4', toTimeStamp(now()), toTimeStamp(now()));

INSERT INTO DESIGN_ENTITY (DESIGN_UUID, DESIGN_DATA, DESIGN_CHECKSUM, DESIGN_CREATED, DESIGN_UPDATED) VALUES (51a0ce28-10a7-4a2e-ac6b-b80f682c0bb7, '{"metadata":"{\"translation\":{\"x\":0.0,\"y\":0.0,\"z\":1.0,\"w\":0.0},\"rotation\":{\"x\":0.0,\"y\":0.0,\"z\":0.0,\"w\":0.0},\"scale\":{\"x\":1.0,\"y\":1.0,\"z\":1.0,\"w\":1.0},\"point\":{\"x\":0.0,\"y\":0.0},\"julia\":false,\"options\":{\"showPreview\":false,\"showTraps\":false,\"showOrbit\":false,\"showPoint\":false,\"previewOrigin\":{\"x\":0.0,\"y\":0.0},\"previewSize\":{\"x\":0.25,\"y\":0.25}}}","manifest":"{\"pluginId\":\"Mandelbrot\"}","script":"fractal {\norbit [-2.0 - 2.0i,+2.0 + 2.0i] [x,n] {\nloop [0, 20] (mod2(x) > 40) {\nx = x * x + w;\n}\n}\ncolor [#FF000000] {\npalette gradient {\n[#FFFFFFFF > #FF000000, 100];\n[#FF000000 > #FFFFFFFF, 100];\n}\ninit {\nm = 100 * (1 + sin(mod(x) * 0.2 / pi));\n}\nrule (n > 0) [1] {\ngradient[m - 1]\n}\n}\n}\n"}', '3', toTimeStamp(now()), toTimeStamp(now()));
