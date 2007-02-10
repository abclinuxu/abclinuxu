<#include "../header.ftl">

<@lib.showMessages/>

<p>Tento krok vyžaduje, abyste se přihlásil(a) na svůj uživatelský účet.
Pokud ještě žádný nemáte, můžete si jej
<a href="${URL.noPrefix("/EditUser?action=register")}">vytvořit</a>.
Zabere vám to jen okamžik a přinese spoustu výhod. Pokud u nás účet
již máte, ale nepamatujete si přihlašovací údaje,
<a href="${URL.noPrefix("/SelectUser?sAction=form&amp;action=forgottenPassword&amp;TITLE=Zaslání+zapomenutého+hesla&amp;url=/Profile")}">najděte si jej</a>
a my vám zašleme heslo.
</p>

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

<#include "../footer.ftl">
