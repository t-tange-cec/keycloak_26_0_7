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
            <#if otpLogin.userSeqretQuestionCredentials?size gt 1>
                <div class="${properties.kcFormGroupClass!}">
                    <div class="${properties.kcInputWrapperClass!}">
                        <#list otpLogin.userSecretQuestionCredentials as seqretQuestionCredential>
                            <div id="kc-secret-question-credential-${seqretQuestionCredential?index}" class="${properties.kcLoginOTPListClass!}"
                                    onclick="toggleOTP(${seqretQuestionCredential?index}, '${seqretQuestionCredential.id}')">
                                <span class="${properties.kcLoginOTPListItemHeaderClass!}">
                                    <span class="${properties.kcLoginOTPListItemIconBodyClass!}">
                                      <i class="${properties.kcLoginOTPListItemIconClass!}" aria-hidden="true"></i>
                                    </span>
                                    <span class="${properties.kcLoginOTPListItemTitleClass!}">${seqretQuestionCredential.userLabel}</span>
                                </span>
                            </div>
                        </#list>
                    </div>
                </div>
            </#if>

            <@field.input name="secret-question" label=msg("loginOtpOneTime") autocomplete="one-time-code" fieldName="totp" autofocus=true />

            <@buttons.loginButton />
        </form>
    </#if>
</@layout.registrationLayout>