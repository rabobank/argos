#!/bin/bash

set -e

usage() {
    echo "Usage: ./change_project_version.sh  <release version>"
}

if [ -z "$1" ]; then
    usage
    exit 8
fi

VERSION=$1

SCRIPT_DIRECTORY=$(cd `dirname $0` && pwd)

PARENT_DIR="${SCRIPT_DIRECTORY}/.."

echo "update version in all modules to ${VERSION}"
mvn -q -f ${PARENT_DIR}/pom.xml versions:set -DnewVersion=${VERSION} -DprocessAllModules=true -DgenerateBackupPoms=false
