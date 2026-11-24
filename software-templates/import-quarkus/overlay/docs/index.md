# ${{values.component_id}}

${{values.description}}

Existing Quarkus application onboarded to RHADS.
Application CI/CD (build, sign, SBOM, ACS, Conforma) runs on **OpenShift Pipelines**.
GitLab CI on this repository only builds TechDocs for Developer Hub.

## Demo path

1. Open the component in OpenShift Dev Spaces. Run Red Hat Dependency Analytics on `pom.xml`, then **Configure Sigstore git commit signing**.
2. Signed `git commit` / `git push` starts the build pipeline (Nexus, OpenShift Builds, cosign, TPA, Conforma).
3. GitLab **tag** promotes to staging; **release** promotes to production.

The workload is listed in Developer Hub Topology for `{app}-dev`, `{app}-staging`, and `{app}-prod`.
