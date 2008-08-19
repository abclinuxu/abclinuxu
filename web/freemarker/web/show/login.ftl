<#include "../header.ftl">

<@lib.showMessages/>

<h1>Přihlášení</h1>

<#if EXTRA_TEMPLATE?exists>
    <#include EXTRA_TEMPLATE>
</#if>

<p>
    Tento krok vyžaduje, abyste se přihlásil(a). Přihlašovací údaje jsou společné pro rodinu serverů
    abclinuxu.cz, itbiz.cz, abcprace.cz, abchost.cz a 64bit.cz, takže můžete použít přihlašovací údaje
    z kteréhokoliv těchto serverů. Pokud jste se ještě na žádném z techto serverů nezaregistrovali,
    využijte jednoduchého <a href="${URL.noPrefix("/EditUser?action=register")}">registračního formuláře</a>.
    Registrace je opravdu jednoduchá, zabere jen okamžik vašeho času a přinese vám mnoho výhod oproti
    neregistrovaným uživatelům.
    <a href="${URL.noPrefix("/EditUser?action=forgottenPassword")}">Zapomněli jste své heslo?</a>
</p>

<form action="https://www.abclinuxu.cz${REQUEST_URI}" method="POST">

<table border="0" cellpadding="5">
  <tr>
    <td>Login: </td>
    <td>
     <input type="text" name="LOGIN" value="${PARAMS.LOGIN?if_exists}" size="8" tabindex="1">
     <span class="error">${ERRORS.LOGIN?if_exists}</span>
    </td>
  </tr>
  <tr>
    <td>Heslo:</td>
    <td><input type="password" name="PASSWORD" size="8" tabindex="2">
    <span class="error">${ERRORS.PASSWORD?if_exists}</span></td>
  </tr>
  <tr>
    <td>&nbsp;</td>
    <td>
        <label><input type="checkbox" name="noCookie" value="yes">Neukládat přihlašovací cookie</label>
        <@lib.showHelp>Použijte, pokud se přihlašujete na cizím počítači.</@lib.showHelp>
    </td>
  </tr>
  <tr>
    <td>&nbsp;</td>
    <td>
        <label><input type="checkbox" name="useHttps" value="yes">Používat HTTPS i po přihlášení</label>
    </td>
  </tr>
  <tr>
    <td>&nbsp;</td>
    <td>
     <input type="submit" name="finish" value="OK" tabindex=3>
    </td>
  </tr>
</table>
<#if PARAMS.action?if_exists=="login">
  <input type="hidden" name="action" value="login2">
<#else>
  <input type="hidden" name="action" value="${PARAMS.action?if_exists}">
</#if>
${TOOL.saveParams(PARAMS, ["LOGIN","PASSWORD","action"])}
</form>

<#include "../footer.ftl">
