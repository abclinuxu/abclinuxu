<#include "../header.ftl">

<@lib.showMessages/>

<h1>Registrace</h1>

<p>
    Gratulujeme, vaše registrace byla úspěšná. Nyní můžete doplnit několik dalších údajů. Nemáte-li čas či chuť,
    nebo byste je chtěli změnit, po přihlášení se v horní části stránky objeví odkaz Nastavení, kde najdete všechny
    volby a preference.
</p>

<dl>
    <dt>Týdenní souhrn</dt>
    <dd>
        Je určen těm, kteří nemají čas denně nás navštěvovat. Pokud si jej přihlásíte,
        každý víkend vám zašleme emailem seznam článků a všechny zprávičky, které jsme
        daný týden vydali.
    </dd>
    <dt>Zpravodaj</dt>
    <dd>
        Pokud si jej přihlásíte, začátkem každého měsíce obdržíte email se spoustou zajímavostí
        ze světa Linuxu i z našeho portálu.
    </dd>
    <dt>Reklamní email</dt>
    <dd>
        Je forma, jak pomoci s financováním tohoto portálu. Maximálně jednou za čtrnáct dní (pravděpodobněji
        párkrát za rok) vám doručíme reklamní sdělení některého našeho inzerenta.
    </dd>
</dl>

<@lib.addForm URL.noPrefix("/EditUser")>
    <@lib.addFormField false, "OpenID", "Podpora přihlašování přes openid se připravuje.">
        <@lib.addInputBare "openid", 24>
            <#-- onChange="new Ajax.Updater('openidError', '/ajax/checkOpenId', {parameters: { value : $F('openid')}})" -->
        </@lib.addInputBare>
    </@lib.addFormField>

    <@lib.addSelect false, "sex", "Vaše pohlaví">
        <@lib.addOption "sex", "nezadávat", "undef" />
        <@lib.addOption "sex", "muž", "man" />
        <@lib.addOption "sex", "žena", "woman" />
    </@lib.addSelect>

    <@lib.addSelect false, "weekly", "Týdenní souhrn">
        <@lib.addOption "weekly", "ano", "yes" />
        <@lib.addOption "weekly", "ne", "no", true />
    </@lib.addSelect>

    <@lib.addSelect false, "monthly", "Zpravodaj">
        <@lib.addOption "monthly", "ano", "yes" />
        <@lib.addOption "monthly", "ne", "no", true />
    </@lib.addSelect>

    <@lib.addSelect false, "ad", "Reklamní e-mail">
        <@lib.addOption "ad", "ano", "yes" />
        <@lib.addOption "ad", "ne", "no", true />
    </@lib.addSelect>

    <@lib.addSubmit "Dokonči" />
    <@lib.addHidden "action", "register3" />
</@lib.addForm>

<#include "../footer.ftl">
