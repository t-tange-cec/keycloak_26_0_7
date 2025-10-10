<#import "template.ftl" as layout>
<#import "field.ftl" as field>
<#import "buttons.ftl" as buttons>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('secret-question'); section>
<!-- template: login-secret-question.ftl -->
    <#if section="header">
        ${msg("doLogIn")}
    <#elseif section="form">
        <form id="kc-secret-question-login-form" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">
            <input id="qid" type="hidden" name="qid" value="${qid!''}">
            <span class="pf-v5-c-form__label-text">
    	      ${question}
	        </span>
            <@field.input name="secretAnswer" label=msg("secretAnswer")  fieldName="secretAnswerr" />
            <@buttons.loginButton />
        </form>
    </#if>
</@layout.registrationLayout>