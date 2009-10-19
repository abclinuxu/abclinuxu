<@lib.addRTE textAreaId="text" formId="form" menu="wiki" />
<#include "../header.ftl">

<@lib.showMessages/>

<h1>Pokládání dotazu</h1>

<#assign rules=TOOL.xpath(RELATION.child, "data/rules")!"UNDEF">
<#if rules!="UNDEF">
    ${TOOL.render(rules,USER!)}
</#if>

<#if ! USER??>
 <h2>Proč se přihlásit</h2>

 <p>Registrovaní čtenáři si mohou nechat sledovat diskusi, takže jim budou emailem posílány
 reakce ostatních čtenářů. Zároveň si budete moci ve svém profilu snadno vyhledat
 tento dotaz. Proto je výhodné se přihlásit. Nemáte-li u nás ještě účet,
 <a href="${URL.noPrefix("/EditUser?action=add")}">zaregistrujte&nbsp;se</a>. </p>
</#if>

<form action="${URL.make("/EditDiscussion")}" method="POST" name="form" enctype="multipart/form-data">
    <table class="siroka" cellpadding="5">
        <#if ! USER??>
            <tr>
                <td class="required">Zadejte vaše jméno</td>
                <td>
                    <input tabindex="4" type="text" size="30" name="author" value="${PARAMS.author!?html}">
                </td>
                <td>
                    nebo <a href="/Profile?action=login">se přihlašte</a>.
                </td>
            </tr>
            <#if ! USER_VERIFIED!false>
                <tr>
                    <td class="required">Aktuální rok</td>
                    <td>
                        <input type="text" size="4" name="antispam" value="${PARAMS.antispam!?html}" tabindex="4">
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
                <@lib.showError key="text"/>
                <@lib.showRTEControls "text"/>
                <textarea tabindex="5" name="text" id="text" class="siroka" rows="20"></textarea>
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
