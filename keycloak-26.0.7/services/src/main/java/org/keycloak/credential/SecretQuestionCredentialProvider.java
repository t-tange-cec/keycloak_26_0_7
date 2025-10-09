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
package org.keycloak.credential;

import org.jboss.logging.Logger;
import org.keycloak.common.util.Base64;
import org.keycloak.common.util.ObjectUtil;
import org.keycloak.common.util.Time;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.SingleUseObjectProvider;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.SecretQuestionCredentialModel;
import org.keycloak.models.credential.dto.SecretQuestionCredentialData;
import org.keycloak.models.credential.dto.SecretQuestionSecretData;
import org.keycloak.storage.UserCredentialStore;

import java.util.List;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
/*
 * ユーザーの秘密の質問資格情報（Credential）を管理・操作するためのプロバイダーです。
 * ]*/
public class SecretQuestionCredentialProvider implements CredentialProvider<CredentialModel>, CredentialInputValidator/*, OnUserCache*/ {
    private static final Logger logger = Logger.getLogger(SecretQuestionCredentialProvider.class);

    protected KeycloakSession session;

    public SecretQuestionCredentialProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public CredentialModel createCredential(RealmModel realm, UserModel user, CredentialModel credentialModel) {
        if (credentialModel.getCreatedDate() == null) {
            credentialModel.setCreatedDate(Time.currentTimeMillis());
        }
        return user.credentialManager().createStoredCredential(credentialModel);
    }

    @Override
    public boolean deleteCredential(RealmModel realm, UserModel user, String credentialId) {
        return user.credentialManager().removeStoredCredentialById(credentialId);
    }

    @Override
    public SecretQuestionCredentialModel getCredentialFromModel(CredentialModel model) {
        return SecretQuestionCredentialModel.createFromCredentialModel(model);
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return getType().equals(credentialType);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        if (!supportsCredentialType(credentialType)) return false;
        return user.credentialManager().getStoredCredentialsByTypeStream(credentialType).findAny().isPresent();
    }

    public boolean isConfiguredFor(RealmModel realm, UserModel user){
        return isConfiguredFor(realm, user, getType());
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput credentialInput) {
        if (!(credentialInput instanceof UserCredentialModel)) {
            logger.error("Expected instance of UserCredentialModel for CredentialInput");
            return false;
        }
        UserCredentialModel userInput = (UserCredentialModel) credentialInput;
        String challengeResponse = credentialInput.getChallengeResponse();
        if (challengeResponse == null) {
            return false;
        }
        if (ObjectUtil.isBlank(credentialInput.getCredentialId())) {
            logger.error("CredentialId is null when validating credential of user "+user.getUsername());
            return false;
        }
 
        UserCredentialStore store = (UserCredentialStore) session.getProvider(UserCredentialStore.class);
        List<CredentialModel> storedCreds = store.getCredentialsByType(realm, user, "secret-question");

        if (storedCreds == null || storedCreds.isEmpty()) {
            logger.warn("No secret-question credentials found for user: " + user.getUsername());
            return false;
        }
        CredentialModel stored = storedCreds.get(0);
        SecretQuestionCredentialModel model = SecretQuestionCredentialModel.createFromCredentialModel(stored);
        SecretQuestionSecretData secretData = model.getSecretQuestionSecretData();

        try {
            byte[] salt = secretData.getSalt();
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            PBEKeySpec spec = new PBEKeySpec(userInput.getChallengeResponse().toCharArray(), salt, model.getSecretQuestionCredentialData().getHashIterations(), 256);
            byte[] hashed = skf.generateSecret(spec).getEncoded();
            String encodedInput = Base64.encodeBytes(hashed);

            return encodedInput.equals(secretData.getValue());
        } catch (Exception e) {
            logger.error("Error validating secret question credential", e);
            return false;
        }
    }

    @Override
    public String getType() {
        return SecretQuestionCredentialModel.TYPE;
    }

    @Override
    public CredentialTypeMetadata getCredentialTypeMetadata(CredentialTypeMetadataContext metadataContext) {
        return CredentialTypeMetadata.builder()
                .type(getType())
                .category(CredentialTypeMetadata.Category.TWO_FACTOR)
                .displayName("secret-question-display-name")
                .helpText("secret-question-help-text")
                .iconCssClass("kcAuthenticatorSecretQuestionClass")
                .createAction(UserModel.RequiredAction.CONFIGURE_SECRET_QUESTION.toString())
                .removeable(true)
                .build(session);
    }
}
