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
                <input type="text" name="name" value="${PARAMS.name?if_exists}" size="24" tabindex="1">
                <@lib.showError key="name" />
            </td>
            <td width="160">
                Přezdívka
                <@lib.showHelp>Přezdívka musí být unikátní.</@lib.showHelp>
            </td>
            <td>
                <input type="text" name="nick" id="nick" value="${PARAMS.nick?if_exists}" size="24" tabindex="6"
                onChange="new Ajax.Updater('nickError', '/ajax/checkNick', {parameters: { value : $F('nick')}})">
                <div class="error" id="nickError">${ERRORS.nick?if_exists}</div>
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
                <input type="text" name="login" id="login" value="${PARAMS.login?if_exists}" size="24" tabindex="2"
                onChange="new Ajax.Updater('loginError', '/ajax/checkLogin', {parameters: { value : $F('login')}})">
                <div class="error" id="loginError">${ERRORS.login?if_exists}</div>
            </td>
            <td  width="160">
                OpenId
                <@lib.showHelp>Podpora přihlašování přes openid se připravuje.</@lib.showHelp>
            </td>
            <td>
                <input type="text" name="openid" id="openid" value="${PARAMS.openid?if_exists}" size="24" tabindex="7">
                <#-- onChange="new Ajax.Updater('openidError', '/ajax/checkOpenId', {parameters: { value : $F('openid')}})" -->
                <div class="error" id="openidError">${ERRORS.openid?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td class="required" width="160">
                Heslo
                <@lib.showHelp>Heslo musí mít nejméně čtyři znaky.</@lib.showHelp>
            </td>
            <td>
                <input type="password" name="password" size="24" tabindex="3">
            </td>
            <td width="160">
                Email
                <@lib.showHelp>Email chráníme před spammery a zasíláme vám pouze vámi vyžádané informace.
                Po registraci vám přijde aktivační email pro ověření, zda jste skutečně vlastníkem emailové schránky.
                </@lib.showHelp>
            </td>
            <td>
                <input type="text" name="email" value="${PARAMS.email?if_exists}" size="24" tabindex="8">
                <@lib.showError key="email" />
            </td>
        </tr>
        <tr>
            <td class="required" width="160">
                Zopakujte heslo:
                <@lib.showHelp>Kontrola hesla.</@lib.showHelp>
            </td>
            <td>
                <input type="password" name="password2" size="24" tabindex="4">
                <@lib.showError key="password" />
            </td>
            <td width="160">
                Týdenní souhrn
                <@lib.showHelp>Každou sobotu rozesílaný automatický email se seznamem článků, zpráviček, diskusí
                a dalších informací, které byly vytvořeny během uplynulého týdne.</@lib.showHelp>
            </td>
            <td>
                <select name="weekly" tabindex="9">
                    <#assign weekly=PARAMS.weekly?default("no")>
                    <option value="yes" <#if weekly=="yes">SELECTED</#if>>ano</option>
                    <option value="no"<#if weekly=="no">SELECTED</#if>>ne</option>
                </select>
            </td>
        </tr>
        <tr>
            <td width="160">Vaše pohlaví</td>
            <td>
                <select name="sex" tabindex="5">
                    <#assign sex=PARAMS.sex?default("UNDEF")>
                    <option value="undef">nezadávat</option>
                    <option value="man" <#if sex=="man">SELECTED</#if>>muž</option>
                    <option value="woman"<#if sex=="woman">SELECTED</#if>>žena</option>
                </select>
                <@lib.showError key="sex" />
            </td>
            <td width="160">
                Zpravodaj
                <@lib.showHelp>Nepravidelný ručně psaný email s novinkami na portále.</@lib.showHelp>
            </td>
            <td>
                <select name="monthly" tabindex="10">
                    <#assign monthly=PARAMS.monthly?default("no")>
                    <option value="yes" <#if monthly=="yes">SELECTED</#if>>ano</option>
                    <option value="no"<#if monthly=="no">SELECTED</#if>>ne</option>
                </select>
            </td>
        </tr>
        <tr>
            <td class="required" width="160">
                Aktuální rok
                <@lib.showHelp>Vložte aktuální rok. Jedná se o ochranu před spamboty.</@lib.showHelp>
            </td>
            <td>
                <input type="text" size="4" name="antispam" value="${PARAMS.antispam?if_exists?html}">
                <@lib.showError key="antispam" />
            </td>
            <td  colspan="2">&nbsp;</td>
        </tr>
        <tr>
            <td width="160">&nbsp;</td>
            <td align="right"><input type="submit" value="Dokonči" tabindex="11"></td>
            <td colspan="2">&nbsp;</td>
        </tr>
    </table>
    <input type="hidden" name="action" value="register2">
</form>


<#include "../footer.ftl">
