FROM openjdk:8-jre-alpine

LABEL maintainer="Rune Brinckmeyer, rune.brinckmeyer@gmail.com"

COPY target/uberjar/peakon-cc.jar /home/runner/

WORKDIR /home/runner

EXPOSE 8080
CMD ["java", "-jar", "peakon-cc.jar"]

