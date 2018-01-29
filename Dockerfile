FROM openjdk:8-alpine

RUN apk update
RUN apk add bash openssh-client
RUN mkdir -p /opt/app/
RUN mkdir /var/run/sshd

COPY ./build/install/sscs-case-loader /opt/app/

RUN mkdir -p /var/tmp/gaps2
RUN mkdir -p /var/tmp/gaps2archive
RUN mkdir -p /var/tmp/valid
RUN mkdir -p /var/tmp/schemaValidationFailed

COPY ./docker/sftp-docker /home/webapp/sscs-sftp-key
RUN chmod 400 /home/webapp/sscs-sftp-key

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD http_proxy="" wget -qO- "http://localhost:8082/health" | grep UP -q

ENTRYPOINT ["/opt/app/bin/sscs-case-loader"]
