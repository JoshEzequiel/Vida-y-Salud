# Veterinaria Vida y Salud

Este proyecto corresponde a un sistema para la gestión de una clínica veterinaria, desarrollado mediante una arquitectura de 10 microservicios independientes.

Para su desarrollo utilicé Java 21, Spring Boot 3.5.14, Spring Web, Spring Data JPA, Bean Validation, WebClient, SLF4J Logs, Actuator, Maven y MariaDB.

La aplicación permite administrar clientes, mascotas, veterinarios, citas, consultas, medicamentos, recetas, inventario, pagos y notificaciones.

## Microservicios del sistema

| Microservicio           | Puerto | Responsabilidad                                         |
| ----------------------- | -----: | ------------------------------------------------------- |
| `cliente-servicio`      |   8081 | Administración de clientes y sus datos de contacto      |
| `mascota-servicio`      |   8082 | Administración de mascotas y validación de su dueño     |
| `veterinario-servicio`  |   8083 | Administración de veterinarios y especialidades         |
| `cita-servicio`         |   8084 | Agendamiento de citas para mascotas y veterinarios      |
| `consulta-servicio`     |   8085 | Registro de consultas, diagnósticos e historial clínico |
| `medicamento-servicio`  |   8086 | Administración de medicamentos y stock disponible       |
| `receta-servicio`       |   8087 | Creación de recetas y sus medicamentos asociados        |
| `inventario-servicio`   |   8088 | Registro de entradas, salidas y ajustes de inventario   |
| `pago-servicio`         |   8089 | Administración de pagos asociados a consultas           |
| `notificacion-servicio` |   8090 | Creación y envío simulado de notificaciones             |

## Requisitos para ejecutar el proyecto

* JDK 21
* IntelliJ IDEA
* Maven
* Laragon con MariaDB
* Postman
* MariaDB ejecutándose en `localhost:3306`
* Usuario de base de datos: `root`
* Contraseña vacía por defecto

En caso de utilizar otro usuario, contraseña o puerto, se deben modificar los archivos `application.properties` de cada microservicio.

## Base de datos

Todos los microservicios utilizan la misma base de datos académica:

veterinaria_db


Aunque comparten la misma base de datos, cada microservicio administra solamente las tablas relacionadas con su responsabilidad y no utiliza los repositorios internos de los otros servicios.

El script para crear la base de datos y todas sus tablas se encuentra en:


database VidaYSalud Veterinaria.sql


Para comenzar desde cero:

1. Iniciar Laragon.
2. Abrir HeidiSQL.
3. Conectarse al servidor MariaDB.
4. Abrir el archivo SQL.
5. Ejecutar el script completo.
6. Verificar que se haya creado la base `veterinaria_db`.

## Cómo abrir el proyecto

El proyecto contiene un `pom.xml` principal que agrupa los 10 microservicios como módulos Maven.

En IntelliJ IDEA:

1. Abrir la carpeta raíz `veterinaria-microservicios`.
2. Esperar la carga de Maven.
3. Utilizar Java 21 como Project SDK.
4. Activar el procesamiento de anotaciones para Lombok:


Settings
→ Build, Execution, Deployment
→ Compiler
→ Annotation Processors
→ Enable annotation processing


5. Recargar los proyectos Maven desde la ventana Maven.

## Orden recomendado de ejecución

Antes de ejecutar los microservicios se debe iniciar Laragon.

Luego se pueden ejecutar las clases principales en el siguiente orden:

1. `ClienteServicioApplication`
2. `VeterinarioServicioApplication`
3. `MedicamentoServicioApplication`
4. `MascotaServicioApplication`
5. `CitaServicioApplication`
6. `ConsultaServicioApplication`
7. `RecetaServicioApplication`
8. `InventarioServicioApplication`
9. `PagoServicioApplication`
10. `NotificacionServicioApplication`

Cada aplicación debe permanecer ejecutándose mientras se prueban los otros servicios.

## Comprobación del estado de los servicios

Cada microservicio incorpora Spring Boot Actuator.

Para comprobar que un servicio está funcionando se puede utilizar:


http://localhost:PUERTO/actuator/health


Ejemplo:


http://localhost:8081/actuator/health
```

Si el servicio y la conexión a MariaDB están funcionando, la respuesta mostrará:

json
{
  "status": "UP"
}


## Arquitectura utilizada

Cada microservicio está organizado siguiendo una separación por capas:


Controller → Service → Repository → MariaDB


### Controller

Recibe las solicitudes HTTP desde Postman o desde otro cliente y devuelve respuestas mediante `ResponseEntity`.

### Service

Contiene la lógica de negocio, las validaciones adicionales, las transacciones, los registros de log y las llamadas a otros microservicios.

### Repository

Se comunica con MariaDB mediante Spring Data JPA y permite realizar operaciones CRUD sin escribir manualmente todas las consultas SQL.

### DTO

Los DTO permiten controlar qué información entra y sale de cada API, evitando exponer directamente las entidades de la base de datos.

### Exception

Cada microservicio posee manejo centralizado de excepciones mediante `@RestControllerAdvice`, entregando respuestas JSON ordenadas y códigos HTTP apropiados.

## Comunicación entre microservicios

Las comunicaciones remotas se realizan mediante `WebClient`.

Se implementaron las siguientes comunicaciones:

* `mascota-servicio` consulta a `cliente-servicio`
* `cita-servicio` consulta a `mascota-servicio` y `veterinario-servicio`
* `consulta-servicio` consulta a `mascota-servicio`, `veterinario-servicio` y `cita-servicio`
* `receta-servicio` consulta a `consulta-servicio` y `medicamento-servicio`
* `inventario-servicio` consulta a `medicamento-servicio`
* `pago-servicio` consulta a `consulta-servicio`
* `notificacion-servicio` consulta a `cliente-servicio`

Por ejemplo, antes de registrar una mascota, `mascota-servicio` consulta remotamente a `cliente-servicio` para comprobar que el dueño exista.

Las llamadas incluyen un tiempo máximo de conexión y respuesta, evitando que una aplicación quede esperando indefinidamente cuando otro microservicio no se encuentra disponible.

## Validaciones y reglas de negocio

El proyecto utiliza Bean Validation mediante anotaciones como:

@NotBlank
@NotNull
@Email
@Size
@Positive
@Future


Además, se implementaron reglas como:

* impedir clientes duplicados por RUT o correo;
* comprobar que el dueño exista antes de registrar una mascota;
* comprobar que la mascota y el veterinario existan antes de crear una cita;
* impedir valores negativos en precios, costos, montos o cantidades;
* validar stock antes de registrar una salida de inventario;
* evitar asociaciones con recursos inexistentes.

## Códigos HTTP utilizados

|                      Código | Uso                                                      |
| --------------------------: | -------------------------------------------------------- |
|                    `200 OK` | Consulta, actualización o eliminación exitosa            |
|               `201 Created` | Recurso creado correctamente                             |
|           `400 Bad Request` | Datos inválidos o incumplimiento de una regla de negocio |
|             `404 Not Found` | Recurso inexistente                                      |
|              `409 Conflict` | Datos duplicados o conflicto de integridad               |
| `500 Internal Server Error` | Error interno no controlado                              |
|   `503 Service Unavailable` | Microservicio remoto no disponible                       |

## Logs

Los microservicios utilizan SLF4J para registrar acciones importantes.

Los logs permiten observar:

* creación de registros;
* actualizaciones;
* eliminaciones;
* búsquedas;
* validaciones fallidas;
* recursos inexistentes;
* errores de comunicación entre servicios.

## Pruebas con Postman

Las colecciones completas se encuentran en:


postman carpeta


Se incluye una colección por cada microservicio y un environment llamado:


Veterinaria - Local 8081 a 8090 - CORREGIDO


El environment contiene las URLs de los servicios y guarda automáticamente identificadores como:


clienteId
mascotaId
veterinarioId
citaId
consultaId
medicamentoId


Para probar el sistema:

1. Importar las 10 colecciones.
2. Importar el archivo de environment.
3. Seleccionar el environment en Postman.
4. Mantener encendidos los microservicios requeridos.
5. Ejecutar las colecciones siguiendo el orden numérico.

Las pruebas comprueban:

* endpoints CRUD;
* códigos HTTP;
* respuestas JSON;
* validaciones;
* recursos inexistentes;
* datos duplicados;
* reglas de negocio;
* comunicación entre microservicios;
* health checks.

## Tecnologías utilizadas

* Java 21
* Spring Boot 3.5.14
* Spring Web
* Spring Data JPA
* Hibernate
* Bean Validation
* WebClient
* Spring Boot Actuator
* SLF4J
* Lombok
* Maven
* MariaDB
* Laragon
* HeidiSQL
* Postman
* IntelliJ IDEA
* GitHub

## Autor

Equipo Joshua Rios
