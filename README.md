# Hello World - Multi-Stage CI Pipeline

Maven project with a GitHub Actions pipeline:
Build -> Unit Test -> Package -> Deploy to Dev -> Deploy to Production.

## Run locally
    mvn test
    mvn package
    java -jar target/hello-world.jar Gautam

## One-time GitHub setup
Settings -> Environments: create `dev` and `production`.
On `production`, add yourself under "Required reviewers" for a manual approval gate.

## Triggers
- Pull request (any branch): Build, Test, Package (no deploy)
- Push to any branch: Build, Test, Package, Deploy to Dev
- Tag `v*` (e.g. `git tag v1.0.0 && git push origin v1.0.0`): all five stages, production waits for approval
