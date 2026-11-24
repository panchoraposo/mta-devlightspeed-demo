# Demo: MTA + Developer Lightspeed (apps ficticias)

Este repo contiene un set de **aplicaciones Java “legacy” (ficticias)** diseñadas para generar **muchos hallazgos típicos** de modernización y poder demostrar:

- **MTA (Migration Toolkit for Applications)**: análisis estático, issues y guías de migración.
- **Developer Lightspeed for MTA**: propuestas de cambios asistidas por GenAI en el IDE (VS Code local u **OpenShift Dev Spaces**).
- **OpenShift** (opcional): despliegue de apps legacy/migradas para comparar.

Producto: [Developer Lightspeed for MTA](https://developers.redhat.com/products/mta/developer-lightspeed)  
Casos/migration paths: [Use cases and migration paths for MTA](https://developers.redhat.com/products/mta/use-cases)

## Apps incluidas (para análisis)

Estas apps están pensadas para audiencias con cargas reales en **JBoss EAP 5/6/7**, **WebSphere / Open Liberty (WAS9 track)**, y **Red Hat Fuse (Camel 2)**, y para demostrar distintos niveles de complejidad.

### Legacy (para análisis)

- **EAP 5-era (Servlet 2.5 + JAXB)**: `apps/fsi-risk-eap5-legacy/`
- **EAP 6 (EJB + JPA + JAX-RS)**: `apps/fsi-trading-eap6-legacy/`
- **EAP 7 (JAX-RS + JMS)**: `apps/fsi-payments-eap7-legacy/`
- **WAS9 / Open Liberty — Accounts (REST + EJB + JPA)**: `apps/fsi-accounts-was9-legacy/`
- **WAS9 / Open Liberty — Cards/Fraud (REST + JMS/MDB)**: `apps/fsi-cards-was9-legacy/`
- **WAS9 / Open Liberty — KYC (JAX-WS + JAXB)**: `apps/fsi-kyc-was9-legacy/`
- **Fuse/Camel 2 (Spring XML DSL)**: `apps/fsi-integration-fuse-camel2-legacy/`
  - **Nota**: por defecto compila con **Camel 2 upstream** para que la demo funcione sin repos privados. Si quieres que sea **Red Hat Fuse real**, activa el perfil Maven `redhat-fuse` (requiere acceso a `maven.repository.redhat.com`).

Además, hay una app **Spring Boot 2** para demos de `spring-boot`/`openjdk`/`jakarta`:

- **Spring Boot 2.7 (Web + Security + JPA + Flyway + Swagger 2)**: `apps/fsi-inventory-springboot2-legacy/`

### Migrated targets (Quarkus 3)

- `migrated-apps/fsi-payments-quarkus3/` (desde EAP7 payments)
- `migrated-apps/fsi-accounts-quarkus3/` (desde WAS9 accounts)
- Stubs: `migrated-apps/fsi-cards-quarkus3/`, `migrated-apps/fsi-kyc-quarkus3/`

> Tip: para una demo de MTA+Lightspeed sobre WAS9, usa source `websphere` y targets `quarkus` + `openjdk17` sobre `apps/fsi-*-was9-legacy` (o una copia en `demo/runs/...`).

## Build local rápido

```bash
mvn -q -f apps/fsi-risk-eap5-legacy/pom.xml package
mvn -q -f apps/fsi-trading-eap6-legacy/pom.xml package
mvn -q -f apps/fsi-payments-eap7-legacy/pom.xml package
mvn -q -f apps/fsi-accounts-was9-legacy/pom.xml package
mvn -q -f apps/fsi-cards-was9-legacy/pom.xml package
mvn -q -f apps/fsi-kyc-was9-legacy/pom.xml package
mvn -q -f apps/fsi-integration-fuse-camel2-legacy/pom.xml package

mvn -q -f apps/fsi-inventory-springboot2-legacy/pom.xml test
```

## Qué “incidentes” intencionales trae el set

Esto no pretende ser “buenas prácticas”; está armado para que **salten hallazgos** y tengas material para corregir con Lightspeed.

- **`javax.*` por todos lados**: Servlets/EJB/JPA/JAX-RS/JMS/JAX-WS/JAXB.
- **APIs removidas del JDK** (al saltar a 11/17): JAXB/JAX‑WS como dependencias explícitas.
- **Crypto débil / tokens inseguros**: `MD5`, `Random`, base64.
- **Configuración legacy**: archivos en `/etc/...`, system properties, credenciales hardcodeadas.
- **Riesgos de seguridad**: SQL/JPQL armados con concatenación; endpoints “admin” con dumping.
- **Camel 2 + Spring XML DSL**: rutas y componentes legacy (`http4://`, XML DSL).
- **Spring Boot 2**: `WebSecurityConfigurerAdapter` (deprecado), `RestTemplate` sin timeouts, `Springfox Swagger 2` (pain típico hacia Boot 3).
- **WAS / Liberty**: `ibm-web-bnd.xml` / `ibm-web-ext.xml`, JNDI `jdbc/*`, JMS/MDB, SOAP handlers.

## Onboarding en Developer Hub (RHADS)

El template `software-templates/import-quarkus/` importa un repositorio Quarkus existente (en esta demo, `migrated-apps/fsi-payments-quarkus3`) y crea el mismo circuito que los templates Quarkus de [demo-rhads](https://github.com/panchoraposo/demo-rhads). En el cluster se publica en el catálogo de GitLab, no por la URL de GitHub. Detalle: `software-templates/import-quarkus/README.md`.

## Deploy en OpenShift (recomendado para demo)

En general, la forma más directa es:

```bash
oc new-project mta-lightspeed-demo

# Build desde Git (reemplaza <GIT_URI> por tu repo)
oc new-build --name orders-jee8-legacy --strategy=docker <GIT_URI> --context-dir=apps/orders-jee8-legacy
oc new-app orders-jee8-legacy
oc expose svc/orders-jee8-legacy
```

Repite el patrón para cada app (legacy y target) usando su `context-dir`.

## Guión de demo repetible (sin que MTA pise cambios)

Ver `demo/demo-script-es.md`.

## Nueva mirada: legacy en VMs → modernización → OpenShift

Ver `demo/demo-script-vm-es.md` y `demo/vm/README.md`.

### Track WAS9 / Open Liberty + Dev Spaces

Ver `demo/demo-script-was9-devspaces-es.md` y `demo/devspaces/README.md`.

Flujo rápido:

```bash
./demo/new-run.sh
./demo/vm/rhel8/provision-liberty-was9.sh
./demo/vm/rhel8/provision-postgres-was9.sh
./demo/vm/rhel8/deploy-liberty-accounts.sh demo/runs/<timestamp>
./demo/vm/rhel8/deploy-liberty-cards.sh demo/runs/<timestamp>
./demo/vm/rhel8/deploy-liberty-kyc.sh demo/runs/<timestamp>
```

## Configurar Lightspeed con tu LLM (MaaS / OpenShift AI)

La extensión de VS Code (local o Dev Spaces) usa un archivo `provider-settings.yaml` editable desde el Command Palette:
`Open the GenAI model provider configuration file`.

La documentación oficial muestra el ejemplo para OpenShift AI (endpoint OpenAI-compatible) y cómo activar un provider usando `&active`:

- [Configuring large language models for analysis (MTA 8.0)](https://docs.redhat.com/en/documentation/migration_toolkit_for_applications/8.0/html/configuring_and_using_red_hat_developer_lightspeed_for_mta/configuring-llm_mta-developer-lightspeed)
- [Configuring and using the Visual Studio Code Extension for MTA (MTA 8.0)](https://docs.redhat.com/en/documentation/migration_toolkit_for_applications/8.0/html-single/configuring_and_using_the_visual_studio_code_extension_for_mta/index)

## (Opcional) Solution Server en el cluster

Si quieres mostrar “memoria colectiva” (solved examples / métricas), puedes habilitar el **Solution Server** (Technology Preview) configurando el `Tackle` CR y secretos (`kai-api-keys`) en `openshift-mta`.

- [Solution Server configurations (MTA 8.0)](https://docs.redhat.com/en/documentation/migration_toolkit_for_applications/8.0/html/configuring_and_using_red_hat_developer_lightspeed_for_mta/solution-server-configurations_mta-developer-lightspeed)
