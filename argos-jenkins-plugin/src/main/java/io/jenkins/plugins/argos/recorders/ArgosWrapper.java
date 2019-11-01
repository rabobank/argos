package io.jenkins.plugins.argos.recorders;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.rabobank.argos.argos4j.Argos4j;
import com.rabobank.argos.argos4j.Argos4jError;
import com.rabobank.argos.argos4j.Argos4jSettings;
import com.rabobank.argos.argos4j.SigningKey;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.security.ACL;
import hudson.tasks.BuildWrapperDescriptor;
import io.jenkins.plugins.argos.ArgosServiceConfiguration;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildWrapper;
import lombok.AllArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.plaincredentials.FileCredentials;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.Collections;

/**
 * Jenkins recorder plugin to output signed link metadata for Jenkins pipeline
 * steps.
 *
 * @author SantiagoTorres
 */
public class ArgosWrapper extends SimpleBuildWrapper {

    /**
     * CredentialId for the key to load.
     * <p>
     * If not defined signing will not be performed.
     */
    @DataBoundSetter
    public String privateKeyCredentialId;

    /**
     * Name of the step to execute.
     * <p>
     * If not defined, will default to step
     */
    @DataBoundSetter
    public String stepName;

    /**
     * The host URL/URI where to post the argos metdata.
     * <p>
     * Protocol information *must* be included.
     */
    @DataBoundSetter
    public String supplyChainId;

    private Argos4j argos4j;

    public String getPrivateKeyCredentialId() {
        return privateKeyCredentialId;
    }

    public String getStepName() {
        return stepName;
    }

    public String getSupplyChainId() {
        return supplyChainId;
    }

    @DataBoundConstructor
    public ArgosWrapper(String privateKeyCredentialId, String stepName, String supplyChainId) {
        this.privateKeyCredentialId = privateKeyCredentialId;
        this.stepName = stepName;
        this.supplyChainId = supplyChainId;
    }

    @PostConstruct
    public void init() {
        checkProperty(privateKeyCredentialId, "privateKeyCredentialId");
        checkProperty(stepName, "stepName");
        checkProperty(supplyChainId, "supplyChainId");
        String argosServiceBaseUrl = ArgosServiceConfiguration.get().getArgosServiceBaseUrl();
        checkProperty(argosServiceBaseUrl, "argosServiceBaseUrl");
        argos4j = new Argos4j(Argos4jSettings.builder().stepName(stepName).argosServerBaseUrl(argosServiceBaseUrl).signingKey(getSigningKey()).supplyChainId(supplyChainId).build());
    }

    private SigningKey getSigningKey() {
        try {
            return SigningKey.builder().pemKey(IOUtils.toByteArray(getCredentials().getContent())).build();
        } catch (IOException e) {
            throw new Argos4jError(e.getMessage(), e);
        }
    }

    private void checkProperty(String value, String fieldName) {
        if (StringUtils.isBlank(value)) {
            throw new Argos4jError(fieldName + " not configured");
        }
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
        argos4j.collectMaterials(new File(workspace.getRemote()));

        context.setDisposer(new PostWrap(argos4j));
    }

    private FileCredentials getCredentials() {
        FileCredentials fileCredential = CredentialsMatchers.firstOrNull(
                CredentialsProvider.lookupCredentials(
                        FileCredentials.class,
                        Jenkins.get(),
                        ACL.SYSTEM,
                        Collections.emptyList()
                ),
                CredentialsMatchers.withId(this.privateKeyCredentialId)
        );

        if (fileCredential == null)
            throw new Argos4jError(" Could not find credentials entry with ID '" + privateKeyCredentialId + "' ");

        return fileCredential;
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
    @Symbol("argos_wrap")
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

    @AllArgsConstructor
    private class PostWrap extends Disposer {

        private final Argos4j argos4j;


        @Override
        public void tearDown(Run<?, ?> build,
                             FilePath workspace,
                             Launcher launcher,
                             TaskListener listener) {
            argos4j.collectProducts(new File(workspace.getRemote()));
            listener.getLogger().println("[argos] uploading metadata to: " + argos4j.getSettings().getArgosServerBaseUrl());
            argos4j.store();
        }
    }
}
