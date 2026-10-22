DROP DATABASE IF EXISTS sistema_uca;

CREATE DATABASE sistema_uca
CHARACTER SET utf8mb4
COLLATE utf8mb4_general_ci;

USE sistema_uca;


CREATE TABLE rol (
                     id_rol INT AUTO_INCREMENT PRIMARY KEY,
                     nombre VARCHAR(50) NOT NULL UNIQUE,
                     descripcion VARCHAR(150)
);


CREATE TABLE usuario (
                         id_usuario INT AUTO_INCREMENT PRIMARY KEY,
                         nombre VARCHAR(80) NOT NULL,
                         correo VARCHAR(100) NOT NULL UNIQUE,
                         password VARCHAR(255) NOT NULL,
                         estado BOOLEAN NOT NULL DEFAULT TRUE,
                         id_rol INT NOT NULL,

                         CONSTRAINT fk_usuario_rol
                             FOREIGN KEY (id_rol)
                                 REFERENCES rol(id_rol)
);


CREATE TABLE categoria (
                           id_categoria INT AUTO_INCREMENT PRIMARY KEY,
                           nombre VARCHAR(100) NOT NULL,
                           descripcion VARCHAR(255)
);


CREATE TABLE modalidad (
                           id_modalidad INT AUTO_INCREMENT PRIMARY KEY,
                           nombre VARCHAR(100) NOT NULL,
                           descripcion VARCHAR(255)
);


CREATE TABLE docente (
                         id_docente INT AUTO_INCREMENT PRIMARY KEY,
                         nombre VARCHAR(100) NOT NULL,
                         apellido VARCHAR(100) NOT NULL,
                         correo VARCHAR(150),
                         telefono VARCHAR(30),
                         especialidad VARCHAR(150)
);


CREATE TABLE curso (
                       id_curso INT AUTO_INCREMENT PRIMARY KEY,
                       nombre VARCHAR(150) NOT NULL,
                       descripcion VARCHAR(255),
                       cupo_maximo INT NOT NULL,
                       fecha_inicio DATE,
                       fecha_fin DATE,
                       costo DECIMAL(10,2) NOT NULL,
                       estado VARCHAR(30) NOT NULL,
                       id_categoria INT NOT NULL,
                       id_modalidad INT NOT NULL,
                       id_docente INT NOT NULL,

                       CONSTRAINT fk_curso_categoria
                           FOREIGN KEY (id_categoria)
                               REFERENCES categoria(id_categoria),

                       CONSTRAINT fk_curso_modalidad
                           FOREIGN KEY (id_modalidad)
                               REFERENCES modalidad(id_modalidad),

                       CONSTRAINT fk_curso_docente
                           FOREIGN KEY (id_docente)
                               REFERENCES docente(id_docente)
);


CREATE TABLE diplomado (
                           id_diplomado INT AUTO_INCREMENT PRIMARY KEY,
                           nombre VARCHAR(150) NOT NULL,
                           descripcion VARCHAR(255),
                           cupo_maximo INT NOT NULL,
                           fecha_inicio DATE,
                           fecha_fin DATE,
                           costo DECIMAL(10,2) NOT NULL,
                           estado VARCHAR(30) NOT NULL,
                           id_categoria INT NOT NULL,
                           id_modalidad INT NOT NULL,
                           id_docente INT NOT NULL,

                           CONSTRAINT fk_diplomado_categoria
                               FOREIGN KEY (id_categoria)
                                   REFERENCES categoria(id_categoria),

                           CONSTRAINT fk_diplomado_modalidad
                               FOREIGN KEY (id_modalidad)
                                   REFERENCES modalidad(id_modalidad),

                           CONSTRAINT fk_diplomado_docente
                               FOREIGN KEY (id_docente)
                                   REFERENCES docente(id_docente)
);


CREATE TABLE cliente (
                         id_cliente INT AUTO_INCREMENT PRIMARY KEY,
                         dui VARCHAR(20),
                         nombre VARCHAR(150) NOT NULL,
                         empresa VARCHAR(150),
                         correo VARCHAR(150),
                         telefono VARCHAR(30),
                         direccion VARCHAR(255)
);


CREATE TABLE participante (
                              id_participante INT AUTO_INCREMENT PRIMARY KEY,
                              dui VARCHAR(20) NOT NULL UNIQUE,
                              nombre VARCHAR(100) NOT NULL,
                              apellido VARCHAR(100) NOT NULL,
                              correo VARCHAR(150),
                              telefono VARCHAR(30)
);


CREATE TABLE estado_inscripcion (
                                    id_estado_inscripcion INT AUTO_INCREMENT PRIMARY KEY,
                                    nombre VARCHAR(50) NOT NULL UNIQUE
);


CREATE TABLE inscripcion (
                             id_inscripcion INT AUTO_INCREMENT PRIMARY KEY,
                             fecha DATE NOT NULL,
                             id_estado_inscripcion INT NOT NULL,
                             id_cliente INT NOT NULL,
                             id_curso INT NOT NULL,

                             CONSTRAINT fk_inscripcion_estado
                                 FOREIGN KEY (id_estado_inscripcion)
                                     REFERENCES estado_inscripcion(id_estado_inscripcion),

                             CONSTRAINT fk_inscripcion_cliente
                                 FOREIGN KEY (id_cliente)
                                     REFERENCES cliente(id_cliente),

                             CONSTRAINT fk_inscripcion_curso
                                 FOREIGN KEY (id_curso)
                                     REFERENCES curso(id_curso)
);


CREATE TABLE inscripcion_participante (
                                          id_inscripcion INT NOT NULL,
                                          id_participante INT NOT NULL,

                                          PRIMARY KEY (id_inscripcion, id_participante),

                                          CONSTRAINT fk_inscripcion_participante_inscripcion
                                              FOREIGN KEY (id_inscripcion)
                                                  REFERENCES inscripcion(id_inscripcion)
                                                  ON DELETE CASCADE,

                                          CONSTRAINT fk_inscripcion_participante_participante
                                              FOREIGN KEY (id_participante)
                                                  REFERENCES participante(id_participante)
);


CREATE TABLE estado_cotizacion (
                                   id_estado_cotizacion INT AUTO_INCREMENT PRIMARY KEY,
                                   nombre VARCHAR(50) NOT NULL UNIQUE
);


CREATE TABLE cotizacion (
                            id_cotizacion INT AUTO_INCREMENT PRIMARY KEY,
                            fecha DATE NOT NULL,
                            subtotal DECIMAL(10,2) NOT NULL DEFAULT 0,
                            total DECIMAL(10,2) NOT NULL DEFAULT 0,
                            id_estado_cotizacion INT NOT NULL,
                            id_cliente INT NOT NULL,

                            CONSTRAINT fk_cotizacion_estado
                                FOREIGN KEY (id_estado_cotizacion)
                                    REFERENCES estado_cotizacion(id_estado_cotizacion),

                            CONSTRAINT fk_cotizacion_cliente
                                FOREIGN KEY (id_cliente)
                                    REFERENCES cliente(id_cliente)
);


CREATE TABLE detalle_cotizacion (
                                    id_detalle INT AUTO_INCREMENT PRIMARY KEY,
                                    descripcion VARCHAR(255) NOT NULL,
                                    cantidad INT NOT NULL,
                                    precio DECIMAL(10,2) NOT NULL,
                                    subtotal DECIMAL(10,2) NOT NULL,
                                    id_cotizacion INT NOT NULL,

                                    CONSTRAINT fk_detalle_cotizacion
                                        FOREIGN KEY (id_cotizacion)
                                            REFERENCES cotizacion(id_cotizacion)
                                            ON DELETE CASCADE
);


CREATE TABLE espacio (
                         id_espacio INT AUTO_INCREMENT PRIMARY KEY,
                         nombre VARCHAR(150) NOT NULL,
                         descripcion VARCHAR(500),
                         capacidad INT NOT NULL,
                         ubicacion VARCHAR(255),
                         costo_hora DECIMAL(10,2) NOT NULL,
                         estado VARCHAR(30) NOT NULL DEFAULT 'DISPONIBLE'
);


CREATE TABLE alquiler (
                          id_alquiler INT AUTO_INCREMENT PRIMARY KEY,
                          fecha DATE NOT NULL,
                          hora_inicio DATETIME NOT NULL,
                          hora_fin DATETIME NOT NULL,
                          cantidad_personas INT NOT NULL,
                          estado VARCHAR(30) NOT NULL,
                          id_cliente INT NOT NULL,
                          id_espacio INT NOT NULL,

                          CONSTRAINT fk_alquiler_cliente
                              FOREIGN KEY (id_cliente)
                                  REFERENCES cliente(id_cliente),

                          CONSTRAINT fk_alquiler_espacio
                              FOREIGN KEY (id_espacio)
                                  REFERENCES espacio(id_espacio)
);


CREATE TABLE servicio_catering (
                                   id_catering INT AUTO_INCREMENT PRIMARY KEY,
                                   nombre VARCHAR(150) NOT NULL,
                                   descripcion VARCHAR(500),
                                   capacidad_minima INT NOT NULL,
                                   capacidad_maxima INT NOT NULL,
                                   precio_por_persona DECIMAL(10,2) NOT NULL,
                                   estado VARCHAR(30) NOT NULL DEFAULT 'DISPONIBLE'
);


CREATE TABLE solicitud_catering (
                                    id_solicitud INT AUTO_INCREMENT PRIMARY KEY,
                                    cantidad_asistentes INT NOT NULL,
                                    menu VARCHAR(255) NOT NULL,
                                    fecha DATE NOT NULL,
                                    hora TIME NOT NULL,
                                    lugar VARCHAR(255) NOT NULL,
                                    estado VARCHAR(30) NOT NULL,
                                    id_cliente INT NOT NULL,
                                    id_catering INT NOT NULL,

                                    CONSTRAINT fk_solicitud_catering_cliente
                                        FOREIGN KEY (id_cliente)
                                            REFERENCES cliente(id_cliente),

                                    CONSTRAINT fk_solicitud_catering_servicio
                                        FOREIGN KEY (id_catering)
                                            REFERENCES servicio_catering(id_catering)
);


CREATE TABLE actividad (
                           id_actividad INT AUTO_INCREMENT PRIMARY KEY,
                           nombre VARCHAR(150) NOT NULL,
                           tipo VARCHAR(100) NOT NULL,
                           fecha DATE NOT NULL,
                           horario VARCHAR(100),
                           hora_inicio DATETIME,
                           hora_fin DATETIME,
                           descripcion VARCHAR(500),
                           id_curso INT,
                           id_diplomado INT,
                           id_alquiler INT,
                           id_solicitud_catering INT,

                           CONSTRAINT fk_actividad_curso
                               FOREIGN KEY (id_curso)
                                   REFERENCES curso(id_curso),

                           CONSTRAINT fk_actividad_diplomado
                               FOREIGN KEY (id_diplomado)
                                   REFERENCES diplomado(id_diplomado),

                           CONSTRAINT fk_actividad_alquiler
                               FOREIGN KEY (id_alquiler)
                                   REFERENCES alquiler(id_alquiler),

                           CONSTRAINT fk_actividad_catering
                               FOREIGN KEY (id_solicitud_catering)
                                   REFERENCES solicitud_catering(id_solicitud)
);


CREATE TABLE estado_pago (
                             id_estado_pago INT AUTO_INCREMENT PRIMARY KEY,
                             nombre VARCHAR(50) NOT NULL UNIQUE
);


CREATE TABLE metodo_pago (
                             id_metodo_pago INT AUTO_INCREMENT PRIMARY KEY,
                             nombre VARCHAR(50) NOT NULL UNIQUE
);


CREATE TABLE pago (
                      id_pago INT AUTO_INCREMENT PRIMARY KEY,
                      fecha DATE NOT NULL,
                      monto DECIMAL(10,2) NOT NULL,
                      referencia VARCHAR(150),
                      id_estado_pago INT NOT NULL,
                      id_cliente INT NOT NULL,
                      id_metodo_pago INT NOT NULL,
                      id_inscripcion INT,
                      id_cotizacion INT,
                      id_alquiler INT,
                      id_solicitud_catering INT,

                      CONSTRAINT fk_pago_estado
                          FOREIGN KEY (id_estado_pago)
                              REFERENCES estado_pago(id_estado_pago),

                      CONSTRAINT fk_pago_cliente
                          FOREIGN KEY (id_cliente)
                              REFERENCES cliente(id_cliente),

                      CONSTRAINT fk_pago_metodo
                          FOREIGN KEY (id_metodo_pago)
                              REFERENCES metodo_pago(id_metodo_pago),

                      CONSTRAINT fk_pago_inscripcion
                          FOREIGN KEY (id_inscripcion)
                              REFERENCES inscripcion(id_inscripcion),

                      CONSTRAINT fk_pago_cotizacion
                          FOREIGN KEY (id_cotizacion)
                              REFERENCES cotizacion(id_cotizacion),

                      CONSTRAINT fk_pago_alquiler
                          FOREIGN KEY (id_alquiler)
                              REFERENCES alquiler(id_alquiler),

                      CONSTRAINT fk_pago_catering
                          FOREIGN KEY (id_solicitud_catering)
                              REFERENCES solicitud_catering(id_solicitud)
);


INSERT INTO rol (id_rol, nombre, descripcion)
VALUES (1, 'ADMIN', 'Administrador del sistema');

INSERT INTO rol (id_rol, nombre, descripcion)
VALUES (2, 'RECEPCIONISTA', 'Encargado de atención y registros');

INSERT INTO rol (id_rol, nombre, descripcion)
VALUES (3, 'CLIENTE', 'Usuario cliente del sistema');

INSERT INTO rol (id_rol, nombre, descripcion)
VALUES (4, 'CONTABILIDAD', 'Encargado de pagos y cotizaciones');


INSERT INTO usuario (
    id_usuario,
    nombre,
    correo,
    password,
    estado,
    id_rol
)
VALUES (
           1,
           'Administrador',
           'admin@uca.edu.sv',
           '$2a$12$ZctOHCIOM3JMF4dhy/R.muecurveBWwn8zoFlLXlhN9FKHCO8y5o2',
           TRUE,
           1
       );

INSERT INTO usuario (
    id_usuario,
    nombre,
    correo,
    password,
    estado,
    id_rol
)
VALUES (
           2,
           'Katherine Garay',
           'katherine@uca.edu.sv',
           '$2a$12$//NnzxGzz3PvkrCRZvTyGuQXx1luLfGh99dO8ongyJLPebBUbwc6.',
           TRUE,
           2
       );


INSERT INTO categoria (id_categoria, nombre, descripcion)
VALUES (
           1,
           'Tecnología',
           'Cursos y diplomados del área de desarrollo de software y TI'
       );

INSERT INTO categoria (id_categoria, nombre, descripcion)
VALUES (
           2,
           'Negocios',
           'Programas enfocados en gestión empresarial, finanzas y liderazgo'
       );

INSERT INTO categoria (id_categoria, nombre, descripcion)
VALUES (
           3,
           'Diseño',
           'Cursos de experiencia de usuario, diseño gráfico y multimedia'
       );

INSERT INTO categoria (id_categoria, nombre, descripcion)
VALUES (
           4,
           'Marketing',
           'Publicidad digital, estrategias de contenido y redes sociales'
       );


INSERT INTO modalidad (id_modalidad, nombre, descripcion)
VALUES (
           1,
           'Presencial',
           'Clases impartidas en las instalaciones del campus'
       );

INSERT INTO modalidad (id_modalidad, nombre, descripcion)
VALUES (
           2,
           'Online',
           'Sesiones mediante una plataforma digital'
       );

INSERT INTO modalidad (id_modalidad, nombre, descripcion)
VALUES (
           3,
           'Híbrido',
           'Combinación de sesiones presenciales y virtuales'
       );


INSERT INTO docente (
    id_docente,
    nombre,
    apellido,
    correo,
    telefono,
    especialidad
)
VALUES (
           1,
           'Ana',
           'Gómez',
           'ana.gomez@uca.edu.sv',
           '7123-4567',
           'Desarrollo de Software y Python'
       );

INSERT INTO docente (
    id_docente,
    nombre,
    apellido,
    correo,
    telefono,
    especialidad
)
VALUES (
           2,
           'Carlos',
           'Ruiz',
           'carlos.ruiz@uca.edu.sv',
           '7890-1234',
           'Marketing Digital y Redes Sociales'
       );

INSERT INTO docente (
    id_docente,
    nombre,
    apellido,
    correo,
    telefono,
    especialidad
)
VALUES (
           3,
           'Laura',
           'Martínez',
           'laura.martinez@uca.edu.sv',
           '7654-3210',
           'Diseño UX/UI y Accesibilidad Web'
       );

INSERT INTO docente (
    id_docente,
    nombre,
    apellido,
    correo,
    telefono,
    especialidad
)
VALUES (
           4,
           'Roberto',
           'Fernández',
           'roberto.fernandez@uca.edu.sv',
           '7321-9876',
           'Gestión Ágil de Proyectos'
       );


INSERT INTO curso (
    id_curso,
    nombre,
    descripcion,
    cupo_maximo,
    fecha_inicio,
    fecha_fin,
    costo,
    estado,
    id_categoria,
    id_modalidad,
    id_docente
)
VALUES (
           1,
           'Programación con Java',
           'Programación orientada a objetos utilizando Java',
           25,
           '2026-10-05',
           '2026-11-28',
           150.00,
           'ACTIVO',
           1,
           1,
           1
       );

INSERT INTO curso (
    id_curso,
    nombre,
    descripcion,
    cupo_maximo,
    fecha_inicio,
    fecha_fin,
    costo,
    estado,
    id_categoria,
    id_modalidad,
    id_docente
)
VALUES (
           2,
           'Curso en Ciberseguridad',
           'Fundamentos de seguridad informática',
           20,
           '2026-10-09',
           '2026-12-10',
           200.00,
           'ACTIVO',
           1,
           1,
           2
       );

INSERT INTO curso (
    id_curso,
    nombre,
    descripcion,
    cupo_maximo,
    fecha_inicio,
    fecha_fin,
    costo,
    estado,
    id_categoria,
    id_modalidad,
    id_docente
)
VALUES (
           3,
           'Marketing Digital',
           'Estrategias de publicidad y redes sociales',
           30,
           '2026-11-02',
           '2026-12-14',
           125.00,
           'INACTIVO',
           4,
           2,
           2
       );


INSERT INTO diplomado (
    id_diplomado,
    nombre,
    descripcion,
    cupo_maximo,
    fecha_inicio,
    fecha_fin,
    costo,
    estado,
    id_categoria,
    id_modalidad,
    id_docente
)
VALUES (
           1,
           'Diplomado en Gestión Empresarial',
           'Formación en administración y gestión empresarial',
           25,
           '2026-09-17',
           '2026-11-30',
           650.00,
           'ACTIVO',
           2,
           2,
           4
       );

INSERT INTO diplomado (
    id_diplomado,
    nombre,
    descripcion,
    cupo_maximo,
    fecha_inicio,
    fecha_fin,
    costo,
    estado,
    id_categoria,
    id_modalidad,
    id_docente
)
VALUES (
           2,
           'Diplomado en Diseño UX/UI',
           'Diseño de interfaces y experiencia de usuario',
           20,
           '2026-10-01',
           '2027-02-28',
           600.00,
           'ACTIVO',
           3,
           3,
           3
       );


INSERT INTO cliente (
    id_cliente,
    dui,
    nombre,
    empresa,
    correo,
    telefono,
    direccion
)
VALUES (
           1,
           '01234567-8',
           'Juan Pérez',
           'Empresa XYZ',
           'juan.perez@gmail.com',
           '7000-0009',
           'San Salvador'
       );

INSERT INTO cliente (
    id_cliente,
    dui,
    nombre,
    empresa,
    correo,
    telefono,
    direccion
)
VALUES (
           2,
           '06141205-9',
           'Carlos Rodríguez',
           'Tech Solutions S.A.',
           'crodriguez@techsol.com',
           '7765-1100',
           'Santa Tecla'
       );

INSERT INTO cliente (
    id_cliente,
    dui,
    nombre,
    empresa,
    correo,
    telefono,
    direccion
)
VALUES (
           3,
           '05047892-3',
           'Ana Lidia Martínez',
           NULL,
           'amartinez.design@gmail.com',
           '7123-4500',
           'Antiguo Cuscatlán'
       );

INSERT INTO cliente (
    id_cliente,
    dui,
    nombre,
    empresa,
    correo,
    telefono,
    direccion
)
VALUES (
           4,
           '04231980-1',
           'Elena Flores',
           NULL,
           'elena.flores80@hotmail.com',
           '7890-3300',
           'Soyapango'
       );


INSERT INTO participante (
    id_participante,
    dui,
    nombre,
    apellido,
    correo,
    telefono
)
VALUES (
           1,
           '12345678-9',
           'Vanessa',
           'Escalón',
           'vanessa@gmail.com',
           '7270-9970'
       );

INSERT INTO participante (
    id_participante,
    dui,
    nombre,
    apellido,
    correo,
    telefono
)
VALUES (
           2,
           '03124567-1',
           'María',
           'Gómez',
           'maria.gomez@gmail.com',
           '7010-2001'
       );

INSERT INTO participante (
    id_participante,
    dui,
    nombre,
    apellido,
    correo,
    telefono
)
VALUES (
           3,
           '04125678-2',
           'Pedro',
           'López',
           'pedro.lopez@gmail.com',
           '7010-2002'
       );


INSERT INTO estado_inscripcion (id_estado_inscripcion, nombre)
VALUES (1, 'Pendiente');

INSERT INTO estado_inscripcion (id_estado_inscripcion, nombre)
VALUES (2, 'Confirmada');

INSERT INTO estado_inscripcion (id_estado_inscripcion, nombre)
VALUES (3, 'Cancelada');

INSERT INTO estado_inscripcion (id_estado_inscripcion, nombre)
VALUES (4, 'Finalizada');


INSERT INTO inscripcion (
    id_inscripcion,
    fecha,
    id_estado_inscripcion,
    id_cliente,
    id_curso
)
VALUES (
           1,
           '2026-09-18',
           2,
           1,
           1
       );

INSERT INTO inscripcion (
    id_inscripcion,
    fecha,
    id_estado_inscripcion,
    id_cliente,
    id_curso
)
VALUES (
           2,
           '2026-09-19',
           1,
           2,
           2
       );


INSERT INTO inscripcion_participante (
    id_inscripcion,
    id_participante
)
VALUES (
           1,
           1
       );

INSERT INTO inscripcion_participante (
    id_inscripcion,
    id_participante
)
VALUES (
           1,
           2
       );

INSERT INTO inscripcion_participante (
    id_inscripcion,
    id_participante
)
VALUES (
           2,
           3
       );


INSERT INTO estado_cotizacion (id_estado_cotizacion, nombre)
VALUES (1, 'Pendiente');

INSERT INTO estado_cotizacion (id_estado_cotizacion, nombre)
VALUES (2, 'En proceso');

INSERT INTO estado_cotizacion (id_estado_cotizacion, nombre)
VALUES (3, 'Aprobada');

INSERT INTO estado_cotizacion (id_estado_cotizacion, nombre)
VALUES (4, 'Rechazada');


INSERT INTO cotizacion (
    id_cotizacion,
    fecha,
    subtotal,
    total,
    id_estado_cotizacion,
    id_cliente
)
VALUES (
           1,
           '2026-09-18',
           300.00,
           339.00,
           3,
           1
       );

INSERT INTO cotizacion (
    id_cotizacion,
    fecha,
    subtotal,
    total,
    id_estado_cotizacion,
    id_cliente
)
VALUES (
           2,
           '2026-09-19',
           500.00,
           565.00,
           1,
           2
       );


INSERT INTO detalle_cotizacion (
    id_detalle,
    descripcion,
    cantidad,
    precio,
    subtotal,
    id_cotizacion
)
VALUES (
           1,
           'Inscripción al curso Programación con Java',
           2,
           150.00,
           300.00,
           1
       );

INSERT INTO detalle_cotizacion (
    id_detalle,
    descripcion,
    cantidad,
    precio,
    subtotal,
    id_cotizacion
)
VALUES (
           2,
           'Alquiler de auditorio',
           1,
           500.00,
           500.00,
           2
       );


INSERT INTO espacio (
    id_espacio,
    nombre,
    descripcion,
    capacidad,
    ubicacion,
    costo_hora,
    estado
)
VALUES (
           1,
           'Auditorio Principal',
           'Auditorio equipado para conferencias y eventos',
           250,
           'Edificio principal, primer nivel',
           500.00,
           'DISPONIBLE'
       );

INSERT INTO espacio (
    id_espacio,
    nombre,
    descripcion,
    capacidad,
    ubicacion,
    costo_hora,
    estado
)
VALUES (
           2,
           'Sala de Conferencias A',
           'Sala para reuniones, capacitaciones y conferencias',
           80,
           'Edificio de aulas, segundo nivel',
           250.00,
           'DISPONIBLE'
       );

INSERT INTO espacio (
    id_espacio,
    nombre,
    descripcion,
    capacidad,
    ubicacion,
    costo_hora,
    estado
)
VALUES (
           3,
           'Laboratorio de Computación',
           'Laboratorio equipado con computadoras y proyector',
           30,
           'Edificio tecnológico, primer nivel',
           200.00,
           'MANTENIMIENTO'
       );


INSERT INTO alquiler (
    id_alquiler,
    fecha,
    hora_inicio,
    hora_fin,
    cantidad_personas,
    estado,
    id_cliente,
    id_espacio
)
VALUES (
           1,
           '2026-10-20',
           '2026-10-20 08:00:00',
           '2026-10-20 12:00:00',
           60,
           'CONFIRMADO',
           2,
           2
       );

INSERT INTO alquiler (
    id_alquiler,
    fecha,
    hora_inicio,
    hora_fin,
    cantidad_personas,
    estado,
    id_cliente,
    id_espacio
)
VALUES (
           2,
           '2026-10-25',
           '2026-10-25 13:00:00',
           '2026-10-25 17:00:00',
           150,
           'PENDIENTE',
           1,
           1
       );


INSERT INTO servicio_catering (
    id_catering,
    nombre,
    descripcion,
    capacidad_minima,
    capacidad_maxima,
    precio_por_persona,
    estado
)
VALUES (
           1,
           'Coffee Break Premium',
           'Café, jugo, agua, bocadillos y postre',
           20,
           150,
           10.00,
           'DISPONIBLE'
       );

INSERT INTO servicio_catering (
    id_catering,
    nombre,
    descripcion,
    capacidad_minima,
    capacidad_maxima,
    precio_por_persona,
    estado
)
VALUES (
           2,
           'Almuerzo Ejecutivo',
           'Plato principal, bebida y postre',
           15,
           200,
           15.00,
           'DISPONIBLE'
       );


INSERT INTO solicitud_catering (
    id_solicitud,
    cantidad_asistentes,
    menu,
    fecha,
    hora,
    lugar,
    estado,
    id_cliente,
    id_catering
)
VALUES (
           1,
           60,
           'Coffee Break con café, jugo y bocadillos',
           '2026-10-20',
           '10:00:00',
           'Sala de Conferencias A',
           'CONFIRMADA',
           2,
           1
       );

INSERT INTO solicitud_catering (
    id_solicitud,
    cantidad_asistentes,
    menu,
    fecha,
    hora,
    lugar,
    estado,
    id_cliente,
    id_catering
)
VALUES (
           2,
           100,
           'Almuerzo ejecutivo',
           '2026-10-25',
           '12:00:00',
           'Auditorio Principal',
           'PENDIENTE',
           1,
           2
       );


INSERT INTO actividad (
    id_actividad,
    nombre,
    tipo,
    fecha,
    horario,
    hora_inicio,
    hora_fin,
    descripcion,
    id_curso,
    id_diplomado,
    id_alquiler,
    id_solicitud_catering
)
VALUES (
           1,
           'Inicio del curso Programación con Java',
           'CURSO',
           '2026-10-05',
           'Lunes y miércoles de 6:00 p. m. a 8:00 p. m.',
           '2026-10-05 18:00:00',
           '2026-10-05 20:00:00',
           'Primera clase del curso de programación',
           1,
           NULL,
           NULL,
           NULL
       );

INSERT INTO actividad (
    id_actividad,
    nombre,
    tipo,
    fecha,
    horario,
    hora_inicio,
    hora_fin,
    descripcion,
    id_curso,
    id_diplomado,
    id_alquiler,
    id_solicitud_catering
)
VALUES (
           2,
           'Conferencia empresarial',
           'ALQUILER',
           '2026-10-20',
           'De 8:00 a. m. a 12:00 p. m.',
           '2026-10-20 08:00:00',
           '2026-10-20 12:00:00',
           'Conferencia organizada por Tech Solutions S.A.',
           NULL,
           NULL,
           1,
           1
       );


INSERT INTO estado_pago (id_estado_pago, nombre)
VALUES (1, 'Pendiente');

INSERT INTO estado_pago (id_estado_pago, nombre)
VALUES (2, 'Parcial');

INSERT INTO estado_pago (id_estado_pago, nombre)
VALUES (3, 'Pagado');


INSERT INTO metodo_pago (id_metodo_pago, nombre)
VALUES (1, 'Efectivo');

INSERT INTO metodo_pago (id_metodo_pago, nombre)
VALUES (2, 'Tarjeta');

INSERT INTO metodo_pago (id_metodo_pago, nombre)
VALUES (3, 'Transferencia');

INSERT INTO metodo_pago (id_metodo_pago, nombre)
VALUES (4, 'Depósito');


INSERT INTO pago (
    id_pago,
    fecha,
    monto,
    referencia,
    id_estado_pago,
    id_cliente,
    id_metodo_pago,
    id_inscripcion,
    id_cotizacion,
    id_alquiler,
    id_solicitud_catering
)
VALUES (
           1,
           '2026-09-18',
           150.00,
           'TRX-2026-0001',
           3,
           1,
           3,
           1,
           NULL,
           NULL,
           NULL
       );

INSERT INTO pago (
    id_pago,
    fecha,
    monto,
    referencia,
    id_estado_pago,
    id_cliente,
    id_metodo_pago,
    id_inscripcion,
    id_cotizacion,
    id_alquiler,
    id_solicitud_catering
)
VALUES (
           2,
           '2026-09-19',
           339.00,
           'TRX-2026-0002',
           3,
           1,
           2,
           NULL,
           1,
           NULL,
           NULL
       );

INSERT INTO pago (
    id_pago,
    fecha,
    monto,
    referencia,
    id_estado_pago,
    id_cliente,
    id_metodo_pago,
    id_inscripcion,
    id_cotizacion,
    id_alquiler,
    id_solicitud_catering
)
VALUES (
           3,
           '2026-09-20',
           500.00,
           'TRX-2026-0003',
           2,
           2,
           3,
           NULL,
           NULL,
           1,
           NULL
       );

