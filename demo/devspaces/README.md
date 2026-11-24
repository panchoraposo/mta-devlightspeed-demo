# Dev Spaces + Developer Lightspeed (WAS9 → Quarkus)

Use OpenShift Dev Spaces instead of local VS Code to analyze the WAS9/Liberty FSI apps and apply Developer Lightspeed fixes toward Red Hat Build of Quarkus 3.

## Open the workspace

Point Dev Spaces at this repository and use the Devfile:

- Path: [`demo/devspaces/.devfile.yaml`](.devfile.yaml)

Factory-style URL (adjust host + git URL):

```text
https://devspaces.<cluster>/#https://<git-host>/<org>/mta-devlightspeed-demo?devfilePath=demo/devspaces/.devfile.yaml
```

Or create a workspace from the Dev Spaces UI and select this Devfile path.

## One-time setup inside the workspace

1. **MTA CLI (Linux)** — the Devfile `postStart` runs `scripts/install-mta-cli.sh`.
   - Default installs a Linux analyzer binary at `/home/user/mta/linux-mta-cli`.
   - For the full Red Hat MTA CLI archive, set env `MTA_CLI_URL` on the Devfile/container to your downloadable tarball URL, then re-run the install command.
2. **MTA / Lightspeed extension** — accept the recommendation in `.vscode/extensions.json` (`redhat.mta-vscode-extension`).
3. **LLM provider** — Command Palette → `MTA: Open the Gen AI model provider configuration file` and configure an OpenAI-compatible endpoint (OpenShift AI / MaaS), for example:

```yaml
models:
  openshift-example-model: &active
    environment:
      OPENAI_API_KEY: "<key>"
    provider: "ChatOpenAI"
    args:
      model: "my-model"
      configuration:
        baseURL: "https://<serving>-<project>.apps.<cluster>/v1"
```

Settings already enable GenAI + agent mode and point `mta-core.analyzerPath` at `/home/user/mta/linux-mta-cli`.

## Analysis profile (WAS9 → Quarkus)

| Field | Value |
|-------|--------|
| Source | `websphere` |
| Targets | `quarkus`, `openjdk17` |

Apps to open / analyze:

- `apps/fsi-accounts-was9-legacy`
- `apps/fsi-cards-was9-legacy`
- `apps/fsi-kyc-was9-legacy`

For a disposable copy (same idea as the laptop `./demo/new-run.sh` flow), copy `demo/source-apps/*-was9-legacy` into a run folder before applying Lightspeed patches.

## After migration

- Reference target: [`migrated-apps/fsi-accounts-quarkus3`](../../migrated-apps/fsi-accounts-quarkus3)
- Stubs: `migrated-apps/fsi-cards-quarkus3`, `migrated-apps/fsi-kyc-quarkus3`
- Post-onboarding Quarkus RHADS / Dev Spaces for *already migrated* apps remains under [`software-templates/import-quarkus`](../../software-templates/import-quarkus) (RHDA + gitsign). This Devfile is for the **MTA analysis** step.
