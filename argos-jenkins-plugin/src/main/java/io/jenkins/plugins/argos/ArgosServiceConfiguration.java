package io.jenkins.plugins.argos;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import hudson.Extension;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;

@Extension
@Slf4j
public class ArgosServiceConfiguration extends GlobalConfiguration {
    public final String url = "foo";

    /**
     * @return the singleton instance
     */
    public static ArgosServiceConfiguration get() {
        return GlobalConfiguration.all().get(ArgosServiceConfiguration.class);
    }

    private String hostname;

    private Integer port;

    private Boolean secure;

    @DataBoundConstructor
    public ArgosServiceConfiguration(String hostname, Integer port, Boolean secure) {
        this.hostname = hostname;
        this.port = port;
        this.secure = secure;
    }

    public ArgosServiceConfiguration() {
        // When Jenkins is restarted, load any saved configuration from disk.
        load();
    }

    public Integer getPort() {
        return port;
    }


    public void setPort(Integer port) {
        this.port = port;
        save();
    }

    public Boolean isSecure() {
        return secure;
    }

    public void setSecure(Boolean secure) {
        this.secure = secure;
        save();
    }

    public String getUrl() {
        return url;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
        save();
    }

    public FormValidation doCheckHostname(@QueryParameter String value) {
        if (StringUtils.isEmpty(value)) {
            return FormValidation.warning("Please specify a hostname.");
        }
        return FormValidation.ok();
    }

    public FormValidation doValidateConnection(@QueryParameter String hostname, @QueryParameter int port,
                                               @QueryParameter boolean secure) throws IOException {
        String url = determineUrl(hostname, port, secure);
        HttpRequest request = new NetHttpTransport().createRequestFactory().buildGetRequest(new GenericUrl(url));
        HttpResponse response = request.execute();
        log.info("{}",response.parseAsString());
        return FormValidation.ok("Your In argos Service instance [%s] is alive!", url);
    }

    private String determineUrl(String hostname, int port, boolean secure) {
        String protocol = secure ? "https://" : "http://";
        return protocol + hostname + ":" + port + "/api";
    }

    public FormValidation doCheckPort(@QueryParameter String value) {
        if (StringUtils.isEmpty(value)) {
            return FormValidation.warning("Please specify a port.");
        }
        try {
            Integer.valueOf(value);
        } catch (NumberFormatException exc) {
            return FormValidation.warning("[%s] is not a number.", value);
        }
        if (port <= 0) {
            return FormValidation.warning("0 or negative port isn't allowed.");
        }
        return FormValidation.ok();
    }

    public String getArgosServiceBaseUrl() {
        return determineUrl(hostname, port, secure);
    }
}
