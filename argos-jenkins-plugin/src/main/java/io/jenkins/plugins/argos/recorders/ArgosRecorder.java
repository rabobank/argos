/*
 * Copyright (C) 2019 Rabobank Nederland
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
package io.jenkins.plugins.argos.recorders;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.rabobank.argos.argos4j.Argos4j;
import com.rabobank.argos.argos4j.Argos4jError;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.plaincredentials.FileCredentials;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * Jenkins recorder plugin to output signed link metadata for Jenkins pipeline
 * steps.
 *
 */
public class ArgosRecorder extends Recorder {

    @DataBoundSetter
    private String supplyChainName;
    /**
     * Credential id with private key to load.
     * <p>
     * If not defined signing will not be performed.
     */
    @DataBoundSetter
    private String privateKeyCredentialId;

    /**
     * Name of the step to execute.
     * <p>
     * If not defined, will default to step
     */
    @DataBoundSetter
    private String stepName;

    /**
     * Run Id of the pipeline
     */
    @DataBoundSetter
    private String runId;

    /**
     * Link metadata used to record this step
     */
    private Argos4j argos4j;


    @DataBoundConstructor
    public ArgosRecorder(String supplyChainName, String privateKeyCredentialId, String stepName, String runId) {
        this.stepName = stepName;
        this.supplyChainName = supplyChainName;
        this.privateKeyCredentialId = privateKeyCredentialId;
        this.runId = runId;
    }

    public String getSupplyChainName() {
        return supplyChainName;
    }

    public String getPrivateKeyCredentialId() {
        return privateKeyCredentialId;
    }

    public String getStepName() {
        return stepName;
    }

    public String getRunId() {
        return runId;
    }

    @Override
    public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
        try {
            String cwdStr = getCwdStr(build);
            listener.getLogger().println("[argos] Recording state before build " + cwdStr);
            listener.getLogger().println("[argos] using step name: " + stepName);

            EnvVars environment = build.getEnvironment(listener);
            argos4j = new ArgosJenkinsHelper(
                    environment.expand(privateKeyCredentialId),
                    environment.expand(stepName),
                    environment.expand(supplyChainName),
                    environment.expand(runId)).createArgos();

            argos4j.collectMaterials(new File(cwdStr));
            return true;
        } catch (IOException e) {
            throw new Argos4jError(e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private String getCwdStr(AbstractBuild<?, ?> build) {
        return Optional.ofNullable(build.getWorkspace()).map(FilePath::getRemote).orElseThrow(() -> new Argos4jError("[argos] Cannot get the build workspace"));
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {

        listener.getLogger().println("[argos] Recording state after build ");

        argos4j.collectProducts(new File(getCwdStr(build)));
        listener.getLogger().println("[argos] Dumping metadata to: " + argos4j.getSettings().getArgosServerBaseUrl());


        argos4j.store();
        return true;
    }


    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }


    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @SuppressWarnings("rawtypes")
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        @Override
        public String getDisplayName() {
            return "argos provenance plugin";
        }

        /**
         * populating the private key credentialId drop-down list
         */
        public ListBoxModel doFillPrivateKeyCredentialIdItems(@AncestorInPath Item item, @QueryParameter String privateKeyCredentialId) {

            StandardListBoxModel result = new StandardListBoxModel();
            if (item == null) {
                if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
                    return result.includeCurrentValue(privateKeyCredentialId);
                }
            } else {
                if (!item.hasPermission(Item.EXTENDED_READ)
                        && !item.hasPermission(CredentialsProvider.USE_ITEM)) {
                    return result.includeCurrentValue(privateKeyCredentialId);
                }
            }
            return result
                    .includeEmptyValue()
                    .includeAs(ACL.SYSTEM,
                            Jenkins.get(),
                            FileCredentials.class)
                    .includeCurrentValue(privateKeyCredentialId);
        }

        /**
         * validating the credentialId
         */
        public FormValidation doCheckprivateKeyCredentialId(@AncestorInPath Item item, @QueryParameter String value) {
            if (item == null) {
                if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
                    return FormValidation.ok();
                }
            } else {
                if (!item.hasPermission(Item.EXTENDED_READ) && !item.hasPermission(CredentialsProvider.USE_ITEM)) {
                    return FormValidation.ok();
                }
            }
            if (StringUtils.isBlank(value)) {
                return FormValidation.ok();
            }
            return FormValidation.ok();
        }
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }
}
