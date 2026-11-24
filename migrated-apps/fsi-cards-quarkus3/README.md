# fsi-cards-quarkus3 (stub)

Placeholder for the Lightspeed migration of `fsi-cards-was9-legacy`.

Suggested target shape (mirror `fsi-accounts-quarkus3` / `fsi-payments-quarkus3`):

- Replace JMS/MDB fraud processing with a Quarkus scheduler, reactive messaging, or an in-process async worker
- `javax.*` → `jakarta.*`, CDI instead of EJB
- Datasource via `application.properties` (no `jdbc/CardsDS` JNDI)
- Drop `ibm-web-*` descriptors

MTA profile: source `websphere`, targets `quarkus` + `openjdk17`.
