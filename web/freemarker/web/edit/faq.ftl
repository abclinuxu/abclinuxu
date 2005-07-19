<#include "../header.ftl">

<@lib.showMessages/>

<p>Chyst�te se upravit �asto kladenou ot�zku. Povolen� jsou jen zm�ny,
kter� vylep�uj� kvalitu odpov�di, form�tov�n�, pravopis, stylistiku
a podobn�. Rozhodn� jsou zak�z�ny dotazy, od toho je zde diskusn� f�rum.
Va�e zm�ny budou ulo�eny jako nov� revize, tud�� je mo�n� je kdykoliv
vr�tit zp�t.
</p>

<#if PARAMS.preview?exists>
    <h1 class="st_nadpis">N�hled</h1>
    <h2>${TOOL.xpath(PREVIEW, "/data/title")}</h2>
    <div>
        ${TOOL.render(TOOL.xpath(PREVIEW.data,"data/text"), USER?if_exists)}
    </div>
</#if>


<form action="${URL.make("/faq/edit")}" method="POST" name="form">
    <table cellpadding="5" style="margin-top:1em">
        <tr>
            <td class="required">Ot�zka</td>
            <td>
                <input tabindex="1" type="text" name="title" size="80" value="${PARAMS.title?if_exists}">
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
                <textarea tabindex="3" name="text" cols="80" rows="20">${PARAMS.text?if_exists}</textarea><br>
                <div class="error">${ERRORS.text?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td>Souvisej�c� odkazy</td>
            <td>
                <table border="0">
                    <tr>
                        <td>Titulek</td>
                        <td>Odkaz</td>
                    </tr>
                    <tr>
                        <td>
                            <input tabindex="4" type="text" name="caption1" size="40" value="${PARAMS.caption1?if_exists}">
                            <div class="error">${ERRORS.caption1?if_exists}</div>
                        </td>
                        <td>
                            <input tabindex="5" type="text" name="link1" size="40" value="${PARAMS.link1?if_exists}">
                            <div class="error">${ERRORS.link1?if_exists}</div>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <input tabindex="6" type="text" name="caption2" size="40" value="${PARAMS.caption2?if_exists}">
                            <div class="error">${ERRORS.caption2?if_exists}</div>
                        </td>
                        <td>
                            <input tabindex="7" type="text" name="link2" size="40" value="${PARAMS.link2?if_exists}">
                            <div class="error">${ERRORS.link2?if_exists}</div>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <input tabindex="8" type="text" name="caption3" size="40" value="${PARAMS.caption3?if_exists}">
                            <div class="error">${ERRORS.caption3?if_exists}</div>
                        </td>
                        <td>
                            <input tabindex="9" type="text" name="link3" size="40" value="${PARAMS.link3?if_exists}">
                            <div class="error">${ERRORS.link3?if_exists}</div>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <input tabindex="10" type="text" name="caption4" size="40" value="${PARAMS.caption4?if_exists}">
                            <div class="error">${ERRORS.caption4?if_exists}</div>
                        </td>
                        <td>
                            <input tabindex="11" type="text" name="link4" size="40" value="${PARAMS.link4?if_exists}">
                            <div class="error">${ERRORS.link4?if_exists}</div>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <input tabindex="12" type="text" name="caption5" size="40" value="${PARAMS.caption5?if_exists}">
                            <div class="error">${ERRORS.caption5?if_exists}</div>
                        </td>
                        <td>
                            <input tabindex="13" type="text" name="link5" size="40" value="${PARAMS.link5?if_exists}">
                            <div class="error">${ERRORS.link5?if_exists}</div>
                        </td>
                    </tr>
                </table>

            </td>
        </tr>
        <tr>
            <td colspan="2" align="center">
                <input tabindex="14" type="submit" name="preview" value="N�hled">
                <input tabindex="15" type="submit" name="submit" value="Dokon�i">
            </td>
        </tr>
    </table>
    <input type="hidden" name="action" value="edit2">
    <input type="hidden" name="rid" value="${RELATION.id}">
</form>

<h1 style="margin-top: 0px;">N�pov�da k form�tov�n�</h1>

<p>Povolen� HTML <a href="http://www.w3.org/TR/html4/index/elements.html">zna�ky</a>:
P, BR, B, I, A, PRE, UL, OL, LI, CODE, DIV, H1, H2, H3, EM, STRONG, CITE, BLOCKQUOTE,
VAR, HR a IT.</p>

<p>Nejrychlej�� zp�sob form�tov�n� je rozd�lovat
text do odstavc�. Syst�m detekuje pr�zdn� ��dky
(dvakr�t enter) a nahrad� je HTML zna�kou odstavce.
Pokud ale v textu pou�ijete zna�ku P �i BR,
pak p�edpokl�d�me, �e o form�tov�n� se budete starat
sami a tato konverze nebude aktivov�na.</p>

<p>Pokud neovl�d�te HTML, doporu�uji si p�e��st jeho
<a href="http://www.kosek.cz/clanky/html/01.html">rychlokurz</a>.</p>

<p>Text mus� b�t HTML validn�, proto znak men��tka �i v�t��tka zapisujte takto:
&lt; jako &amp;lt; a &gt; jako &amp;gt;. Dal��m �ast�m probl�mem
je, jak vlo�it v�pis logu �i konfigura�n� soubor. V tomto
p��pad� v� text vlo�te mezi zna�ky PRE, p��li� dlouh� ��dky rozd�lte kl�vesou enter.</p>

<#include "../footer.ftl">
