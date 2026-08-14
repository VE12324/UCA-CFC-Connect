# UCA-CFC Connect

Proyecto de cátedra para Desarrollo de Aplicaciones con Web Frameworks (DWF404), Ciclo II 2026, Universidad Don Bosco.

La idea es hacer una API REST con Spring Boot para el Centro de Formación Continua (CFC) de la UCA, que les sirva para manejar en un solo sistema los cursos y diplomados, las inscripciones, la parte comercial (cotizaciones, alquiler de espacios, catering), la agenda y los pagos. Ahora mismo todo eso lo llevan medio disperso, entonces el objetivo es centralizarlo.

Docente: Ing. Yesenia Escobar

## Grupo G01T

- Mariana Guadalupe Ramírez Lara (RL252957) 
- Gabriela Vanessa Alberto Escalón (AE252972) 
- Katherine Gissella Garay Alvarado (GA252993) 
- Samuel Eliezer Rivera De Paz (RD253031) 
- Cristian Josué Torres Reyes (TR240516) 

## Tecnologías

Java 21, Spring Boot (Web, Data JPA, Security), Hibernate, JWT, MySQL o PostgreSQL (todavía no se decide cuál usar en producción), Maven, Swagger/OpenAPI para documentar la API, JUnit 5 + Mockito para pruebas.

## Módulos

1. Gestión académica
2. Clientes
3. Inscripciones
4. Cotizaciones
5. Alquiler de espacios
6. Catering
7. Agenda institucional
8. Pagos
9. Seguridad (usuarios, roles, JWT)

## Estructura

```
uca-cfc-connect/
├── src/main/java/com/udb/ucacfconnect/
│   ├── controller/   endpoints REST
│   ├── service/      lógica de negocio
│   ├── repository/   interfaces JpaRepository
│   ├── model/         entidades JPA
│   ├── dto/           DTOs y validaciones
│   ├── exception/     excepciones custom y @ControllerAdvice
│   ├── security/       Spring Security + JWT
│   └── config/         Swagger y otras configuraciones
├── src/main/resources/
├── src/test/java/...
├── pom.xml
└── README.md
```


## Ramas

Usamos main, develop y una rama feature por módulo (ej. `feature/modulo-cotizaciones`), una por integrante según lo que le toque. Cada quien hace su PR hacia develop y necesita al menos una revisión antes de mergear.

## Roles de usuario

- ADMIN: configuración del sistema, catálogos, aprobar cotizaciones, reportes
- RECEPCIONISTA: clientes, inscripciones, alquileres, catering, cotizaciones, agenda, confirmar pagos
- CLIENTE: se registra, ve la oferta académica, pide cotizaciones/alquileres/catering, revisa su estado y pagos
- CONTABILIDAD: registra y valida pagos, emite comprobantes, estados financieros

## Estado

Vamos en la fase de planificación y diseño (entrega 14 de agosto). Faltan el primer avance (11 de septiembre) y el proyecto funcional con la defensa (17 de octubre).
