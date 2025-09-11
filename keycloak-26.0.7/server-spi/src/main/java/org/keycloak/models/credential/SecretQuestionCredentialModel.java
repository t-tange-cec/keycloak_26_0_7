package org.keycloak.models.credential;

import org.keycloak.credential.CredentialModel;
import org.keycloak.models.credential.dto.SecretQuestionCredentialData;
import org.keycloak.models.credential.dto.SecretQuestionSecretData;
import org.keycloak.util.JsonSerialization;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class SecretQuestionCredentialModel extends CredentialModel {

    public static final String TYPE = "secret-question";
    public static final String SECRET_QUESTION_HISTORY = "secret-question-history";

    private final SecretQuestionCredentialData credentialData;
    private final SecretQuestionSecretData secretData;

    private SecretQuestionCredentialModel(SecretQuestionCredentialData credentialData, SecretQuestionSecretData secretData) {
        this.credentialData = credentialData;
        this.secretData = secretData;
    }

    public static SecretQuestionCredentialModel createFromValues(SecretQuestionCredentialData credentialData, SecretQuestionSecretData secretData) {
        return new SecretQuestionCredentialModel(credentialData, secretData);
    }

    public static SecretQuestionCredentialModel createFromValues(String algorithm, byte[] salt, int hashIterations, String encodedPassword){
        return createFromValues(algorithm, salt, hashIterations, null, encodedPassword);
    }

    public static SecretQuestionCredentialModel createFromValues(String algorithm, byte[] salt, int hashIterations, Map<String, List<String>> additionalParameters, String encodedPassword){
        SecretQuestionCredentialData credentialData = new SecretQuestionCredentialData(hashIterations, algorithm, additionalParameters);
        SecretQuestionSecretData secretData = new SecretQuestionSecretData(encodedPassword, salt);

        SecretQuestionCredentialModel secretQuestionCredentialModel = new SecretQuestionCredentialModel(credentialData, secretData);

        try {
        	secretQuestionCredentialModel.setCredentialData(JsonSerialization.writeValueAsString(credentialData));
        	secretQuestionCredentialModel.setSecretData(JsonSerialization.writeValueAsString(secretData));
        	secretQuestionCredentialModel.setType(TYPE);
            return secretQuestionCredentialModel;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static SecretQuestionCredentialModel createFromCredentialModel(CredentialModel credentialModel) {
        try {
            SecretQuestionCredentialData credentialData = JsonSerialization.readValue(credentialModel.getCredentialData(),
                    SecretQuestionCredentialData.class);
            SecretQuestionSecretData secretData = JsonSerialization.readValue(credentialModel.getSecretData(), SecretQuestionSecretData.class);
            secretQuestionCredentialModel secretQuestionCredentialModel = new SecretQuestionCredentialModel(credentialData, secretData);
            secretQuestionCredentialModel.setCreatedDate(credentialModel.getCreatedDate());
            secretQuestionCredentialModel.setCredentialData(credentialModel.getCredentialData());
            secretQuestionCredentialModel.setId(credentialModel.getId());
            secretQuestionCredentialModel.setSecretData(credentialModel.getSecretData());
            secretQuestionCredentialModel.setType(credentialModel.getType());
            secretQuestionCredentialModel.setUserLabel(credentialModel.getUserLabel());

            return secretQuestionCredentialModel;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public SecretQuestionCredentialData getSecretQuestionCredentialData() {
        return credentialData;
    }

    public SecretQuestionSecretData getSecretQuestionSecretData() {
        return secretData;
    }


}
