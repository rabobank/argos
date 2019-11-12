import hudson.security.csrf.DefaultCrumbIssuer
import jenkins.branch.*
import jenkins.model.Jenkins
import jenkins.plugins.git.*
import org.jenkinsci.plugins.workflow.multibranch.*

import java.util.logging.Logger

Logger logger = Logger.getLogger("")

def instance = Jenkins.getInstance()

// set CSRF protection
instance.setCrumbIssuer(new DefaultCrumbIssuer(true))

def hudsonRealm = new HudsonPrivateSecurityRealm(false)
hudsonRealm.createAccount('admin','admin')
instance.setSecurityRealm(hudsonRealm)

def strategy = new FullControlOnceLoggedInAuthorizationStrategy()
instance.setAuthorizationStrategy(strategy)

instance.save()

logger.info("--> CSRF protection set ")

instance.save()
