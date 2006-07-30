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

  <tr>
   <td class="required" width="100">Jeho role</td>
   <td>
       <@lib.showOption "roles", "root", "Root", "checkbox" /><br>
       <@lib.showOption "roles", "poll admin", "Administrátor anket", "checkbox" /><br>
       <@lib.showOption "roles", "survey admin", "Administrátor anket/prùzkumù", "checkbox" /><br>
       <@lib.showOption "roles", "article admin", "Administrátor èlánkù", "checkbox" /><br>
       <@lib.showOption "roles", "discussion admin", "Administrátor diskusí", "checkbox" /><br>
       <@lib.showOption "roles", "content admin", "Administrátor dokumentù", "checkbox" /><br>
       <@lib.showOption "roles", "requests admin", "Administrátor po¾adavkù", "checkbox" /><br>
       <@lib.showOption "roles", "attachment admin", "Administrátor pøíloh", "checkbox" /><br>
       <@lib.showOption "roles", "category admin", "Administrátor sekcí", "checkbox" /><br>
       <@lib.showOption "roles", "dictionary admin", "Administrátor slovníku", "checkbox" /><br>
       <@lib.showOption "roles", "blog digest admin", "Administrátor blog digestu", "checkbox" /><br>
       <@lib.showOption "roles", "tip admin", "Administrátor tipù", "checkbox" /><br>
       <@lib.showOption "roles", "user admin", "Administrátor u¾ivatelù", "checkbox" /><br>
       <@lib.showOption "roles", "news admin", "Administrátor zprávièek", "checkbox" /><br>
       <@lib.showOption "roles", "email invalidator", "Smí invalidovat emaily", "checkbox" /><br>
       <@lib.showOption "roles", "move relation", "Smí pøesunout relaci", "checkbox" /><br>
       <@lib.showOption "roles", "derive content", "Smí vytvoøit podstránku dokumentu", "checkbox" /><br>
       <@lib.showOption "roles", "remove relation", "Smí smazat relaci a objekt", "checkbox" /><br>
       <@lib.showOption "roles", "blog digest admin", "Administrátor blog digestu", "checkbox" /><br>
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
