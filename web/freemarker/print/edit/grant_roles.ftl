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
       <@lib.showOption "roles", "bazaar admin", "Administrátor bazaru", "checkbox" /><br>
       <@lib.showOption "roles", "blog digest admin", "Administrátor blog digestu", "checkbox" /><br>
       <@lib.showOption "roles", "article admin", "Administrátor článků", "checkbox" /><br>
       <@lib.showOption "roles", "discussion admin", "Administrátor diskusí", "checkbox" /><br>
       <@lib.showOption "roles", "content admin", "Administrátor dokumentů", "checkbox" /><br>
       <@lib.showOption "roles", "games admin", "Administrátor her", "checkbox" /><br>
       <@lib.showOption "roles", "requests admin", "Administrátor požadavků", "checkbox" /><br>
       <@lib.showOption "roles", "attachment admin", "Administrátor příloh", "checkbox" /><br>
       <@lib.showOption "roles", "category admin", "Administrátor sekcí", "checkbox" /><br>
       <@lib.showOption "roles", "dictionary admin", "Administrátor slovníku", "checkbox" /><br>
       <@lib.showOption "roles", "software admin", "Administrátor softwarového katalogu", "checkbox" /><br>
       <@lib.showOption "roles", "tag admin", "Administrátor štítků", "checkbox" /><br>
       <@lib.showOption "roles", "tip admin", "Administrátor tipů", "checkbox" /><br>
       <@lib.showOption "roles", "user admin", "Administrátor uživatelů", "checkbox" /><br>
       <@lib.showOption "roles", "news admin", "Administrátor zpráviček", "checkbox" /><br>
       <@lib.showOption "roles", "advertisement admin", "Administrátor reklamních pozic", "checkbox" /><br>
       <@lib.showOption "roles", "email invalidator", "Smí invalidovat emaily", "checkbox" /><br>
       <@lib.showOption "roles", "move relation", "Smí přesunout relaci", "checkbox" /><br>
       <@lib.showOption "roles", "derive content", "Smí vytvořit podstránku dokumentu", "checkbox" /><br>
       <@lib.showOption "roles", "remove relation", "Smí smazat relaci a objekt", "checkbox" /><br>
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
