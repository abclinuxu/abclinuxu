<#include "/include/macros.ftl">
<#include "../header.ftl">

<#call showMessages>

<p>Tento formuláø slou¾í pro zneplatnìní emailu u¾ivatelùm.
Pokud se nám vrátí nìkterı email jako nedoruèitelnı,
zde uveïte èíslo u¾ivatele. Jedno èíslo na jeden øádek.
Po odeslání bude email tìchto u¾ivatelù oznaèen jako
neplatnı a pøí¹tì jim nebude vygenerován ¾ádnı dal¹í
email, dokud si sami nezmìní adresu.</p>

<form action="${URL.make("/EditUser")}" method="POST">

 <table width=100 border=0 cellpadding=5>
  <tr>
   <td width="120">Èísla u¾ivatelù</td>
   <td>
    <textarea name="users" cols="20" rows="6" tabindex="1">${PARAMS.users?if_exists}</textarea>
   </td>
  </tr>
  <tr>
   <td width="120">&nbsp;</td>
   <td><input type="submit" value="Pokraèuj" tabindex="1"></td>
  </tr>
 </table>

 <input type="hidden" name="action" value="invalidateEmail2">
</form>


<#include "../footer.ftl">
