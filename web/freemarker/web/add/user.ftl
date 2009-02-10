<#include "../header.ftl">

<@lib.showMessages/>

<h1>Registrace</h1>

<p>
    Děkujeme, že jste se rozhodli registrovat na rodině portálů abclinuxu.cz, itbiz.cz, abcprace.cz, abchost.cz a 64bit.cz.
    S jediným účtem se budete moci přihlásit na všech těchto serverech. Snažili jsme se registraci učinit co nejjednodušší,
    další informace a nastavení se následně můžete upravit v nastavení.
</p>

<p>
    Registrace vám přinese mnoho výhod oproti neregistrovaným uživatelům. Můžete snadno sledovat navštívené diskuse,
    najít vaše dotazy v poradně, monitorovat emailem zvolené dokumenty, upravit si vzhled a chování portálu a mnoho
    dalšího.
</p>

<form action="${URL.noPrefix("/EditUser")}" method="POST">
    <table class="siroka" border="0" cellpadding=5>
        <tr>
            <td class="required" width="160">
                Jméno
                <@lib.showHelp>Jméno musí mít nejméně tři znaky.</@lib.showHelp>
            </td>
            <td>
                <input type="text" name="name" value="${PARAMS.name!}" size="24" tabindex="1">
                <@lib.showError key="name" />
            </td>
            <td class="required" width="160">
                Heslo
                <@lib.showHelp>Heslo musí mít nejméně čtyři znaky.</@lib.showHelp>
            </td>
            <td>
                <input type="password" name="password" size="24" tabindex="4">
            </td>
        </tr>
        <tr>
            <td class="required" width="160">
                Login
                <@lib.showHelp>Přihlašovací jméno (login) musí být unikátní a mít nejméně tři znaky. Povolené znaky
                jsou písmena A až Z, číslice, pomlčka, tečka a podtržítko. Login se nedá změnit, je součástí jabber
                účtu a adresy vaši stránky /lide/vas-login.
                </@lib.showHelp>
            </td>
            <td>
                <input type="text" name="login" id="login" value="${PARAMS.login!}" size="24" tabindex="2"
                onChange="new Ajax.Updater('loginError', '/ajax/checkLogin', {parameters: { value : $F('login')}})">
                <div class="error" id="loginError">${ERRORS.login!}</div>
            </td>
            <td class="required" width="160">
                Zopakujte heslo:
            </td>
            <td>
                <input type="password" name="password2" size="24" tabindex="5">
                <@lib.showError key="password" />
            </td>
        </tr>
        <tr>
            <td width="160">
                Přezdívka
                <@lib.showHelp>Přezdívka musí být unikátní.</@lib.showHelp>
            </td>
            <td>
                <input type="text" name="nick" id="nick" value="${PARAMS.nick!}" size="24" tabindex="3"
                onChange="new Ajax.Updater('nickError', '/ajax/checkNick', {parameters: { value : $F('nick')}})">
                <div class="error" id="nickError">${ERRORS.nick!}</div>
            </td>
            <td class="required" width="160">
                Aktuální rok
                <@lib.showHelp>Vložte aktuální rok. Jedná se o ochranu před spamboty.</@lib.showHelp>
            </td>
            <td>
                <input type="text" size="4" name="antispam" value="${PARAMS.antispam!?html}" tabindex="6">
                <@lib.showError key="antispam" />
            </td>
        </tr>
        <tr>
            <td width="160">&nbsp;</td>
            <td align="right"><input type="submit" value="Dokonči" tabindex="7"></td>
            <td colspan="2">&nbsp;</td>
        </tr>
    </table>
    <input type="hidden" name="action" value="register2">
</form>


<#include "../footer.ftl">
