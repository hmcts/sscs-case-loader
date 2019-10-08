FROM atmoz/sftp:alpine

COPY ./data/keys/sftp-docker.pub /home/sftp/.ssh/keys/sftp-docker.pub
COPY ./data/incoming /home/sftp/incoming
RUN chmod -R 777 /home/sftp/incoming

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD netstat -an | grep "0.0.0.0:22" -q
