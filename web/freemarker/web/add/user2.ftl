<#include "../header.ftl">

<@lib.showMessages/>

<h1>Registrace</h1>

<p>
    Gratulujeme, vaše registrace byla úspěšná. Nyní můžete doplnit několik dalších údajů. Nemáte-li čas či chuť,
    nebo byste je chtěli změnit, po přihlášení se v horní části stránky objeví odkaz Nastavení, kde najdete všechny
    volby a preference.
</p>

<dl>
    <dt>Email</dt>
    <dd>
        Email chráníme před spammery a zasíláme vám pouze vámi vyžádané informace, které můžete kdykoliv odhlásit.
    </dd>
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

<form action="${URL.noPrefix("/EditUser")}" method="POST">
    <table class="siroka" border="0" cellpadding=5>
        <tr>
            <td width="160">
                Email
            </td>
            <td>
                <input type="text" name="email" value="${PARAMS.email!}" size="24" tabindex="8">
                <@lib.showError key="email" />
            </td>
            <td  width="160">
                OpenId
                <@lib.showHelp>Podpora přihlašování přes openid se připravuje.</@lib.showHelp>
            </td>
            <td>
                <input type="text" name="openid" id="openid" value="${PARAMS.openid!}" size="24" tabindex="7">
                <#-- onChange="new Ajax.Updater('openidError', '/ajax/checkOpenId', {parameters: { value : $F('openid')}})" -->
                <div class="error" id="openidError">${ERRORS.openid!}</div>
            </td>
        </tr>
        <tr>
            <td width="160">Vaše pohlaví</td>
            <td>
                <select name="sex" tabindex="5">
                    <#assign sex=PARAMS.sex!"UNDEF">
                    <option value="undef">nezadávat</option>
                    <option value="man" <#if sex=="man">SELECTED</#if>>muž</option>
                    <option value="woman"<#if sex=="woman">SELECTED</#if>>žena</option>
                </select>
                <@lib.showError key="sex" />
            </td>
            <td width="160">
                Týdenní souhrn
            </td>
            <td>
                <select name="weekly" tabindex="9">
                    <#assign weekly=PARAMS.weekly!"no">
                    <option value="yes" <#if weekly=="yes">SELECTED</#if>>ano</option>
                    <option value="no"<#if weekly=="no">SELECTED</#if>>ne</option>
                </select>
            </td>
        </tr>
        <tr>
            <td width="160">
                Zpravodaj
            </td>
            <td>
                <select name="monthly" tabindex="10">
                    <#assign monthly=PARAMS.monthly!"no">
                    <option value="yes" <#if monthly=="yes">SELECTED</#if>>ano</option>
                    <option value="no"<#if monthly=="no">SELECTED</#if>>ne</option>
                </select>
            </td>
            <td width="160">
                Reklamní email
            </td>
            <td>
                <select name="ad" tabindex="3">
                    <#assign advertisement=PARAMS.ad!"no">
                    <option value="yes" <#if advertisement=="yes">SELECTED</#if>>ano</option>
                    <option value="no" <#if advertisement=="no">SELECTED</#if>>ne</option>
                </select>
            </td>
        </tr>
        <tr>
            <td width="160">&nbsp;</td>
            <td align="right"><input type="submit" value="Dokonči" tabindex="11"></td>
            <td colspan="2">&nbsp;</td>
        </tr>
    </table>
    <input type="hidden" name="action" value="register3">
</form>


<#include "../footer.ftl">
