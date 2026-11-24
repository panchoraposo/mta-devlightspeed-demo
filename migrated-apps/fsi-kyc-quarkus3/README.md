# fsi-kyc-quarkus3 (stub)

Placeholder for the Lightspeed migration of `fsi-kyc-was9-legacy`.

Suggested target shape:

- Replace JAX-WS SOAP with Quarkus REST (or Quarkus CXF only if SOAP must stay)
- Move `/etc/fsi/kyc/scoring.properties` + system properties to MicroProfile Config
- Remove MD5 audit handler; use a proper hashing / audit approach
- Drop `ibm-web-*` descriptors

MTA profile: source `websphere`, targets `quarkus` + `openjdk17`.
