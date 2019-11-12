package io.jenkins.plugins.argos.recorders;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.rabobank.argos.argos4j.Argos4j;
import com.rabobank.argos.argos4j.Argos4jError;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
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
import org.kohsuke.stapler.QueryParameter;

import java.io.File;
import java.util.Optional;

/**
 * Jenkins recorder plugin to output signed link metadata for Jenkins pipeline
 * steps.
 *
 */
public class ArgosRecorder extends Recorder {

    private String supplyChainId;
    /**
     * Credential id with private key to load.
     * <p>
     * If not defined signing will not be performed.
     */
    private String privateKeyCredentialId;

    /**
     * Name of the step to execute.
     * <p>
     * If not defined, will default to step
     */
    private String stepName;

    /**
     * Link metadata used to record this step
     */
    @XStreamOmitField
    private Argos4j argos4j;


    @DataBoundConstructor
    public ArgosRecorder(String supplyChainId, String privateKeyCredentialId, String stepName) {
        this.stepName = stepName;
        this.supplyChainId = supplyChainId;
        this.privateKeyCredentialId = privateKeyCredentialId;
    }


    @Override
    public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {

        String cwdStr = getCwdStr(build);

        listener.getLogger().println("[argos] Recording state before build " + cwdStr);
        listener.getLogger().println("[argos] using step name: " + stepName);

        argos4j = new ArgosJenkinsHelper(privateKeyCredentialId, stepName, supplyChainId).createArgos();

        argos4j.collectMaterials(new File(cwdStr));
        return true;
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
