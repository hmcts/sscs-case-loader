FROM openjdk:8-alpine

RUN apk update
RUN apk add bash openssh-client
RUN mkdir -p /opt/sscs/sscs-case-loader
RUN mkdir /var/run/sshd

COPY build/install/sscs-case-loader /opt/app/

RUN mkdir -p /var/tmp/gaps2
RUN mkdir -p /var/tmp/gaps2archive
RUN mkdir -p /var/tmp/valid
RUN mkdir -p /var/tmp/schemaValidationFailed

COPY ./docker/sftp-docker /home/webapp/sscs-sftp-key
RUN chmod 400 /home/webapp/sscs-sftp-key

ENV JAVA_OPTS=""

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD http_proxy="" curl --silent --fail http://localhost:8082/health

EXPOSE 8082

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /opt/sscs/sscs-case-loader/app.jar" ]
CMD ["sh", "-c"]
