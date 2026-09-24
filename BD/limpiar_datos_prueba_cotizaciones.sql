-- LIMPIEZA DE DATOS DE PRUEBA TEMPORALES — COTIZACIONES
START TRANSACTION;

CREATE TEMPORARY TABLE tmp_cotizaciones_prueba AS
    SELECT DISTINCT id_cotizacion
    FROM detalle_cotizacion
    WHERE descripcion LIKE '[PRUEBA TEMPORAL]%';

DELETE FROM pago WHERE id_cotizacion IN (SELECT id_cotizacion FROM tmp_cotizaciones_prueba);
DELETE FROM detalle_cotizacion WHERE id_cotizacion IN (SELECT id_cotizacion FROM tmp_cotizaciones_prueba);
DELETE FROM cotizacion WHERE id_cotizacion IN (SELECT id_cotizacion FROM tmp_cotizaciones_prueba);

DROP TEMPORARY TABLE tmp_cotizaciones_prueba;

COMMIT;
