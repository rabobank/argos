/**
 * Copyright (C) 2019 - 2020 Rabobank Nederland
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
import jenkins.model.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.plugins.credentials.CredentialsScope
import org.apache.commons.fileupload.disk.DiskFileItem
import org.apache.commons.fileupload.disk.DiskFileItemFactory
import org.jenkinsci.plugins.plaincredentials.impl.FileCredentialsImpl
import com.cloudbees.plugins.credentials.SecretBytes
import com.cloudbees.plugins.credentials.impl.*
import groovy.json.JsonSlurper
import java.util.logging.Logger
import java.util.logging.Level
import java.nio.file.Files

def Logger logger = Logger.getLogger("")

store = Jenkins.instance.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()

secretKeyBob = new UsernamePasswordCredentialsImpl(
    CredentialsScope.GLOBAL,
    "bob",
    "bobs key",
    "c8df0a497ab0df7136c4f97892f17914e6e5e021fdc039f0ea7c27d5a95c1254",
    "test")

secretKeyAlice = new UsernamePasswordCredentialsImpl(
    CredentialsScope.GLOBAL,
    "alice",
    "alices key",
    "56c7d930998b6039a925d5dee95f1349f53fc51e9bcdfcd7d50ecb58a9201a12",
    "test")
    
    
domain = Domain.global()
    
// Add credentials to Jenkins credential store
store.addCredentials(domain, secretKeyBob)
store.addCredentials(domain, secretKeyAlice)

logger.info("--> credetials added.")
