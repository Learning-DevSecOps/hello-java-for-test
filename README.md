# Hello World Java - GitHub Actions

Builds `src/HelloWorld.java` into `hello-world.jar` and uploads it as a workflow artifact.

## Run locally
    mkdir -p out && javac -d out src/HelloWorld.java
    jar --create --file hello-world.jar --main-class HelloWorld -C out .
    java -jar hello-world.jar

## Get the JAR from GitHub
Push to `main` -> Actions tab -> open the run -> Artifacts section -> `hello-world-jar`.
