<#include "../header.ftl">

<@lib.showMessages/>

<h1>N�hled dotazu</h1>

<p>
    Nyn� si prohl�dn�te vzhled va�eho dotazu. Zkontrolujte
    si pravopis, obsah i t�n va�eho textu. Uv�domte si, �e
    toto nen� placen� technick� podpora, ale dobrovoln�
    a neplacen� pr�ce ochotn�ch lid�. Pokud se v�m text n�jak nel�b�,
    opravte jej a zvolte N�hled. Pokud jste s n�m spokojeni,
    zvolte OK.
</p>

<#if PREVIEW?exists>
    <h2>N�hled va�eho dotazu</h2>
    <@lib.showThread PREVIEW, 0, TOOL.createEmptyDiscussion(), false />
</#if>

<h2>Zde m��ete prov�st sv� �pravy</h2>

<form action="${URL.make("/EditDiscussion")}" method="POST" name="form">
    <table class="siroka" cellpadding="5">
        <#if ! USER?exists>
            <tr>
                <td class="required">Login a heslo</td>
                <td>
                    <input tabindex="1" type="text" name="LOGIN" size="8">
                    <input tabindex="2" type="password" name="PASSWORD" size="8">
                </td>
            </tr>
            <tr>
                <td class="required">nebo va�e jm�no</td>
                <td>
                    <input tabindex="3" type="text" size="30" name="author" value="${PARAMS.author?if_exists}">
                    <div class="error">${ERRORS.author?if_exists}</div>
                </td>
            </tr>
            <#if ! USER_VERIFIED?if_exists>
                <tr>
                    <td class="required">Aktu�ln� rok</td>
                    <td>
                        <input type="text" size="4" name="antispam" value="${PARAMS.antispam?if_exists?html}" tabindex="4">
                        <a class="info" href="#">?<span class="tooltip">Vlo�te aktu�ln� rok. Jedn� se o ochranu p�ed spamboty.
                        Po �sp�n�m ov��en� se ulo�� cookie (v�etn� va�eho jm�na) a tato kontrola p�estane b�t prov�d�na.</span></a>
                        <div class="error">${ERRORS.antispam?if_exists}</div>
                    </td>
                </tr>
            </#if>
        </#if>
        <tr>
            <td class="required">Titulek</td>
            <td>
                <input tabindex="4" type="text" name="title" size="40" maxlength="70" value="${PARAMS.title?if_exists?html}">
                <div class="error">${ERRORS.title?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td class="required">Dotaz</td>
            <td>
                <div class="form-edit">
                    <a href="javascript:insertAtCursor(document.form.text, '<b>', '</b>');" id="serif" title="Vlo�it zna�ku tu�n�"><b>B</b></a>
                    <a href="javascript:insertAtCursor(document.form.text, '<i>', '</i>');" id="serif" title="Vlo�it zna�ku kurz�va"><i>I</i></a>
                    <a href="javascript:insertAtCursor(document.form.text, '<a href=&quot;&quot;>', '</a>');" id="mono" title="Vlo�it zna�ku odkazu">&lt;a&gt;</a>
                    <a href="javascript:insertAtCursor(document.form.text, '<p>', '</p>');" id="mono" title="Vlo�it zna�ku odstavce">&lt;p&gt;</a>
                    <a href="javascript:insertAtCursor(document.form.text, '<pre>', '</pre>');" id="mono" title="Vlo�it zna�ku form�tovan�ho textu. Vhodn� pro konfigura�n� soubory �i v�pisy.">&lt;pre&gt;</a>
                    <a href="javascript:insertAtCursor(document.form.text, '<code>', '</code>');" id="mono" title="Vlo�it zna�ku pro p�smo s pevnou ���kou">&lt;code&gt;</a>
                </div>
                <div class="error">${ERRORS.text?if_exists}</div>
                <textarea tabindex="5" name="text" class="siroka" rows="20">${PARAMS.text?if_exists?html}</textarea>
            </td>
        </tr>
        <tr>
            <td>&nbsp;</td>
            <td>
                <input tabindex="6" type="submit" name="preview" value="Zopakuj n�hled">
                <input tabindex="7" type="submit" name="finish" value="Dokon�i">
            </td>
        </tr>
    </table>
    <input type="hidden" name="action" value="addQuez2">
    <input type="hidden" name="rid" value="${PARAMS.rid}">
</form>

<#include "/include/napoveda-k-auto-formatovani.txt">

<#include "../footer.ftl">
