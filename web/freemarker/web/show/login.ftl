<#include "../header.ftl">

<@lib.showMessages/>

<p>Tento krok vy¾aduje, abyste se pøihlásil. Pokud je¹tì
nejste registrován, mù¾ete tak uèinit
<a href="${URL.noPrefix("/EditUser?action=register")}">zde</a>.</p>

<form action="${REQUEST_URI}" method="POST">

<table border=0 cellpadding=5>
  <tr>
    <td>Login: </td>
    <td>
     <input type="text" name="LOGIN" value="${PARAMS.LOGIN?if_exists}" size="8" tabindex="1">
     <span class="error">${ERRORS.LOGIN?if_exists}</span>
    </td>
  </tr>
  <tr>
    <td>Heslo:</td>
    <td><input type="password" name="PASSWORD" size=8 tabindex=2>
    <span class="error">${ERRORS.PASSWORD?if_exists}</span></td>
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

<form action="${URL.noPrefix("/SelectUser")}" method="POST">
 <input type="submit" value="Zapomenul jsem heslo/login">
 <input type="hidden" name="sAction" value="form">
 <input type="hidden" name="action" value="forgottenPassword">
 <input type="hidden" name="url" value="${URL.noPrefix("/Profile")}">
 <input type="hidden" name="TITLE" value="Zaslání zapomenutého hesla">
</form>

<#include "../footer.ftl">
