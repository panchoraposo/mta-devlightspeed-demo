## Objetivo

Levantar 1–2 apps **legacy corriendo en VMs** (estado “as-is”), modernizarlas con **MTA + Lightspeed**, y luego desplegar la versión modernizada en **OpenShift**.

Este directorio no pretende ser “infra perfecta”; está pensado para **demo reproducible**.

## Convención de demo (anti-pisadas)

En tu laptop, siempre crea un run y trabaja desde ahí:

```bash
./demo/new-run.sh
```

Y usa como input las apps del run: `demo/runs/<timestamp>/apps/...`

Si quieres reproducir la demo con **legacy + modern** en paralelo, usa:

```bash
./demo/new-run-dual.sh
```

Ese script deja:

- Legacy: `demo/runs/<timestamp>/apps/`
- Modern (Quarkus): `demo/runs/<timestamp>/modern-apps/`

## Prerrequisitos en las VMs

Dos VMs (o una sola si quieres simplificar):

- **VM EAP 7 (RHEL 8, sin contenedores)**: Java 11 + systemd + unzip.
  - Requiere que descargues el ZIP de **JBoss EAP 7** desde el Customer Portal (lo subimos por SCP).
- **VM Inventory (Spring Boot 2, sin contenedores)**: Java 11 + systemd.

> Si prefieres, también puedes ejecutar Spring Boot 2 en contenedor, pero para una demo de “VM legacy” suele lucir mejor con `systemd`.

## Configuración local

1) Copia el ejemplo y completa hosts/usuario/key:

```bash
cp demo/vm/.env.example demo/vm/.env
```

2) Ajusta `VM_TRADING_HOST` y `VM_INVENTORY_HOST`.

## Flujo rápido (lo que vas a hacer en la demo)

1) Crear run: `./demo/new-run.sh`
2) Publicar artifacts a VMs (WAR/JAR) y arrancar servicios:
   - Provision:
     - `./demo/vm/rhel8/provision-eap7.sh`
     - `./demo/vm/rhel8/provision-postgres.sh`
     - `./demo/vm/rhel8/provision-sb2.sh`
   - Deploy:
     - `./demo/vm/rhel8/deploy-eap7-payments.sh <run_dir>`
     - `./demo/vm/rhel8/deploy-sb2-inventory.sh <run_dir>`
3) Mostrar que las legacy apps corren en VM (curl / navegador).
4) Modernizar con MTA+Lightspeed desde el run (código fuente).
5) Build + deploy en OpenShift usando los proyectos modernizados (idealmente `migrated-apps/` o un nuevo `demo/runs/.../modern/`).

