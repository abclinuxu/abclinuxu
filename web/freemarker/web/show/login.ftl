<#include "../header.ftl">

<@lib.showMessages/>

<div style="width:250px; float:right; padding:0.5em; margin:0.5em; font-size:small; border-left:1px solid silver">
<img src="/images/site2/sf-login.gif" width="100" height="40" alt="Stickfish login" style="float:right; margin:0.5em;">
    Tento krok vyžaduje, abyste se přihlásil(a). Přihlašovací údaje jsou společné servery
    AbcLinuxu.cz a <a href="http://www.64bit.cz">64bit.cz</a>. Pokud jste se ještě na žádném z techto serverů nezaregistrovali,
    využijte <a href="${URL.noPrefix("/EditUser?action=register")}">registrační formulář</a>.
    Registrace je opravdu jednoduchá, zabere jen okamžik vašeho času a přinese vám mnoho výhod oproti
    neregistrovaným uživatelům.
</div>

<h1>Přihlášení</h1>

<#if EXTRA_TEMPLATE?exists>
    <#include EXTRA_TEMPLATE>
</#if>

<#if SYSTEM_CONFIG.getLoginUseHttps()>
    <form action="https://${SYSTEM_CONFIG.getHostname()}${REQUEST_URI}" method="POST">
<#else>
    <form action="${REQUEST_URI}" method="POST">
</#if>

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
    <td><input type="password" name="PASSWORD" size="8" tabindex="2"> <a href="${URL.noPrefix("/EditUser?action=forgottenPassword")}">Zapomněli jste své heslo?</a>
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
     <input type="submit" name="finish" value="Přihlásit" tabindex=3>
    </td>
  </tr>
</table>
<#if PARAMS.action?if_exists=="login">
  <input type="hidden" name="action" value="login2">
<#else>
  <input type="hidden" name="action" value="${PARAMS.action?if_exists}">
</#if>
${TOOL.saveParams(PARAMS, ["LOGIN","PASSWORD","action","useHttps","noCookie"])}
</form>

<#include "../footer.ftl">
