<#include "../header.ftl">

<@lib.showMessages/>

<p>Tento krok vy¾aduje, abyste se pøihlásil(a) na svùj u¾ivatelský úèet.
Pokud je¹tì ¾ádný nemáte, mù¾ete si jej
<a href="${URL.noPrefix("/EditUser?action=register")}">vytvoøit</a>.
Zabere vám to jen okam¾ik a pøinese spoustu výhod. Pokud u nás úèet
ji¾ máte, ale nepamatujete si pøihla¹ovací údaje,
<a href="${URL.noPrefix("/SelectUser?sAction=form&amp;action=forgottenPassword&amp;TITLE=Zaslání+zapomenutého+hesla&amp;url=/Profile")}">najdìte si jej</a>
a my vám za¹leme heslo.
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
