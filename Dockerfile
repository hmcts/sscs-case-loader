ARG APP_INSIGHTS_AGENT_VERSION=2.3.1

FROM hmctspublic.azurecr.io/base/java:openjdk-8-distroless-1.2

WORKDIR /app
COPY . .

USER root
RUN gradle build --no-daemon --console plain

COPY lib/AI-Agent.xml /opt/app/
COPY --from=builder /home/gradle/src/build/libs/sscs-case-loader.jar /opt/app/

WORKDIR /opt/app

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD http_proxy="" wget -q http://localhost:8082/health || exit 1

EXPOSE 8082

ENTRYPOINT exec java ${JAVA_OPTS} -jar "/opt/app/sscs-case-loader.jar"
