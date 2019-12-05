/**
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
package io.jenkins.plugins.argos;

import hudson.Extension;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@Extension
@Slf4j
public class ArgosServiceConfiguration extends GlobalConfiguration {

    /**
     * @return the singleton instance
     */
    public static ArgosServiceConfiguration get() {
        return GlobalConfiguration.all().get(ArgosServiceConfiguration.class);
    }

    private String hostname;

    private Integer port;

    private boolean secure;

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

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
        save();
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
                                               @QueryParameter boolean secure) {
        FormValidation formValidation;
        String spec = determineUrl(hostname, port, secure) + "/actuator/health";
        try {
            URL url = new URL(spec);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            if (con.getResponseCode() == 200) {
                formValidation = FormValidation.ok("Your In argos Service instance [%s] is alive!", url);
            } else {
                formValidation = FormValidation.error("status code " + con.getResponseCode() + "on" + url);
            }
            con.disconnect();
        } catch (IOException e) {
            formValidation = FormValidation.error("Error " + e.getMessage() + "on" + spec);
        }
        return formValidation;
    }

    private String determineUrl(String hostname, int port, boolean secure) {
        String protocol = secure ? "https://" : "http://";
        return protocol + hostname + ":" + port;
    }

    public FormValidation doCheckPort(@QueryParameter String value) {
        if (StringUtils.isEmpty(value)) {
            return FormValidation.warning("Please specify a port.");
        }
        try {
            Integer.parseInt(value);
        } catch (NumberFormatException exc) {
            return FormValidation.warning("[%s] is not a number.", value);
        }
        if (Integer.parseInt(value) <= 0) {
            return FormValidation.warning("0 or negative port isn't allowed.");
        }
        return FormValidation.ok();
    }

    public String getArgosServiceBaseUrl() {
        return determineUrl(hostname, port, secure);
    }
}
