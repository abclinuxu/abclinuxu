<#include "../header.ftl">

<@lib.showMessages/>

<h1>Náhled dotazu</h1>

<p>
    Nyní si prohlédněte vzhled vašeho dotazu. Zkontrolujte
    si pravopis, obsah i tón vašeho textu. Uvědomte si, že
    toto není placená technická podpora, ale dobrovolná
    a neplacená práce ochotných lidí. Pokud se vám text nějak nelíbí,
    opravte jej a zvolte Náhled. Pokud jste s ním spokojeni,
    zvolte OK.
</p>

<#if PREVIEW?exists>
    <h2>Náhled vašeho dotazu</h2>
    <@lib.showThread PREVIEW, 0, TOOL.createEmptyDiscussion(), false />
</#if>

<h2>Zde můžete provést své úpravy</h2>

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
                <td class="required">nebo vaše jméno</td>
                <td>
                    <input tabindex="3" type="text" size="30" name="author" value="${PARAMS.author?if_exists}">
                    <div class="error">${ERRORS.author?if_exists}</div>
                </td>
            </tr>
            <#if ! USER_VERIFIED?if_exists>
                <tr>
                    <td class="required">Aktuální rok</td>
                    <td>
                        <input type="text" size="4" name="antispam" value="${PARAMS.antispam?if_exists?html}" tabindex="4">
                        <a class="info" href="#">?<span class="tooltip">Vložte aktuální rok. Jedná se o ochranu před spamboty.
                        Po úspěšném ověření se uloží cookie (včetně vašeho jména) a tato kontrola přestane být prováděna.</span></a>
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
                    <a href="javascript:insertAtCursor(document.form.text, '<b>', '</b>');" id="serif" title="Vložit značku tučně"><b>B</b></a>
                    <a href="javascript:insertAtCursor(document.form.text, '<i>', '</i>');" id="serif" title="Vložit značku kurzíva"><i>I</i></a>
                    <a href="javascript:insertAtCursor(document.form.text, '<a href=&quot;&quot;>', '</a>');" id="mono" title="Vložit značku odkazu">&lt;a&gt;</a>
                    <a href="javascript:insertAtCursor(document.form.text, '<p>', '</p>');" id="mono" title="Vložit značku odstavce">&lt;p&gt;</a>
                    <a href="javascript:insertAtCursor(document.form.text, '<pre>', '</pre>');" id="mono" title="Vložit značku formátovaného textu. Vhodné pro konfigurační soubory či výpisy.">&lt;pre&gt;</a>
                    <a href="javascript:insertAtCursor(document.form.text, '<code>', '</code>');" id="mono" title="Vložit značku pro písmo s pevnou šířkou">&lt;code&gt;</a>
                </div>
                <div class="error">${ERRORS.text?if_exists}</div>
                <textarea tabindex="5" name="text" class="siroka" rows="20">${PARAMS.text?if_exists?html}</textarea>
            </td>
        </tr>
        <tr>
            <td>&nbsp;</td>
            <td>
                <input tabindex="6" type="submit" name="preview" value="Zopakuj náhled">
                <input tabindex="7" type="submit" name="finish" value="Dokonči">
            </td>
        </tr>
    </table>
    <input type="hidden" name="action" value="addQuez2">
    <input type="hidden" name="rid" value="${PARAMS.rid}">
</form>

<#include "/include/napoveda-k-auto-formatovani.txt">

<#include "../footer.ftl">
