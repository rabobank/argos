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
    echo "Usage: ./release_with_tag.sh  <release version>"
    echo
    echo "This will force a release of the project with the <release version>"
}

push() {
    git tag -f ${VERSION}
    git push -f origin refs/tags/${VERSION}
}

if [ -z "$1" ]; then
    printf "\033[31m%-5s\033[0m %s\n" "ERROR" "No version parameter."
    usage
    exit 8
fi

VERSION=$1

printf "\033[33m%-5s\033[0m %s\n" "WARN" "This will force a release of the project with the version ${VERSION}"
printf "\033[33m%-5s\033[0m %s\n" "WARN" "with a force push of the tag ${VERSION}"
while true; do
    read -p "Are you sure you wish to do this [y/n]: " yn
    case $yn in
        [Yy]* ) push; break;;
        [Nn]* ) echo "Abort..."; exit;;
        * ) echo "Please answer yes(y) or no(n).";;
    esac
done
