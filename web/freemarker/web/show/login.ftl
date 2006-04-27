<#include "../header.ftl">

<@lib.showMessages/>

<p>Tento krok vy�aduje, abyste se p�ihl�sil(a) na sv�j u�ivatelsk� ��et.
Pokud je�t� ��dn� nem�te, m��ete si jej
<a href="${URL.noPrefix("/EditUser?action=register")}">vytvo�it</a>.
Zabere v�m to jen okam�ik a p�inese spoustu v�hod. Pokud u n�s ��et
ji� m�te, ale nepamatujete si p�ihla�ovac� �daje,
<a href="${URL.noPrefix("/SelectUser?sAction=form&amp;action=forgottenPassword&amp;TITLE=Zasl�n�+zapomenut�ho+hesla&amp;url=/Profile")}">najd�te si jej</a>
a my v�m za�leme heslo.
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
