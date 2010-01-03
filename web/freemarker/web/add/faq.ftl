<@lib.addRTE textAreaId="text" formId="form" menu="wiki" />
<#include "../header.ftl">

<@lib.showMessages/>

<p>
    Chystáte se vložit položku do databáze <b>zodpovězených</b> otázek.
    Pokud potřebujete poradit, jste na špatné stránce.
    <a href="/hledani">Prohledejte</a> nejdříve naši rozsáhlou databázi,
    a pokud odpověď nenajdete, položte svůj dotaz do <a href="/diskuse.jsp">diskusního fóra</a>.
    Tento formulář je určen zkušenějším uživatelům, kteří se chtějí
    podělit o řešení otázky, která bývá často kladena v diskusním
    fóru.
</p>

<p>
    Vyplňte jednotlivé položky formuláře. Do textu
    odpovědi zadejte co nejpodrobnější a nejpřesnější odpověď. Do souvisejících
    odkazů umístěte link na dokument s dalšími informacemi, například na článek
    zabývající se touto tématikou nebo na diskusi ve fóru, kde byl problem
    (vy)řešen.
</p>
<br />

<#if PARAMS.preview??>
    <fieldset>
        <legend>Náhled</legend>
        <h1 style="margin-bottom: 1em;">${PREVIEW.title!}</h1>
        <div>
            ${TOOL.render(TOOL.xpath(PREVIEW.data,"data/text")!, USER!)}
        </div>
    </fieldset>
</#if>
<br />

<@lib.addForm URL.make("/faq/edit"), "name='form'">
    <@lib.addInput true, "title", "Otázka" />
    <@lib.addTextArea true, "text", "Odpověď", 20>
        <@lib.showRTEControls "text"/>
    </@lib.addTextArea>
    <@lib.addFormField>
        <@lib.addSubmitBare "Náhled", "preview" />
        <@lib.addSubmitBare "Dokonči", "submit" />
    </@lib.addFormField>

    <@lib.addHidden "action", "add2" />
    <@lib.addHidden "rid", RELATION.id />
</@lib.addForm>

<#include "/include/napoveda-k-auto-formatovani.txt">

<#include "../footer.ftl">
