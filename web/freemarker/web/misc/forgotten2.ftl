<#include "/include/macros.ftl">
<#include "../header.ftl">

<#call showMessages>

<p>Nalezeno ${USERS?size} u�ivatel�.</p>

<#if USERS?size gt 5>
<p>Zobrazuji prvn� p�t. Pokud mezi nimi chyb�te, vra�te se na minulou
str�nku a zp�esn�te v�b�r.</p>
</#if>

<p>Zvolte si sv�ho u�ivatele a stiskn�te OK.
Syst�m neprodlen� ode�le va�e heslo na registrovanou emailovou adresu.
Pokud v�m email nep�ijde, pravd�podobn� va�e emailov� adresa
ji� nen� platn�. V tomto p��pad� kontaktujte administr�tora.</p>

<form action="${URL.noPrefix("/ForgottenPassword")}" method="POST">
 <table>
  <tr>
    <td>Login</td>
    <td>Jm�no u�ivatele</td>
  </tr>
<#list TOOL.sublist(USERS,0,5) as user>
  <tr>
    <td>
     <input type="radio" name="uid" value="${user.id}" <#if user_index==0>CHECKED</#if> >
     ${user.login}
    </td>
    <td>${user.name}</td>
  </tr>
</#list>
  <tr>
    <td colspan="2"><input type="submit" name="finish" value="OK" tabindex=3></td>
  </tr>
 </table>
 <input type="hidden" name="action" value="send">
</form>

<#include "../footer.ftl">
