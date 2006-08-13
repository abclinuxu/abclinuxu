<#include "../header.ftl">

<@lib.showMessages/>

<p>
    Chystáte se vlo¾it polo¾ku do databáze <b>zodpovìzených</b> otázek.
    Pokud potøebujete poradit, jste na ¹patné stránce.
    <a href="/Search">Prohledejte</a> nejdøíve na¹i rozsáhlou databázi,
    a pokud odpovìï nenajdete, polo¾te svùj dotaz do <a href="/diskuse.jsp">diskusního fóra</a>.
    Tento formuláø je urèen zku¹enìj¹ím u¾ivatelùm, kteøí se chtìjí
    podìlit o øe¹ení otázky, která bývá èasto kladena v diskusním
    fóru.
</p>

<p>
    Vyplòte jednotlivé polo¾ky formuláøe. Do textu
    odpovìdi zadejte co nejpodrobnìj¹í a nejpøesnìj¹í odpovìï. Do souvisejících
    odkazù umístìte link na dokument s dal¹ími informacemi, napøíklad na èlánek
    zabývající se touto tématikou nebo na diskusi ve fóru, kde byl problem
    (vy)øe¹en.
</p>

<#if PARAMS.preview?exists>
    <fieldset style="margin-top: 1em;">
        <legend>Náhled</legend>
        <h1 style="margin-bottom: 1em;">${TOOL.xpath(PREVIEW, "/data/title")?if_exists}</h1>
        <div>
            ${TOOL.render(TOOL.xpath(PREVIEW.data,"data/text")?if_exists, USER?if_exists)}
        </div>
    </fieldset>
</#if>


<form action="${URL.make("/faq/edit")}" method="POST" name="form">
    <table cellpadding="5" style="margin-top:1em">
        <tr>
            <td class="required">Otázka</td>
            <td>
                <input tabindex="1" type="text" name="title" size="80" value="${PARAMS.title?if_exists?html}">
                <div class="error">${ERRORS.title?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td class="required">Odpovìï</td>
            <td>
                <div class="form-edit">
                    <a href="javascript:insertAtCursor(document.form.text, '<b>', '</b>');" id="serif" title="Vlo¾it znaèku tuènì"><b>B</b></a>
                    <a href="javascript:insertAtCursor(document.form.text, '<i>', '</i>');" id="serif" title="Vlo¾it znaèku kurzíva"><i>I</i></a>
                    <a href="javascript:insertAtCursor(document.form.text, '<a href=&quot;&quot;>', '</a>');" id="mono" title="Vlo¾it znaèku odkazu">&lt;a&gt;</a>
                    <a href="javascript:insertAtCursor(document.form.text, '<p>', '</p>');" id="mono" title="Vlo¾it znaèku odstavce">&lt;p&gt;</a>
                    <a href="javascript:insertAtCursor(document.form.text, '<pre>', '</pre>');" id="mono" title="Vlo¾it znaèku formátovaného textu. Vhodné pro konfiguraèní soubory èi výpisy.">&lt;pre&gt;</a>
                    <a href="javascript:insertAtCursor(document.form.text, '<code>', '</code>');" id="mono" title="Vlo¾it znaèku pro písmo s pevnou ¹íøkou">&lt;code&gt;</a>
                </div>
                <textarea tabindex="2" name="text" cols="80" rows="20">${PARAMS.text?if_exists?html}</textarea><br>
                <div class="error">${ERRORS.text?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td colspan="2" align="center">
                <input tabindex="3" type="submit" name="preview" value="Náhled">
                <input tabindex="4" type="submit" name="submit" value="Dokonèi">
            </td>
        </tr>
    </table>
    <input type="hidden" name="action" value="add2">
    <input type="hidden" name="rid" value="${RELATION.id}">
</form>

<#include "/include/napoveda-k-auto-formatovani.txt">

<#include "../footer.ftl">
