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

## Run App Locally

```bash
./gradlew composeUp
```

To see the logs of the container do 

```bash
docker logs `docker ps --filter 'name=kicker_kicker' --format '{{.ID}}'`
``` 

To shut down all containers exec

```
./gradlew composeDown
```

## Dockerize Production Version
```bash
./gradlew -Ppush buildDocker
```

Creates the docker package and pushes it to the registry.

To get access to the registry do `aws ecr get-login --no-include-email` and execute the returned 
docker command.

