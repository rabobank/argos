package io.jenkins.plugins.argos.recorders;

import com.rabobank.argos.argos4j.Argos4j;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
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

/**
 * Jenkins recorder plugin to output signed link metadata for Jenkins pipeline
 * steps.
 *
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

    @XStreamOmitField
    private Argos4j argos4j;

    @DataBoundConstructor
    public ArgosWrapper(String privateKeyCredentialId, String stepName, String supplyChainId) {
        this.privateKeyCredentialId = privateKeyCredentialId;
        this.stepName = stepName;
        this.supplyChainId = supplyChainId;
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
        argos4j = new ArgosJenkinsHelper(privateKeyCredentialId, stepName, supplyChainId).createArgos();

        argos4j.collectMaterials(new File(workspace.getRemote()));

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
            argos4j.collectProducts(new File(workspace.getRemote()));
            listener.getLogger().println("[argos] uploading metadata to: " + argos4j.getSettings().getArgosServerBaseUrl());
            argos4j.store();
        }
    }
}
