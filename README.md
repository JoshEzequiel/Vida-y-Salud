# Vida y Salud - Sistema Veterinario con Microservicios

Proyecto académico desarrollado para la asignatura **Desarrollo FullStack I**, basado en una arquitectura distribuida con microservicios independientes.
El sistema permite gestionar clientes, mascotas, veterinarios, citas, consultas, medicamentos, recetas, inventario, pagos y notificaciones.

La solución fue implementada con **Java 21**, **Spring Boot**, **MariaDB**, **API Gateway**, **Swagger/OpenAPI**, pruebas unitarias con **JUnit/Mockito** y despliegue local mediante **Docker Compose**.

---

## Integrantes

* [Joshua Rios Donoso]
* [Genesis Lincoqueo]
* [Gabriel Caceres]


---

## Tecnologías utilizadas

* Java 21
* Spring Boot 3
* Spring Data JPA
* Spring Web
* Spring WebFlux / WebClient
* Spring Cloud Gateway
* MariaDB
* Maven
* Docker
* Docker Compose
* Swagger / OpenAPI
* JUnit 5
* Mockito
* JaCoCo
* IntelliJ IDEA
* Postman / Navegador

---

## Arquitectura del sistema

El proyecto está construido usando una arquitectura de microservicios.
Cada microservicio tiene responsabilidad propia y se comunica con otros servicios cuando necesita validar información externa.

La estructura interna de los servicios sigue el patrón:

```text
Controller -> Service -> Repository -> Model
```

Además, se utiliza un **API Gateway** para centralizar el acceso a los endpoints del sistema desde un solo puerto.

---

## Microservicios del proyecto

| Microservicio         | Puerto | Responsabilidad              |
| --------------------- | -----: | ---------------------------- |
| cliente-servicio      |   8081 | Gestión de clientes          |
| mascota-servicio      |   8082 | Gestión de mascotas          |
| veterinario-servicio  |   8083 | Gestión de veterinarios      |
| cita-servicio         |   8084 | Gestión de citas             |
| consulta-servicio     |   8085 | Gestión de consultas médicas |
| medicamento-servicio  |   8086 | Gestión de medicamentos      |
| receta-servicio       |   8087 | Gestión de recetas           |
| inventario-servicio   |   8088 | Gestión de inventario        |
| pago-servicio         |   8089 | Gestión de pagos             |
| notificacion-servicio |   8090 | Gestión de notificaciones    |
| api-gateway           |   8080 | Enrutamiento centralizado    |

---

## Base de datos

El sistema utiliza **MariaDB** como motor de base de datos. Para separar ambientes se definieron dos bases:

```text
veterinaria_db       -> ambiente dev
veterinaria_db_test  -> ambiente test
```

En Docker, MariaDB se ejecuta como un contenedor independiente. Desde el equipo local se accede por:

```text
Host: localhost
Puerto: 3307
Usuario: root
Contraseña: root123
```

Dentro de la red Docker, los microservicios no usan `localhost`, sino el nombre del contenedor:

```text
mariadb:3306
```

Esta separación permite ejecutar pruebas sin afectar la base usada en desarrollo.

---

## Estructura general del proyecto

```text
Vida-y-Salud-main
│
├── api-gateway
├── cliente-servicio
├── mascota-servicio
├── veterinario-servicio
├── cita-servicio
├── consulta-servicio
├── medicamento-servicio
├── receta-servicio
├── inventario-servicio
├── pago-servicio
├── notificacion-servicio
│
├── docker
│   └── init
│       └── 01-veterinaria.sql
│
└── docker-compose.yml
```

Cada microservicio contiene su propio `Dockerfile`, lo que permite construirlo como una imagen independiente.

---

## Configuración YAML, perfiles y Docker

Cada microservicio utiliza tres archivos de configuración:

```text
application.yml
application-dev.yml
application-test.yml
```

El archivo `application.yml` define el perfil activo por defecto:

```yaml
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
```

El perfil `dev` se usa para ejecutar el sistema en desarrollo y Docker, conectando a la base principal `veterinaria_db`. El perfil `test` se usa para pruebas y conecta a `veterinaria_db_test`.

En `docker-compose.yml`, cada microservicio recibe variables de entorno como:

```yaml
environment:
  SPRING_PROFILES_ACTIVE: dev
  DB_URL: jdbc:mariadb://mariadb:3306/veterinaria_db
  DB_USER: root
  DB_PASSWORD: root123
```

Dentro de Docker, las comunicaciones internas se realizan usando el nombre del servicio, por ejemplo:

```text
mariadb:3306
cliente-servicio:8081
mascota-servicio:8082
api-gateway:8080
```

Con esta configuración, el ecosistema puede levantarse completo mediante Docker Compose sin modificar el código fuente.


---

## API Gateway

El API Gateway se ejecuta en el puerto:

```text
8080
```

Desde este servicio se centralizan las rutas hacia los demás microservicios.

Ejemplos de rutas mediante Gateway:

```text
http://localhost:8080/api/v1/clientes
http://localhost:8080/api/v1/mascotas
http://localhost:8080/api/v1/veterinarios
http://localhost:8080/api/v1/citas
http://localhost:8080/api/v1/consultas
http://localhost:8080/api/v1/medicamentos
http://localhost:8080/api/v1/recetas
http://localhost:8080/api/v1/inventario
http://localhost:8080/api/v1/pagos
http://localhost:8080/api/v1/notificaciones
```

Dentro de Docker, el Gateway se comunica con los microservicios usando el nombre del servicio, por ejemplo:

```text
http://cliente-servicio:8081
http://mascota-servicio:8082
http://cita-servicio:8084
```

---

## Comunicación entre microservicios

Algunos microservicios consumen endpoints de otros servicios mediante `WebClient`.

Ejemplos:

* `mascota-servicio` valida que exista el cliente antes de registrar una mascota.
* `cita-servicio` valida la existencia de mascota y veterinario antes de registrar una cita.
* `consulta-servicio` puede validar información de citas.
* `receta-servicio` puede comunicarse con consulta y medicamento.
* `inventario-servicio` puede trabajar relacionado con medicamentos.
* `pago-servicio` puede trabajar asociado a consultas.

También se manejan errores cuando un servicio remoto no responde o cuando el dato validado no existe.

---

## Reglas de negocio implementadas

Algunas reglas consideradas en el sistema son:

* No registrar clientes con RUT duplicado.
* No registrar clientes con correo duplicado.
* No registrar mascotas asociadas a clientes inexistentes.
* No registrar citas en fechas pasadas.
* No duplicar horarios de un veterinario.
* No cancelar una cita que ya fue atendida.
* Validar relaciones entre entidades antes de guardar información.
* Manejar errores cuando un recurso no existe.

---

## Ejecución con Docker Compose

Para ejecutar el sistema completo se debe tener instalado:

* Docker Desktop
* WSL habilitado en Windows
* Conexión a internet para descargar imágenes la primera vez

Desde la raíz del proyecto ejecutar:

```powershell
docker compose up -d --build
```

Este comando construye y levanta:

* MariaDB
* Los 10 microservicios
* API Gateway

Para revisar que todo esté ejecutándose:

```powershell
docker ps
```

Para detener los contenedores:

```powershell
docker compose down
```

Importante: no usar `docker compose down -v` si no se desea borrar el volumen de la base de datos.

---

## Verificación del despliegue

Una vez levantado el proyecto con Docker, se puede probar el Gateway desde el navegador:

```text
http://localhost:8080/api/v1/clientes
http://localhost:8080/api/v1/mascotas
http://localhost:8080/api/v1/veterinarios
http://localhost:8080/api/v1/citas
http://localhost:8080/api/v1/consultas
http://localhost:8080/api/v1/medicamentos
http://localhost:8080/api/v1/recetas
http://localhost:8080/api/v1/inventario
http://localhost:8080/api/v1/pagos
http://localhost:8080/api/v1/notificaciones
```

También se puede revisar el estado de los servicios con Actuator:

```text
http://localhost:8080/actuator/health
http://localhost:8081/actuator/health
http://localhost:8082/actuator/health
http://localhost:8083/actuator/health
http://localhost:8084/actuator/health
```

---

## Pruebas en Postman mediante API Gateway

Las pruebas REST también se realizaron desde Postman usando el API Gateway como punto único de entrada. Todas las rutas usan el puerto `8080`.

| Microservicio | Método | Ruta Gateway |
| --- | --- | --- |
| cliente-servicio | GET | `http://localhost:8080/api/v1/clientes` |
| mascota-servicio | GET | `http://localhost:8080/api/v1/mascotas` |
| veterinario-servicio | GET | `http://localhost:8080/api/v1/veterinarios` |
| cita-servicio | GET | `http://localhost:8080/api/v1/citas` |
| consulta-servicio | GET | `http://localhost:8080/api/v1/consultas` |
| medicamento-servicio | GET | `http://localhost:8080/api/v1/medicamentos` |
| receta-servicio | GET | `http://localhost:8080/api/v1/recetas` |
| inventario-servicio | GET | `http://localhost:8080/api/v1/inventario` |
| pago-servicio | GET | `http://localhost:8080/api/v1/pagos` |
| notificacion-servicio | GET | `http://localhost:8080/api/v1/notificaciones` |

Se dejó evidencia en la carpeta `evidencias/postman`, validando respuestas HTTP `200 OK` y datos en formato JSON.

---

## Swagger / OpenAPI

Cada microservicio cuenta con documentación Swagger/OpenAPI para visualizar y probar endpoints, modelos, parámetros, cuerpos JSON y códigos de respuesta.

| Microservicio | Swagger |
| --- | --- |
| cliente-servicio | `http://localhost:8081/swagger-ui/index.html` |
| mascota-servicio | `http://localhost:8082/swagger-ui/index.html` |
| veterinario-servicio | `http://localhost:8083/swagger-ui/index.html` |
| cita-servicio | `http://localhost:8084/swagger-ui/index.html` |
| consulta-servicio | `http://localhost:8085/swagger-ui/index.html` |
| medicamento-servicio | `http://localhost:8086/swagger-ui/index.html` |
| receta-servicio | `http://localhost:8087/swagger-ui/index.html` |
| inventario-servicio | `http://localhost:8088/swagger-ui/index.html` |
| pago-servicio | `http://localhost:8089/swagger-ui/index.html` |
| notificacion-servicio | `http://localhost:8090/swagger-ui/index.html` |

Se dejó evidencia en la carpeta `evidencias/swagger`, ejecutando endpoints `GET` desde Swagger y validando respuesta HTTP `200 OK` con formato JSON.


---

## Pruebas unitarias y perfiles de test

El proyecto incluye pruebas unitarias con **JUnit 5**, **Mockito** y **DataFaker** en `src/test/java`.

Se implementó separación de ambientes mediante perfiles:

```text
dev  -> veterinaria_db
test -> veterinaria_db_test
```

Las clases `PerfilTest` cargan Spring con `@ActiveProfiles("test")` y validan que la conexión apunte a `veterinaria_db_test`. Las clases `ServiceTest` prueban reglas de negocio usando mocks, asserts y datos simulados generados con DataFaker.

| Microservicio | Prueba de perfil | Prueba de servicio |
| --- | --- | --- |
| cliente-servicio | ClientePerfilTest | ClienteServiceTest |
| mascota-servicio | MascotaPerfilTest | MascotaServiceTest |
| veterinario-servicio | VeterinarioPerfilTest | VeterinarioServiceTest |
| cita-servicio | CitaPerfilTest | CitaServiceTest |
| consulta-servicio | ConsultaPerfilTest | ConsultaServiceTest |
| medicamento-servicio | MedicamentoPerfilTest | MedicamentoServiceTest |
| receta-servicio | RecetaPerfilTest | RecetaServiceTest |
| inventario-servicio | InventarioPerfilTest | InventarioServiceTest |
| pago-servicio | PagoPerfilTest | PagoServiceTest |
| notificacion-servicio | NotificacionPerfilTest | NotificacionServiceTest |

Las pruebas unitarias de servicio usan Mockito para simular repositorios y clientes remotos, por lo que no modifican datos reales. La base `veterinaria_db_test` se usa para validar configuración y pruebas que cargan el contexto Spring con perfil `test`.

Para ejecutar pruebas desde IntelliJ se puede usar el botón de ejecución de cada clase. También se pueden ejecutar con Maven desde cada microservicio:

```powershell
mvn test
```

Si el comando `mvn` no está disponible en Windows, se pueden ejecutar directamente desde IntelliJ o usando el Maven Wrapper del proyecto, si existe:

```powershell
.\mvnw.cmd test
```


---

## Reporte de cobertura JaCoCo

JaCoCo genera reportes de cobertura después de ejecutar las pruebas.

Ruta del reporte:

```text
target/site/jacoco/index.html
```

Este reporte permite revisar qué clases y métodos fueron cubiertos por las pruebas unitarias.

---

## Endpoints principales

### Clientes

```text
GET    /api/v1/clientes
GET    /api/v1/clientes/{id}
POST   /api/v1/clientes
PUT    /api/v1/clientes/{id}
DELETE /api/v1/clientes/{id}
```

### Mascotas

```text
GET    /api/v1/mascotas
GET    /api/v1/mascotas/{id}
POST   /api/v1/mascotas
PUT    /api/v1/mascotas/{id}
DELETE /api/v1/mascotas/{id}
```

### Veterinarios

```text
GET    /api/v1/veterinarios
GET    /api/v1/veterinarios/{id}
POST   /api/v1/veterinarios
PUT    /api/v1/veterinarios/{id}
DELETE /api/v1/veterinarios/{id}
```

### Citas

```text
GET    /api/v1/citas
GET    /api/v1/citas/{id}
POST   /api/v1/citas
PUT    /api/v1/citas/{id}
DELETE /api/v1/citas/{id}
```

### Consultas

```text
GET    /api/v1/consultas
GET    /api/v1/consultas/{id}
POST   /api/v1/consultas
PUT    /api/v1/consultas/{id}
DELETE /api/v1/consultas/{id}
```

### Medicamentos

```text
GET    /api/v1/medicamentos
GET    /api/v1/medicamentos/{id}
POST   /api/v1/medicamentos
PUT    /api/v1/medicamentos/{id}
DELETE /api/v1/medicamentos/{id}
```

### Recetas

```text
GET    /api/v1/recetas
GET    /api/v1/recetas/{id}
POST   /api/v1/recetas
PUT    /api/v1/recetas/{id}
DELETE /api/v1/recetas/{id}
```

### Inventario

```text
GET    /api/v1/inventario
GET    /api/v1/inventario/{id}
POST   /api/v1/inventario
PUT    /api/v1/inventario/{id}
DELETE /api/v1/inventario/{id}
```

### Pagos

```text
GET    /api/v1/pagos
GET    /api/v1/pagos/{id}
POST   /api/v1/pagos
PUT    /api/v1/pagos/{id}
DELETE /api/v1/pagos/{id}
```

### Notificaciones

```text
GET    /api/v1/notificaciones
GET    /api/v1/notificaciones/{id}
POST   /api/v1/notificaciones
PUT    /api/v1/notificaciones/{id}
DELETE /api/v1/notificaciones/{id}
```

---

## Comandos útiles

Levantar todo el sistema:

```powershell
docker compose up -d --build
```

Ver contenedores activos:

```powershell
docker ps
```

Ver logs de un servicio:

```powershell
docker logs cliente-servicio
```

Ver logs del Gateway:

```powershell
docker logs api-gateway
```

Detener el sistema:

```powershell
docker compose down
```

Construir un servicio específico:

```powershell
docker compose up -d --build cliente-servicio
```

Entrar a MariaDB dentro del contenedor:

```powershell
docker exec -it veterinaria-mariadb mariadb -uroot -proot123 veterinaria_db
```

Consultar tablas:

```sql
SHOW TABLES;
```

---

## Variables de entorno usadas en Docker

| Variable               | Uso                                  |
| ---------------------- | ------------------------------------ |
| SPRING_PROFILES_ACTIVE | Perfil activo del microservicio      |
| DB_URL                 | URL de conexión a MariaDB            |
| DB_USER                | Usuario de base de datos             |
| DB_PASSWORD            | Contraseña de base de datos          |
| CLIENTE_URL            | URL interna de cliente-servicio      |
| MASCOTA_URL            | URL interna de mascota-servicio      |
| VETERINARIO_URL        | URL interna de veterinario-servicio  |
| CITA_URL               | URL interna de cita-servicio         |
| CONSULTA_URL           | URL interna de consulta-servicio     |
| MEDICAMENTO_URL        | URL interna de medicamento-servicio  |
| RECETA_URL             | URL interna de receta-servicio       |
| INVENTARIO_URL         | URL interna de inventario-servicio   |
| PAGO_URL               | URL interna de pago-servicio         |
| NOTIFICACION_URL       | URL interna de notificacion-servicio |

---

## Problemas comunes

### El puerto ya está ocupado

Si aparece un error como:

```text
port is already allocated
```

significa que algún servicio está corriendo en IntelliJ o en otro proceso.

Solución:

```powershell
netstat -ano | findstr :8081
taskkill /PID <PID> /F
```

También se puede detener el servicio desde IntelliJ.

---

### La base de datos aparece vacía

Si MariaDB levanta pero no aparecen tablas, revisar que exista el archivo:

```text
docker/init/01-veterinaria.sql
```

El script debe contener la estructura y los datos de la base.

---

### Un microservicio no inicia

Revisar logs:

```powershell
docker logs nombre-del-contenedor
```

Ejemplo:

```powershell
docker logs cita-servicio
```

---

## Estado actual del proyecto

El proyecto cuenta con:

* 10 microservicios independientes.
* API Gateway funcional como entrada única por el puerto `8080`.
* Base de datos MariaDB en Docker con separación `dev` y `test`.
* Comunicación REST entre microservicios mediante WebClient.
* Configuración YAML con perfiles `application-dev.yml` y `application-test.yml`.
* Pruebas unitarias con JUnit, Mockito y DataFaker.
* Pruebas de perfil para validar el uso de `veterinaria_db_test`.
* Documentación Swagger/OpenAPI en todos los microservicios.
* Evidencia de pruebas en Postman usando API Gateway.
* Docker Compose para despliegue local.
* Manejo de errores y validaciones de negocio.

---

## Conclusión

El sistema permite demostrar una arquitectura distribuida basada en microservicios, donde cada servicio tiene una responsabilidad definida y se comunica con otros servicios cuando corresponde.

El uso de Docker Compose facilita el despliegue local del sistema completo, incluyendo base de datos, microservicios y API Gateway.
Además, el uso de Swagger, pruebas unitarias y configuración YAML ayuda a mantener el proyecto documentado, probado y preparado para defensa técnica.
