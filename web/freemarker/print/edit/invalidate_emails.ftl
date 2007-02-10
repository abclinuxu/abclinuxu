<#include "../header.ftl">

<@lib.showMessages/>

<p>Tento formulář slouží pro zneplatnění emailu uživatelům.
Pokud se nám vrátí některý email jako nedoručitelný,
zde uveďte číslo uživatele. Jedno číslo na jeden řádek.
Po odeslání bude email těchto uživatelů označen jako
neplatný a příště jim nebude vygenerován žádný další
email, dokud si sami nezmění adresu.</p>

<form action="${URL.make("/EditUser")}" method="POST">

 <table width=100 border=0 cellpadding=5>
  <tr>
   <td width="120">Čísla uživatelů</td>
   <td>
    <textarea name="users" cols="20" rows="6" tabindex="1">${PARAMS.users?if_exists}</textarea>
   </td>
  </tr>
  <tr>
   <td width="120">&nbsp;</td>
   <td><input type="submit" value="Pokračuj" tabindex="1"></td>
  </tr>
 </table>

 <input type="hidden" name="action" value="invalidateEmail2">
</form>

<a href="${URL.make("/SelectUser?sAction=form&amp;url=/EditUser&amp;action=invalidateEmail2")}">Najdi uživatele</a>

<#include "../footer.ftl">
