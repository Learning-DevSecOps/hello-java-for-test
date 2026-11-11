# Adding a new statement here

# Hello World - Multi-Stage CI/CD Pipeline

A small Java project used to demonstrate a complete CI/CD pipeline with GitHub Actions and Maven.
The pipeline compiles the code, runs unit tests, packages a JAR, and deploys it through a dev and a production environment.

## Pipeline at a glance

```
Build  ->  Unit Test  ->  Package  ->  Deploy to Dev  ->  Deploy to Production
 (1)         (2)           (3)            (4)                   (5)
```

Each stage is a separate job and starts only if the previous stage passed. If any stage fails, every later stage is skipped.

## Project structure

```
hello-java-ci/
├── .github/workflows/ci.yml                  Pipeline definition
├── pom.xml                                   Maven build configuration
├── src/main/java/com/example/HelloWorld.java Application code
└── src/test/java/com/example/HelloWorldTest.java  Unit tests
```

## When the pipeline runs

| Event | Stages that run |
|---|---|
| Push to any branch | 1, 2, 3, 4 |
| Pull request | 1, 2, 3 (no deployment) |
| Push of a tag starting with `v` (for example `v1.0.0`) | 1, 2, 3, 4, 5 (production waits for approval) |
| Manual run from the Actions tab (`workflow_dispatch`) | 1, 2, 3, 4 |

## Stage details

### Stage 1: Build

- **Purpose:** confirm that the code compiles. This is the fastest check, so it runs first and stops the pipeline early if there is a syntax error or a missing dependency.
- **What it does:** checks out the repository, installs JDK 17 (Temurin) with the Maven dependency cache, then runs `mvn -B -ntp compile`.
- **Output:** compiled classes in `target/classes` on the runner. Nothing is saved after the job, because each job runs on a fresh machine.
- **Fails when:** the Java code has a compile error, or a dependency in `pom.xml` cannot be resolved.

### Stage 2: Unit Test

- **Purpose:** confirm the code behaves correctly. This is the quality gate. Code that does not pass the tests never reaches packaging or deployment.
- **What it does:** runs `mvn -B -ntp test`. Maven's Surefire plugin finds the JUnit 5 tests under `src/test/java` and runs them. `HelloWorldTest` has three tests: greeting with a null name, with a blank name, and with a name that has extra spaces.
- **Output:** test reports in `target/surefire-reports/`, uploaded as the artifact `test-reports` (kept 14 days). The upload step uses `if: always()`, so the reports are saved even when a test fails, which makes failures easier to diagnose.
- **Fails when:** any assertion fails or a test throws an error.
- **Note:** if no tests exist, Maven still passes with "No tests to run", so keep tests in `src/test/java`.

### Stage 3: Package

- **Purpose:** build the deliverable, the runnable JAR, once. The same JAR is then used for every deployment, so what you tested is what you deploy.
- **What it does:** runs `mvn -B -ntp package -DskipTests`. The tests are skipped here because stage 2 already ran them. The Maven JAR plugin writes the `Main-Class` entry into the JAR manifest so it can be run with `java -jar`.
- **Output:** `target/hello-world.jar`, uploaded as the artifact `hello-world-jar` (kept 30 days). The step fails if the JAR is missing (`if-no-files-found: error`).
- **Fails when:** packaging errors occur or the JAR file is not produced.

### Stage 4: Deploy to Dev

- **Purpose:** release the build to the development environment and check that it actually starts.
- **What it does:** downloads the `hello-world-jar` artifact from stage 3, prints a deployment message that includes the branch name, and runs `java -jar hello-world.jar Dev` as a smoke test. The deployment itself is simulated. In a real project this is where you would copy the JAR to a server or push a container image.
- **Runs when:** the event is not a pull request, so unreviewed pull request code is never deployed.
- **Environment:** `dev`, which you can see under Settings > Environments and in the run summary.
- **Fails when:** the artifact is missing or the JAR does not start.

### Stage 5: Deploy to Production

- **Purpose:** release to production, and publish a downloadable release.
- **What it does:** downloads the same JAR, runs a smoke test with `java -jar hello-world.jar Production`, then creates a GitHub Release for the tag with the JAR attached and auto-generated release notes.
- **Runs when:** the pushed ref is a tag starting with `v`, and stage 4 succeeded.
- **Environment:** `production`. If you add yourself under Required reviewers, the job pauses until you approve it, which gives you a manual approval gate.
- **Permissions:** this is the only job with `contents: write`, which it needs to create the release. All other jobs have read-only access.
- **Fails when:** the approval is rejected, the tag already has a release, or the smoke test fails.

## Artifacts produced

| Artifact | Created in | Contains | Kept for |
|---|---|---|---|
| `test-reports` | Stage 2 | Surefire test result files | 14 days |
| `hello-world-jar` | Stage 3 | `hello-world.jar` | 30 days |

To download one, open the run in the Actions tab and scroll to the Artifacts section. GitHub downloads artifacts as a zip file.

## One-time GitHub setup

1. Push the project so that `.github/workflows/ci.yml` is at the repository root.
2. Go to Settings > Environments and create two environments: `dev` and `production`.
3. On `production`, add yourself under Required reviewers.

## Run it locally

```
mvn test
mvn package
java -jar target/hello-world.jar Gautam
```

## Create a production release

```
git tag v1.0.0
git push origin v1.0.0
```

Approve the `production` environment when the run reaches stage 5.

## Experiments to try

| Experiment | Expected result |
|---|---|
| Change an assertion in `HelloWorldTest` so it fails | Pipeline stops at stage 2; stages 3 to 5 are skipped; test reports are still uploaded |
| Add a syntax error to `HelloWorld.java` | Pipeline stops at stage 1 |
| Open a pull request | Stages 1 to 3 run; no deployment |
| Push to a new branch | Stages 1 to 4 run |
| Push a `v*` tag | All five stages run; stage 5 waits for approval |

## Notes

- Each job installs Java again because every job runs on a separate, fresh runner. Files move between jobs only through artifacts.
- The runner is pinned to `ubuntu-24.04` so the pipeline does not change when GitHub updates the `ubuntu-latest` label.
- Stages 4 and 5 simulate deployment. Replace the smoke test steps with real deployment commands when you move beyond the demo.
