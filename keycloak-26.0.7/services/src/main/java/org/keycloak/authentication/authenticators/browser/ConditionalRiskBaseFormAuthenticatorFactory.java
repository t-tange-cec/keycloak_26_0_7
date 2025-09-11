/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.authentication.authenticators.browser;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.credential.SecretQuestionCredentialModel;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.List;

import static java.util.Arrays.asList;
import static org.keycloak.authentication.authenticators.browser.ConditionalRiskBaseFormAuthenticator.DEFAULT_RISKBASE_OUTCOME;
import static org.keycloak.authentication.authenticators.browser.ConditionalRiskBaseFormAuthenticator.FORCE;
import static org.keycloak.authentication.authenticators.browser.ConditionalRiskBaseFormAuthenticator.FORCE_RISKBASE_FOR_HTTP_HEADER;
import static org.keycloak.authentication.authenticators.browser.ConditionalRiskBaseFormAuthenticator.FORCE_RISKBASE_ROLE;
import static org.keycloak.authentication.authenticators.browser.ConditionalRiskBaseFormAuthenticator.RISKBASE_CONTROL_USER_ATTRIBUTE;
import static org.keycloak.authentication.authenticators.browser.ConditionalRiskBaseFormAuthenticator.SKIP;
import static org.keycloak.authentication.authenticators.browser.ConditionalRiskBaseFormAuthenticator.SKIP_RISKBASE_FOR_HTTP_HEADER;
import static org.keycloak.authentication.authenticators.browser.ConditionalRiskBaseFormAuthenticator.SKIP_RISKBASE_ROLE;
import static org.keycloak.provider.ProviderConfigProperty.LIST_TYPE;
import static org.keycloak.provider.ProviderConfigProperty.ROLE_TYPE;
import static org.keycloak.provider.ProviderConfigProperty.STRING_TYPE;

/**
 * An {@link AuthenticatorFactory} for {@link ConditionalRiskBaseFormAuthenticator}s.
 *
 * @author <a href="mailto:thomas.darimont@gmail.com">Thomas Darimont</a>
 */
public class ConditionalRiskBaseFormAuthenticatorFactory implements AuthenticatorFactory {

    public static final String PROVIDER_ID = "auth-conditional-riskbase-form";

    public static final ConditionalRiskBaseFormAuthenticator SINGLETON = new ConditionalRiskBaseFormAuthenticator();

    @Override
    public Authenticator create(KeycloakSession session) {
        return SINGLETON;
    }

    @Override
    public void init(Config.Scope config) {
        //NOOP
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        //NOOP
    }

    @Override
    public void close() {
        //NOOP
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getReferenceCategory() {
        return SecretQuestionCredentialModel.TYPE;
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return true;
    }


    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public String getDisplayType() {
        return "Conditional Riskbase Form";
    }

    @Override
    public String getHelpText() {
        return "Validates a riskbase on a separate riskbase form. Only shown if required based on the configured conditions.";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {

        ProviderConfigProperty forceRiskBaseUserAttribute = new ProviderConfigProperty();
        forceRiskBaseUserAttribute.setType(STRING_TYPE);
        forceRiskBaseUserAttribute.setName(RISKBASE_CONTROL_USER_ATTRIBUTE);
        forceRiskBaseUserAttribute.setLabel("Riskbase control User Attribute");
        forceRiskBaseUserAttribute.setHelpText("The name of the user attribute to explicitly control RISKBASE auth. " +
                "If attribute value is 'force' then Riskbase is always required. " +
                "If value is 'skip' the Riskbase auth is skipped. Otherwise this check is ignored.");

        ProviderConfigProperty skipRiskBaseRole = new ProviderConfigProperty();
        skipRiskBaseRole.setType(ROLE_TYPE);
        skipRiskBaseRole.setName(SKIP_RISKBASE_ROLE);
        skipRiskBaseRole.setLabel("Skip RISKBASE for Role");
        skipRiskBaseRole.setHelpText("RISKBASE is always skipped if user has the given Role.");

        ProviderConfigProperty forceRiskBaseRole = new ProviderConfigProperty();
        forceRiskBaseRole.setType(ROLE_TYPE);
        forceRiskBaseRole.setName(FORCE_RISKBASE_ROLE);
        forceRiskBaseRole.setLabel("Force Riskbase for Role");
        forceRiskBaseRole.setHelpText("Riskbase is always required if user has the given Role.");

        ProviderConfigProperty skipRiskBaseForHttpHeader = new ProviderConfigProperty();
        skipRiskBaseForHttpHeader.setType(STRING_TYPE);
        skipRiskBaseForHttpHeader.setName(SKIP_RISKBASE_FOR_HTTP_HEADER);
        skipRiskBaseForHttpHeader.setLabel("Skip Riskbase for Header");
        skipRiskBaseForHttpHeader.setHelpText("Riskbase is skipped if a HTTP request header does matches the given pattern." +
                "Can be used to specify trusted networks via: X-Forwarded-Host: (1.2.3.4|1.2.3.5)." +
                "In this case requests from 1.2.3.4 and 1.2.3.5 come from a trusted source.");
        skipRiskBaseForHttpHeader.setDefaultValue("");

        ProviderConfigProperty forceRiskBaseForHttpHeader = new ProviderConfigProperty();
        forceRiskBaseForHttpHeader.setType(STRING_TYPE);
        forceRiskBaseForHttpHeader.setName(FORCE_RISKBASE_FOR_HTTP_HEADER);
        forceRiskBaseForHttpHeader.setLabel("Force Riskbase for Header");
        forceRiskBaseForHttpHeader.setHelpText("Riskbase required if a HTTP request header matches the given pattern.");
        forceRiskBaseForHttpHeader.setDefaultValue("");

        ProviderConfigProperty defaultOutcome = new ProviderConfigProperty();
        defaultOutcome.setType(LIST_TYPE);
        defaultOutcome.setName(DEFAULT_RISKBASE_OUTCOME);
        defaultOutcome.setLabel("Fallback Riskbase handling");
        defaultOutcome.setOptions(asList(SKIP, FORCE));
        defaultOutcome.setHelpText("What to do in case of every check abstains. Defaults to force RISKBASE authentication.");

        return asList(forceRiskBaseUserAttribute, skipRiskBaseRole, forceRiskBaseRole, skipRiskBaseForHttpHeader, forceRiskBaseForHttpHeader, defaultOutcome);
    }
}
