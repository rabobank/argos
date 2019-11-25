import hudson.model.FreeStyleProject
import hudson.plugins.git.GitSCM
import hudson.plugins.git.extensions.impl.CleanCheckout
import hudson.plugins.git.extensions.impl.CloneOption
import hudson.plugins.git.extensions.impl.LocalBranch
import hudson.tasks.Shell
import io.jenkins.plugins.argos.ArgosServiceConfiguration
import io.jenkins.plugins.argos.recorders.ArgosRecorder
import jenkins.branch.*
import jenkins.plugins.git.*
import org.jenkinsci.plugins.workflow.multibranch.*

import java.util.logging.Logger

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

argosConfig.setHostname("argos-service")
argosConfig.setPort(8080)

FreeStyleProject fp = instance.createProject(FreeStyleProject.class, "argos-test-app-freestyle-recording")
fp.setScm(new GitSCM("https://github.com/rabobank/argos-test-app.git"))
argosRecorder = new ArgosRecorder("argos-test-app", "bob", "build", '${BUILD_NUMBER}')
fp.getPublishersList().add(argosRecorder)
fp.getBuildersList().add(new Shell("mvn clean install"))

instance.save()
