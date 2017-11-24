# Kicker [![Build Status](https://travis-ci.org/mbrtargeting/kicker.svg?branch=master)](https://travis-ci.org/mbrtargeting/kicker)

## Configuration

Create your test configuration in your project folder as
`./kicker.properties`.

Example configuration file:
```
port=8080
slackToken=xoxb-1564223413123-adkjADAsjdladdj
connectionHbm2ddl=create-drop
```

## Build App
This creates a fat jar (`build/lib/kicker-all.jar`) containing the Java backend and the compiled fronted
```
./gradlew buildApp
```

## Run App

```bash
./gradlew runApp
```

## Dockerize
```bash
./gradlew -Ppush buildDocker
```

Creates the docker package and pushes it to the registry.

To get access to the registry do `aws ecr get-login --no-include-email` and execute the returned 
docker command.

