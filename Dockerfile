ARG APP_INSIGHTS_AGENT_VERSION=2.5.1

FROM hmctspublic.azurecr.io/base/java:17-distroless

COPY lib/AI-Agent.xml /opt/app/
COPY build/libs/sscs-case-loader.jar /opt/app/

CMD ["sscs-case-loader.jar"]
EXPOSE 8082

