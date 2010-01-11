<#include "../header.ftl">

<h2>Úprava ankety</h2>

<p>
    Chystáte se upravit anketu. Můžete měnit texty otázky či jednotlivých
    voleb. Můžete dokonce i přidat nové volby. Tento formulář ale není určen
    pro mazání či změnu pořadí voleb. Změna počtu hlasů je možná jen na úrovni
    databáze.
</p>

<@lib.showMessages/>

<#if PREVIEW??>
    <fieldset>
    <legend>Náhled</legend>
        <@lib.showPoll PREVIEW/>
    </fieldset>
</#if>

<@lib.addForm URL.make("/EditPoll")>
    <@lib.addTextArea true, "question", "Otázka", 3, "", POLL.text />

    <@lib.addSelect true, "multichoice", "Více možností">
        <@lib.addOption "multichoice", "Ano", "yes", POLL.multiChoice />
        <@lib.addOption "multichoice", "Ne", "no", ! POLL.multiChoice />
    </@lib.addSelect>

    <@lib.addSelect true, "closed", "Uzavřená">
        <@lib.addOption "closed", "Ano", "yes", POLL.closed />
        <@lib.addOption "closed", "Ne", "no", ! POLL.closed />
    </@lib.addSelect>

    <#list POLL.choices as choice>
        <@lib.addFormField (choice_index+1 < 3), "Volba " + choice_index>
            <input type="text" name="choices" size="60" maxlength="255" value="${choice.text?html}">
            <#if choice_index == 1>
                <@lib.showError "choices" />
            </#if>
        </@lib.addFormField>
    </#list>

    <#list (POLL.choices?size+1)..10 as i>
        <@lib.addFormField false, "Volba "+i>
            <input type="text" name="choices" size="60" maxlength="255" value="">
        </@lib.addFormField>
    </#list>

    <@lib.addFormField>
        <@lib.addSubmitBare "Náhled", "preview" />
        <@lib.addSubmitBare "Dokonči" />
    </@lib.addFormField>

    <@lib.addHidden "action", "edit2" />
    <@lib.addHidden "rid", RELATION.id />
</@lib.addForm>

<#include "../footer.ftl">
