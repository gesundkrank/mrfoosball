# Mr Foosball [![Build Status](https://travis-ci.com/gesundkrank/mrfoosball.svg?branch=master)](https://travis-ci.com/gesundkrank/mrfoosball)

## Configuration

Create your test configuration in your project folder as
`./mrfoosball.properties`.

Example configuration file:
```
port=8080
slackToken=xoxb-1564223413123-adkjADAsjdladdj
connectionHbm2ddl=create-drop
```

## Run App Locally

```bash
./gradlew [-Pproduction] composeUp
```

To see the logs of the container do 

```bash
docker logs `docker ps --filter 'name=mrfoosball_mrfoosball' --format '{{.ID}}'`
``` 

To shut down all containers exec

```
./gradlew composeDown
```

## Dockerize Production Version
```bash
./gradlew buildDocker
```

## Terraform

### Run Terraform only for network setup (vpc & dns)

```bash
terraform apply -target=module.network_setup
```

Creates the docker package and pushes it to the registry.

To get access to the registry do `aws ecr get-login --no-include-email` and execute the returned 
docker command.
