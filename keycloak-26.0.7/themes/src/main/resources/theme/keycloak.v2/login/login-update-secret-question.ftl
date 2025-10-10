<#import "template.ftl" as layout>
<#import "password-commons.ftl" as passwordCommons>
<#import "field.ftl" as field>
<#import "buttons.ftl" as buttons>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('password','password-confirm'); section>
<!-- template: login-update-secret-question.ftl -->
    <#if section = "header">
        ${msg("updateSecretQuestion")}
    <#elseif section = "form">
        <form id="kc-update_secret-question-form" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post" novalidate="novalidate">
			<label for="qid">Secret QuestionÅF</label>
			<select name="qid" id="qid">
			  <#list selectionOptions as option>
			    <option value="${option.id}">${option.message}</option>
			  </#list>
			</select>
            <@field.input name="secretAnswer" label=msg("secretAnswer") fieldName="secretAnswer" />
            <@buttons.actionGroup>
                <#if isAppInitiatedAction??>
                    <@buttons.button label="doSubmit" class=["kcButtonPrimaryClass"]/>
                    <@buttons.button label="doCancel" name="cancel-aia" class=["kcButtonSecondaryClass"]/>
                <#else>
                    <@buttons.button label="doSubmit" class=["kcButtonPrimaryClass", "kcButtonBlockClass"]/>
                </#if>
            </@buttons.actionGroup>
        </form>
    </#if>
</@layout.registrationLayout>
