<#import "template.ftl" as layout>
<#import "field.ftl" as field>
<#import "buttons.ftl" as buttons>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('totp'); section>
<!-- template: login-risk.ftl -->

    <#if section="header">
        ${msg("doLogIn")}
    <#elseif section="form">
        <form id="kc-riskbase-login-form" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">
            <input id="selectedCredentialId" type="hidden" name="selectedCredentialId" value="${riskbaseLogin.selectedCredentialId!''}">
            <#if riskbaseLogin.userriskbaseCredentials?size gt 1>
                <div class="${properties.kcFormGroupClass!}">
                    <div class="${properties.kcInputWrapperClass!}">
                        <#list riskbaseLogin.userriskbaseCredentials as riskbaseCredential>
                            <div id="kc-riskbase-credential-${riskbaseCredential?index}" class="${properties.kcLoginriskbaseListClass!}">
                                <span class="${properties.kcLoginriskbaseListItemHeaderClass!}">
                                    <span class="${properties.kcLoginriskbaseListItemIconBodyClass!}">
                                      <i class="${properties.kcLoginriskbaseListItemIconClass!}" aria-hidden="true"></i>
                                    </span>
                                    <span class="${properties.kcLoginriskbaseListItemTitleClass!}">${riskbaseCredential.userLabel}</span>
                                </span>
                            </div>
                        </#list>
                    </div>
                </div>
            </#if>

            <@field.input name="answer" label=msg("loginRiskBase") autocomplete="one-time-code" fieldName="answer" autofocus=true />

            <@buttons.loginButton />
        </form>
    </#if>
</@layout.registrationLayout>