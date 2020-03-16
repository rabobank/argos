/*
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
package io.jenkins.plugins.argos.recorders;

import com.rabobank.argos.argos4j.FileCollector;
import com.rabobank.argos.argos4j.LinkBuilder;
import com.rabobank.argos.argos4j.LocalFileCollector;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildWrapperDescriptor;
import jenkins.tasks.SimpleBuildWrapper;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;


/**
 * Jenkins recorder plugin to output signed link metadata for Jenkins pipeline
 * steps.
 */
public class ArgosWrapper extends SimpleBuildWrapper implements Serializable {

    /**
     * CredentialId for the key to load.
     * <p>
     * If not defined signing will not be performed.
     */
    @DataBoundSetter
    public String privateKeyCredentialId;

    /**
     * Name of the segment to execute.
     */
    @DataBoundSetter
    public String layoutSegmentName;

    /**
     * Name of the step to execute.
     */
    @DataBoundSetter
    public String stepName;

    /**
     * The host URL/URI where to post the argos metdata.
     * <p>
     * Protocol information *must* be included.
     */
    @DataBoundSetter
    public String supplyChainIdentifier;

    /**
     * Run Id of the pipeline
     */
    @DataBoundSetter
    public String runId;

    private LinkBuilder argosLinkBuilder;

    @DataBoundConstructor
    public ArgosWrapper(String privateKeyCredentialId, String stepName, String layoutSegmentName, String supplyChainIdentifier, String runId) {
        this.privateKeyCredentialId = privateKeyCredentialId;
        this.layoutSegmentName = layoutSegmentName;
        this.stepName = stepName;
        this.supplyChainIdentifier = supplyChainIdentifier;
        this.runId = runId;
    }

    @Override
    public void setUp(SimpleBuildWrapper.Context context,
                      Run<?, ?> build,
                      FilePath workspace,
                      Launcher launcher,
                      TaskListener listener,
                      EnvVars initialEnvironment) {

        listener.getLogger().println("[argos] wrapping step ");
        listener.getLogger().println("[argos] using step name: " + this.stepName);


        listener.getLogger().println("[argos] creating metadata... ");
        argosLinkBuilder = new ArgosJenkinsHelper(privateKeyCredentialId, layoutSegmentName, stepName, supplyChainIdentifier, runId).createArgosLinkBuilder();

        argosLinkBuilder.collectMaterials(createFileCollector(workspace));

        context.setDisposer(new PostWrap());
    }


    /**
     * Descriptor for {@link ArgosRecorder}. Used as a singleton. The class is
     * marked as public so that it can be accessed from views.
     * <p>
     * <p>
     * See
     * <tt>src/main/resources/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension
    @Symbol("argosWrapper")
    public static final class DescriptorImpl extends BuildWrapperDescriptor {

        public DescriptorImpl() {
            super(ArgosWrapper.class);
            load();
        }

        @Override
        public String getDisplayName() {
            return "argos record wrapper";
        }

        @Override
        public boolean isApplicable(AbstractProject<?, ?> item) {
            return true;
        }

    }

    private class PostWrap extends Disposer {

        @Override
        public void tearDown(Run<?, ?> build,
                             FilePath workspace,
                             Launcher launcher,
                             TaskListener listener) {
            argosLinkBuilder.collectProducts(createFileCollector(workspace));
            listener.getLogger().println("[argos] uploading metadata to: " + argosLinkBuilder.getSettings().getArgosServerBaseUrl());
            argosLinkBuilder.store(ArgosJenkinsHelper.getPrivateKeyPassword(privateKeyCredentialId));
        }
    }

    private FileCollector createFileCollector(FilePath workspace) {
        Path path = new File(workspace.getRemote()).toPath();
        return LocalFileCollector.builder()
                .basePath(path)
                .path(path)
                .build();
    }
}
