FROM hmcts/cnp-java-base:openjdk-8u191-jre-alpine3.9-2.0.1

COPY . /home/gradle/src
USER root
RUN chown -R gradle:gradle /home/gradle/src
USER gradle

WORKDIR /home/gradle/src
RUN gradle assemble

FROM openjdk:8-jre-alpine

COPY --from=builder /home/gradle/src/build/libs/sscs-case-loader.jar /opt/app/

WORKDIR /opt/app

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD http_proxy="" wget -q http://localhost:8082/health || exit 1

EXPOSE 8082

ENTRYPOINT exec java ${JAVA_OPTS} -jar "/opt/app/sscs-case-loader.jar"
