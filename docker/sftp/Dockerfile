FROM atmoz/sftp:alpine

ADD ./data/keys/sftp-docker.pub  /home/sftp/.ssh/keys/sftp-docker.pub
ADD ./data/incoming/SSCS_Extract_Reference_2017-07-30-09-01-31.xml /home/sftp/incoming/SSCS_Extract_Reference_2017-07-30-09-01-31.xml

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD netstat -an | grep "0.0.0.0:22" -q
