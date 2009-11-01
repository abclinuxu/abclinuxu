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

<form action="${URL.make("/faq/edit")}" method="POST" name="form">
    <table cellpadding="5" class="siroka">
        <tr>
            <td class="required">Otázka</td>
            <td>
                <input tabindex="1" type="text" name="title" size="80" value="${PARAMS.title!?html}">
                <div class="error">${ERRORS.title!}</div>
            </td>
        </tr>
        <tr>
            <td class="required">Odpověď</td>
            <td>
                <@lib.showError key="text"/>
                <@lib.showRTEControls "text"/>
                <textarea tabindex="2" name="text" id="text" class="siroka" rows="20">${PARAMS.text!?html}</textarea><br>
            </td>
        </tr>
        <tr>
            <td colspan="2" align="center">
                <input tabindex="3" type="submit" name="preview" value="Náhled">
                <input tabindex="4" type="submit" name="submit" value="Dokonči">
            </td>
        </tr>
    </table>
    <input type="hidden" name="action" value="add2">
    <input type="hidden" name="rid" value="${RELATION.id}">
</form>

<#include "/include/napoveda-k-auto-formatovani.txt">

<#include "../footer.ftl">
