FROM openjdk:8-jre

COPY build/install/sscs-case-loader /opt/app/

WORKDIR /opt/app

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD http_proxy="" curl --silent --fail http://localhost:8080/health

EXPOSE 8080

ENTRYPOINT ["/opt/app/bin/sscs-case-loader"]
