<#include "../header.ftl">

<@lib.showMessages/>

<p>Chyst�te se upravit �asto kladenou ot�zku. Povolen� jsou jen zm�ny,
kter� vylep�uj� kvalitu odpov�di, form�tov�n�, pravopis, stylistiku
a podobn�. Rozhodn� jsou zak�z�ny dotazy, od toho je zde <a href="/diskuse.jsp">diskusn� f�rum</a>.
Va�e zm�ny budou ulo�eny jako nov� revize, tud� je mo�n� je kdykoliv
vr�tit zp�t.</p>
<br />

<#if PARAMS.preview?exists>
    <fieldset>
        <legend>N�hled</legend>
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
            <td class="required">Ot�zka</td>
            <td>
                <input tabindex="1" type="text" name="title" size="80" value="${PARAMS.title?if_exists?html}">
                <div class="error">${ERRORS.title?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td class="required">Odpov��</td>
            <td>
                <div class="form-edit">
                    <a href="javascript:insertAtCursor(document.form.text, '<b>', '</b>');" id="serif" title="Vlo�it zna�ku tu�n�"><b>B</b></a>
                    <a href="javascript:insertAtCursor(document.form.text, '<i>', '</i>');" id="serif" title="Vlo�it zna�ku kurz�va"><i>I</i></a>
                    <a href="javascript:insertAtCursor(document.form.text, '<a href=&quot;&quot;>', '</a>');" id="mono" title="Vlo�it zna�ku odkazu">&lt;a&gt;</a>
                    <a href="javascript:insertAtCursor(document.form.text, '<p>', '</p>');" id="mono" title="Vlo�it zna�ku odstavce">&lt;p&gt;</a>
                    <a href="javascript:insertAtCursor(document.form.text, '<pre>', '</pre>');" id="mono" title="Vlo�it zna�ku form�tovan�ho textu. Vhodn� pro konfigura�n� soubory �i v�pisy.">&lt;pre&gt;</a>
                    <a href="javascript:insertAtCursor(document.form.text, '<code>', '</code>');" id="mono" title="Vlo�it zna�ku pro p�smo s pevnou ���kou">&lt;code&gt;</a>
                </div>
                <textarea tabindex="2" name="text" class="siroka" rows="20">${PARAMS.text?if_exists?html}</textarea><br>
                <div class="error">${ERRORS.text?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td colspan="2" align="center">
                <input tabindex="3" type="submit" name="preview" value="N�hled">
                <input tabindex="4" type="submit" name="submit" value="Dokon�i">
            </td>
        </tr>
    </table>
    <input type="hidden" name="action" value="edit2">
    <input type="hidden" name="rid" value="${RELATION.id}">
</form>

<#include "/include/napoveda-k-auto-formatovani.txt">

<#include "../footer.ftl">
