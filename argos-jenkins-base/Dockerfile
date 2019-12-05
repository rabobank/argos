#
# Copyright (C) 2019 Rabobank Nederland
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

FROM jenkins/jenkins:lts-alpine

USER root

RUN apk add --no-cache maven

# temporary patch to enable file url's
# A PR is made to upstream
RUN sed -i -e 's/http\.\+/\(http\|file\)\.\+/g' /usr/local/bin/install-plugins.sh

COPY config/files/list-of-plugins.txt /plugins.txt
RUN /usr/local/bin/install-plugins.sh $(cat /plugins.txt)
