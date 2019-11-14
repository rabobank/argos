FROM jenkins/jenkins:lts-alpine

COPY config/files/list-of-plugins.txt /plugins.txt
RUN /usr/local/bin/install-plugins.sh $(cat /plugins.txt)
