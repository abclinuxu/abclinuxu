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

<#if PARAMS.preview?exists>
    <fieldset>
        <legend>Náhled</legend>
        <h1 style="margin-bottom: 1em;">${TOOL.xpath(PREVIEW, "/data/title")?if_exists}</h1>
        <div>
            ${TOOL.render(TOOL.xpath(PREVIEW.data,"data/text")?if_exists, USER?if_exists)}
        </div>
    </fieldset>
</#if>
<br />

<form action="${URL.make("/faq/edit")}" method="POST" name="form">
    <table cellpadding="5" class="siroka">
        <tr>
            <td class="required">Otázka</td>
            <td>
                <input tabindex="1" type="text" name="title" size="80" value="${PARAMS.title?if_exists?html}">
                <div class="error">${ERRORS.title?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td class="required">Odpověď</td>
            <td>
                <div class="form-edit">
                    <a href="javascript:insertAtCursor(document.form.text, '<b>', '</b>');" id="serif" title="Vložit značku tučně"><b>B</b></a>
                    <a href="javascript:insertAtCursor(document.form.text, '<i>', '</i>');" id="serif" title="Vložit značku kurzíva"><i>I</i></a>
                    <a href="javascript:insertAtCursor(document.form.text, '<a href=&quot;&quot;>', '</a>');" id="mono" title="Vložit značku odkazu">&lt;a&gt;</a>
                    <a href="javascript:insertAtCursor(document.form.text, '<p>', '</p>');" id="mono" title="Vložit značku odstavce">&lt;p&gt;</a>
                    <a href="javascript:insertAtCursor(document.form.text, '<pre>', '</pre>');" id="mono" title="Vložit značku formátovaného textu. Vhodné pro konfigurační soubory či výpisy.">&lt;pre&gt;</a>
                    <a href="javascript:insertAtCursor(document.form.text, '<code>', '</code>');" id="mono" title="Vložit značku pro písmo s pevnou šířkou">&lt;code&gt;</a>
                </div>
                <textarea tabindex="2" name="text" class="siroka" rows="20">${PARAMS.text?if_exists?html}</textarea><br>
                <div class="error">${ERRORS.text?if_exists}</div>
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
