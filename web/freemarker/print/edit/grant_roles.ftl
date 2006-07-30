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

  <tr>
   <td class="required" width="100">Jeho role</td>
   <td>
       <@lib.showOption "roles", "root", "Root", "checkbox" /><br>
       <@lib.showOption "roles", "poll admin", "Administr�tor anket", "checkbox" /><br>
       <@lib.showOption "roles", "survey admin", "Administr�tor anket/pr�zkum�", "checkbox" /><br>
       <@lib.showOption "roles", "article admin", "Administr�tor �l�nk�", "checkbox" /><br>
       <@lib.showOption "roles", "discussion admin", "Administr�tor diskus�", "checkbox" /><br>
       <@lib.showOption "roles", "content admin", "Administr�tor dokument�", "checkbox" /><br>
       <@lib.showOption "roles", "requests admin", "Administr�tor po�adavk�", "checkbox" /><br>
       <@lib.showOption "roles", "attachment admin", "Administr�tor p��loh", "checkbox" /><br>
       <@lib.showOption "roles", "category admin", "Administr�tor sekc�", "checkbox" /><br>
       <@lib.showOption "roles", "dictionary admin", "Administr�tor slovn�ku", "checkbox" /><br>
       <@lib.showOption "roles", "blog digest admin", "Administr�tor blog digestu", "checkbox" /><br>
       <@lib.showOption "roles", "tip admin", "Administr�tor tip�", "checkbox" /><br>
       <@lib.showOption "roles", "user admin", "Administr�tor u�ivatel�", "checkbox" /><br>
       <@lib.showOption "roles", "news admin", "Administr�tor zpr�vi�ek", "checkbox" /><br>
       <@lib.showOption "roles", "email invalidator", "Sm� invalidovat emaily", "checkbox" /><br>
       <@lib.showOption "roles", "move relation", "Sm� p�esunout relaci", "checkbox" /><br>
       <@lib.showOption "roles", "derive content", "Sm� vytvo�it podstr�nku dokumentu", "checkbox" /><br>
       <@lib.showOption "roles", "remove relation", "Sm� smazat relaci a objekt", "checkbox" /><br>
       <@lib.showOption "roles", "blog digest admin", "Administr�tor blog digestu", "checkbox" /><br>
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
