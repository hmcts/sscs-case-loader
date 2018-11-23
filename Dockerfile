FROM gradle:jdk8 as builder

WORKDIR /home/gradle/src
COPY . /home/gradle/src

USER root
RUN gradle build --no-daemon --console plain

FROM openjdk:8-jre-alpine

COPY --from=builder /home/gradle/src/build/libs/sscs-case-loader.jar /opt/app/

WORKDIR /opt/app

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD http_proxy="" wget -q http://localhost:8082/health || exit 1

EXPOSE 8082

ENTRYPOINT exec java ${JAVA_OPTS} -jar "/opt/app/sscs-case-loader.jar"
