<#include "/include/macros.ftl">
<#include "../header.ftl">

<#call showMessages>

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

  <#macro isDefined(name)><#if ! PARAMS.roles?exists><#return></#if><#if PARAMS.roles.contains(name)>checked</#if></#macro>

  <tr>
   <td class="required" width="100">Jeho role</td>
   <td>
     <input type="checkbox" name="roles" value="root" <#call isDefined("root")>> Root<br>
     <input type="checkbox" name="roles" value="user admin" <#call isDefined("user admin")>> Administrátor u¾ivatelù<br>
     <input type="checkbox" name="roles" value="discussion admin" <#call isDefined("discussion admin")>> Administrátor diskusí<br>
     <input type="checkbox" name="roles" value="news admin" <#call isDefined("news admin")>> Administrátor zprávièek<br>
     <input type="checkbox" name="roles" value="article admin" <#call isDefined("article admin")>> Administrátor èlánkù<br>
     <input type="checkbox" name="roles" value="dictionary admin" <#call isDefined("dictionary admin")>> Administrátor slovníku<br>
     <input type="checkbox" name="roles" value="tip admin" <#call isDefined("tip admin")>> Administrátor tipù<br>
     <input type="checkbox" name="roles" value="category admin" <#call isDefined("category admin")>> Administrátor sekcí<br>
     <input type="checkbox" name="roles" value="survey admin" <#call isDefined("survey admin")>> Administrátor anket<br>
     <input type="checkbox" name="roles" value="poll admin" <#call isDefined("poll admin")>> Administrátor malých anket<br>
     <input type="checkbox" name="roles" value="move relation" <#call isDefined("move relation")>> Smí pøesunout relaci<br>
     <input type="checkbox" name="roles" value="remove relation" <#call isDefined("remove relation")>> Smí smazat relaci a objekt<br>
     <input type="checkbox" name="roles" value="email invalidator" <#call isDefined("email invalidator")>> Smí invalidovat emaily
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
