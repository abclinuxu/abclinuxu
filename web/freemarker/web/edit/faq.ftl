<#include "../header.ftl">

<@lib.showMessages/>

<p>Chystáte se upravit èasto kladenou otázku. Povolené jsou jen zmìny,
které vylep¹ují kvalitu odpovìdi, formátování, pravopis, stylistiku
a podobnì. Rozhodnì jsou zakázány dotazy, od toho je zde diskusní fórum.
Va¹e zmìny budou ulo¾eny jako nová revize, tudí¾ je mo¾né je kdykoliv
vrátit zpìt.
</p>

<#if PARAMS.preview?exists>
    <fieldset style="margin-top: 1em;">
        <legend>Náhled</legend>
        <h1 style="margin-bottom: 1em;">${TOOL.xpath(PREVIEW, "/data/title")}</h1>
        <div>
            ${TOOL.render(TOOL.xpath(PREVIEW.data,"data/text"), USER?if_exists)}
        </div>
        <#if XML.data.links[0]?exists>
            <h3>Související odkazy</h3>
            <ul>
                <#list XML.data.links.link as link>
                    <li>
                        <a href="${link.@url}">${link}</a>
                    </li>
                </#list>
            </ul>
        </#if>
    </fieldset>
</#if>


<form action="${URL.make("/faq/edit")}" method="POST" name="form">
    <table cellpadding="5" style="margin-top:1em">
        <tr>
            <td class="required">Otázka</td>
            <td>
                <input tabindex="1" type="text" name="title" size="80" value="${PARAMS.title?if_exists}">
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
                <textarea tabindex="3" name="text" cols="80" rows="20">${PARAMS.text?if_exists?html}</textarea><br>
                <div class="error">${ERRORS.text?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td>Související odkazy</td>
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
                    <tr>
                        <td>
                            <input tabindex="14" type="text" name="caption6" size="40" value="${PARAMS.caption6?if_exists}">
                            <div class="error">${ERRORS.caption6?if_exists}</div>
                        </td>
                        <td>
                            <input tabindex="15" type="text" name="link6" size="40" value="${PARAMS.link6?if_exists}">
                            <div class="error">${ERRORS.link6?if_exists}</div>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <input tabindex="16" type="text" name="caption7" size="40" value="${PARAMS.caption7?if_exists}">
                            <div class="error">${ERRORS.caption7?if_exists}</div>
                        </td>
                        <td>
                            <input tabindex="17" type="text" name="link7" size="40" value="${PARAMS.link7?if_exists}">
                            <div class="error">${ERRORS.link7?if_exists}</div>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <input tabindex="18" type="text" name="caption8" size="40" value="${PARAMS.caption8?if_exists}">
                            <div class="error">${ERRORS.caption8?if_exists}</div>
                        </td>
                        <td>
                            <input tabindex="19" type="text" name="link8" size="40" value="${PARAMS.link8?if_exists}">
                            <div class="error">${ERRORS.link8?if_exists}</div>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <input tabindex="20" type="text" name="caption9" size="40" value="${PARAMS.caption9?if_exists}">
                            <div class="error">${ERRORS.caption9?if_exists}</div>
                        </td>
                        <td>
                            <input tabindex="21" type="text" name="link9" size="40" value="${PARAMS.link9?if_exists}">
                            <div class="error">${ERRORS.link9?if_exists}</div>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <input tabindex="22" type="text" name="caption10" size="40" value="${PARAMS.caption10?if_exists}">
                            <div class="error">${ERRORS.caption10?if_exists}</div>
                        </td>
                        <td>
                            <input tabindex="23" type="text" name="link10" size="40" value="${PARAMS.link10?if_exists}">
                            <div class="error">${ERRORS.link10?if_exists}</div>
                        </td>
                    </tr>
                </table>

            </td>
        </tr>
        <tr>
            <td colspan="2" align="center">
                <input tabindex="24" type="submit" name="preview" value="Náhled">
                <input tabindex="25" type="submit" name="submit" value="Dokonèi">
            </td>
        </tr>
    </table>
    <input type="hidden" name="action" value="edit2">
    <input type="hidden" name="rid" value="${RELATION.id}">
</form>

<h1 style="margin-top: 0px;">Nápovìda k formátování</h1>

<p>Povolené HTML <a href="http://www.w3.org/TR/html4/index/elements.html">znaèky</a>:
P, BR, B, I, A, PRE, UL, OL, LI, CODE, DIV, H1, H2, H3, EM, STRONG, CITE, BLOCKQUOTE,
VAR, HR a IT.</p>

<p>Nejrychlej¹í zpùsob formátování je rozdìlovat
text do odstavcù. Systém detekuje prázdné øádky
(dvakrát enter) a nahradí je HTML znaèkou odstavce.
Pokud ale v textu pou¾ijete znaèku P èi BR,
pak pøedpokládáme, ¾e o formátování se budete starat
sami a tato konverze nebude aktivována.</p>

<p>Pokud neovládáte HTML, doporuèuji si pøeèíst jeho
<a href="http://www.kosek.cz/clanky/html/01.html">rychlokurz</a>.</p>

<p>Text musí být HTML validní, proto znak men¹ítka èi vìt¹ítka zapisujte takto:
&lt; jako &amp;lt; a &gt; jako &amp;gt;. Dal¹ím èastým problémem
je, jak vlo¾it výpis logu èi konfiguraèní soubor. V tomto
pøípadì vá¹ text vlo¾te mezi znaèky PRE, pøíli¹ dlouhé øádky rozdìlte klávesou enter.</p>

<#include "../footer.ftl">
