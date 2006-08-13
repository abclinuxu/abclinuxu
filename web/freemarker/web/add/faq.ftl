<#include "../header.ftl">

<@lib.showMessages/>

<p>
    Chyst�te se vlo�it polo�ku do datab�ze <b>zodpov�zen�ch</b> ot�zek.
    Pokud pot�ebujete poradit, jste na �patn� str�nce.
    <a href="/Search">Prohledejte</a> nejd��ve na�i rozs�hlou datab�zi,
    a pokud odpov�� nenajdete, polo�te sv�j dotaz do <a href="/diskuse.jsp">diskusn�ho f�ra</a>.
    Tento formul�� je ur�en zku�en�j��m u�ivatel�m, kte�� se cht�j�
    pod�lit o �e�en� ot�zky, kter� b�v� �asto kladena v diskusn�m
    f�ru.
</p>

<p>
    Vypl�te jednotliv� polo�ky formul��e. Do textu
    odpov�di zadejte co nejpodrobn�j�� a nejp�esn�j�� odpov��. Do souvisej�c�ch
    odkaz� um�st�te link na dokument s dal��mi informacemi, nap��klad na �l�nek
    zab�vaj�c� se touto t�matikou nebo na diskusi ve f�ru, kde byl problem
    (vy)�e�en.
</p>

<#if PARAMS.preview?exists>
    <fieldset style="margin-top: 1em;">
        <legend>N�hled</legend>
        <h1 style="margin-bottom: 1em;">${TOOL.xpath(PREVIEW, "/data/title")?if_exists}</h1>
        <div>
            ${TOOL.render(TOOL.xpath(PREVIEW.data,"data/text")?if_exists, USER?if_exists)}
        </div>
    </fieldset>
</#if>


<form action="${URL.make("/faq/edit")}" method="POST" name="form">
    <table cellpadding="5" style="margin-top:1em">
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
                <textarea tabindex="2" name="text" cols="80" rows="20">${PARAMS.text?if_exists?html}</textarea><br>
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
    <input type="hidden" name="action" value="add2">
    <input type="hidden" name="rid" value="${RELATION.id}">
</form>

<#include "/include/napoveda-k-auto-formatovani.txt">

<#include "../footer.ftl">
