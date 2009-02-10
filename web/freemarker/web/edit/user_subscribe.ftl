<#include "../header.ftl">

<@lib.showMessages/>

<p>
    Náš portál pro vás připravil několik atraktivních služeb.
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
    <dt>Emailové rozhraní k poradnám</dt>
    <dd>
        Pro každý nový příspěvek diskuse umístěné v některé poradně se odešle všem přihlášeným
        uživatelům email s jeho obsahem a adresou, na které je možné odpovědět.
    </dd>
    <dt>Reklamní email</dt>
    <dd>
        Je forma, jak pomoci s financováním tohoto portálu. Maximálně jednou za čtrnáct dní (pravděpodobněji
        párkrát za rok) vám doručíme reklamní sdělení některého našeho inzerenta.
    </dd>
</dl>

<p>
    Další službou je  portálu AbcLinuxu.cz.
</p>

<p>
    Pro vaši ochranu nejdříve zadejte současné heslo.
</p>

<form action="${URL.noPrefix("/EditUser")}" method="POST">
    <table width="100%" border=0 cellpadding=5>
        <tr>
            <td class="required" width="60">Heslo</td>
            <td>
                <input type="password" name="PASSWORD" size="16" tabindex="1">

                <div class="error">${ERRORS.PASSWORD!}</div>
            </td>
        </tr>
        <tr>
            <td width="60">Týdenní souhrn</td>
            <td>
                <select name="weekly" tabindex="2">
                    <#assign weekly=PARAMS.weekly!>
                    <option value="yes" <#if weekly=="yes">SELECTED</#if>>ano</option>
                    <option value="no" <#if weekly=="no">SELECTED</#if>>ne</option>
                </select>
            </td>
        </tr>
        <tr>
            <td width="60">Měsíční zpravodaj</td>
            <td>
                <select name="monthly" tabindex="3">
                    <#assign monthly=PARAMS.monthly!"no">
                    <option value="yes" <#if monthly=="yes">SELECTED</#if>>ano</option>
                    <option value="no" <#if monthly=="no">SELECTED</#if>>ne</option>
                </select>
            </td>
        </tr>
        <tr>
            <td width="60">Reklamní email</td>
            <td>
                <select name="ad" tabindex="3">
                    <#assign advertisement=PARAMS.ad!"no">
                    <option value="yes" <#if advertisement=="yes">SELECTED</#if>>ano</option>
                    <option value="no" <#if advertisement=="no">SELECTED</#if>>ne</option>
                </select>
            </td>
        </tr>
        <tr>
            <td width="60">Diskusní fórum</td>
            <td>
                <select name="forum" tabindex="4">
                    <#assign forum=PARAMS.forum!"no">
                    <option value="yes" <#if forum=="yes">SELECTED</#if>>ano</option>
                    <option value="no" <#if forum=="no">SELECTED</#if>>ne</option>
                </select>
            </td>
        </tr>
        <tr>
            <td width="60">&nbsp;</td>
            <td><input type="submit" value="Dokonči" tabindex="5"></td>
        </tr>
    </table>
    <input type="hidden" name="action" value="subscribe2">
    <input type="hidden" name="uid" value="${MANAGED.id}">
</form>


<#include "../footer.ftl">
