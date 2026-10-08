# QuickShop - GitHub Actions CI/CD Pipeline

![Java](https://img.shields.io/badge/Java-17-blue)
![Maven](https://img.shields.io/badge/Maven-Build-red)
![GitHub Actions](https://img.shields.io/badge/GitHub%20Actions-CI%2FCD-black)
![Docker](https://img.shields.io/badge/Docker-Container-blue)
![Trivy](https://img.shields.io/badge/Trivy-Security%20Scan-purple)

## Project Overview

QuickShop is a small Java application used to demonstrate a focused, end-to-end CI/CD pipeline with GitHub Actions.

The application itself is intentionally simple. The purpose of the project is not Java application development; the purpose is to demonstrate how a DevOps engineer can automate source-code checkout, Java setup, Maven build and testing, JAR packaging, Docker image creation, container security scanning, secure Docker Hub authentication, image tagging, and image publishing.

The completed pipeline automatically runs whenever code is pushed to the `main` branch or a pull request targets `main`.

## What This Project Achieves

```text
Developer
   |
   | git push
   v
GitHub Repository
   |
   v
GitHub Actions
   |
   +-- Checkout source code
   +-- Set up Java 17
   +-- Maven clean/package
   +-- Run unit tests
   +-- Create JAR artifact
   +-- Build Docker image
   +-- Scan image with Trivy
   +-- Authenticate to Docker Hub
   +-- Tag Docker image
   +-- Push approved image
           |
           v
       Docker Hub
           |
           v
 bustopsy777/quickshop
   +-- latest
   +-- <Git commit SHA>
```

## CI vs. CD in This Project

### Continuous Integration (CI)

The CI portion validates each change before publication:

```text
Checkout -> Java setup -> Maven build/test -> JAR -> Docker build -> Trivy scan
```

If the build, unit tests, Docker build, or configured Trivy security gate fails, the workflow fails and the publishing steps do not proceed.

### Continuous Delivery (CD)

For the scope of this project, CD means publishing an approved container image to Docker Hub:

```text
Trivy passes -> Docker Hub login -> Image tagging -> Docker Hub push
```

This project deliberately stops at the container registry. Kubernetes, AKS, Helm, and Argo CD are outside this project's scope.

---

# Technology Stack

| Technology | Purpose |
|---|---|
| Git | Source-control versioning |
| GitHub | Remote source-code repository |
| GitHub Actions | CI/CD automation |
| GitHub-hosted Ubuntu runner | Executes workflow jobs |
| Java 17 | Application runtime/language |
| Maven | Build, dependency management, testing, packaging |
| JUnit 5 | Unit testing |
| Docker | Container image creation |
| Trivy | Container vulnerability scanning |
| GitHub Secrets | Secure CI/CD credential storage |
| Docker Hub | Container image registry |

---

# Repository

GitHub repository:

`johnson-busuyi/QuickShop-GitHubActions`

Docker Hub image repository:

`bustopsy777/quickshop`

---

# 1. Create the Local Project

The project was created on a Windows 11 workstation using Git Bash.

```bash
cd ~
mkdir QuickShop-GitHubActions
cd QuickShop-GitHubActions
pwd
```

Example project path:

```text
/c/Users/user/QuickShop-GitHubActions
```

## Create the Maven directory structure

```bash
mkdir -p src/main/java/com/quickshop
mkdir -p src/test/java/com/quickshop
```

Verify:

```bash
find src -type d
```

Expected structure:

```text
src
src/main
src/main/java
src/main/java/com
src/main/java/com/quickshop
src/test
src/test/java
src/test/java/com
src/test/java/com/quickshop
```

### Why `src`?

`src` means source. Maven follows a standard project layout:

```text
src/main/java -> production application code
src/test/java -> unit-test code
```

### What does `mkdir -p` mean?

`-p` means `parents`. It creates all missing parent directories in the path and does not fail simply because an existing directory is already present.

---

# 2. Maven Configuration - `pom.xml`

The project's Maven configuration file is `pom.xml`.

POM means **Project Object Model**. It tells Maven information such as the project coordinates, Java version, dependencies, test framework, and build plugins.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>com.quickshop</groupId>
    <artifactId>quickshop</artifactId>
    <version>1.0.0</version>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.10.2</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.2.5</version>
            </plugin>
        </plugins>
    </build>

</project>
```

Important configuration:

```text
Project        -> quickshop
Version        -> 1.0.0
Java           -> 17
Unit tests     -> JUnit 5
Test execution -> Maven Surefire
```

---

# 3. QuickShop Java Application

File:

`src/main/java/com/quickshop/QuickShop.java`

```java
package com.quickshop;

public class QuickShop {

    public static String getWelcomeMessage() {
        return "Welcome to QuickShop";
    }

    public static String getHealthStatus() {
        return "QuickShop is healthy";
    }

    public static void main(String[] args) {
        System.out.println(getWelcomeMessage());
        System.out.println(getHealthStatus());
    }
}
```

When run, the application produces:

```text
Welcome to QuickShop
QuickShop is healthy
```

The application is intentionally small because the project focuses on CI/CD rather than application development.

---

# 4. Unit Testing

File:

`src/test/java/com/quickshop/QuickShopTest.java`

```java
package com.quickshop;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class QuickShopTest {

    @Test
    void testWelcomeMessage() {
        assertEquals("Welcome to QuickShop", QuickShop.getWelcomeMessage());
    }

    @Test
    void testHealthStatus() {
        assertEquals("QuickShop is healthy", QuickShop.getHealthStatus());
    }
}
```

Run the tests locally:

```bash
mvn test
```

Successful result:

```text
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

The tests provide an early CI quality gate. If an expected value no longer matches the application's actual behavior, Maven returns a failure and the CI workflow stops.

---

# 5. Build and Package the Application

Run:

```bash
mvn clean package
```

`clean` removes the previous Maven build output in `target/`.

`package` compiles the source, runs the tests, and packages the Java application as a JAR.

Verify the JAR:

```bash
ls -lh target/*.jar
```

Generated artifact:

```text
target/quickshop-1.0.0.jar
```

## JAR vs. artifact

A JAR is a specific Java archive format. An artifact is a broader DevOps term for an output produced by a build and intended for storage, publication, testing, or deployment.

In this project:

```text
Java source -> Maven -> quickshop-1.0.0.jar -> build artifact
```

Other examples of artifacts include WAR files, ZIP files, test reports, security reports, and container images.

---

# 6. `.gitignore`

The project uses:

```text
target/
*.log
.idea/
.vscode/
*.iml
```

The locally generated `target/` directory is intentionally excluded from Git.

The source code is committed, and GitHub Actions reproduces the JAR itself. This validates that the CI environment can build the application from source instead of relying on a developer's local build output.

---

# 7. Initialize the Local Git Repository

```bash
git init
git status
```

The initial branch was named `master`, so it was renamed to `main`:

```bash
git branch -M main
```

`-M` renames the branch and forces the rename if necessary. It is effectively the force-capable form of `-m`.

Stage the project:

```bash
git add .
git status
```

Create the initial commit:

```bash
git commit -m "Initial QuickShop Java application"
```

Verify:

```bash
git log --oneline
```

Initial commit observed during the project:

```text
f9f86cf Initial QuickShop Java application
```

---

# 8. Create and Connect the GitHub Repository

A new GitHub repository named:

`QuickShop-GitHubActions`

was created without automatically generating a README, `.gitignore`, or license.

This was intentional because the project already had an independent local Git history. Creating a README during remote repository creation would have created an additional remote commit and could have required an unnecessary merge/rebase before the first push.

Connect the local repository:

```bash
git remote add origin https://github.com/johnson-busuyi/QuickShop-GitHubActions.git
```

Verify:

```bash
git remote -v
```

Push and configure upstream tracking:

```bash
git push -u origin main
```

`-u` sets `origin/main` as the upstream branch. Future pushes can normally use simply:

```bash
git push
```

---

# 9. GitHub Actions Workflow Directory

GitHub Actions workflow YAML files are stored under:

```text
.github/workflows/
```

Create the directory:

```bash
mkdir -p .github/workflows
```

Verify:

```bash
find .github -type d
```

Result:

```text
.github
.github/workflows
```

Workflow file:

```text
.github/workflows/ci-cd.yml
```

---

# 10. Initial GitHub Actions CI Workflow

The first workflow intentionally started small:

```yaml
name: QuickShop CI/CD

on:
  push:
    branches:
      - main
  pull_request:
    branches:
      - main

jobs:
  build-and-test:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout source code
        uses: actions/checkout@v4

      - name: Set up Java 17
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'

      - name: Build and test with Maven
        run: mvn clean package
```

Commit and push:

```bash
git add .github/workflows/ci-cd.yml
git commit -m "Add GitHub Actions CI workflow"
git push
```

The push to `main` automatically triggered the workflow.

## Important GitHub Actions concepts

### `on`

Defines workflow triggers.

```yaml
on:
  push:
    branches:
      - main
```

means the workflow runs on pushes to `main`.

The workflow also runs for pull requests targeting `main`.

### `jobs`

A workflow contains one or more jobs. This project uses a job named:

```text
build-and-test
```

### `runs-on`

```yaml
runs-on: ubuntu-latest
```

instructs GitHub to provision a temporary GitHub-hosted Ubuntu runner for the job.

### `steps`

Steps execute sequentially within the job.

### `uses`

`uses:` invokes a reusable GitHub Action, for example:

```yaml
uses: actions/checkout@v4
```

### `run`

`run:` executes a shell command on the runner, for example:

```yaml
run: mvn clean package
```

## Initial workflow result

The first workflow completed successfully:

```text
Checkout source code      PASS
Set up Java 17            PASS
Build/test with Maven     PASS
```

This established the project's initial CI pipeline.

---

# 11. Dockerize QuickShop

A Dockerfile was created in the project root:

```dockerfile
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY target/quickshop-1.0.0.jar app.jar

CMD ["java", "-cp", "app.jar", "com.quickshop.QuickShop"]
```

## Dockerfile explanation

### Base image

```dockerfile
FROM eclipse-temurin:17-jre
```

Uses a Java 17 runtime image.

### Working directory

```dockerfile
WORKDIR /app
```

Sets `/app` as the working directory inside the container.

### Copy artifact

```dockerfile
COPY target/quickshop-1.0.0.jar app.jar
```

Copies the Maven-generated JAR into the container image.

### Container command

```dockerfile
CMD ["java", "-cp", "app.jar", "com.quickshop.QuickShop"]
```

Runs the QuickShop main class when the container starts.

## Build locally

```bash
docker build -t quickshop:1.0 .
```

Meaning:

```text
docker build -> build an image
-t           -> assign image name/tag
quickshop    -> image name
1.0          -> image tag
.            -> current directory is the build context
```

The local Docker build succeeded.

## Run locally

```bash
docker run --rm quickshop:1.0
```

Output:

```text
Welcome to QuickShop
QuickShop is healthy
```

`--rm` automatically removes the stopped container after execution.

The application exits normally after printing the messages because it is a command-line application rather than a persistent HTTP service.

---

# 12. Add Docker Build to GitHub Actions

The workflow was extended with:

```yaml
      - name: Build Docker image
        run: docker build -t quickshop:${{ github.sha }} .
```

`${{ github.sha }}` is the Git commit SHA that triggered the workflow.

This provides traceability between source code and the container image:

```text
Git commit -> GitHub Actions build -> Docker image tagged with commit SHA
```

---

# 13. Troubleshooting - Dockerfile Missing on the GitHub Runner

The first GitHub Actions Docker build failed with an error similar to:

```text
failed to read dockerfile:
open Dockerfile: no such file or directory
```

## Root cause

The Dockerfile had been created and tested on the Windows workstation but had not been committed to Git.

Therefore:

```text
Developer laptop -> Dockerfile exists
Git repository   -> Dockerfile not tracked
GitHub           -> Dockerfile absent
Actions runner   -> cannot find Dockerfile
```

This demonstrates an important CI/CD principle:

> A hosted CI runner can only use repository content that has been committed/pushed, downloaded/generated during the workflow, or otherwise explicitly made available to the runner.

## Investigation

Local status showed:

```text
Untracked files:
    Dockerfile
```

The workflow YAML had also been edited directly on GitHub, so the remote repository temporarily became one commit ahead of the local repository.

After fetching:

```bash
git fetch origin
git status
```

Git reported that local `main` was behind `origin/main` by one commit and could be fast-forwarded.

The remote workflow change was synchronized safely with:

```bash
git pull --ff-only origin main
```

`--ff-only` permits the pull only when Git can move the local branch directly forward without creating a merge commit.

The Dockerfile was then staged, committed, and pushed:

```bash
git add Dockerfile
git commit -m "Add Dockerfile for QuickShop container"
git push
```

The next GitHub Actions workflow succeeded, including the Docker image build.

## Lesson learned

A file working locally does not mean it is available to CI. Always verify that every required build file is tracked and present in the remote repository.

This was the project's primary real-world troubleshooting exercise.

---

# 14. Trivy Container Security Scan

After the Docker image built successfully, a security gate was added using Trivy.

Workflow step:

```yaml
      - name: Scan Docker image with Trivy
        uses: aquasecurity/trivy-action@master
        with:
          image-ref: 'quickshop:${{ github.sha }}'
          format: 'table'
          severity: 'HIGH,CRITICAL'
          exit-code: '1'
```

The scan runs after Docker build and before Docker Hub publishing.

Configured severity scope:

```text
HIGH
CRITICAL
```

The configured `exit-code: '1'` makes the scan a pipeline security gate for findings matching the action's configured behavior.

Conceptually:

```text
Docker image
     |
     v
Trivy scan
     |
     +-- acceptable -> continue
     |
     +-- blocking finding/error -> workflow fails
```

The QuickShop Trivy workflow run completed successfully.

## Production hardening note

For a learning project, `aquasecurity/trivy-action@master` was used during implementation. In a production pipeline, third-party Actions should preferably be pinned to a trusted immutable commit SHA (or at minimum a reviewed stable release tag) to reduce supply-chain risk.

---

# 15. Docker Hub Repository

A Docker Hub repository was created:

```text
bustopsy777/quickshop
```

Description:

> QuickShop Docker image built, tested, security-scanned, and published automatically using GitHub Actions CI/CD.

The Docker Hub repository acts as the registry for images that successfully pass the CI/security stages.

---

# 16. Docker Hub Access Token

A Docker Hub personal access token was created for GitHub Actions.

The Docker Hub password and token were **not** placed in source code, the workflow YAML, or this README.

This follows the principle:

```text
Credentials -> secret store -> CI/CD runtime
```

not:

```text
Credentials -> source code/YAML -> Git repository
```

---

# 17. GitHub Repository Secrets

Under the GitHub repository:

```text
Settings -> Secrets and variables -> Actions
```

two repository secrets were configured:

```text
DOCKERHUB_USERNAME
DOCKERHUB_TOKEN
```

`DOCKERHUB_USERNAME` stores the Docker Hub username.

`DOCKERHUB_TOKEN` stores the Docker Hub access token.

The workflow accesses them using:

```yaml
${{ secrets.DOCKERHUB_USERNAME }}
${{ secrets.DOCKERHUB_TOKEN }}
```

The actual token must never be committed or documented.

---

# 18. Docker Hub Login, Tagging, and Publishing

After the security scan, the workflow authenticates to Docker Hub:

```yaml
      - name: Log in to Docker Hub
        uses: docker/login-action@v3
        with:
          username: ${{ secrets.DOCKERHUB_USERNAME }}
          password: ${{ secrets.DOCKERHUB_TOKEN }}
```

The image is then tagged twice:

```yaml
      - name: Tag Docker image
        run: |
          docker tag quickshop:${{ github.sha }} bustopsy777/quickshop:${{ github.sha }}
          docker tag quickshop:${{ github.sha }} bustopsy777/quickshop:latest
```

Finally, both tags are pushed:

```yaml
      - name: Push Docker image to Docker Hub
        run: |
          docker push bustopsy777/quickshop:${{ github.sha }}
          docker push bustopsy777/quickshop:latest
```

## Why publish two tags?

### `latest`

```text
bustopsy777/quickshop:latest
```

Provides a convenient reference to the newest successfully published image.

### Git commit SHA

```text
bustopsy777/quickshop:<Git commit SHA>
```

Provides immutable-style traceability back to the exact Git commit that generated the image.

For production deployments, a commit SHA, release version, or another immutable version identifier is generally safer than relying only on `latest`.

---

# 19. Final GitHub Actions Workflow

The completed workflow is:

```yaml
name: QuickShop CI/CD

on:
  push:
    branches:
      - main
  pull_request:
    branches:
      - main

jobs:
  build-and-test:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout source code
        uses: actions/checkout@v4

      - name: Set up Java 17
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'

      - name: Build and test with Maven
        run: mvn clean package

      - name: Build Docker image
        run: docker build -t quickshop:${{ github.sha }} .

      - name: Scan Docker image with Trivy
        uses: aquasecurity/trivy-action@master
        with:
          image-ref: 'quickshop:${{ github.sha }}'
          format: 'table'
          severity: 'HIGH,CRITICAL'
          exit-code: '1'

      - name: Log in to Docker Hub
        uses: docker/login-action@v3
        with:
          username: ${{ secrets.DOCKERHUB_USERNAME }}
          password: ${{ secrets.DOCKERHUB_TOKEN }}

      - name: Tag Docker image
        run: |
          docker tag quickshop:${{ github.sha }} bustopsy777/quickshop:${{ github.sha }}
          docker tag quickshop:${{ github.sha }} bustopsy777/quickshop:latest

      - name: Push Docker image to Docker Hub
        run: |
          docker push bustopsy777/quickshop:${{ github.sha }}
          docker push bustopsy777/quickshop:latest
```

> Note: This reflects the completed learning workflow. See the improvement section below for production hardening recommendations, including avoiding registry publication from pull-request runs and pinning third-party Actions.

---

# 20. Final Pipeline Behavior

A push to `main` now performs:

```text
1. GitHub receives a commit
2. GitHub Actions workflow triggers
3. GitHub provisions an Ubuntu runner
4. Repository is checked out
5. Java 17 is configured
6. Maven cleans and builds the project
7. Unit tests execute
8. JAR artifact is created
9. Docker image is built
10. Trivy scans the image
11. Docker Hub authentication occurs using GitHub Secrets
12. Image receives a Git SHA tag
13. Image receives the latest tag
14. Both tags are pushed to Docker Hub
```

Final registry destination:

```text
bustopsy777/quickshop
```

The completed workflow turned green, and the published image/tags were verified in Docker Hub.

---

# 21. Workflow Run History and Troubleshooting Evidence

The implementation produced useful workflow history:

```text
Run 1 - PASS
Initial GitHub Actions CI workflow
Build and unit tests succeeded

Run 2 - FAIL
Docker build introduced
Failure: Dockerfile missing from GitHub repository

Run 3 - PASS
Dockerfile committed and pushed
Maven + Docker build succeeded

Run 4 - PASS
Trivy security scan added
Security stage succeeded

Final run - PASS
Docker Hub authentication, tagging, and push succeeded
Image verified in Docker Hub
```

A failed pipeline is not automatically a project failure. In DevOps work, the important skill is being able to inspect logs, identify the failing stage, determine root cause, apply a controlled fix, and verify the next run.

---

# 22. GitHub-Hosted Runner

This project uses:

```yaml
runs-on: ubuntu-latest
```

GitHub provisions a temporary Ubuntu environment for each job. The runner performs the work and is discarded after the job completes.

Advantages for this project include:

- no VM provisioning required;
- no runner operating-system maintenance;
- quick setup;
- isolated/ephemeral job environments;
- ideal for learning GitHub Actions.

A separate follow-up project can replace this with an Azure VM configured as a **self-hosted GitHub Actions runner**.

A GitHub Actions self-hosted runner is different from an Azure DevOps self-hosted agent. The same VM can technically host both pieces of software, but each CI/CD platform requires its own agent/runner registration and configuration.

---

# 23. Security Practices Demonstrated

This project implements several practical security principles:

1. **No Docker Hub token in source control.** Credentials are stored in GitHub Secrets.
2. **Container scanning before publication.** Trivy runs before Docker Hub login/tag/push.
3. **Security gate behavior.** The scan is configured to return a failure exit code for blocking results.
4. **Commit-based image traceability.** Images receive a Git SHA tag.
5. **Generated artifacts excluded from Git.** `target/` is reproduced by CI rather than trusted from a workstation.
6. **Token-based registry authentication.** A Docker Hub access token is used instead of embedding a normal account password.
7. **No secrets documented.** Secret values are intentionally absent from the repository and README.

---

# 24. Project Structure

```text
QuickShop-GitHubActions/
|
+-- .github/
|   +-- workflows/
|       +-- ci-cd.yml
|
+-- src/
|   +-- main/
|   |   +-- java/
|   |       +-- com/
|   |           +-- quickshop/
|   |               +-- QuickShop.java
|   |
|   +-- test/
|       +-- java/
|           +-- com/
|               +-- quickshop/
|                   +-- QuickShopTest.java
|
+-- .gitignore
+-- Dockerfile
+-- pom.xml
+-- README.md
```

`target/` is generated during Maven builds but intentionally excluded from Git.

---

# 25. Verification Commands

## Local Maven test

```bash
mvn test
```

Expected:

```text
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## Local Maven package

```bash
mvn clean package
```

Verify:

```bash
ls -lh target/*.jar
```

## Local Docker build

```bash
docker build -t quickshop:1.0 .
```

## Local Docker execution

```bash
docker run --rm quickshop:1.0
```

Expected:

```text
Welcome to QuickShop
QuickShop is healthy
```

## Git synchronization

```bash
git status
git log --oneline
```

## Remote synchronization when GitHub was edited directly

```bash
git fetch origin
git status
git pull --ff-only origin main
```

---

# 26. Key Lessons Learned

### 1. CI/CD systems only know what is available to the runner

The Dockerfile worked locally but failed in GitHub Actions because it had not been committed.

### 2. Local success does not guarantee CI success

CI uses a different environment. A reproducible pipeline must contain or retrieve everything it needs.

### 3. Start a pipeline small and add stages gradually

This project evolved intentionally:

```text
Build/Test
    -> Docker
    -> Trivy
    -> Docker Hub
```

This made troubleshooting easier because each new capability was validated before adding the next.

### 4. Secrets do not belong in YAML

Credentials should be injected at runtime from a secret-management facility.

### 5. Image tags provide traceability

A Git SHA tag connects the published image to the source revision that produced it.

### 6. Security should occur before publication

The pipeline scans the image before the Docker Hub push steps.

### 7. A CI failure is useful information

The failed Docker workflow demonstrated how to read the failing step, determine root cause, fix source control, and rerun the pipeline.

---

# 27. Interview Talking Points

## Describe the project

> I built a Java CI/CD pipeline using GitHub Actions. A push to the main branch triggers a GitHub-hosted Ubuntu runner, checks out the code, configures Java 17, builds and tests the application with Maven, packages it into a JAR, builds a Docker image, scans the image with Trivy, authenticates to Docker Hub using GitHub Secrets, tags the image with both the Git commit SHA and latest, and publishes the approved image to Docker Hub.

## What is the difference between CI and CD here?

> CI validates the source through build, unit tests, packaging, container build, and security scanning. The CD portion publishes the approved container image to Docker Hub so it is available for downstream deployment.

## Why use a Git SHA Docker tag?

> It provides traceability. I can map a container image back to the exact Git commit that produced it, which is useful for auditing, troubleshooting, and rollback decisions.

## Why use GitHub Secrets?

> Registry credentials should not be hard-coded in source control. GitHub Secrets allows the workflow to inject credentials at runtime without exposing the actual token in the repository YAML.

## What problem did you troubleshoot?

> My first Docker build in GitHub Actions failed because the Dockerfile existed on my laptop but had not been committed to the Git repository. The GitHub-hosted runner therefore could not find it. I confirmed the Dockerfile was untracked with `git status`, synchronized a GitHub-side workflow edit with `git pull --ff-only`, committed the Dockerfile, pushed it, and verified the next workflow succeeded.

## Why use Trivy?

> Trivy scans the container image for known vulnerabilities. I placed the scan before the Docker Hub publication stages so security validation occurs before the image is published.

## What is a GitHub-hosted runner?

> It is an execution environment provisioned by GitHub to run workflow jobs. In this project I used `ubuntu-latest`, so I did not need to provision or maintain a runner VM myself.

## Could you use a self-hosted runner instead?

> Yes. I could register an Azure Linux VM as a GitHub Actions self-hosted runner and change the workflow's `runs-on` configuration. That would give more control over installed tools, network access, and compute, but it would also make me responsible for runner security, patching, availability, and lifecycle management.

---

# 28. GitHub-Hosted vs. Self-Hosted Runner

This project intentionally uses GitHub-hosted runners.

```text
GitHub-hosted
GitHub manages infrastructure
Ephemeral runner
Low maintenance
Fast setup

Self-hosted
Organization manages infrastructure
Can use an Azure VM/on-premises host
Custom tools/networking possible
Requires patching, security, maintenance, and runner lifecycle management
```

A planned follow-up lab is to run GitHub Actions using a self-hosted runner on Azure and compare the operational model with `ubuntu-latest`.

---

# 29. Recommended Production Improvements

This is a focused learning project. A production implementation should consider the following improvements.

## Publish only from trusted events

The current workflow is triggered by both pushes and pull requests. Registry login and publishing should normally be restricted to trusted `push` events on `main` or to release/tag events rather than occurring during untrusted pull-request validation.

A common design is:

```text
Pull request
  -> build/test/scan only

Push to main
  -> build/test/scan
  -> authenticate
  -> publish
```

This can be implemented using job separation or `if:` conditions.

## Pin third-party Actions

Instead of tracking a mutable branch such as:

```yaml
uses: aquasecurity/trivy-action@master
```

pin Actions to reviewed stable versions or immutable commit SHAs.

## Add dependency caching

Maven dependencies can be cached to reduce build time.

## Add Docker Buildx/build cache

Docker layer caching can improve image build performance.

## Add a `.dockerignore`

A `.dockerignore` can reduce Docker build context and prevent unnecessary files from being sent to the Docker daemon.

## Add artifact retention

The generated JAR and test/security reports can be uploaded using GitHub Actions artifacts when retention or inspection is required.

## Add dependency/SAST scanning

The pipeline can be expanded with dependency scanning, CodeQL, or other static-analysis controls.

## Add branch protection

Protect `main`, require pull requests, and require the CI checks to pass before merging.

## Use GitHub Environments for controlled release

For higher-risk deployment/publishing stages, GitHub Environments can provide approvals and environment-scoped secrets.

## Add deployment

A downstream environment could consume the Docker Hub image, but Kubernetes was intentionally excluded from this project's scope.

---

# 30. Common Git Workflow Used

For normal local changes:

```bash
git status
git add <file>
git commit -m "Describe the change"
git push
```

If GitHub is changed directly and local Git is behind:

```bash
git fetch origin
git status
git pull --ff-only origin main
```

This keeps the local working copy synchronized with the remote repository when a fast-forward is possible.

---

# 31. Final Verification Checklist

- [x] Java 17 project created
- [x] Maven `pom.xml` configured
- [x] QuickShop application created
- [x] Two JUnit tests created
- [x] `mvn test` passed
- [x] `mvn clean package` passed
- [x] JAR artifact created
- [x] `.gitignore` configured
- [x] Local Git repository initialized
- [x] Main branch configured
- [x] GitHub repository created
- [x] Local repository connected to GitHub
- [x] GitHub Actions workflow created
- [x] GitHub-hosted Ubuntu runner used
- [x] Java configured automatically in CI
- [x] Maven build/test passed in GitHub Actions
- [x] Dockerfile created
- [x] Docker image built locally
- [x] Docker container tested locally
- [x] Docker build automated in GitHub Actions
- [x] Missing-Dockerfile pipeline failure diagnosed and fixed
- [x] Trivy container security scan added
- [x] Docker Hub repository created
- [x] Docker Hub access token created
- [x] Docker Hub credentials stored in GitHub Secrets
- [x] Docker Hub authentication automated
- [x] Docker image tagged with Git SHA
- [x] Docker image tagged as `latest`
- [x] Docker image pushed automatically
- [x] Published image verified in Docker Hub
- [x] End-to-end workflow completed successfully

---

# 32. Final Architecture Summary

```text
                 QUICKSHOP CI/CD

Developer Workstation
        |
        | git push
        v
+---------------------------+
| GitHub Repository         |
| QuickShop-GitHubActions   |
+-------------+-------------+
              |
              | workflow trigger
              v
+---------------------------+
| GitHub Actions            |
| GitHub-hosted Ubuntu      |
+-------------+-------------+
              |
              +--> Checkout repository
              |
              +--> Set up Java 17
              |
              +--> Maven clean/package
              |       |
              |       +--> Compile
              |       +--> Unit tests
              |       +--> JAR artifact
              |
              +--> Docker build
              |
              +--> Trivy security scan
              |       |
              |       +--> blocking failure -> STOP
              |       +--> pass -> continue
              |
              +--> Docker Hub login
              |       using GitHub Secrets
              |
              +--> Tag image
              |       +--> Git SHA
              |       +--> latest
              |
              +--> Push image
                      |
                      v
             +---------------------+
             | Docker Hub          |
             | bustopsy777/        |
             | quickshop           |
             +---------------------+
```

---

# 33. Project Outcome

This project demonstrates a focused GitHub Actions CI/CD implementation without introducing unnecessary deployment infrastructure.

The final solution proves the ability to:

- structure a small Java/Maven application;
- create automated unit tests;
- manage source code with Git and GitHub;
- create GitHub Actions workflows;
- use GitHub-hosted runners and migrate the same workflow to an Azure self-hosted runner;
- automate Maven builds and testing;
- package a Java application into a JAR;
- containerize an application with Docker;
- implement container vulnerability scanning with Trivy;
- protect registry credentials using GitHub Secrets;
- implement commit-to-image traceability;
- publish approved container images automatically to Docker Hub;
- troubleshoot a real CI failure caused by missing repository content;
- verify successful end-to-end CI/CD execution.

The project was subsequently extended by replacing the GitHub-hosted runner with an Azure Linux self-hosted GitHub Actions runner, as documented below.


---

# 34. Extension: Azure Self-Hosted GitHub Actions Runner

After the original QuickShop pipeline was working successfully on GitHub's hosted Ubuntu runner, the project was extended to run the same CI/CD workload on a self-managed Azure Linux virtual machine.

This extension demonstrates the operational difference between:

```yaml
runs-on: ubuntu-latest
```

and:

```yaml
runs-on: [self-hosted, Linux, X64]
```

With `ubuntu-latest`, GitHub provisions and maintains the temporary runner environment. With the self-hosted configuration, the infrastructure owner is responsible for the virtual machine, operating system, runner installation, required build tools, security, updates, disk space, availability, and runner lifecycle.

## Self-Hosted Runner Architecture

```text
Developer
    |
    | git push / workflow trigger
    v
GitHub Repository
QuickShop-GitHubActions
    |
    v
GitHub Actions
    |
    | runs-on:
    | [self-hosted, Linux, X64]
    v
Azure Linux VM
johnson-busuyi-VM
    |
    +-- GitHub Actions Runner
    |      managed by systemd
    |
    +-- Checkout source code
    +-- Set up Java 17
    +-- Maven build and unit tests
    +-- Build Docker image
    +-- Trivy vulnerability scan
    +-- Authenticate to Docker Hub
    +-- Tag image with Git SHA and latest
    +-- Push approved image
             |
             v
Docker Hub
bustopsy777/quickshop
```

The GitHub Actions runner is separate from any Azure DevOps self-hosted agent installed on the same VM. Each uses its own agent/runner software, registration, and CI/CD platform.

## Azure Runner Host

The VM was verified with:

```bash
hostname
whoami
```

Result:

```text
hostname: johnson-busuyi-VM
user:     johnson-busuyi
```

A dedicated runner directory was created:

```bash
mkdir actions-runner && cd actions-runner
pwd
```

Working directory:

```text
/home/johnson-busuyi/actions-runner
```

## Downloading and Extracting the Runner

GitHub runner setup was opened from:

```text
QuickShop-GitHubActions
Settings
Actions
Runners
New self-hosted runner
Linux
x64
```

The exact download command generated by GitHub was used.

An early mistake provided a troubleshooting lesson: a placeholder command containing `....` was accidentally used. The resulting archive was only 9 bytes, showing that the runner had not actually downloaded. The invalid file was removed and the exact GitHub-generated command was used.

Correct package:

```text
actions-runner-linux-x64-2.337.0.tar.gz
```

Approximate size:

```text
216 MB
```

It was extracted with:

```bash
tar xzf ./actions-runner-linux-x64-2.337.0.tar.gz
```

The directory then contained files such as:

```text
bin/
config.sh
env.sh
externals/
run.sh
safe_sleep.sh
```

## Runner Registration

GitHub generates a temporary command following this pattern:

```bash
./config.sh --url https://github.com/johnson-busuyi/QuickShop-GitHubActions --token <TEMPORARY_REGISTRATION_TOKEN>
```

The real token must never be committed, documented, or shared publicly.

Configuration used:

```text
Runner group: Default
Runner name:  johnson-busuyi-VM
Labels:       self-hosted, Linux, X64
Work folder:  _work
```

Successful registration produced:

```text
Connected to GitHub
Runner successfully added
Settings Saved
```

The `_work` directory is the workspace in which GitHub checks out repositories and executes jobs.

## Troubleshooting: Runner Registered to the Wrong Repository

The runner was initially registered to the CartFlow GitHub repository instead of `QuickShop-GitHubActions`.

The GitHub-side CartFlow runner registration was removed. Attempting to configure QuickShop afterward produced:

```text
Cannot configure the runner because it is already configured.
To reconfigure the runner, run 'config.cmd remove' or './config.sh remove' first.
```

This showed that removing a runner from GitHub did not remove the local runner configuration from the VM.

The remaining files were identified with:

```bash
ls -la | grep -E '\.runner|\.credentials'
```

They were:

```text
.credentials
.credentials_rsaparams
.runner
```

Before deletion they were backed up:

```bash
mkdir ~/runner-config-backup
cp .runner .credentials .credentials_rsaparams ~/runner-config-backup/
ls -la ~/runner-config-backup
```

The stale active configuration was then removed:

```bash
rm .runner .credentials .credentials_rsaparams
```

Cleanup was verified:

```bash
ls -la | grep -E '\.runner|\.credentials'
```

No output confirmed the stale local registration data was gone. The existing runner software did not need to be downloaded again. The VM was then registered successfully with the correct QuickShop repository.

## Manual Runner Test

The runner was initially started manually:

```bash
./run.sh
```

Successful connectivity showed:

```text
Connected to GitHub
Current runner version: '2.337.0'
Listening for Jobs
```

In this mode, pressing `Ctrl+C`, closing SSH, or terminating the process makes the runner unavailable. Manual mode was used only for initial validation.

## Verifying Self-Hosted Runner Prerequisites

Unlike a GitHub-hosted runner, the self-hosted machine must be prepared and maintained by the infrastructure owner.

Docker:

```bash
docker --version
```

Verified:

```text
Docker version 29.1.3
```

Java:

```bash
java -version
```

Verified:

```text
OpenJDK 17
```

Maven:

```bash
mvn -version
```

Verified:

```text
Apache Maven 3.8.7
Java 17
Linux amd64
```

The workflow still uses `actions/setup-java@v4`, making the required Java version explicit rather than depending solely on the VM's pre-existing configuration.

## Migrating the Workflow

Original:

```yaml
runs-on: ubuntu-latest
```

Updated:

```yaml
runs-on: [self-hosted, Linux, X64]
```

The job requires a runner matching all three labels:

```text
self-hosted
Linux
X64
```

An editing mistake briefly produced:

```yaml
runs-on: runs-on: [self-hosted, Linux, X64]
```

It was caught during review and corrected before commit.

Final declaration:

```yaml
jobs:
  build-and-test:
    runs-on: [self-hosted, Linux, X64]
```

## Successful Azure Execution

After committing the change, the runner displayed:

```text
Running job: build-and-test
```

and later:

```text
Job build-and-test completed with result: Succeeded
```

This proved that GitHub matched the workflow labels to `johnson-busuyi-VM` and dispatched QuickShop to the Azure VM.

The pipeline continued to perform:

```text
Checkout
   |
Set up Java 17
   |
Maven clean package
   |
Unit tests
   |
Docker build
   |
Trivy scan
   |
Docker Hub login
   |
Tag image
   |
Push Git SHA + latest
```

## Installing the Runner as a Linux Service

Manual `./run.sh` execution depends on an active process/terminal, so the runner was installed as a systemd service:

```bash
sudo ./svc.sh install
```

Generated service:

```text
actions.runner.johnson-busuyi-QuickShop-GitHubActions.johnson-busuyi-VM.service
```

The service runs as:

```text
johnson-busuyi
```

It was started and verified with:

```bash
sudo ./svc.sh start
sudo ./svc.sh status
```

Successful status included:

```text
Loaded: loaded (...; enabled; ...)
Active: active (running)
Connected to GitHub
Current runner version: '2.337.0'
Listening for Jobs
```

`enabled` means systemd is configured to start the runner when the VM boots. `active (running)` means it is currently running.

## Unattended Verification

After systemd setup, SSH sessions were closed.

GitHub still showed:

```text
johnson-busuyi-VM
self-hosted
Linux
X64
Idle
```

The full QuickShop workflow was then re-run without opening SSH and without manually running `./run.sh`.

The workflow completed successfully and turned green.

This proved true unattended operation:

```text
GitHub workflow trigger
        |
        v
systemd-managed runner
        |
        v
Azure VM receives job
        |
        v
Build / Test / Scan / Publish
        |
        v
Successful workflow
```

## Useful Service Commands

```bash
cd ~/actions-runner
sudo ./svc.sh status
sudo ./svc.sh start
sudo ./svc.sh stop
```

If intentionally decommissioning the service:

```bash
sudo ./svc.sh uninstall
```

## GitHub-Hosted vs Self-Hosted

**GitHub-hosted**

```yaml
runs-on: ubuntu-latest
```

GitHub provisions and maintains the temporary runner, common tooling is preinstalled, and little runner infrastructure administration is required.

**Azure self-hosted**

```yaml
runs-on: [self-hosted, Linux, X64]
```

The organization provisions and maintains the VM and runner. OS patching, security, build tools, networking, disk cleanup, availability, and runner lifecycle become operational responsibilities. The environment can be customized, but if the VM is stopped or unavailable, jobs assigned only to it cannot run.

## Self-Hosted Runner Security Considerations

Important practices include:

- restrict who can modify workflows;
- protect the `main` branch;
- require pull-request review where appropriate;
- never hard-code secrets;
- use GitHub Secrets for registry credentials;
- apply least privilege;
- patch the Azure VM and runner software;
- monitor disk usage and old Docker images;
- restrict network access with Azure NSGs and host controls;
- avoid unnecessary inbound ports;
- carefully review third-party Actions;
- pin important third-party Actions to trusted immutable commit SHAs where practical;
- be cautious about running untrusted pull-request code on persistent self-hosted runners;
- separate sensitive production runners from general development workloads when appropriate.

The Trivy gate remains before Docker Hub publication, so configured HIGH or CRITICAL findings cause the workflow to fail before image publication.

## Updated Self-Hosted CI/CD Flow

```text
Developer
    |
    | push to main
    v
GitHub
    |
    v
GitHub Actions
    |
    | self-hosted + Linux + X64
    v
Azure VM: johnson-busuyi-VM
    |
    +--> Checkout
    +--> Java 17
    +--> Maven clean package
    +--> Unit tests
    +--> Docker build
    +--> Trivy HIGH/CRITICAL scan
    +--> Docker Hub login via GitHub Secrets
    +--> Tag with commit SHA and latest
    +--> Push
             |
             v
Docker Hub
bustopsy777/quickshop
```

## Final Self-Hosted Workflow

```yaml
name: QuickShop CI/CD

on:
  push:
    branches:
      - main
  pull_request:
    branches:
      - main

jobs:
  build-and-test:
    runs-on: [self-hosted, Linux, X64]

    steps:
      - name: Checkout source code
        uses: actions/checkout@v4

      - name: Set up Java 17
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'

      - name: Build and test with Maven
        run: mvn clean package

      - name: Build Docker image
        run: docker build -t quickshop:${{ github.sha }} .

      - name: Scan Docker image with Trivy
        uses: aquasecurity/trivy-action@master
        with:
          image-ref: 'quickshop:${{ github.sha }}'
          format: 'table'
          severity: 'HIGH,CRITICAL'
          exit-code: '1'

      - name: Log in to Docker Hub
        uses: docker/login-action@v3
        with:
          username: ${{ secrets.DOCKERHUB_USERNAME }}
          password: ${{ secrets.DOCKERHUB_TOKEN }}

      - name: Tag Docker image
        run: |
          docker tag quickshop:${{ github.sha }} bustopsy777/quickshop:${{ github.sha }}
          docker tag quickshop:${{ github.sha }} bustopsy777/quickshop:latest

      - name: Push Docker image to Docker Hub
        run: |
          docker push bustopsy777/quickshop:${{ github.sha }}
          docker push bustopsy777/quickshop:latest
```

For production hardening, publishing should normally be restricted so untrusted pull-request workflows cannot push release images. Third-party Actions should also be pinned to trusted immutable commit SHAs where practical.

## Troubleshooting Lessons

This extension produced realistic operational lessons:

- **Wrong repository:** The runner was accidentally registered to CartFlow and had to be moved to QuickShop.
- **Placeholder values:** Example values such as `YOUR_REMOVAL_TOKEN`, `<token>`, or `....` must never be executed literally.
- **9-byte archive:** An unexpectedly tiny download indicated an invalid runner URL/placeholder.
- **Stale local registration:** `.runner`, `.credentials`, and `.credentials_rsaparams` remained after server-side removal and had to be backed up and cleaned.
- **Manual runner lifecycle:** `./run.sh` stops when its process ends.
- **systemd:** Installing the runner as a service enabled unattended operation.
- **Prerequisites:** Docker, Java, Maven, permissions, networking, disk capacity, and patching are the self-hosted runner owner's responsibility.

## Interview Talking Points

A concise project explanation:

> I first built the QuickShop CI/CD pipeline on GitHub-hosted Ubuntu runners. I then migrated the same workflow to a self-hosted GitHub Actions runner on an Azure Linux VM. I registered the VM with repository-level runner labels, verified Docker, Java, and Maven prerequisites, changed the workflow to target `self-hosted`, `Linux`, and `X64`, and validated that GitHub dispatched the job to the Azure VM. I then installed the runner as a systemd service and proved it could execute the complete pipeline without an active SSH session.

A troubleshooting example:

> During configuration I initially registered the runner to the wrong repository. After removing the server-side registration, local runner credential files still caused the runner to appear configured. I backed up and removed the stale `.runner` and credential files, registered the VM with the correct repository, and successfully executed the pipeline.

A security explanation:

> Self-hosted runners provide more control but also transfer operational responsibility to the organization. I would protect workflow changes, limit untrusted pull-request execution, use least-privilege secrets, patch the VM, restrict network access, monitor disk and Docker state, and pin important third-party Actions.

## Additional Skills Demonstrated

The completed project now also demonstrates:

- Azure Linux self-hosted GitHub Actions runners;
- runner labels and job routing;
- Azure VM administration;
- Linux command-line administration;
- systemd service management;
- runner registration and lifecycle management;
- runner prerequisite validation;
- unattended CI/CD execution;
- self-hosted runner troubleshooting;
- comparison of GitHub-hosted and self-hosted execution.

## Final Project Status

```text
QuickShop application                 COMPLETE
Maven build                           COMPLETE
JUnit tests                           COMPLETE
Docker image                          COMPLETE
GitHub Actions CI/CD                  COMPLETE
Trivy security gate                   COMPLETE
Docker Hub publication                COMPLETE
GitHub-hosted runner validation       COMPLETE
Azure self-hosted runner              COMPLETE
Linux systemd runner service          COMPLETE
Unattended workflow verification      COMPLETE
```

The QuickShop project is now an end-to-end CI/CD portfolio project demonstrating both GitHub-managed execution and self-managed Azure runner infrastructure.

---

## Author

**Busuyi Johnson**

DevOps / Cloud Engineering Portfolio Project

## Disclaimer

This repository is a learning and portfolio project. Credentials, access tokens, and other secrets must never be committed to source control. Any production implementation should apply organization-specific security controls, protected branches, least-privilege credentials, reviewed/pinned third-party Actions, and appropriate release/deployment approval policies.
