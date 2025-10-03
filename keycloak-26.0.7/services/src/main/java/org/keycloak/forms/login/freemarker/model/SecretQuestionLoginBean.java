/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates
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
 *
 */

package org.keycloak.forms.login.freemarker.model;

import java.util.List;
import java.util.stream.Collectors;

import org.keycloak.credential.CredentialModel;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.credential.SecretQuestionCredentialProvider;
import org.keycloak.credential.SecretQuestionCredentialProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.SecretQuestionCredentialModel;

/**
 * Used for Secret Question login
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class SecretQuestionLoginBean {

    private final String selectedCredentialId;
    private final List<SecretQuestionCredential> userSecretQuestionCredentials;

    public SecretQuestionLoginBean(KeycloakSession session, RealmModel realm, UserModel user, String selectedCredentialId) {

        this.userSecretQuestionCredentials = user.credentialManager().getStoredCredentialsByTypeStream(SecretQuestionCredentialModel.TYPE)
                .map(SecretQuestionCredential::new)
                .collect(Collectors.toList());

        // This means user did not yet manually selected any Secret Question credential through the UI. So just go with the default one with biggest priority
        if (selectedCredentialId == null || selectedCredentialId.isEmpty()) {
        	SecretQuestionCredentialProvider secretQuestionCredentialProvider = (SecretQuestionCredentialProvider)session.getProvider(CredentialProvider.class, SecretQuestionCredentialProviderFactory.PROVIDER_ID);
        	SecretQuestionCredentialModel secretQuestionCredential = secretQuestionCredentialProvider
                    .getDefaultCredential(session, realm, user);

            selectedCredentialId = secretQuestionCredential==null ? null : secretQuestionCredential.getId();
        }

        this.selectedCredentialId = selectedCredentialId;
    }


    public List<SecretQuestionCredential> getUserSecretQuestionCredentials() {
        return userSecretQuestionCredentials; 
    }

    public String getSelectedCredentialId() {
        return selectedCredentialId;
    }


    public static class SecretQuestionCredential {

        private final String id;
        private final String userLabel;

        public SecretQuestionCredential(CredentialModel credentialModel) {
            this.id = credentialModel.getId();
            // TODO: "Unnamed" OTP credentials should be displayed in the UI in gray
            this.userLabel = credentialModel.getUserLabel() == null || credentialModel.getUserLabel().isEmpty() ? "unnamed" : credentialModel.getUserLabel();
        }

        public String getId() {
            return id;
        }

        public String getUserLabel() {
            return userLabel;
        }
    }
}
