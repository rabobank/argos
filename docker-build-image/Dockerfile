FROM maven:3.6.3-jdk-11

RUN apt-get update && apt-get install -y openjdk-8-jdk

COPY files/settings.xml /root/.m2/settings.xml
COPY files/toolchains.xml /root/.m2/toolchains.xml