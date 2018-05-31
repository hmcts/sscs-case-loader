FROM atmoz/sftp:alpine

ADD ./data/keys/sftp-docker.pub  /home/sftp/.ssh/keys/sftp-docker.pub

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD netstat -an | grep "0.0.0.0:22" -q
