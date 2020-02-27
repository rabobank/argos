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

if [ -z $1 ]
then
  echo "Usage: $0 [namespace/]<image>[:version]"
  exit 1
fi

image=$1
shift

registry_src="hub.docker.com"
registry_dst="nexus3.rabobank.nl:8086"

echo "Processing ${image}"

printf "Determine source namespace: "
if [[ ${image} == *"/"* ]]; then
  namespace="$(echo ${image} | cut -d '/' -f 1)"
else
  namespace="library"
fi
echo $namespace

if [[ ${image} == *":"* ]]; then
  image_version="$(echo $image | cut -d ':' -f 2)"
else
  image_version=""
fi

printf "Determine image name: "
image_name="$(echo ${image} | cut -d: -f1 | awk -F/ '{print $NF}')"
echo $image_name

if [ "${image_version}" == "" ]; then
  echo "Getting all tags on '${registry_src}'"
  image_versions=$(curl -s https://${registry_src}/v2/repositories/${namespace}/${image_name}/tags/?page_size=500 | jq -r '.results|.[]|.name')
else
  image_versions=${image_version}
fi
printf "Image version(s): "
echo $image_versions

for version in ${image_versions}
do
  # Nexus3 does not use the 'library' namespace, so strip it from the destination namespace
  if [ "${namespace}" == "library" ]
  then
    namespace_dst=""
  else
    namespace_dst="${namespace}/"
  fi

  echo "Mirror ${namespace}/${image_name}:${version} to ${registry_dst}/${namespace_dst}${image_name}:${version}"

  url="https://${registry_dst}/v2/${namespace_dst}${image_name}/manifests/${version}"
  if curl --insecure --output /dev/null --silent --fail "${url}"; then
    echo "Image already exists, skipping..."
    continue
  fi

  echo "Pulling ${namespace}/${image_name}:${version}"
  docker pull ${namespace}/${image_name}:${version}
  docker tag ${namespace}/${image_name}:${version} ${registry_dst}/${namespace_dst}${image_name}:${version}

  echo "Pushing ${registry_dst}/${namespace_dst}${image_name}:${version}"
  docker push ${registry_dst}/${namespace_dst}${image_name}:${version}
done
