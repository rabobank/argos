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
import java.net.MalformedURLException;
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

    private String url;
    
    @DataBoundConstructor
    public ArgosServiceConfiguration(String url) {
        this.url = url;
    }

    public ArgosServiceConfiguration() {
        // When Jenkins is restarted, load any saved configuration from disk.
        load();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
        save();
    }

    public FormValidation doCheckUrl(@QueryParameter String value) {
        if (StringUtils.isEmpty(value)) {
            return FormValidation.warning("Please specify a hostname.");
        }
        return FormValidation.ok();
    }
     

    public FormValidation doValidateConnection(@QueryParameter String url) {
        FormValidation formValidation;
        String inputUrl = url + "/actuator/health";
        try {
            URL conUrl = new URL(inputUrl);
            HttpURLConnection con = (HttpURLConnection) conUrl.openConnection();
            con.setRequestMethod("GET");
            if (con.getResponseCode() == 200) {
                formValidation = FormValidation.ok("Your In argos Service instance [%s] is alive!", conUrl);
            } else {
                formValidation = FormValidation.error("status code " + con.getResponseCode() + " on " + conUrl);
            }
            con.disconnect();
        } catch (IOException e) {
            formValidation = FormValidation.error("Error " + e.getMessage() + " on " + inputUrl);
        }
        return formValidation;
    }

    public String getArgosServiceBaseUrl() {
        return this.url;
    }
}
