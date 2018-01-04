FROM openjdk:8

COPY kicker-all.jar /kicker/kicker-all.jar

WORKDIR /kicker

ENTRYPOINT java -jar kicker-all.jar

EXPOSE 8080
