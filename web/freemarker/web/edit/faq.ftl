<#include "../header.ftl">

<@lib.showMessages/>

<p>Chystáte se upravit èasto kladenou otázku. Povolené jsou jen zmìny,
které vylep¹ují kvalitu odpovìdi, formátování, pravopis, stylistiku
a podobnì. Rozhodnì jsou zakázány dotazy, od toho je zde <a href="/diskuse.jsp">diskusní fórum</a>.
Va¹e zmìny budou ulo¾eny jako nová revize, tudí¾ je mo¾né je kdykoliv
vrátit zpìt.</p>
<br />

<#if PARAMS.preview?exists>
    <fieldset>
        <legend>Náhled</legend>
        <h1 style="margin-bottom: 1em;">${TOOL.xpath(PREVIEW, "/data/title")}</h1>
        <div>
            ${TOOL.render(TOOL.xpath(PREVIEW.data,"data/text"), USER?if_exists)}
        </div>
    </fieldset>
</#if>
<br />

<form action="${URL.make("/faq/edit")}" method="POST" name="form">
    <table class="siroka" cellpadding="5">
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
                <textarea tabindex="2" name="text" class="siroka" rows="20">${PARAMS.text?if_exists?html}</textarea><br>
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
    <input type="hidden" name="action" value="edit2">
    <input type="hidden" name="rid" value="${RELATION.id}">
</form>

<#include "/include/napoveda-k-auto-formatovani.txt">

<#include "../footer.ftl">
