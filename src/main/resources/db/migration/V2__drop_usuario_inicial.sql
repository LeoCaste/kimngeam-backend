-- La columna `inicial` es dato derivado (primera letra de `nombre`), no se
-- persiste: el backend la calcula al construir el response. Ver CLAUDE.md.
ALTER TABLE usuario DROP COLUMN inicial;
