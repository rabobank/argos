#!/bin/sh

if [ -z "$@" ]; then
	java -jar /argos-service.jar
else
	if [ "$1" = "version" ]; then
		echo ${ARGOS_VERSION:-"no version"}
	else
		$@
	fi
fi