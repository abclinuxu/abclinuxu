<@lib.addRTE textAreaId="desc" formId="dictForm" menu="wiki" />
<#include "../header.ftl">

<@lib.showMessages/>

<h1>Nový pojem</h1>

<p>
    Napište jméno pojmu a jeho vysvětlení. Pojem by měl být buď linuxový nebo počítačový.
    Jméno pište malými písmeny, velká písmena použijte jen pro ustálené zkratky (například SCSI).
    První znak jména pojmu musí být písmeno (a-z).
    Ze jména se vygeneruje URL adresa (po normalizacích - odstranění všech znaků kromě písmen, čísel,
    tečky a pomlčky, na konci i všech teček a pomlček). Pokud se vám výsledné URL nelíbí, kontaktujte
    administrátory.
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
    <@lib.addTextArea true, "desc", "Popis">
        <@lib.showRTEControls "desc"/>
    </@lib.addTextArea>

    <@lib.addFormField>
        <@lib.addSubmitBare "Náhled", "preview" />
        <#if PARAMS.preview??>
            <@lib.addSubmitBare "Dokonči", "submit" />
        </#if>
    </@lib.addFormField>

    <@lib.addHidden "action", "add2" />
</@lib.addForm>

<#include "../footer.ftl">
