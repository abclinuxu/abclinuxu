<#include "../header.ftl">

<@lib.showMessages/>

<form action="${URL.noPrefix("/EditUser")}" method="POST">
 <table width="100%" border=0 cellpadding=5>
  <tr>
   <td class="required" width="100">Va�e heslo</td>
   <td>
    <input type="password" name="PASSWORD" size="16" tabindex="1">
    <div class="error">${ERRORS.PASSWORD?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td class="required" width="100">U�ivatel</td>
   <td>
    ${MANAGED.name}
    <div class="error">${ERRORS.uid?if_exists}</div>
   </td>
  </tr>

  <#macro isDefined(name)><#if PARAMS.roles?exists and PARAMS.roles.contains(name)>checked</#if></#macro>

  <tr>
   <td class="required" width="100">Jeho role</td>
   <td>
     <input type="checkbox" name="roles" value="root" <@isDefined "root"/>> Root<br>
     <input type="checkbox" name="roles" value="user admin" <@isDefined "user admin"/>> Administr�tor u�ivatel�<br>
     <input type="checkbox" name="roles" value="discussion admin" <@isDefined "discussion admin"/>> Administr�tor diskus�<br>
     <input type="checkbox" name="roles" value="news admin" <@isDefined "news admin"/>> Administr�tor zpr�vi�ek<br>
     <input type="checkbox" name="roles" value="article admin" <@isDefined "article admin"/>> Administr�tor �l�nk�<br>
     <input type="checkbox" name="roles" value="dictionary admin" <@isDefined "dictionary admin"/>> Administr�tor slovn�ku<br>
     <input type="checkbox" name="roles" value="tip admin" <@isDefined "tip admin"/>> Administr�tor tip�<br>
     <input type="checkbox" name="roles" value="category admin" <@isDefined "category admin"/>> Administr�tor sekc�<br>
     <input type="checkbox" name="roles" value="survey admin" <@isDefined "survey admin"/>> Administr�tor anket<br>
     <input type="checkbox" name="roles" value="poll admin" <@isDefined "poll admin"/>> Administr�tor mal�ch anket<br>
     <input type="checkbox" name="roles" value="requests admin" <@isDefined "requests admin"/>> Administr�tor po�adavk�<br>
     <input type="checkbox" name="roles" value="move relation" <@isDefined "move relation"/>> Sm� p�esunout relaci<br>
     <input type="checkbox" name="roles" value="remove relation" <@isDefined "remove relation"/>> Sm� smazat relaci a objekt<br>
     <input type="checkbox" name="roles" value="email invalidator" <@isDefined "email invalidator"/>> Sm� invalidovat emaily
   </td>
  </tr>
  <tr>
   <td width="100">&nbsp;</td>
   <td><input type="submit" value="Dokon�i" tabindex="3"></td>
  </tr>
 </table>
 <input type="hidden" name="uid" value="${PARAMS.uid}">
 <input type="hidden" name="action" value="grant3">
</form>


<#include "../footer.ftl">
