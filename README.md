# Kicker [![Build Status](https://travis-ci.org/mbrtargeting/kicker.svg?branch=master)](https://travis-ci.org/mbrtargeting/kicker)

## Configuration

Create your test configuration in your project folder as
`./kicker.properties`.

Example configuration file:
```
port=8080
slackToken=xoxb-1564223413123-adkjADAsjdladdj
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

## Debianize
```bash
./gradlew debianizeApp
```

Creates the `.deb` package in `build/distributions/`.

