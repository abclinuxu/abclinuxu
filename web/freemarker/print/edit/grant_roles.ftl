<#include "../header.ftl">

<@lib.showMessages/>

<form action="${URL.noPrefix("/EditUser")}" method="POST">
 <table width="100%" border=0 cellpadding=5>
  <tr>
   <td class="required" width="100">Vaše heslo</td>
   <td>
    <input type="password" name="PASSWORD" size="16" tabindex="1">
    <div class="error">${ERRORS.PASSWORD?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td class="required" width="100">Uživatel</td>
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
       <@lib.showOption "roles", "survey admin", "Administrátor anket/průzkumů", "checkbox" /><br>
       <@lib.showOption "roles", "blog digest admin", "Administrátor blog digestu", "checkbox" /><br>
       <@lib.showOption "roles", "discussion admin", "Administrátor diskusí", "checkbox" /><br>
       <@lib.showOption "roles", "tag admin", "Administrátor štítků", "checkbox" /><br>
       <@lib.showOption "roles", "tip admin", "Administrátor tipů", "checkbox" /><br>
       <@lib.showOption "roles", "user admin", "Administrátor uživatelů", "checkbox" /><br>
       <@lib.showOption "roles", "news admin", "Administrátor zpráviček", "checkbox" /><br>
       <@lib.showOption "roles", "advertisement admin", "Administrátor reklamních pozic", "checkbox" /><br>
       <@lib.showOption "roles", "email invalidator", "Smí invalidovat emaily", "checkbox" /><br>
   </td>
  </tr>
  <tr>
   <td width="100">&nbsp;</td>
   <td><input type="submit" value="Dokonči" tabindex="3"></td>
  </tr>
 </table>
 <input type="hidden" name="uid" value="${PARAMS.uid}">
 <input type="hidden" name="action" value="grant3">
</form>


<#include "../footer.ftl">
