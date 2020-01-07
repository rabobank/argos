/**
 * Copyright (C) 2020 Rabobank Nederland
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import hudson.security.csrf.DefaultCrumbIssuer

import hudson.security.*
import jenkins.branch.*
import jenkins.model.Jenkins
import jenkins.plugins.git.*
import org.jenkinsci.plugins.workflow.multibranch.*

import java.util.logging.Logger

Logger logger = Logger.getLogger("")

def instance = Jenkins.getInstance()

// set admin user
def hudsonRealm = new HudsonPrivateSecurityRealm(false)
hudsonRealm.createAccount('admin','admin')
instance.setSecurityRealm(hudsonRealm)

def strategy = new FullControlOnceLoggedInAuthorizationStrategy()
instance.setAuthorizationStrategy(strategy)

instance.save()

logger.info("--> Jenkins initialized ")

instance.save()
