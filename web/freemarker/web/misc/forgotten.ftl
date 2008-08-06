<#include "/include/macros.ftl">
<#include "../header.ftl">

<@lib.showMessages>

<p>Zapomněli jste své heslo? Žádný problém. Pomocí tohoto formuláře
najdete své konto. Pak jednodušete svůj výběr potvrdíte a systém
neprodleně odešle vaše heslo na registrovanou emailovou adresu.</p>

<p>Zadejte svůj login či jméno. Stačí i jeho část, nejméně však
tři znaky. Pro větší bezpečnost
však bude vrácen seznam maximálně pěti nalezených uživatelů.</p>

<form action="${URL.noPrefix("/ForgottenPassword")}" method="POST">
<table cellpadding=5>
  <tr>
    <td>Přihlašovací jméno: </td>
    <td><input type="text" name="login" value="${PARAMS.login?if_exists}" size=8 tabindex=1>
    <span class="error">${ERRORS?if_exists.login?if_exists}</span></td>
  </tr>
  <tr>
    <td>Vaše jméno: </td>
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
