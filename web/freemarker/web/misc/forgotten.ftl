<#include "/include/macros.ftl">
<#include "../header.ftl">

<#call showMessages>

<p>Zapomn�li jste sv� heslo? ��dn� probl�m. Pomoc� tohoto formul��e
najdete sv� konto. Pak jednodu�ete sv�j v�b�r potvrd�te a syst�m
neprodlen� ode�le va�e heslo na registrovanou emailovou adresu.</p>

<p>Zadejte sv�j login �i jm�no. Sta�� i jeho ��st, nejm�n� v�ak
t�i znaky. Pro v�t�� bezpe�nost
v�ak bude vr�cen seznam maxim�ln� p�ti nalezen�ch u�ivatel�.</p>

<form action="${URL.noPrefix("/ForgottenPassword")}" method="POST">
<table cellpadding=5>
  <tr>
    <td>P�ihla�ovac� jm�no: </td>
    <td><input type="text" name="login" value="${PARAMS.login?if_exists}" size=8 tabindex=1>
    <span class="error">${ERRORS?if_exists.login?if_exists}</span></td>
  </tr>
  <tr>
    <td>Va�e jm�no: </td>
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
