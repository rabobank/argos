#!/bin/bash
#
# Copyright (C) 2019 - 2020 Rabobank Nederland
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


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
