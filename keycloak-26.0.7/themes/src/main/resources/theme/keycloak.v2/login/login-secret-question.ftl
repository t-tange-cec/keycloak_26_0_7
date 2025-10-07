<#import "template.ftl" as layout>
<#import "field.ftl" as field>
<#import "buttons.ftl" as buttons>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('secret-question'); section>
<!-- template: login-secret-question.ftl -->
    <#if section="header">
        ${msg("doLogIn")}
    <#elseif section="form">
        <form id="kc-secret-question-login-form" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">
            <@field.input name="qid" label=msg("qid")  fieldName="qid" />
            <@field.input name="secretAnswer" label=msg("secretAnswer")  fieldName="answer" />
            <@buttons.loginButton />
        </form>
    </#if>
</@layout.registrationLayout>