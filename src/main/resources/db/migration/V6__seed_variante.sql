-- variante es tabla de lookup desde la V1 (variante(nombre) PRIMARY KEY,
-- referenciada por FK desde contexto_cultural, corpus_chunk y validacion),
-- pero nunca se sembró: hasta ahora ningún flujo real insertaba un valor no
-- nulo ahí. Fase 4 sí lo hace (POST /validaciones/expresion acepta
-- "variante" en el request, ver docs/API_CONTRACTS.md), así que sin esto
-- cualquier variante no vacía viola la FK. Las cuatro variantes son las que
-- ya usa el frontend prototipo (ver docs/audit.md).
INSERT INTO variante (nombre) VALUES
    ('Nguluche'),
    ('Chedungun'),
    ('Lafkenche'),
    ('Pewenche');
