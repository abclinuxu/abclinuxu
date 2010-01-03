<#include "../header.ftl">

<@lib.showMessages/>

<p>
    Tato stránka je určena pro vytváření nových anket. Zadejte
    text otázky a nejméně dvě volby. Z HTML značek jsou povoleny
    jen nový řádek a odkaz. Dále můžete určit, zda jeden hlasující
    může vybrat více voleb, nebo si musí zvolit jednu jedinou.
</p>

<#if POLL??>
    <fieldset>
    <legend>Náhled</legend>
        <@lib.showPoll POLL/>
    </fieldset>
</#if>

<@lib.addForm URL.make("/EditPoll")>
    <@lib.addTextArea true, "question", "Otázka", 3, "class='siroka'" />

    <#if RELATION.id==250>
        <@lib.addFormField false, "URL">
            /ankety/<@lib.addInputBare "url", 20 />
        </@lib.addFormField>
    </#if>

    <@lib.addSelect true, "multichoice", "Více možností">
        <@lib.addOption "multichoice", "Ano", "yes" />
        <@lib.addOption "multichoice", "Ne", "no", true />
    </@lib.addSelect>

    <#list 1..10 as i>
        <@lib.addFormField (i < 3), "Volba "+i>
            <input type="text" name="choices" size="60" maxlength="255" tabindex="4"
            value="<#if choices?size gt (i-1)>${choices[i-1]}</#if>">

            <#if i == 1>
                <div class="error">${ERRORS.choices!}</div>
            </#if>
        </@lib.addFormField>
    </#list>

    <@lib.addFormField>
        <@lib.addSubmitBare "Náhled", "preview" />
        <@lib.addSubmitBare "Dokonči" />
    </@lib.addFormField>

    <@lib.addHidden "action", "add2" />
    <@lib.addHidden "rid", RELATION.id />
</@lib.addForm>


<#include "../footer.ftl">
