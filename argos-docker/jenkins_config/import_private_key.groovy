import jenkins.model.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.plugins.credentials.CredentialsScope
import org.apache.commons.fileupload.disk.DiskFileItem
import org.apache.commons.fileupload.disk.DiskFileItemFactory
import org.jenkinsci.plugins.plaincredentials.impl.FileCredentialsImpl
import com.cloudbees.plugins.credentials.SecretBytes
import groovy.json.JsonSlurper
import java.util.logging.Logger
import java.util.logging.Level
import java.nio.file.Files

def Logger logger = Logger.getLogger("")

store = Jenkins.instance.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()
    
// Load private key
factory = new DiskFileItemFactory()
dfi = factory.createItem("", "application/octet-stream", false, "bob")
out = dfi.getOutputStream()
file = new File("/var/jenkins_home/bob")
Files.copy(file.toPath(), out)
secretFile = new FileCredentialsImpl(
    CredentialsScope.GLOBAL,
    "bob",
    "bobs key",
    dfi, // Don't use FileItem
    "",
    "")
    
    
domain = Domain.global()
    
// Add credentials to Jenkins credential store
store.addCredentials(domain, secretFile)

logger.info("--> Bob's private key added.")
