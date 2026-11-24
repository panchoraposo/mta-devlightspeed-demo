# Demo: WAS9 (Open Liberty) → Dev Spaces Lightspeed → Quarkus 3

## Objetivo

1. Correr apps FSI **WAS9-style** en una VM con **Open Liberty** (estado “as-is”).
2. Modernizarlas con **MTA + Developer Lightspeed** en **OpenShift Dev Spaces** (sin depender de VS Code local).
3. Comparar con el target Quarkus 3 (`migrated-apps/fsi-accounts-quarkus3`) y desplegar en OpenShift.

## Apps

| App | Contexto | Story |
|-----|----------|--------|
| `fsi-accounts-was9-legacy` | `/fsi-accounts` | Core banking REST + EJB + JPA |
| `fsi-cards-was9-legacy` | `/fsi-cards` | Card auth + fraud JMS/MDB |
| `fsi-kyc-was9-legacy` | `/fsi-kyc` | KYC onboarding SOAP/JAX-WS |

## Preparación (una sola vez)

1. Configura `demo/vm/.env` (ver `demo/vm/.env.example`):

```bash
cp demo/vm/.env.example demo/vm/.env
# set VM_WAS9_HOST, VM_USER, VM_SSH_KEY
# optional: OPENLIBERTY_ZIP_PATH or let provision download Open Liberty
```

2. Verifica SSH:

```bash
ssh -i "$VM_SSH_KEY" "$VM_USER@$VM_WAS9_HOST" 'uname -a'
```

3. Cluster con **OpenShift Dev Spaces** + LLM accesible (OpenShift AI / OpenAI-compatible) para Lightspeed.

## Paso 1: Run reproducible

```bash
./demo/new-run.sh
```

Anota el `run_dir` (ej. `demo/runs/20260420T185828`).

## Paso 2: Legacy en VM (Open Liberty)

```bash
./demo/vm/rhel8/provision-liberty-was9.sh
./demo/vm/rhel8/provision-postgres-was9.sh
./demo/vm/rhel8/deploy-liberty-accounts.sh demo/runs/<timestamp>
./demo/vm/rhel8/deploy-liberty-cards.sh demo/runs/<timestamp>
./demo/vm/rhel8/deploy-liberty-kyc.sh demo/runs/<timestamp>
```

Validación (puerto default `9080`):

- Accounts: `GET/POST http://<VM_WAS9_HOST>:9080/fsi-accounts/api/accounts`
- Cards: `GET/POST http://<VM_WAS9_HOST>:9080/fsi-cards/api/auths`
- KYC: `http://<VM_WAS9_HOST>:9080/fsi-kyc/KycService?wsdl`

## Paso 3: MTA + Lightspeed en Dev Spaces

1. Abre el repo en Dev Spaces con Devfile `demo/devspaces/.devfile.yaml`  
   (detalle: `demo/devspaces/README.md`).
2. Espera el bootstrap (`~/mta/linux-mta-cli` + settings MTA).
3. Instala la extensión MTA si el workspace lo pide.
4. Configura el LLM (`MTA: Open the Gen AI model provider configuration file`).
5. Perfil de análisis:
   - **Source:** `websphere`
   - **Targets:** `quarkus`, `openjdk17`
6. Analiza `apps/fsi-accounts-was9-legacy` (o la copia del run).
7. Elige 3–6 issues high/mandatory y pide fixes con Lightspeed (agent mode).

Repite (o en paralelo) con cards / KYC según tiempo de demo.

## Paso 4: Target Quarkus + OpenShift

- Referencia ya migrada: `migrated-apps/fsi-accounts-quarkus3`
- Build local: `./mvnw -f migrated-apps/fsi-accounts-quarkus3 quarkus:dev` (puerto **8088**)
- Deploy OpenShift:

```bash
oc new-project mta-lightspeed-demo
oc new-build --name accounts-quarkus3 --strategy=docker <GIT_URI> \
  --context-dir=migrated-apps/fsi-accounts-quarkus3
oc new-app accounts-quarkus3
oc expose svc/accounts-quarkus3
```

## Cierre

- **As-is:** Liberty VM (`:9080/fsi-*`) + descriptores `ibm-web-*`
- **To-be:** Quarkus 3 en OpenShift + issues MTA resueltos vía Lightspeed en Dev Spaces
