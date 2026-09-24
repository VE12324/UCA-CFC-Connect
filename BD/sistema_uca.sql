-- versión 5.2.1
-- https://www.phpmyadmin.net/


SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


-- Base de datos: `sistema_uca`
--


--
-- Estructura de tabla para la tabla `actividad`
--

CREATE TABLE `actividad` (
                             `id_actividad` int(11) NOT NULL,
                             `nombre` varchar(150) NOT NULL,
                             `tipo` varchar(100) NOT NULL,
                             `fecha` date NOT NULL,
                             `horario` varchar(100) DEFAULT NULL,
                             `hora_inicio` datetime DEFAULT NULL,
                             `hora_fin` datetime DEFAULT NULL,
                             `descripcion` varchar(500) DEFAULT NULL,
                             `id_curso` int(11) DEFAULT NULL,
                             `id_diplomado` int(11) DEFAULT NULL,
                             `id_alquiler` int(11) DEFAULT NULL,
                             `id_solicitud_catering` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `actividad`
--

INSERT INTO `actividad` (`id_actividad`, `nombre`, `tipo`, `fecha`, `horario`, `hora_inicio`, `hora_fin`, `descripcion`, `id_curso`, `id_diplomado`, `id_alquiler`, `id_solicitud_catering`) VALUES
                                                                                                                                                                                                 (1, 'Inicio del curso Programación con Java', 'CURSO', '2026-10-05', 'Lunes y miércoles de 6:00 p. m. a 8:00 p. m.', '2026-10-05 18:00:00', '2026-10-05 20:00:00', 'Primera clase del curso de programación', 1, NULL, NULL, NULL),
                                                                                                                                                                                                 (2, 'Conferencia empresarial', 'ALQUILER', '2026-10-20', 'De 8:00 a. m. a 12:00 p. m.', '2026-10-20 08:00:00', '2026-10-20 12:00:00', 'Conferencia organizada por Tech Solutions S.A.', NULL, NULL, 1, 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `alquiler`
--

CREATE TABLE `alquiler` (
                            `id_alquiler` int(11) NOT NULL,
                            `fecha` date NOT NULL,
                            `hora_inicio` datetime NOT NULL,
                            `hora_fin` datetime NOT NULL,
                            `cantidad_personas` int(11) NOT NULL,
                            `estado` varchar(30) NOT NULL,
                            `id_cliente` int(11) NOT NULL,
                            `id_espacio` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `alquiler`
--

INSERT INTO `alquiler` (`id_alquiler`, `fecha`, `hora_inicio`, `hora_fin`, `cantidad_personas`, `estado`, `id_cliente`, `id_espacio`) VALUES
                                                                                                                                          (1, '2026-10-20', '2026-10-20 08:00:00', '2026-10-20 12:00:00', 60, 'CONFIRMADO', 2, 2),
                                                                                                                                          (2, '2026-10-25', '2026-10-25 13:00:00', '2026-10-25 17:00:00', 150, 'PENDIENTE', 1, 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `categoria`
--

CREATE TABLE `categoria` (
                             `id_categoria` int(11) NOT NULL,
                             `nombre` varchar(100) NOT NULL,
                             `descripcion` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `categoria`
--

INSERT INTO `categoria` (`id_categoria`, `nombre`, `descripcion`) VALUES
                                                                      (1, 'Tecnología', 'Cursos y diplomados del área de desarrollo de software y TI'),
                                                                      (2, 'Negocios', 'Programas enfocados en gestión empresarial, finanzas y liderazgo'),
                                                                      (3, 'Diseño', 'Cursos de experiencia de usuario, diseño gráfico y multimedia'),
                                                                      (4, 'Marketing', 'Publicidad digital, estrategias de contenido y redes sociales');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `cliente`
--

CREATE TABLE `cliente` (
                           `id_cliente` int(11) NOT NULL,
                           `dui` varchar(20) DEFAULT NULL,
                           `nombre` varchar(150) NOT NULL,
                           `empresa` varchar(150) DEFAULT NULL,
                           `correo` varchar(150) DEFAULT NULL,
                           `telefono` varchar(30) DEFAULT NULL,
                           `direccion` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `cliente`
--

INSERT INTO `cliente` (`id_cliente`, `dui`, `nombre`, `empresa`, `correo`, `telefono`, `direccion`) VALUES
                                                                                                        (1, '01234567-8', 'Juan Pérez', 'Empresa XYZ', 'juan.perez@gmail.com', '7000-0009', 'San Salvador'),
                                                                                                        (2, '06141205-9', 'Carlos Rodríguez', 'Tech Solutions S.A.', 'crodriguez@techsol.com', '7765-1100', 'Santa Tecla'),
                                                                                                        (3, '05047892-3', 'Ana Lidia Martínez', NULL, 'amartinez.design@gmail.com', '7123-4500', 'Antiguo Cuscatlán'),
                                                                                                        (4, '04231980-1', 'Elena Flores', NULL, 'elena.flores80@hotmail.com', '7890-3300', 'Soyapango');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `cotizacion`
--

CREATE TABLE `cotizacion` (
                              `id_cotizacion` int(11) NOT NULL,
                              `fecha` date NOT NULL,
                              `subtotal` decimal(10,2) NOT NULL DEFAULT 0.00,
                              `total` decimal(10,2) NOT NULL DEFAULT 0.00,
                              `id_estado_cotizacion` int(11) NOT NULL,
                              `id_cliente` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `cotizacion`
--

INSERT INTO `cotizacion` (`id_cotizacion`, `fecha`, `subtotal`, `total`, `id_estado_cotizacion`, `id_cliente`) VALUES
                                                                                                                   (1, '2026-09-18', 300.00, 339.00, 3, 1),
                                                                                                                   (2, '2026-09-19', 500.00, 565.00, 1, 2);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `curso`
--

CREATE TABLE `curso` (
                         `id_curso` int(11) NOT NULL,
                         `nombre` varchar(150) NOT NULL,
                         `descripcion` varchar(255) DEFAULT NULL,
                         `cupo_maximo` int(11) NOT NULL,
                         `fecha_inicio` date DEFAULT NULL,
                         `fecha_fin` date DEFAULT NULL,
                         `costo` decimal(10,2) NOT NULL,
                         `estado` varchar(30) NOT NULL,
                         `id_categoria` int(11) NOT NULL,
                         `id_modalidad` int(11) NOT NULL,
                         `id_docente` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `curso`
--

INSERT INTO `curso` (`id_curso`, `nombre`, `descripcion`, `cupo_maximo`, `fecha_inicio`, `fecha_fin`, `costo`, `estado`, `id_categoria`, `id_modalidad`, `id_docente`) VALUES
                                                                                                                                                                           (1, 'Programación con Java', 'Programación orientada a objetos utilizando Java', 25, '2026-10-05', '2026-11-28', 150.00, 'ACTIVO', 1, 1, 1),
                                                                                                                                                                           (2, 'Curso en Ciberseguridad', 'Fundamentos de seguridad informática', 20, '2026-10-09', '2026-12-10', 200.00, 'ACTIVO', 1, 1, 2),
                                                                                                                                                                           (3, 'Marketing Digital', 'Estrategias de publicidad y redes sociales', 30, '2026-11-02', '2026-12-14', 125.00, 'INACTIVO', 4, 2, 2);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `detalle_cotizacion`
--

CREATE TABLE `detalle_cotizacion` (
                                      `id_detalle` int(11) NOT NULL,
                                      `descripcion` varchar(255) NOT NULL,
                                      `cantidad` int(11) NOT NULL,
                                      `precio` decimal(10,2) NOT NULL,
                                      `subtotal` decimal(10,2) NOT NULL,
                                      `id_cotizacion` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `detalle_cotizacion`
--

INSERT INTO `detalle_cotizacion` (`id_detalle`, `descripcion`, `cantidad`, `precio`, `subtotal`, `id_cotizacion`) VALUES
                                                                                                                      (1, 'Inscripción al curso Programación con Java', 2, 150.00, 300.00, 1),
                                                                                                                      (2, 'Alquiler de auditorio', 1, 500.00, 500.00, 2);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `diplomado`
--

CREATE TABLE `diplomado` (
                             `id_diplomado` int(11) NOT NULL,
                             `nombre` varchar(150) NOT NULL,
                             `descripcion` varchar(255) DEFAULT NULL,
                             `cupo_maximo` int(11) NOT NULL,
                             `fecha_inicio` date DEFAULT NULL,
                             `fecha_fin` date DEFAULT NULL,
                             `costo` decimal(10,2) NOT NULL,
                             `estado` varchar(30) NOT NULL,
                             `id_categoria` int(11) NOT NULL,
                             `id_modalidad` int(11) NOT NULL,
                             `id_docente` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `diplomado`
--

INSERT INTO `diplomado` (`id_diplomado`, `nombre`, `descripcion`, `cupo_maximo`, `fecha_inicio`, `fecha_fin`, `costo`, `estado`, `id_categoria`, `id_modalidad`, `id_docente`) VALUES
                                                                                                                                                                                   (1, 'Diplomado en Gestión Empresarial', 'Formación en administración y gestión empresarial', 25, '2026-09-17', '2026-11-30', 650.00, 'ACTIVO', 2, 2, 4),
                                                                                                                                                                                   (2, 'Diplomado en Diseño UX/UI', 'Diseño de interfaces y experiencia de usuario', 20, '2026-10-01', '2027-02-28', 600.00, 'ACTIVO', 3, 3, 3);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `docente`
--

CREATE TABLE `docente` (
                           `id_docente` int(11) NOT NULL,
                           `nombre` varchar(100) NOT NULL,
                           `apellido` varchar(100) NOT NULL,
                           `correo` varchar(150) DEFAULT NULL,
                           `telefono` varchar(30) DEFAULT NULL,
                           `especialidad` varchar(150) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `docente`
--

INSERT INTO `docente` (`id_docente`, `nombre`, `apellido`, `correo`, `telefono`, `especialidad`) VALUES
                                                                                                     (1, 'Ana', 'Gómez', 'ana.gomez@uca.edu.sv', '7123-4567', 'Desarrollo de Software y Python'),
                                                                                                     (2, 'Carlos', 'Ruiz', 'carlos.ruiz@uca.edu.sv', '7890-1234', 'Marketing Digital y Redes Sociales'),
                                                                                                     (3, 'Laura', 'Martínez', 'laura.martinez@uca.edu.sv', '7654-3210', 'Diseño UX/UI y Accesibilidad Web'),
                                                                                                     (4, 'Roberto', 'Fernández', 'roberto.fernandez@uca.edu.sv', '7321-9876', 'Gestión Ágil de Proyectos');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `espacio`
--

CREATE TABLE `espacio` (
                           `id_espacio` int(11) NOT NULL,
                           `nombre` varchar(150) NOT NULL,
                           `descripcion` varchar(500) DEFAULT NULL,
                           `capacidad` int(11) NOT NULL,
                           `ubicacion` varchar(255) DEFAULT NULL,
                           `costo_hora` decimal(10,2) NOT NULL,
                           `estado` varchar(30) NOT NULL DEFAULT 'DISPONIBLE'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `espacio`
--

INSERT INTO `espacio` (`id_espacio`, `nombre`, `descripcion`, `capacidad`, `ubicacion`, `costo_hora`, `estado`) VALUES
                                                                                                                    (1, 'Auditorio Principal', 'Auditorio equipado para conferencias y eventos', 250, 'Edificio principal, primer nivel', 500.00, 'DISPONIBLE'),
                                                                                                                    (2, 'Sala de Conferencias A', 'Sala para reuniones, capacitaciones y conferencias', 80, 'Edificio de aulas, segundo nivel', 250.00, 'DISPONIBLE'),
                                                                                                                    (3, 'Laboratorio de Computación', 'Laboratorio equipado con computadoras y proyector', 30, 'Edificio tecnológico, primer nivel', 200.00, 'MANTENIMIENTO'),
                                                                                                                    (4, 'Salon Domingo Savio', 'Este es el salon domingo savio', 90, 'Edificio principal, tercer nivel', 10.00, 'OCUPADO');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `estado_cotizacion`
--

CREATE TABLE `estado_cotizacion` (
                                     `id_estado_cotizacion` int(11) NOT NULL,
                                     `nombre` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `estado_cotizacion`
--

INSERT INTO `estado_cotizacion` (`id_estado_cotizacion`, `nombre`) VALUES
                                                                       (3, 'Aprobada'),
                                                                       (2, 'En proceso'),
                                                                       (1, 'Pendiente'),
                                                                       (4, 'Rechazada');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `estado_inscripcion`
--

CREATE TABLE `estado_inscripcion` (
                                      `id_estado_inscripcion` int(11) NOT NULL,
                                      `nombre` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `estado_inscripcion`
--

INSERT INTO `estado_inscripcion` (`id_estado_inscripcion`, `nombre`) VALUES
                                                                         (3, 'Cancelada'),
                                                                         (2, 'Confirmada'),
                                                                         (4, 'Finalizada'),
                                                                         (1, 'Pendiente');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `estado_pago`
--

CREATE TABLE `estado_pago` (
                               `id_estado_pago` int(11) NOT NULL,
                               `nombre` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `estado_pago`
--

INSERT INTO `estado_pago` (`id_estado_pago`, `nombre`) VALUES
                                                           (3, 'Pagado'),
                                                           (2, 'Parcial'),
                                                           (1, 'Pendiente');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `inscripcion`
--

CREATE TABLE `inscripcion` (
                               `id_inscripcion` int(11) NOT NULL,
                               `fecha` date NOT NULL,
                               `id_estado_inscripcion` int(11) NOT NULL,
                               `id_cliente` int(11) NOT NULL,
                               `id_curso` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `inscripcion`
--

INSERT INTO `inscripcion` (`id_inscripcion`, `fecha`, `id_estado_inscripcion`, `id_cliente`, `id_curso`) VALUES
                                                                                                             (1, '2026-09-18', 2, 1, 1),
                                                                                                             (2, '2026-09-19', 1, 2, 2);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `inscripcion_participante`
--

CREATE TABLE `inscripcion_participante` (
                                            `id_inscripcion` int(11) NOT NULL,
                                            `id_participante` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `inscripcion_participante`
--

INSERT INTO `inscripcion_participante` (`id_inscripcion`, `id_participante`) VALUES
                                                                                 (1, 1),
                                                                                 (1, 2),
                                                                                 (2, 3);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `metodo_pago`
--

CREATE TABLE `metodo_pago` (
                               `id_metodo_pago` int(11) NOT NULL,
                               `nombre` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `metodo_pago`
--

INSERT INTO `metodo_pago` (`id_metodo_pago`, `nombre`) VALUES
                                                           (4, 'Depósito'),
                                                           (1, 'Efectivo'),
                                                           (2, 'Tarjeta'),
                                                           (3, 'Transferencia');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `modalidad`
--

CREATE TABLE `modalidad` (
                             `id_modalidad` int(11) NOT NULL,
                             `nombre` varchar(100) NOT NULL,
                             `descripcion` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `modalidad`
--

INSERT INTO `modalidad` (`id_modalidad`, `nombre`, `descripcion`) VALUES
                                                                      (1, 'Presencial', 'Clases impartidas en las instalaciones del campus'),
                                                                      (2, 'Online', 'Sesiones mediante una plataforma digital'),
                                                                      (3, 'Híbrido', 'Combinación de sesiones presenciales y virtuales');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `pago`
--

CREATE TABLE `pago` (
                        `id_pago` int(11) NOT NULL,
                        `fecha` date NOT NULL,
                        `monto` decimal(10,2) NOT NULL,
                        `referencia` varchar(150) DEFAULT NULL,
                        `id_estado_pago` int(11) NOT NULL,
                        `id_cliente` int(11) NOT NULL,
                        `id_metodo_pago` int(11) NOT NULL,
                        `id_inscripcion` int(11) DEFAULT NULL,
                        `id_cotizacion` int(11) DEFAULT NULL,
                        `id_alquiler` int(11) DEFAULT NULL,
                        `id_solicitud_catering` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `pago`
--

INSERT INTO `pago` (`id_pago`, `fecha`, `monto`, `referencia`, `id_estado_pago`, `id_cliente`, `id_metodo_pago`, `id_inscripcion`, `id_cotizacion`, `id_alquiler`, `id_solicitud_catering`) VALUES
                                                                                                                                                                                                (1, '2026-09-18', 150.00, 'TRX-2026-0001', 3, 1, 3, 1, NULL, NULL, NULL),
                                                                                                                                                                                                (2, '2026-09-19', 339.00, 'TRX-2026-0002', 3, 1, 2, NULL, 1, NULL, NULL),
                                                                                                                                                                                                (3, '2026-09-20', 500.00, 'TRX-2026-0003', 2, 2, 3, NULL, NULL, 1, NULL);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `participante`
--

CREATE TABLE `participante` (
                                `id_participante` int(11) NOT NULL,
                                `dui` varchar(20) NOT NULL,
                                `nombre` varchar(100) NOT NULL,
                                `apellido` varchar(100) NOT NULL,
                                `correo` varchar(150) DEFAULT NULL,
                                `telefono` varchar(30) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `participante`
--

INSERT INTO `participante` (`id_participante`, `dui`, `nombre`, `apellido`, `correo`, `telefono`) VALUES
                                                                                                      (1, '12345678-9', 'Vanessa', 'Escalón', 'vanessa@gmail.com', '7270-9970'),
                                                                                                      (2, '03124567-1', 'María', 'Gómez', 'maria.gomez@gmail.com', '7010-2001'),
                                                                                                      (3, '04125678-2', 'Pedro', 'López', 'pedro.lopez@gmail.com', '7010-2002');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `rol`
--

CREATE TABLE `rol` (
                       `id_rol` int(11) NOT NULL,
                       `nombre` varchar(50) NOT NULL,
                       `descripcion` varchar(150) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `rol`
--

INSERT INTO `rol` (`id_rol`, `nombre`, `descripcion`) VALUES
                                                          (1, 'ADMIN', 'Administrador del sistema'),
                                                          (2, 'RECEPCIONISTA', 'Encargado de atención y registros'),
                                                          (3, 'CLIENTE', 'Usuario cliente del sistema'),
                                                          (4, 'CONTABILIDAD', 'Encargado de pagos y cotizaciones');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `servicio_catering`
--

CREATE TABLE `servicio_catering` (
                                     `id_catering` int(11) NOT NULL,
                                     `nombre` varchar(150) NOT NULL,
                                     `descripcion` varchar(500) DEFAULT NULL,
                                     `capacidad_minima` int(11) NOT NULL,
                                     `capacidad_maxima` int(11) NOT NULL,
                                     `precio_por_persona` decimal(10,2) NOT NULL,
                                     `estado` varchar(30) NOT NULL DEFAULT 'DISPONIBLE'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `servicio_catering`
--

INSERT INTO `servicio_catering` (`id_catering`, `nombre`, `descripcion`, `capacidad_minima`, `capacidad_maxima`, `precio_por_persona`, `estado`) VALUES
                                                                                                                                                     (1, 'Coffee Break Premium', 'Café, jugo, agua, bocadillos y postre', 20, 150, 10.00, 'DISPONIBLE'),
                                                                                                                                                     (2, 'Almuerzo Ejecutivo', 'Plato principal, bebida y postre', 15, 200, 15.00, 'DISPONIBLE');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `solicitud_catering`
--

CREATE TABLE `solicitud_catering` (
                                      `id_solicitud` int(11) NOT NULL,
                                      `cantidad_asistentes` int(11) NOT NULL,
                                      `menu` varchar(255) NOT NULL,
                                      `fecha` date NOT NULL,
                                      `hora` time NOT NULL,
                                      `lugar` varchar(255) NOT NULL,
                                      `estado` varchar(30) NOT NULL,
                                      `id_cliente` int(11) NOT NULL,
                                      `id_catering` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `solicitud_catering`
--

INSERT INTO `solicitud_catering` (`id_solicitud`, `cantidad_asistentes`, `menu`, `fecha`, `hora`, `lugar`, `estado`, `id_cliente`, `id_catering`) VALUES
                                                                                                                                                      (1, 60, 'Coffee Break con café, jugo y bocadillos', '2026-10-20', '10:00:00', 'Sala de Conferencias A', 'CONFIRMADA', 2, 1),
                                                                                                                                                      (2, 100, 'Almuerzo ejecutivo', '2026-10-25', '12:00:00', 'Auditorio Principal', 'PENDIENTE', 1, 2);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuario`
--

CREATE TABLE `usuario` (
                           `id_usuario` int(11) NOT NULL,
                           `nombre` varchar(80) NOT NULL,
                           `correo` varchar(100) NOT NULL,
                           `password` varchar(255) NOT NULL,
                           `estado` tinyint(1) NOT NULL DEFAULT 1,
                           `id_rol` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `usuario`
--

INSERT INTO `usuario` (`id_usuario`, `nombre`, `correo`, `password`, `estado`, `id_rol`) VALUES
                                                                                             (1, 'Administrador', 'admin@uca.edu.sv', '$2a$12$ZctOHCIOM3JMF4dhy/R.muecurveBWwn8zoFlLXlhN9FKHCO8y5o2', 1, 1),
                                                                                             (2, 'Katherine Garay', 'katherine@uca.edu.sv', '$2a$12$//NnzxGzz3PvkrCRZvTyGuQXx1luLfGh99dO8ongyJLPebBUbwc6.', 1, 2);

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `actividad`
--
ALTER TABLE `actividad`
    ADD PRIMARY KEY (`id_actividad`),
  ADD KEY `fk_actividad_curso` (`id_curso`),
  ADD KEY `fk_actividad_diplomado` (`id_diplomado`),
  ADD KEY `fk_actividad_alquiler` (`id_alquiler`),
  ADD KEY `fk_actividad_catering` (`id_solicitud_catering`);

--
-- Indices de la tabla `alquiler`
--
ALTER TABLE `alquiler`
    ADD PRIMARY KEY (`id_alquiler`),
  ADD KEY `fk_alquiler_cliente` (`id_cliente`),
  ADD KEY `fk_alquiler_espacio` (`id_espacio`);

--
-- Indices de la tabla `categoria`
--
ALTER TABLE `categoria`
    ADD PRIMARY KEY (`id_categoria`);

--
-- Indices de la tabla `cliente`
--
ALTER TABLE `cliente`
    ADD PRIMARY KEY (`id_cliente`);

--
-- Indices de la tabla `cotizacion`
--
ALTER TABLE `cotizacion`
    ADD PRIMARY KEY (`id_cotizacion`),
  ADD KEY `fk_cotizacion_estado` (`id_estado_cotizacion`),
  ADD KEY `fk_cotizacion_cliente` (`id_cliente`);

--
-- Indices de la tabla `curso`
--
ALTER TABLE `curso`
    ADD PRIMARY KEY (`id_curso`),
  ADD KEY `fk_curso_categoria` (`id_categoria`),
  ADD KEY `fk_curso_modalidad` (`id_modalidad`),
  ADD KEY `fk_curso_docente` (`id_docente`);

--
-- Indices de la tabla `detalle_cotizacion`
--
ALTER TABLE `detalle_cotizacion`
    ADD PRIMARY KEY (`id_detalle`),
  ADD KEY `fk_detalle_cotizacion` (`id_cotizacion`);

--
-- Indices de la tabla `diplomado`
--
ALTER TABLE `diplomado`
    ADD PRIMARY KEY (`id_diplomado`),
  ADD KEY `fk_diplomado_categoria` (`id_categoria`),
  ADD KEY `fk_diplomado_modalidad` (`id_modalidad`),
  ADD KEY `fk_diplomado_docente` (`id_docente`);

--
-- Indices de la tabla `docente`
--
ALTER TABLE `docente`
    ADD PRIMARY KEY (`id_docente`);

--
-- Indices de la tabla `espacio`
--
ALTER TABLE `espacio`
    ADD PRIMARY KEY (`id_espacio`);

--
-- Indices de la tabla `estado_cotizacion`
--
ALTER TABLE `estado_cotizacion`
    ADD PRIMARY KEY (`id_estado_cotizacion`),
  ADD UNIQUE KEY `nombre` (`nombre`);

--
-- Indices de la tabla `estado_inscripcion`
--
ALTER TABLE `estado_inscripcion`
    ADD PRIMARY KEY (`id_estado_inscripcion`),
  ADD UNIQUE KEY `nombre` (`nombre`);

--
-- Indices de la tabla `estado_pago`
--
ALTER TABLE `estado_pago`
    ADD PRIMARY KEY (`id_estado_pago`),
  ADD UNIQUE KEY `nombre` (`nombre`);

--
-- Indices de la tabla `inscripcion`
--
ALTER TABLE `inscripcion`
    ADD PRIMARY KEY (`id_inscripcion`),
  ADD KEY `fk_inscripcion_estado` (`id_estado_inscripcion`),
  ADD KEY `fk_inscripcion_cliente` (`id_cliente`),
  ADD KEY `fk_inscripcion_curso` (`id_curso`);

--
-- Indices de la tabla `inscripcion_participante`
--
ALTER TABLE `inscripcion_participante`
    ADD PRIMARY KEY (`id_inscripcion`,`id_participante`),
  ADD KEY `fk_inscripcion_participante_participante` (`id_participante`);

--
-- Indices de la tabla `metodo_pago`
--
ALTER TABLE `metodo_pago`
    ADD PRIMARY KEY (`id_metodo_pago`),
  ADD UNIQUE KEY `nombre` (`nombre`);

--
-- Indices de la tabla `modalidad`
--
ALTER TABLE `modalidad`
    ADD PRIMARY KEY (`id_modalidad`);

--
-- Indices de la tabla `pago`
--
ALTER TABLE `pago`
    ADD PRIMARY KEY (`id_pago`),
  ADD KEY `fk_pago_estado` (`id_estado_pago`),
  ADD KEY `fk_pago_cliente` (`id_cliente`),
  ADD KEY `fk_pago_metodo` (`id_metodo_pago`),
  ADD KEY `fk_pago_inscripcion` (`id_inscripcion`),
  ADD KEY `fk_pago_cotizacion` (`id_cotizacion`),
  ADD KEY `fk_pago_alquiler` (`id_alquiler`),
  ADD KEY `fk_pago_catering` (`id_solicitud_catering`);

--
-- Indices de la tabla `participante`
--
ALTER TABLE `participante`
    ADD PRIMARY KEY (`id_participante`),
  ADD UNIQUE KEY `dui` (`dui`);

--
-- Indices de la tabla `rol`
--
ALTER TABLE `rol`
    ADD PRIMARY KEY (`id_rol`),
  ADD UNIQUE KEY `nombre` (`nombre`);

--
-- Indices de la tabla `servicio_catering`
--
ALTER TABLE `servicio_catering`
    ADD PRIMARY KEY (`id_catering`);

--
-- Indices de la tabla `solicitud_catering`
--
ALTER TABLE `solicitud_catering`
    ADD PRIMARY KEY (`id_solicitud`),
  ADD KEY `fk_solicitud_catering_cliente` (`id_cliente`),
  ADD KEY `fk_solicitud_catering_servicio` (`id_catering`);

--
-- Indices de la tabla `usuario`
--
ALTER TABLE `usuario`
    ADD PRIMARY KEY (`id_usuario`),
  ADD UNIQUE KEY `correo` (`correo`),
  ADD KEY `fk_usuario_rol` (`id_rol`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `actividad`
--
ALTER TABLE `actividad`
    MODIFY `id_actividad` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT de la tabla `alquiler`
--
ALTER TABLE `alquiler`
    MODIFY `id_alquiler` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT de la tabla `categoria`
--
ALTER TABLE `categoria`
    MODIFY `id_categoria` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT de la tabla `cliente`
--
ALTER TABLE `cliente`
    MODIFY `id_cliente` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT de la tabla `cotizacion`
--
ALTER TABLE `cotizacion`
    MODIFY `id_cotizacion` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT de la tabla `curso`
--
ALTER TABLE `curso`
    MODIFY `id_curso` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de la tabla `detalle_cotizacion`
--
ALTER TABLE `detalle_cotizacion`
    MODIFY `id_detalle` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT de la tabla `diplomado`
--
ALTER TABLE `diplomado`
    MODIFY `id_diplomado` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT de la tabla `docente`
--
ALTER TABLE `docente`
    MODIFY `id_docente` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT de la tabla `espacio`
--
ALTER TABLE `espacio`
    MODIFY `id_espacio` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT de la tabla `estado_cotizacion`
--
ALTER TABLE `estado_cotizacion`
    MODIFY `id_estado_cotizacion` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT de la tabla `estado_inscripcion`
--
ALTER TABLE `estado_inscripcion`
    MODIFY `id_estado_inscripcion` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT de la tabla `estado_pago`
--
ALTER TABLE `estado_pago`
    MODIFY `id_estado_pago` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de la tabla `inscripcion`
--
ALTER TABLE `inscripcion`
    MODIFY `id_inscripcion` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT de la tabla `metodo_pago`
--
ALTER TABLE `metodo_pago`
    MODIFY `id_metodo_pago` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT de la tabla `modalidad`
--
ALTER TABLE `modalidad`
    MODIFY `id_modalidad` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de la tabla `pago`
--
ALTER TABLE `pago`
    MODIFY `id_pago` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de la tabla `participante`
--
ALTER TABLE `participante`
    MODIFY `id_participante` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de la tabla `rol`
--
ALTER TABLE `rol`
    MODIFY `id_rol` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT de la tabla `servicio_catering`
--
ALTER TABLE `servicio_catering`
    MODIFY `id_catering` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT de la tabla `solicitud_catering`
--
ALTER TABLE `solicitud_catering`
    MODIFY `id_solicitud` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT de la tabla `usuario`
--
ALTER TABLE `usuario`
    MODIFY `id_usuario` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `actividad`
--
ALTER TABLE `actividad`
    ADD CONSTRAINT `fk_actividad_alquiler` FOREIGN KEY (`id_alquiler`) REFERENCES `alquiler` (`id_alquiler`),
  ADD CONSTRAINT `fk_actividad_catering` FOREIGN KEY (`id_solicitud_catering`) REFERENCES `solicitud_catering` (`id_solicitud`),
  ADD CONSTRAINT `fk_actividad_curso` FOREIGN KEY (`id_curso`) REFERENCES `curso` (`id_curso`),
  ADD CONSTRAINT `fk_actividad_diplomado` FOREIGN KEY (`id_diplomado`) REFERENCES `diplomado` (`id_diplomado`);

--
-- Filtros para la tabla `alquiler`
--
ALTER TABLE `alquiler`
    ADD CONSTRAINT `fk_alquiler_cliente` FOREIGN KEY (`id_cliente`) REFERENCES `cliente` (`id_cliente`),
  ADD CONSTRAINT `fk_alquiler_espacio` FOREIGN KEY (`id_espacio`) REFERENCES `espacio` (`id_espacio`);

--
-- Filtros para la tabla `cotizacion`
--
ALTER TABLE `cotizacion`
    ADD CONSTRAINT `fk_cotizacion_cliente` FOREIGN KEY (`id_cliente`) REFERENCES `cliente` (`id_cliente`),
  ADD CONSTRAINT `fk_cotizacion_estado` FOREIGN KEY (`id_estado_cotizacion`) REFERENCES `estado_cotizacion` (`id_estado_cotizacion`);

--
-- Filtros para la tabla `curso`
--
ALTER TABLE `curso`
    ADD CONSTRAINT `fk_curso_categoria` FOREIGN KEY (`id_categoria`) REFERENCES `categoria` (`id_categoria`),
  ADD CONSTRAINT `fk_curso_docente` FOREIGN KEY (`id_docente`) REFERENCES `docente` (`id_docente`),
  ADD CONSTRAINT `fk_curso_modalidad` FOREIGN KEY (`id_modalidad`) REFERENCES `modalidad` (`id_modalidad`);

--
-- Filtros para la tabla `detalle_cotizacion`
--
ALTER TABLE `detalle_cotizacion`
    ADD CONSTRAINT `fk_detalle_cotizacion` FOREIGN KEY (`id_cotizacion`) REFERENCES `cotizacion` (`id_cotizacion`) ON DELETE CASCADE;

--
-- Filtros para la tabla `diplomado`
--
ALTER TABLE `diplomado`
    ADD CONSTRAINT `fk_diplomado_categoria` FOREIGN KEY (`id_categoria`) REFERENCES `categoria` (`id_categoria`),
  ADD CONSTRAINT `fk_diplomado_docente` FOREIGN KEY (`id_docente`) REFERENCES `docente` (`id_docente`),
  ADD CONSTRAINT `fk_diplomado_modalidad` FOREIGN KEY (`id_modalidad`) REFERENCES `modalidad` (`id_modalidad`);

--
-- Filtros para la tabla `inscripcion`
--
ALTER TABLE `inscripcion`
    ADD CONSTRAINT `fk_inscripcion_cliente` FOREIGN KEY (`id_cliente`) REFERENCES `cliente` (`id_cliente`),
  ADD CONSTRAINT `fk_inscripcion_curso` FOREIGN KEY (`id_curso`) REFERENCES `curso` (`id_curso`),
  ADD CONSTRAINT `fk_inscripcion_estado` FOREIGN KEY (`id_estado_inscripcion`) REFERENCES `estado_inscripcion` (`id_estado_inscripcion`);

--
-- Filtros para la tabla `inscripcion_participante`
--
ALTER TABLE `inscripcion_participante`
    ADD CONSTRAINT `fk_inscripcion_participante_inscripcion` FOREIGN KEY (`id_inscripcion`) REFERENCES `inscripcion` (`id_inscripcion`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_inscripcion_participante_participante` FOREIGN KEY (`id_participante`) REFERENCES `participante` (`id_participante`);

--
-- Filtros para la tabla `pago`
--
ALTER TABLE `pago`
    ADD CONSTRAINT `fk_pago_alquiler` FOREIGN KEY (`id_alquiler`) REFERENCES `alquiler` (`id_alquiler`),
  ADD CONSTRAINT `fk_pago_catering` FOREIGN KEY (`id_solicitud_catering`) REFERENCES `solicitud_catering` (`id_solicitud`),
  ADD CONSTRAINT `fk_pago_cliente` FOREIGN KEY (`id_cliente`) REFERENCES `cliente` (`id_cliente`),
  ADD CONSTRAINT `fk_pago_cotizacion` FOREIGN KEY (`id_cotizacion`) REFERENCES `cotizacion` (`id_cotizacion`),
  ADD CONSTRAINT `fk_pago_estado` FOREIGN KEY (`id_estado_pago`) REFERENCES `estado_pago` (`id_estado_pago`),
  ADD CONSTRAINT `fk_pago_inscripcion` FOREIGN KEY (`id_inscripcion`) REFERENCES `inscripcion` (`id_inscripcion`),
  ADD CONSTRAINT `fk_pago_metodo` FOREIGN KEY (`id_metodo_pago`) REFERENCES `metodo_pago` (`id_metodo_pago`);

--
-- Filtros para la tabla `solicitud_catering`
--
ALTER TABLE `solicitud_catering`
    ADD CONSTRAINT `fk_solicitud_catering_cliente` FOREIGN KEY (`id_cliente`) REFERENCES `cliente` (`id_cliente`),
  ADD CONSTRAINT `fk_solicitud_catering_servicio` FOREIGN KEY (`id_catering`) REFERENCES `servicio_catering` (`id_catering`);

--
-- Filtros para la tabla `usuario`
--
ALTER TABLE `usuario`
    ADD CONSTRAINT `fk_usuario_rol` FOREIGN KEY (`id_rol`) REFERENCES `rol` (`id_rol`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;