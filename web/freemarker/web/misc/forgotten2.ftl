<#include "/include/macros.ftl">
<#include "../header.ftl">

<#call showMessages>

<p>Nalezeno ${USERS?size} u¾ivatelù.</p>

<#if USERS?size gt 5>
<p>Zobrazuji první pìt. Pokud mezi nimi chybíte, vra»te se na minulou
stránku a zpøesnìte výbìr.</p>
</#if>

<p>Zvolte si svého u¾ivatele a stisknìte OK.
Systém neprodlenì ode¹le va¹e heslo na registrovanou emailovou adresu.
Pokud vám email nepøijde, pravdìpodobnì va¹e emailová adresa
ji¾ není platná. V tomto pøípadì kontaktujte administrátora.</p>

<form action="${URL.noPrefix("/ForgottenPassword")}" method="POST">
 <table>
  <tr>
    <td>Login</td>
    <td>Jméno u¾ivatele</td>
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
