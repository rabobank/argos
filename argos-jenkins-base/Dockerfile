FROM jenkins/jenkins:lts-alpine

USER root

RUN apk add --no-cache maven

# temporary patch to enable file url's
# A PR is made to upstream
RUN sed -i -e 's/http\.\+/\(http\|file\)\.\+/g' /usr/local/bin/install-plugins.sh

COPY config/files/list-of-plugins.txt /plugins.txt
RUN /usr/local/bin/install-plugins.sh $(cat /plugins.txt)
