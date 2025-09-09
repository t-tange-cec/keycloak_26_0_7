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

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;

import jakarta.ws.rs.core.MultivaluedMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.keycloak.authentication.authenticators.browser.ConditionalRiskBaseFormAuthenticator.RiskBaseDecision.ABSTAIN;
import static org.keycloak.authentication.authenticators.browser.ConditionalRiskBaseFormAuthenticator.RiskBaseDecision.SHOW_RISKBASE;
import static org.keycloak.authentication.authenticators.browser.ConditionalRiskBaseFormAuthenticator.RiskBaseDecision.SKIP_RISKBASE;
import static org.keycloak.models.utils.KeycloakModelUtils.getRoleFromString;

public class ConditionalRiskBaseFormAuthenticator extends RiskBaseFormAuthenticator {

    public static final String SKIP = "skip";

    public static final String FORCE = "force";

    public static final String RISKBASE_CONTROL_USER_ATTRIBUTE = "RiskBaseControlAttribute";

    public static final String SKIP_RISKBASE_ROLE = "skipRiskBaseRole";

    public static final String FORCE_RISKBASE_ROLE = "forceRiskBaseRole";

    public static final String SKIP_RISKBASE_FOR_HTTP_HEADER = "noRiskBaseRequiredForHeaderPattern";

    public static final String FORCE_RISKBASE_FOR_HTTP_HEADER = "forceRiskBaseForHeaderPattern";

    public static final String DEFAULT_RISKBASE_OUTCOME = "defaultRiskBaseOutcome";

    enum RiskBaseDecision {
        SKIP_RISKBASE, SHOW_RISKBASE, ABSTAIN
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {

        Map<String, String> config = context.getAuthenticatorConfig().getConfig();

        if (tryConcludeBasedOn(voteForUserRiskBaseControlAttribute(context.getUser(), config), context)) {
            return;
        }

        if (tryConcludeBasedOn(voteForUserRole(context.getRealm(), context.getUser(), config), context)) {
            return;
        }

        if (tryConcludeBasedOn(voteForHttpHeaderMatchesPattern(context.getHttpRequest().getHttpHeaders().getRequestHeaders(), config), context)) {
            return;
        }

        if (tryConcludeBasedOn(voteForDefaultFallback(config), context)) {
            return;
        }

        showRiskBaseForm(context);
    }

    private RiskBaseDecision voteForDefaultFallback(Map<String, String> config) {

        if (!config.containsKey(DEFAULT_RISKBASE_OUTCOME)) {
            return ABSTAIN;
        }

        switch (config.get(DEFAULT_RISKBASE_OUTCOME)) {
            case SKIP:
                return SKIP_RISKBASE;
            case FORCE:
                return SHOW_RISKBASE;
            default:
                return ABSTAIN;
        }
    }

    private boolean tryConcludeBasedOn(RiskBaseDecision state, AuthenticationFlowContext context) {

        switch (state) {

            case SHOW_RISKBASE:
                showRiskBaseForm(context);
                return true;

            case SKIP_RISKBASE:
                context.success();
                return true;

            default:
                return false;
        }
    }

    private boolean tryConcludeBasedOn(RiskBaseDecision state) {

        switch (state) {

            case SHOW_RISKBASE:
                return true;

            case SKIP_RISKBASE:
                return false;

            default:
                return false;
        }
    }

    private void showRiskBaseForm(AuthenticationFlowContext context) {
        super.authenticate(context);
    }

    private RiskBaseDecision voteForUserRiskBaseControlAttribute(UserModel user, Map<String, String> config) {

        if (!config.containsKey(RISKBASE_CONTROL_USER_ATTRIBUTE)) {
            return ABSTAIN;
        }

        String attributeName = config.get(RISKBASE_CONTROL_USER_ATTRIBUTE);
        if (attributeName == null) {
            return ABSTAIN;
        }

        Optional<String> value = user.getAttributeStream(attributeName).findFirst();
        if (!value.isPresent()) {
            return ABSTAIN;
        }

        switch (value.get().trim()) {
            case SKIP:
                return SKIP_RISKBASE;
            case FORCE:
                return SHOW_RISKBASE;
            default:
                return ABSTAIN;
        }
    }

    private RiskBaseDecision voteForHttpHeaderMatchesPattern(MultivaluedMap<String, String> requestHeaders, Map<String, String> config) {

        if (!config.containsKey(FORCE_RISKBASE_FOR_HTTP_HEADER) && !config.containsKey(SKIP_RISKBASE_FOR_HTTP_HEADER)) {
            return ABSTAIN;
        }

        //Inverted to allow white-lists, e.g. for specifying trusted remote hosts: X-Forwarded-Host: (1.2.3.4|1.2.3.5)
        if (containsMatchingRequestHeader(requestHeaders, config.get(SKIP_RISKBASE_FOR_HTTP_HEADER))) {
            return SKIP_RISKBASE;
        }

        if (containsMatchingRequestHeader(requestHeaders, config.get(FORCE_RISKBASE_FOR_HTTP_HEADER))) {
            return SHOW_RISKBASE;
        }

        return ABSTAIN;
    }

    private boolean containsMatchingRequestHeader(MultivaluedMap<String, String> requestHeaders, String headerPattern) {

        if (headerPattern == null) {
            return false;
        }

        //TODO cache RequestHeader Patterns
        //TODO how to deal with pattern syntax exceptions?
        // need CASE_INSENSITIVE flag so that we also have matches when the underlying container use a different case than what
        // is usually expected (e.g.: vertx)
        Pattern pattern = Pattern.compile(headerPattern, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

        for (Map.Entry<String, List<String>> entry : requestHeaders.entrySet()) {

            String key = entry.getKey();

            for (String value : entry.getValue()) {

                String headerEntry = key.trim() + ": " + value.trim();

                if (pattern.matcher(headerEntry).matches()) {
                    return true;
                }
            }
        }

        return false;
    }

    private RiskBaseDecision voteForUserRole(RealmModel realm, UserModel user, Map<String, String> config) {

        if (!config.containsKey(SKIP_RISKBASE_ROLE) && !config.containsKey(FORCE_RISKBASE_ROLE)) {
            return ABSTAIN;
        }

        if (userHasRole(realm, user, config.get(SKIP_RISKBASE_ROLE))) {
            return SKIP_RISKBASE;
        }

        if (userHasRole(realm, user, config.get(FORCE_RISKBASE_ROLE))) {
            return SHOW_RISKBASE;
        }

        return ABSTAIN;
    }

    private boolean userHasRole(RealmModel realm, UserModel user, String roleName) {

        if (roleName == null) {
            return false;
        }

        RoleModel role = getRoleFromString(realm, roleName);
        if (role != null) {
            return user.hasRole(role);
        }
        return false;
    }

    private boolean isRiskBaseRequired(KeycloakSession session, RealmModel realm, UserModel user) {
        MultivaluedMap<String, String> requestHeaders = session.getContext().getRequestHeaders().getRequestHeaders();
        return realm.getAuthenticatorConfigsStream().anyMatch(configModel -> {
            if (tryConcludeBasedOn(voteForUserRiskBaseControlAttribute(user, configModel.getConfig()))) {
                return true;
            }
            if (tryConcludeBasedOn(voteForUserRole(realm, user, configModel.getConfig()))) {
                return true;
            }
            if (tryConcludeBasedOn(voteForHttpHeaderMatchesPattern(requestHeaders, configModel.getConfig()))) {
                return true;
            }
            if (configModel.getConfig().get(DEFAULT_RISKBASE_OUTCOME) != null
                    && configModel.getConfig().get(DEFAULT_RISKBASE_OUTCOME).equals(FORCE)
                    && configModel.getConfig().size() <= 1) {
                return true;
            }
            if (containsConditionalRiskBaseConfig(configModel.getConfig())
                && voteForUserRiskBaseControlAttribute(user, configModel.getConfig()) == ABSTAIN
                && voteForUserRole(realm, user, configModel.getConfig()) == ABSTAIN
                && voteForHttpHeaderMatchesPattern(requestHeaders, configModel.getConfig()) == ABSTAIN
                && (voteForDefaultFallback(configModel.getConfig()) == SHOW_RISKBASE
                    || voteForDefaultFallback(configModel.getConfig()) == ABSTAIN)) {
                return true;
            }
            return false;
        });
    }

    private boolean containsConditionalRiskBaseConfig(Map config) {
        return config.containsKey(RISKBASE_CONTROL_USER_ATTRIBUTE)
            || config.containsKey(SKIP_RISKBASE_ROLE)
            || config.containsKey(FORCE_RISKBASE_ROLE)
            || config.containsKey(SKIP_RISKBASE_FOR_HTTP_HEADER)
            || config.containsKey(FORCE_RISKBASE_FOR_HTTP_HEADER)
            || config.containsKey(DEFAULT_RISKBASE_OUTCOME);
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        if (!isRiskBaseRequired(session, realm, user)) {
            user.removeRequiredAction(UserModel.RequiredAction.CONFIGURE_RISKBASE);
        } else if (user.getRequiredActionsStream().noneMatch(UserModel.RequiredAction.CONFIGURE_RISKBASE.name()::equals)) {
            user.addRequiredAction(UserModel.RequiredAction.CONFIGURE_RISKBASE.name());
        }
    }
}
