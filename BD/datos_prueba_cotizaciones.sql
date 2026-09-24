
-- DATOS DE PRUEBA TEMPORALES — COTIZACIONES

START TRANSACTION;

-- 1) Limpieza de datos de prueba previos (misma lógica que limpiar_datos_prueba_cotizaciones.sql)
CREATE TEMPORARY TABLE tmp_cotizaciones_prueba AS
    SELECT DISTINCT id_cotizacion
    FROM detalle_cotizacion
    WHERE descripcion LIKE '[PRUEBA TEMPORAL]%';

DELETE FROM pago WHERE id_cotizacion IN (SELECT id_cotizacion FROM tmp_cotizaciones_prueba);
DELETE FROM detalle_cotizacion WHERE id_cotizacion IN (SELECT id_cotizacion FROM tmp_cotizaciones_prueba);
DELETE FROM cotizacion WHERE id_cotizacion IN (SELECT id_cotizacion FROM tmp_cotizaciones_prueba);

DROP TEMPORARY TABLE tmp_cotizaciones_prueba;

-- 2) Cotización de prueba 1 — Pendiente
INSERT INTO cotizacion (fecha, subtotal, total, id_estado_cotizacion, id_cliente)
VALUES (CURDATE(), 150.00, 169.50,
        (SELECT id_estado_cotizacion FROM estado_cotizacion WHERE nombre = 'Pendiente'), 3);
SET @cotizacion1 = LAST_INSERT_ID();

INSERT INTO detalle_cotizacion (descripcion, cantidad, precio, subtotal, id_cotizacion)
VALUES ('[PRUEBA TEMPORAL] Inscripción a taller de Excel intermedio', 2, 75.00, 150.00, @cotizacion1);

-- 3) Cotización de prueba 2 — En proceso
INSERT INTO cotizacion (fecha, subtotal, total, id_estado_cotizacion, id_cliente)
VALUES (CURDATE(), 440.00, 497.20,
        (SELECT id_estado_cotizacion FROM estado_cotizacion WHERE nombre = 'En proceso'), 4);
SET @cotizacion2 = LAST_INSERT_ID();

INSERT INTO detalle_cotizacion (descripcion, cantidad, precio, subtotal, id_cotizacion)
VALUES ('[PRUEBA TEMPORAL] Alquiler de sala de conferencias (1 día)', 1, 200.00, 200.00, @cotizacion2),
       ('[PRUEBA TEMPORAL] Refrigerio para 30 personas', 30, 8.00, 240.00, @cotizacion2);

-- 4) Cotización de prueba 3 — Pendiente
INSERT INTO cotizacion (fecha, subtotal, total, id_estado_cotizacion, id_cliente)
VALUES (CURDATE(), 350.00, 395.50,
        (SELECT id_estado_cotizacion FROM estado_cotizacion WHERE nombre = 'Pendiente'), 2);
SET @cotizacion3 = LAST_INSERT_ID();

INSERT INTO detalle_cotizacion (descripcion, cantidad, precio, subtotal, id_cotizacion)
VALUES ('[PRUEBA TEMPORAL] Inscripción a diplomado de gestión de proyectos', 1, 350.00, 350.00, @cotizacion3);

COMMIT;
