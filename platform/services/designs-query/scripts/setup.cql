CREATE KEYSPACE IF NOT EXISTS designs WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };

CREATE ROLE IF NOT EXISTS admin WITH PASSWORD = 'password' AND LOGIN = true;

GRANT ALL PERMISSIONS ON KEYSPACE designs TO admin;

USE designs;

CREATE TABLE IF NOT EXISTS DESIGNS_VIEW (
  DESIGN_UUID UUID,
  DESIGN_JSON TEXT,
  DESIGN_CHECKSUM TEXT,
  DESIGN_TIMESTAMP TIMESTAMP,
  PRIMARY KEY (DESIGN_UUID)
);

INSERT INTO DESIGNS_VIEW (DESIGN_UUID, DESIGN_JSON, DESIGN_CHECKSUM, DESIGN_TIMESTAMP) VALUES (48f6563e-e095-4d91-b473-4d8afc758c43, '{"metadata":"{\"translation\":{\"x\":0.0,\"y\":0.0,\"z\":1.0,\"w\":0.0},\"rotation\":{\"x\":0.0,\"y\":0.0,\"z\":0.0,\"w\":0.0},\"scale\":{\"x\":1.0,\"y\":1.0,\"z\":1.0,\"w\":1.0},\"point\":{\"x\":0.0,\"y\":0.0},\"julia\":false,\"options\":{\"showPreview\":false,\"showTraps\":false,\"showOrbit\":false,\"showPoint\":false,\"previewOrigin\":{\"x\":0.0,\"y\":0.0},\"previewSize\":{\"x\":0.25,\"y\":0.25}}}","manifest":"{\"pluginId\":\"Mandelbrot\"}","script":"fractal {\norbit [-2.0 - 2.0i,+2.0 + 2.0i] [x,n] {\nloop [0, 200] (mod2(x) > 40) {\nx = x * x + w;\n}\n}\ncolor [#FF000000] {\npalette gradient {\n[#FFFFFFFF > #FF000000, 100];\n[#FF000000 > #FFFFFFFF, 100];\n}\ninit {\nm = 100 * (1 + sin(mod(x) * 0.2 / pi));\n}\nrule (n > 0) [1] {\ngradient[m - 1]\n}\n}\n}\n"}', '0', toTimeStamp(now())) IF NOT EXISTS;

INSERT INTO DESIGNS_VIEW (DESIGN_UUID, DESIGN_JSON, DESIGN_CHECKSUM, DESIGN_TIMESTAMP) VALUES (afb0f952-b26a-46de-8e65-40d5e98dc243, '{"metadata":"{\"translation\":{\"x\":0.0,\"y\":0.0,\"z\":1.0,\"w\":0.0},\"rotation\":{\"x\":0.0,\"y\":0.0,\"z\":0.0,\"w\":0.0},\"scale\":{\"x\":1.0,\"y\":1.0,\"z\":1.0,\"w\":1.0},\"point\":{\"x\":0.0,\"y\":0.0},\"julia\":false,\"options\":{\"showPreview\":false,\"showTraps\":false,\"showOrbit\":false,\"showPoint\":false,\"previewOrigin\":{\"x\":0.0,\"y\":0.0},\"previewSize\":{\"x\":0.25,\"y\":0.25}}}","manifest":"{\"pluginId\":\"Mandelbrot\"}","script":"fractal {\norbit [-2.0 - 2.0i,+2.0 + 2.0i] [x,n] {\nloop [0, 100] (mod2(x) > 40) {\nx = x * x + w;\n}\n}\ncolor [#FF000000] {\npalette gradient {\n[#FFFFFFFF > #FF000000, 100];\n[#FF000000 > #FFFFFFFF, 100];\n}\ninit {\nm = 100 * (1 + sin(mod(x) * 0.2 / pi));\n}\nrule (n > 0) [1] {\ngradient[m - 1]\n}\n}\n}\n"}', '1', toTimeStamp(now())) IF NOT EXISTS;

INSERT INTO DESIGNS_VIEW (DESIGN_UUID, DESIGN_JSON, DESIGN_CHECKSUM, DESIGN_TIMESTAMP) VALUES (51a0ce28-10a7-4a2e-ac6b-b80f682c0bb7, '{"metadata":"{\"translation\":{\"x\":0.0,\"y\":0.0,\"z\":1.0,\"w\":0.0},\"rotation\":{\"x\":0.0,\"y\":0.0,\"z\":0.0,\"w\":0.0},\"scale\":{\"x\":1.0,\"y\":1.0,\"z\":1.0,\"w\":1.0},\"point\":{\"x\":0.0,\"y\":0.0},\"julia\":false,\"options\":{\"showPreview\":false,\"showTraps\":false,\"showOrbit\":false,\"showPoint\":false,\"previewOrigin\":{\"x\":0.0,\"y\":0.0},\"previewSize\":{\"x\":0.25,\"y\":0.25}}}","manifest":"{\"pluginId\":\"Mandelbrot\"}","script":"fractal {\norbit [-2.0 - 2.0i,+2.0 + 2.0i] [x,n] {\nloop [0, 20] (mod2(x) > 40) {\nx = x * x + w;\n}\n}\ncolor [#FF000000] {\npalette gradient {\n[#FFFFFFFF > #FF000000, 100];\n[#FF000000 > #FFFFFFFF, 100];\n}\ninit {\nm = 100 * (1 + sin(mod(x) * 0.2 / pi));\n}\nrule (n > 0) [1] {\ngradient[m - 1]\n}\n}\n}\n"}', '2', toTimeStamp(now())) IF NOT EXISTS;
