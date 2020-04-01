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
import hudson.security.*
import hudson.tasks.*
import jenkins.branch.*
import jenkins.plugins.git.*
import groovy.json.JsonSlurper
import org.jenkinsci.plugins.workflow.multibranch.*
import com.cloudbees.hudson.plugins.folder.computed.PeriodicFolderTrigger
import hudson.plugins.git.extensions.GitSCMExtension
import hudson.plugins.git.extensions.impl.PathRestriction
import hudson.plugins.git.extensions.impl.CloneOption
import hudson.plugins.git.extensions.impl.LocalBranch
import hudson.plugins.git.extensions.impl.CleanCheckout
import hudson.tasks.Shell
import hudson.plugins.git.GitSCM
import hudson.model.FreeStyleProject

import io.jenkins.plugins.argos.ArgosServiceConfiguration
import io.jenkins.plugins.argos.recorders.ArgosRecorder

import java.util.logging.Logger
import java.util.logging.Level

def Logger logger = Logger.getLogger("")

def instance = Jenkins.getInstance()

def job = instance.getJob("argos-test-app-pipeline")
    
if (job) {
    logger.info("--> project argos-test-app already defined, first delete it")
    job.delete();
}

job = instance.getJob("argos-test-app-freestyle-recording")
    
if (job) {
    logger.info("--> project argos-test-app already defined, first delete it")
    job.delete();
}
    
logger.info("--> set Argos Test App Project")
GitSCMSource scms = new GitSCMSource(null, "https://github.com/rabobank/argos-test-app.git", "", "*", "", false)

extensions = [];

CloneOption ext1 = new CloneOption(false, false, "", 5) 
extensions.add(ext1)

LocalBranch ext2 = new LocalBranch("**")
extensions.add(ext2)

CleanCheckout ext3 = new CleanCheckout()
extensions.add(ext3)

scms.setExtensions(extensions);

WorkflowMultiBranchProject mp = instance.createProject(WorkflowMultiBranchProject.class, "argos-test-app-pipeline");
mp.getSourcesList().add(new BranchSource(scms, new DefaultBranchPropertyStrategy(null)));

argosConfig = instance.getExtensionList(ArgosServiceConfiguration)[0]

argosConfig.setUrl("http://argos-service:8080")

FreeStyleProject fp = instance.createProject(FreeStyleProject.class, "argos-test-app-freestyle-recording")
fp.setScm(new GitSCM("https://github.com/rabobank/argos-test-app.git"))
argosRecorder = new ArgosRecorder("root_label.child_label:argos-test-app", "default-npa2", "segment 1", "build", '${BUILD_NUMBER}')
fp.getPublishersList().add(argosRecorder)
fp.getBuildersList().add(new Shell("mvn clean install"))

instance.save()
