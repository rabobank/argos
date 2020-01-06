#!/bin/sh
#
# Copyright (C) 2020 Rabobank Nederland
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

set -x
name_space=$(grep "name_space=" build.config | cut -d '=' -f2)
docker_from_image=$(grep "docker_from_image=" build.config | cut -d '=' -f2)
latest_tag=$(grep "latest_tag" build.config | cut -d '=' -f2)

REGISTRY="${name_space}"
JENKINS_NAME=$(grep "jenkins_name=" build.config | cut -d '=' -f2)
IMAGE_LATEST="${JENKINS_NAME}:${latest_tag}"

image() {
  build_image=${REGISTRY}/${IMAGE_LATEST}
  echo "Build image ${build_image}"
  docker build \
    --tag ${build_image} \
  .
}

push_final() {
  image
  docker push ${REGISTRY}/${JENKINS_NAME}:${latest_tag}
}

help() {
  echo "Usage: ./build.sh <function>"
  echo ""
  echo "Functions"
  printf "   \033[36m%-30s\033[0m %s\n" "image" "Build the Docker image."
  printf "   \033[36m%-30s\033[0m %s\n" "push_final" "Push the Docker image to the internal registry."
  echo ""
  echo "Content of build.config"
  printf "   \033[36m%-30s\033[0m %s\n" "name=<name>" "Name of the Docker image, required."
  echo ""
}

if [ -z "${1}" ]; then
  echo "ERROR: function required"
  help
  exit 1
fi
${1}