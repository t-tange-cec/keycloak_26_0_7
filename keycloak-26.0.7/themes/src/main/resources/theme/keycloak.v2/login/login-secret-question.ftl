<#import "template.ftl" as layout>
<#import "field.ftl" as field>
<#import "buttons.ftl" as buttons>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('totp'); section>
<!-- template: login-secret-question.ftl -->

    <#if section="header">
        ${msg("doLogIn")}
    <#elseif section="form">
        <form id="kc-otp-login-form" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">
            <input id="selectedCredentialId" type="hidden" name="selectedCredentialId" value="${seqretQuestionLogin.selectedCredentialId!''}">
            <#if secretQuestionLogin.userSeqretQuestionCredentials?size gt 1>
                <div class="${properties.kcFormGroupClass!}">
                    <div class="${properties.kcInputWrapperClass!}">
                        <#list secretQuestionLogin.userSecretQuestionCredentials as seqretQuestionCredential>
                            <div id="kc-secret-question-credential-${seqretQuestionCredential?index}">
                            </div>
                        </#list>
                    </div>
                </div>
            </#if>
            <@field.input name="secret-answer" label=msg("loginAnswer")  fieldName="answer" autofocus=true />

            <@buttons.loginButton />
        </form>
    </#if>
</@layout.registrationLayout>