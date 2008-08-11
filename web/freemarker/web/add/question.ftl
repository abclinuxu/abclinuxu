<#include "../header.ftl">

<@lib.showMessages/>

<h1>Pokládání dotazu</h1>

<#assign rules=TOOL.xpath(RELATION.child, "data/rules")?default("UNDEF")>
<#if rules!="UNDEF">
    ${TOOL.render(rules,USER?if_exists)}
</#if>

<#if ! USER?exists>
 <h2>Proč se přihlásit</h2>

 <p>Registrovaní čtenáři si mohou nechat sledovat diskusi, takže jim budou emailem posílány
 reakce ostatních čtenářů. Zároveň si budete moci ve svém profilu snadno vyhledat
 tento dotaz. Proto je výhodné se přihlásit. Nemáte-li u nás ještě účet,
 <a href="${URL.noPrefix("/EditUser?action=add")}">zaregistrujte&nbsp;se</a>. </p>
</#if>

<form action="${URL.make("/EditDiscussion")}" method="POST" name="form" enctype="multipart/form-data">
    <table class="siroka" cellpadding="5">
        <#if ! USER?exists>
            <tr>
                <td class="required">Login a heslo</td>
                <td>
                    <input tabindex="1" type="text" name="LOGIN" size="8">
                    <input tabindex="2" type="password" name="PASSWORD" size="8">
                    <label><input tabindex="3" type="checkbox" name="noCookie" value="yes">Neukládat přihlašovací cookie</label>
                    <@lib.showHelp>Použijte, pokud se přihlašujete na cizím počítači.</@lib.showHelp>
                </td>
            </tr>
            <tr>
                <td class="required">nebo vaše jméno</td>
                <td>
                    <input tabindex="4" type="text" size="30" name="author" value="${PARAMS.author?if_exists?html}">
                </td>
            </tr>
            <#if ! USER_VERIFIED?if_exists>
                <tr>
                    <td class="required">Aktuální rok</td>
                    <td>
                        <input type="text" size="4" name="antispam" value="${PARAMS.antispam?if_exists?html}" tabindex="4">
                        <a class="info" href="#">?<span class="tooltip">Vložte aktuální rok. Jedná se o ochranu
                        před spamboty. Po úspěšném ověření se uloží cookie (včetně vašeho jména) a tato kontrola
                        přestane být prováděna.</span></a>
                    </td>
                </tr>
            </#if>
        </#if>
        <tr>
            <td class="required">Titulek</td>
            <td><input tabindex="4" type="text" name="title" size="40" maxlength="70"></td>
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
                <textarea tabindex="5" name="text" class="siroka" rows="20"></textarea><br>
            </td>
        </tr>
        <tr>
            <td>Příloha</td>
            <td>
                Vložení přílohy: <input type="file" name="attachment" tabindex="6">
                <@lib.showHelp>Například výpis logu, konfigurační soubor, snímek obrazovky a podobně.</@lib.showHelp>
            </td>
        </tr>
        <tr>
            <td>&nbsp;</td>
            <td><input tabindex="7" type="submit" name="preview" value="Náhled dotazu"></td>
        </tr>
    </table>
    <input type="hidden" name="action" value="addQuez2">
    <input type="hidden" name="rid" value="${PARAMS.rid}">
</form>

<#include "/include/napoveda-k-auto-formatovani.txt">


<#include "../footer.ftl">
