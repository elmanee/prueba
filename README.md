# Proyecto de Integración Backend - GestoPago

Este repositorio contiene la implementación robusta de la integración entre nuestro backend interno y los servicios externos de **GestoPago**. La solución está construida sobre Spring Boot y se encarga de la obtención, almacenamiento en caché y persistencia local del catálogo de productos y tokens de autenticación de forma segura y automatizada.

---

## 1. Descripción General de la Solución

El objetivo principal del módulo es consumir el endpoint de catálogo de productos externo (`GET /sistema/service/getProductList.do`) administrado por GestoPago.

El flujo de integración está completamente automatizado y protegido:
* **Autenticación Dinámica:** El consumo del catálogo requiere un *Bearer Token*, el cual no está quemado (hardcoded) en el código. El sistema cuenta con un *Scheduler* que solicita dinámicamente el token utilizando las credenciales base parametrizadas en `application.properties`.
* **Sincronización Nocturna:** Un proceso Cron (job) se ejecuta de madrugada para refrescar de forma proactiva el catálogo.

---

## 2. Arquitectura de Capas e Implementación

El proyecto sigue una arquitectura limpia (Clean Architecture) dividida en las siguientes capas lógicas:

* **Controller:** Expone los endpoints internos para el consumo del frontend o aplicaciones satélites.
* **Service (`service` y `service.impl`):** Contiene la lógica de negocio, reglas de caché, validación y persistencia de base de datos.
* **Client / Integration (`client`):** Capa de comunicación HTTP declarativa utilizando **Spring Cloud OpenFeign** para dialogar con los endpoints de GestoPago.
* **DTOs (`model` y `entity`):** Mapeo de objetos de transferencia de datos (JSON) y entidades JPA.
* **Configuración (`config`):** Ajustes de Beans, configuración de Base de Datos y Caché.

### Patrón Cache-Aside: Redis + PostgreSQL
Implementamos un patrón híbrido de alta disponibilidad:
1. **Caché Primaria (Redis):** Cuando se solicita el catálogo, Spring consulta primero la memoria ultra-rápida de Redis (vía `@Cacheable`). 
2. **Persistencia Local (PostgreSQL):** Ante un *cache miss* (o ejecución programada), el sistema acude a GestoPago. Una vez obtenida la data, esta no solo se envía a Redis, sino que se sincroniza físicamente en PostgreSQL. 
3. **Manejo de UUIDs:** Para evitar fallos en los upserts masivos (causados por IDs genéricos o nulos de la API proveedora), el sistema limpia la tabla (`deleteAllInBatch`) y asigna UUIDs aleatorios locales a los 900+ productos antes de ejecutar un `saveAll()`, garantizando la consistencia relacional.

---

## 3. Configuración y Manejo de Credenciales

Toda la configuración sensible, URLs de endpoints, IDs de distribuidor, códigos de dispositivo y cron jobs se externalizaron en `application.properties`.
* **Protección de Secretos:** Ningún *password* o *token* se encuentra embebido en el código fuente de Java.
* **Manejo de Token en Memoria:** El *Bearer Token* es consumido desde GestoPago y almacenado temporalmente en Base de Datos y Redis. El servicio intercepta este token en memoria y lo inyecta limpiamente en las cabeceras HTTP de OpenFeign, protegiéndolo de exposición accidental.

---

## 4. Manejo de Excepciones y Resiliencia

El backend está preparado para fallos de red y caídas del proveedor externo:

* **ErrorDecoder Feign (`GestoPagoErrorDecoder`):** Intercepta respuestas HTTP (400, 401, 403, 404, 500, 503) desde GestoPago y las traduce en excepciones controladas.
* **GlobalExceptionHandler:** Utilizando `@RestControllerAdvice`, el sistema captura las fallas de comunicación, *Timeouts* de conexión y errores de autenticación, traduciéndolas en respuestas JSON homogéneas (`GenericResponse`). Nunca se exponen *Stack Traces* al usuario final.
* **Tolerancia a Fallos en Schedulers:** El job de sincronización nocturna envuelve su llamado en bloques *try-catch* para garantizar que un fallo en la API externa no congele o destruya el hilo principal de tareas de Spring Boot.

---

## 5. Estrategia de Logging y Monitoreo

La aplicación implementa SLF4J con estrategias claras de trazabilidad:
* **Trazabilidad de inicio a fin:** Se emiten logs (`log.info`) al arrancar una petición a GestoPago y al finalizarla, midiendo la cantidad de productos recuperados y persistidos.
* **Protección de Datos Sensibles:** La impresión del *Bearer Token* se ha omitido explícitamente en todos los niveles de log (incluso en nivel `DEBUG` de Feign) para cumplir con normativas de seguridad de la información.
* **Diagnóstico de Errores:** Excepciones externas generan trazas de error (`log.error`) detallando el código HTTP o causa de la falla para facilitar la tarea del equipo de soporte.

---

## 6. Decisiones Técnicas Destacadas

* **Lenguaje y Framework:** Construido sobre **Java 17/21** y **Spring Boot 3.3.6** para asegurar soporte a largo plazo (LTS) y acceso a APIs modernas (Records, Switch Expressions, etc).
* **Serialización Avanzada en Redis:** Configuración de un `ObjectMapper` personalizado para la caché. 
  * Se registró el módulo **`JavaTimeModule`** para habilitar la serialización nativa de fechas Java 8 (`LocalDateTime`).
  * Se deshabilitó `WRITE_DATES_AS_TIMESTAMPS` para guardar fechas en formato ISO-8601, más fáciles de leer al depurar Redis.
* **Integrity SaveAll Fix:** Implementación de `UUID.randomUUID()` para las llaves primarias de productos de GestoPago, lo que resolvió definitivamente el problema de sobrescritura de Hibernate/Spring Data JPA al procesar *arrays* JSON con identificadores inválidos.
