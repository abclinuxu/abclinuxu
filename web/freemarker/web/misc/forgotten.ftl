<#include "/include/macros.ftl">
<#include "../header.ftl">

<#call showMessages>

<p>Zapomnìli jste své heslo? ®ádný problém. Pomocí tohoto formuláøe
najdete své konto. Pak jednodu¹ete svùj výbìr potvrdíte a systém
neprodlenì ode¹le va¹e heslo na registrovanou emailovou adresu.</p>

<p>Zadejte svùj login èi jméno. Staèí i jeho èást, nejménì v¹ak
tøi znaky. Pro vìt¹í bezpeènost
v¹ak bude vrácen seznam maximálnì pìti nalezených u¾ivatelù.</p>

<form action="${URL.noPrefix("/ForgottenPassword")}" method="POST">
<table cellpadding=5>
  <tr>
    <td>Pøihla¹ovací jméno: </td>
    <td><input type="text" name="login" value="${PARAMS.login?if_exists}" size=8 tabindex=1>
    <span class="error">${ERRORS?if_exists.login?if_exists}</span></td>
  </tr>
  <tr>
    <td>Va¹e jméno: </td>
    <td><input type="text" name="name" value="${PARAMS.name?if_exists}" size=16 tabindex=2>
    <span class="error">${ERRORS?if_exists.name?if_exists}</span></td>
  </tr>
  <tr>
    <td colspan="2"><input type="submit" name="finish" value="OK" tabindex=3></td>
  </tr>
</table>
<input type="hidden" name="action" value="choose">
</form>

<#include "../footer.ftl">
