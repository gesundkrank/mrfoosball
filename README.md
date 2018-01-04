# Kicker [![Build Status](https://travis-ci.org/mbrtargeting/kicker.svg?branch=master)](https://travis-ci.org/mbrtargeting/kicker)

## Run App Locally

```bash
SLACK_TOKEN=<slackToken> ./gradlew composeUp
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
./gradlew buildDocker
```

## Push Docker Container

```bash
./gradlew dockerPush 
```

Creates the docker package and pushes it to the registry.

To get access to the registry do `aws ecr get-login --no-include-email` and execute the returned 
docker command.

