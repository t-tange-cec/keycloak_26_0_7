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

package org.keycloak.authentication.requiredactions;

import org.keycloak.Config;
import org.keycloak.authentication.AuthenticatorUtil;
import org.keycloak.authentication.CredentialRegistrator;
import org.keycloak.authentication.InitiatedActionSupport;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.credential.CredentialModel;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.credential.SecretQuestionCredentialProvider;
import org.keycloak.events.Details;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.UserModel;
import org.keycloak.models.Constants;
import org.keycloak.models.credential.SecretQuestionCredentialModel;
import org.keycloak.models.utils.CredentialValidation;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.validation.Validation;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.utils.CredentialHelper;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class UpdateSecretQuestion implements RequiredActionProvider, RequiredActionFactory, CredentialRegistrator {
    @Override
    public InitiatedActionSupport initiatedActionSupport() {
        return InitiatedActionSupport.SUPPORTED;
    }

    @Override
    public void evaluateTriggers(RequiredActionContext context) {
    }

    @Override
    public void requiredActionChallenge(RequiredActionContext context) {
        Response challenge = context.form()
                .setAttribute("mode", context.getUriInfo().getQueryParameters().getFirst("mode"))
                .createResponse(UserModel.RequiredAction.CONFIGURE_SECRET_QUESTION);
        context.challenge(challenge);
    }

    @Override
    public void processAction(RequiredActionContext context) {
        EventBuilder event = context.getEvent();
        event.event(EventType.UPDATE_CREDENTIAL);
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        String answer = formData.getFirst("secretAnswer");
        String qid = formData.getFirst("qid");
        String userLabel = formData.getFirst("userLabel");

        SecretQuestionCredentialModel credentialModel = SecretQuestionCredentialModel.createFromAnswer(answer);
        event.detail(Details.CREDENTIAL_TYPE, credentialModel.getType());

        EventBuilder deprecatedEvent = event.clone().event(EventType.UPDATE_SECRET_QUESTION);
        if (Validation.isBlank(answer)) {
            Response challenge = context.form()
                    .addError(new FormMessage(Validation.FIELD_SECRET_QUESTION, Messages.MISSING_SECRET_QUESTION))
                    .createResponse(UserModel.RequiredAction.CONFIGURE_SECRET_QUESTION);
            context.challenge(challenge);
            return;
        }
        SecretQuestionCredentialProvider secretQuestionCredentialProvider = (SecretQuestionCredentialProvider) context.getSession().getProvider(CredentialProvider.class, "keycloak-secret-question");
        final Stream<CredentialModel> secretQuestionCredentials  = (secretQuestionCredentialProvider.isConfiguredFor(context.getRealm(), context.getUser()))
            ? context.getUser().credentialManager().getStoredCredentialsByTypeStream(SecretQuestionCredentialModel.TYPE)
            : Stream.empty();
        if (secretQuestionCredentials.count() >= 1 && Validation.isBlank(userLabel)) {
            Response challenge = context.form()
                    .addError(new FormMessage(Validation.FIELD_SECRET_QUESTION, Messages.MISSING_SECRET_QUESTION))
                    .createResponse(UserModel.RequiredAction.CONFIGURE_SECRET_QUESTION);
            context.challenge(challenge);
            return;
        }

        if ("on".equals(formData.getFirst("logout-sessions"))) {
            AuthenticatorUtil.logoutOtherSessions(context);
        }

        if (!CredentialHelper.createSecretQuestionCredential(context.getSession(), context.getRealm(), context.getUser(), answer, credentialModel)) {
            Response challenge = context.form()
                    .addError(new FormMessage(Validation.FIELD_SECRET_QUESTION, Messages.INVALID_SECRET_QUESTION))
                    .createResponse(UserModel.RequiredAction.CONFIGURE_SECRET_QUESTION);
            context.challenge(challenge);
            return;
        }
        context.success();
        deprecatedEvent.success();
    }


    // Use separate method, so it's possible to override in the custom provider
    protected boolean validateSequetQuestionCredential(RequiredActionContext context, String token, SecretQuestionCredentialModel credentialModel) {
        return true;
    }


    @Override
    public void close() {

    }

    @Override
    public RequiredActionProvider create(KeycloakSession session) {
        return this;
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public String getDisplayText() {
        return "Update Secret Question";
    }


    @Override
    public String getId() {
        return UserModel.RequiredAction.CONFIGURE_SECRET_QUESTION.name();
    }

    @Override
    public String getCredentialType(KeycloakSession session, AuthenticationSessionModel authenticationSession) {
        return SecretQuestionCredentialModel.TYPE;
    }

    @Override
    public boolean isOneTimeAction() {
        return true;
    }
}
