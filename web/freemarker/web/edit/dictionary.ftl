<@lib.addRTE textAreaId="desc" formId="dictForm" menu="wiki" />
<#include "../header.ftl">

<@lib.showMessages/>

<h1>Úprava pojmu</h1>

<p>
    Zadejte jméno pojmu a jeho vysvětlení. Pojem by měl být buď linuxový nebo počítačový. Pokud je
    potřeba, upravte jméno pojmu. Mělo by být psáno malými písmeny, velká písmena použijte jen pro
    ustálené zkratky (například SCSI). První znak jména pojmu musí být písmeno (a-z).
    URL se při úpravě nemění, pokud se vám nelíbí, kontaktujte administrátory.
</p>

<p>
    Pokud v popisu nepoužijete formatovací znaky &lt;br&gt; nebo &lt;p&gt;, systém automaticky
    nahradí prázdné řádky značkou pro nový odstavec.
</p>

<#if PARAMS.preview??>
    <h2>Náhled</h2>

    <fieldset>
        <legend>Náhled</legend>
        <h3>${PARAMS.name!}</h3>
        <#if PARAMS.desc??>
            <div class="dict-item">
            ${TOOL.render(PARAMS.desc,USER!)}
            </div>
        </#if>
    </fieldset>
</#if>

<@lib.addForm URL.make("/edit"), "name='dictForm'">
    <@lib.addInput true, "name", "Pojem" />
    <@lib.addTextArea true, "desc", "Popis", 20>
        <@lib.showRTEControls "desc"/>
    </@lib.addTextArea>

    <@lib.addFormField false, "Popis změny", "Text bude zobrazen v historii dokumentu">
        <@lib.addInputBare "rev_descr" />
    </@lib.addFormField>

    <@lib.addFormField>
        <@lib.addSubmitBare "Náhled", "preview" />
        <@lib.addSubmitBare "Dokonči", "finish" />
    </@lib.addFormField>

    <@lib.addHidden "action", "edit2" />
    <@lib.addHidden "rid", RELATION.id />
</@lib.addForm>

<#include "../footer.ftl">
