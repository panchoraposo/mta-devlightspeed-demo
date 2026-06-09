## Objetivo

Nueva versión de la demo:

1) Tener 1–2 **apps legacy corriendo en VMs** (estado “as-is”).
2) Modernizarlas con **MTA + Lightspeed** (en el IDE, sobre una copia reproducible).
3) Desplegar la versión modernizada en **OpenShift**.

## Actores / ambientes

- **Laptop** (tu IDE + MTA/Lightspeed): donde corrés análisis y aplicás fixes.
- **VM EAP 7 (RHEL 8, sin contenedores)**: corre legacy Java EE (WAR) en JBoss EAP 7.
- **VM Inventory (RHEL 8, sin contenedores)**: corre legacy Spring Boot 2 (JAR) con systemd.
- **OpenShift**: corre la(s) app(s) modernizada(s) (containers).

## Preparación (una sola vez)

1) Configura las VMs en `demo/vm/.env`:

```bash
cp demo/vm/.env.example demo/vm/.env
```

2) Verifica el acceso SSH a las VMs:

```bash
ssh -i "$VM_SSH_KEY" "$VM_USER@$VM_EAP7_HOST" 'uname -a'
ssh -i "$VM_SSH_KEY" "$VM_USER@$VM_INVENTORY_HOST" 'uname -a'
```

3) Descarga el ZIP de **JBoss EAP 7** en tu laptop y configura `EAP7_ZIP_PATH` en `demo/vm/.env`.
4) En ambas VMs, asegúrate de tener `sudo` (para instalar Java, systemd y firewall).

## Paso 1: Crear un run reproducible (siempre)

```bash
./demo/new-run.sh
```

Toma nota del `run_dir` que imprime (por ejemplo `demo/runs/20260420T185828`).

> Si también quieres tener una copia reproducible de la app modernizada (Quarkus),
> puedes usar:
>
> ```bash
> ./demo/new-run-dual.sh
> ```
>
> Esto crea dos árboles dentro del run:
> - Legacy: `demo/runs/<timestamp>/apps/`
> - Modern: `demo/runs/<timestamp>/modern-apps/`

## Paso 2: Levantar legacy en VMs (desde tu laptop)

```bash
./demo/vm/rhel8/provision-eap7.sh
./demo/vm/rhel8/deploy-eap7-payments.sh demo/runs/<timestamp>

./demo/vm/rhel8/provision-sb2.sh
./demo/vm/rhel8/deploy-sb2-inventory.sh demo/runs/<timestamp>
```

### Validación rápida (desde tu laptop)

- Trading (WildFly):
  - `POST http://<VM_EAP7_HOST>:8080/fsi-payments/api/payments`
  - `GET  http://<VM_EAP7_HOST>:8080/fsi-payments/api/payments/<paymentId>`
- Inventory (Spring Boot 2):
  - `GET http://<VM_INVENTORY_HOST>:8082/api/v1/inventory/products`

## Paso 3: Modernización con MTA + Lightspeed (en el IDE)

Abre el código **del run** (no `demo/source-apps`):

- `demo/runs/<timestamp>/apps/fsi-payments-eap7-legacy`
- `demo/runs/<timestamp>/apps/fsi-inventory-springboot2-legacy`

Recomendación de targets para maximizar issues:

- Trading:
  - `quarkus` (o `quarkus3+`) + `openjdk17`
- Inventory:
  - `spring-boot` (o `spring-boot3+`) + `openjdk17`

Elige 3–6 issues “mandatory/high effort” y pide fixes. Vuelve a ejecutar el análisis.

## Paso 4: Build + deploy en OpenShift (modernizada)

Estrategia de demo recomendada:

- Mantener el resultado modernizado en un directorio separado de las apps legacy.
  Opciones recomendadas:
  - `migrated-apps/<proyecto-modern>/` (directorio estable en el repo)
  - `demo/runs/<timestamp>/modern-apps/<proyecto-modern>/` (copia reproducible por demo run)
- Deploy en OpenShift con `oc new-build` usando `--context-dir=migrated-apps/<nuevo-proyecto>`.

Ejemplo (placeholder):

```bash
oc new-project mta-lightspeed-demo

# app modernizada (reemplaza <GIT_URI> y <CONTEXT_DIR>)
oc new-build --name trading-modern --strategy=docker <GIT_URI> --context-dir=<CONTEXT_DIR>
oc new-app trading-modern
oc expose svc/trading-modern
```

## Cierre de demo (comparación)

- “As-is” en VMs: endpoints legacy (WildFly / Boot2)
- “To-be” en OpenShift: endpoints modernizados + artefactos de MTA (issues resueltos)

