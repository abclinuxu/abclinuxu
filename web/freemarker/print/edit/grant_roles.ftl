<#include "../header.ftl">

<@lib.showMessages/>

<form action="${URL.noPrefix("/EditUser")}" method="POST">
 <table width="100%" border=0 cellpadding=5>
  <tr>
   <td class="required" width="100">Va¹e heslo</td>
   <td>
    <input type="password" name="PASSWORD" size="16" tabindex="1">
    <div class="error">${ERRORS.PASSWORD?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td class="required" width="100">U¾ivatel</td>
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
     <input type="checkbox" name="roles" value="user admin" <@isDefined "user admin"/>> Administrátor u¾ivatelù<br>
     <input type="checkbox" name="roles" value="discussion admin" <@isDefined "discussion admin"/>> Administrátor diskusí<br>
     <input type="checkbox" name="roles" value="news admin" <@isDefined "news admin"/>> Administrátor zprávièek<br>
     <input type="checkbox" name="roles" value="article admin" <@isDefined "article admin"/>> Administrátor èlánkù<br>
     <input type="checkbox" name="roles" value="dictionary admin" <@isDefined "dictionary admin"/>> Administrátor slovníku<br>
     <input type="checkbox" name="roles" value="tip admin" <@isDefined "tip admin"/>> Administrátor tipù<br>
     <input type="checkbox" name="roles" value="category admin" <@isDefined "category admin"/>> Administrátor sekcí<br>
     <input type="checkbox" name="roles" value="survey admin" <@isDefined "survey admin"/>> Administrátor anket<br>
     <input type="checkbox" name="roles" value="poll admin" <@isDefined "poll admin"/>> Administrátor malých anket<br>
     <input type="checkbox" name="roles" value="requests admin" <@isDefined "requests admin"/>> Administrátor po¾adavkù<br>
     <input type="checkbox" name="roles" value="move relation" <@isDefined "move relation"/>> Smí pøesunout relaci<br>
     <input type="checkbox" name="roles" value="remove relation" <@isDefined "remove relation"/>> Smí smazat relaci a objekt<br>
     <input type="checkbox" name="roles" value="email invalidator" <@isDefined "email invalidator"/>> Smí invalidovat emaily
   </td>
  </tr>
  <tr>
   <td width="100">&nbsp;</td>
   <td><input type="submit" value="Dokonèi" tabindex="3"></td>
  </tr>
 </table>
 <input type="hidden" name="uid" value="${PARAMS.uid}">
 <input type="hidden" name="action" value="grant3">
</form>


<#include "../footer.ftl">
