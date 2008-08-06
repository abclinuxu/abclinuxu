<#include "/include/macros.ftl">
<#include "../header.ftl">

<@lib.showMessages>

<p>Nalezeno ${USERS?size} uživatelů.</p>

<#if USERS?size gt 5>
<p>Zobrazuji první pět. Pokud mezi nimi chybíte, vraťte se na minulou
stránku a zpřesněte výběr.</p>
</#if>

<p>Zvolte si svého uživatele a stiskněte OK.
Systém neprodleně odešle vaše heslo na registrovanou emailovou adresu.
Pokud vám email nepřijde, pravděpodobně vaše emailová adresa
již není platná. V tomto případě kontaktujte administrátora.</p>

<form action="${URL.noPrefix("/ForgottenPassword")}" method="POST">
 <table>
  <tr>
    <td>Login</td>
    <td>Jméno uživatele</td>
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
