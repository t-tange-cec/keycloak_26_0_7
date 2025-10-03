package org.keycloak.models.credential;

import org.keycloak.credential.CredentialModel;
import org.keycloak.models.credential.dto.SecretQuestionCredentialData;
import org.keycloak.models.credential.dto.SecretQuestionSecretData;
import org.keycloak.models.RealmModel;
import org.keycloak.util.JsonSerialization;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class SecretQuestionCredentialModel extends CredentialModel {

    public static final String TYPE = "secret-question";

    private final SecretQuestionCredentialData credentialData;
    private final SecretQuestionSecretData secretData;

    public static SecretQuestionCredentialModel createFromAnswer(String answer) {

    	SecretQuestionSecretData secretData = new  SecretQuestionSecretData(answer,null);
    	SecretQuestionCredentialModel credentialModel = new SecretQuestionCredentialModel((SecretQuestionCredentialData)null,secretData);
    	credentialModel.setType(TYPE);
    	credentialModel.setUserLabel("My answer");
        return credentialModel;
    }

    private SecretQuestionCredentialModel(SecretQuestionCredentialData credentialData, SecretQuestionSecretData secretData) {
        this.credentialData = credentialData;
        this.secretData = secretData;
    }

    public static SecretQuestionCredentialModel createFromValues(SecretQuestionCredentialData credentialData, SecretQuestionSecretData secretData) {
        return new SecretQuestionCredentialModel(credentialData, secretData);
    }

    public static SecretQuestionCredentialModel createFromValues(String algorithm, byte[] salt, int hashIterations, String encodedAnswer){
        return createFromValues(algorithm, salt, hashIterations, null, encodedAnswer);
    }

    public static SecretQuestionCredentialModel createFromValues(String algorithm, byte[] salt, int hashIterations, Map<String, List<String>> additionalParameters, String encodedAnswer){
        SecretQuestionCredentialData credentialData = new SecretQuestionCredentialData(hashIterations, algorithm, additionalParameters);
        SecretQuestionSecretData secretData = new SecretQuestionSecretData(encodedAnswer, salt);

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
            SecretQuestionCredentialModel secretQuestionCredentialModel = new SecretQuestionCredentialModel(credentialData, secretData);
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
