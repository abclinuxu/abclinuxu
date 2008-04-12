<#include "../header.ftl">

<@lib.showMessages/>

<p>Chystáte se upravit často kladenou otázku. Povolené jsou jen změny,
které vylepšují kvalitu odpovědi, formátování, pravopis, stylistiku
a podobně. Rozhodně jsou zakázány dotazy, od toho je zde <a href="/diskuse.jsp">diskusní fórum</a>.
Vaše změny budou uloženy jako nová revize, tudíž je možné je kdykoliv
vrátit zpět.</p>
<br />

<#if PARAMS.preview?exists>
    <fieldset>
        <legend>Náhled</legend>
        <h1 style="margin-bottom: 1em;">${PREVIEW.title?if_exists}</h1>
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
            <td>
                Popis změny
                <a class="info" href="#">?<span class="tooltip">Text bude zobrazen v historii dokumentu</span></a>
            </td>
            <td>
                <input tabindex="3" type="text" name="rev_descr" size="40" value="${PARAMS.rev_descr?if_exists?html}">
                <div class="error">${ERRORS.rev_descr?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td colspan="2" align="center">
                <input tabindex="4" type="submit" name="preview" value="Náhled">
                <input tabindex="5" type="submit" name="submit" value="Dokonči">
            </td>
        </tr>
    </table>
    <input type="hidden" name="action" value="edit2">
    <input type="hidden" name="rid" value="${RELATION.id}">
</form>

<#include "/include/napoveda-k-auto-formatovani.txt">

<#include "../footer.ftl">
