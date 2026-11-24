# Import existing Quarkus app

Template de Developer Hub para onboardear un repositorio Quarkus que ya existe al circuito RHADS (el mismo que **Agentic app — Quarkus MCP**).

En esta demo el origen es `fsi-payments`:

`https://github.com/panchoraposo/mta-devlightspeed-demo/tree/main/migrated-apps/fsi-payments-quarkus3`

El formulario deja ese URL y el nombre `fsi-payments` por defecto. Para otra app, cambiá el nombre y la URL.

El catálogo del cluster lee GitLab (`platform-engineers/rhads-platform`), no GitHub. Por eso registrar el `template.yaml` de GitHub devuelve `NotFoundError`.

## Note: MTA WAS9 analysis

For **MTA + Developer Lightspeed** on WAS9/Liberty legacy apps in Dev Spaces, use `demo/devspaces/` (not this Quarkus import template).
