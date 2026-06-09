## Objetivo

Repetir la demo sin que MTA pise tus cambios, y forzar una **migración “más compleja”** (muchos incidentes) por app.

Este repo mantiene:

- **Golden source (no tocar)**: `demo/source-apps/`
- **Cada demo en una copia fresca**: `demo/runs/<timestamp>/apps/`
- **Salidas de MTA aisladas**: `demo/runs/<timestamp>/mta-output/`

## Preparar un run (siempre)

```bash
./demo/new-run.sh
```

El script te imprime el path del run creado. A partir de ahí, trabaja siempre con:

- Inputs: `demo/runs/<timestamp>/apps/<app>/`
- Output: `demo/runs/<timestamp>/mta-output/<app>/`

## Análisis recomendado (VS Code Extension)

En la extensión:

- Abre el proyecto que quieres analizar desde el run (por ejemplo `demo/runs/<timestamp>/apps/fsi-trading-eap6-legacy`).
- Ejecuta el análisis apuntando al target que corresponda (Quarkus/Camel4/EAP8/Spring Boot 3/OpenJDK 17).
- Cuando aparezcan issues, elige 3–5 de los “mandatory” o “high effort” y solicita fixes con Lightspeed.
- Vuelve a ejecutar el análisis.

> Tip práctico: mantén el output fuera del input (usando el output del run) para que no te llene el proyecto con artefactos de análisis.

## Guión sugerido por app (para maximizar incidentes)

### 1) `fsi-trading-eap6-legacy` (EAP6 → Quarkus 3 / Hibernate 6)

**Input**: `demo/runs/<timestamp>/apps/fsi-trading-eap6-legacy/`

- **Target recomendado**: `quarkus` (o `quarkus3+` si está disponible) + `openjdk17`
- **Qué debería saltar**:
  - EJB `@Stateless`, CMT (`@TransactionAttribute`)
  - `persistence.xml` + propiedades `hibernate.*` legacy (renames/removals)
  - Hibernate legacy API (Criteria/`Query.iterate`)
  - JNDI (`InitialContext`, `lookup`)
  - JAX-WS (`javax.xml.ws`) / SOAP client

**Revisa primero**:

- Reemplazo de EJB por CDI + `@Transactional`
- Eliminación de JNDI (`@Inject` / configuración)
- Sustitución de Criteria/`iterate` por JPA Criteria/JPQL moderno

### 2) `fsi-payments-eap7-legacy` (EAP7 → Quarkus)

**Input**: `demo/runs/<timestamp>/apps/fsi-payments-eap7-legacy/`

- **Target recomendado**: `quarkus` + `openjdk17`
- **Qué debería saltar**:
  - JMS (`javax.jms.*`) + `@MessageDriven` (no soportado en Quarkus)
  - JNDI (`InitialContext`)
  - JPA `@PersistenceContext` + `persistence.xml`
  - Hibernate legacy bits (`@Type(type="...")`, Criteria)

**Revisa primero**:

- Migrar JMS/MDB a SmallRye Reactive Messaging
- Reemplazar JNDI con inyección/config
- Ajustar transacciones (Quarkus exige límites explícitos)

### 3) `fsi-integration-fuse-camel2-legacy` (Camel 2 → Camel 4)

**Input**: `demo/runs/<timestamp>/apps/fsi-integration-fuse-camel2-legacy/`

- **Target recomendado**: `camel4+` (o `camel4`) + opcional `quarkus`
- **Qué debería saltar**:
  - `camel-http4` (componente legacy)
  - Componentes removidos en Camel 4 (rest-swagger, swagger-java, xstream, zipkin, etc.)
  - Spring XML DSL legacy

**Revisa primero**:

- Removals de componentes Camel 4 (reemplazos sugeridos)
- Modernizar rutas / dependencias

### 4) `fsi-risk-eap5-legacy` (Servlet 2.5 / JAXB / legacy config → targets modernos)

**Input**: `demo/runs/<timestamp>/apps/fsi-risk-eap5-legacy/`

- **Target recomendado**: `jakarta-ee9` (o `eap8+`) + `openjdk17` + opcional `quarkus`
- **Qué debería saltar**:
  - Servlet 2.5 / `web.xml` legacy
  - JAXB (`javax.xml.bind.DatatypeConverter`)
  - JNDI (`InitialContext`)
  - JAX-WS (`javax.xml.ws`) / SOAP client
  - Log4j 1.x (si corrés también targets que lo marquen)

**Revisa primero**:

- `javax.*` → `jakarta.*` (si vas a EAP8/Boot3)
- Eliminar JAXB DatatypeConverter (java.time / libs)
- Externalizar config (sin `/etc/...` y system properties)

### 5) `fsi-inventory-springboot2-legacy` (Spring Boot 2 → Spring Boot 3)

**Input**: `demo/runs/<timestamp>/apps/fsi-inventory-springboot2-legacy/`

- **Target recomendado**: `spring-boot` (o `spring-boot3+`) + `openjdk17`
- **Qué debería saltar**:
  - `javax.validation` / `javax.persistence` / `javax.annotation` (Jakarta switch)
  - `WebSecurityConfigurerAdapter` (deprecado; cambio a `SecurityFilterChain`)
  - Springfox Swagger 2 (pain típico al ir a Boot 3)
  - `RestTemplate` legacy (hacia `WebClient`)

**Revisa primero**:

- Migration `javax.*` → `jakarta.*`
- Reescritura de security config (sin adapter)
- Reemplazo de Springfox por springdoc-openapi

## CLI (opcional, si quieres output 100% controlado)

Usa el binario de MTA CLI que tengas disponible en tu máquina (por ejemplo, el que descargues desde la distribución oficial).

Ejemplo (ajusta targets a tu demo):

```bash
<path-to-mta-cli> analyze \
  --input demo/runs/<timestamp>/apps/fsi-trading-eap6-legacy \
  --output demo/runs/<timestamp>/mta-output/fsi-trading-eap6-legacy \
  --target quarkus \
  --target openjdk17
```

