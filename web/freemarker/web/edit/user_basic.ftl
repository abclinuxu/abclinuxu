<#include "../header.ftl">

<@lib.showMessages/>

<p>Na této stránce si mù¾ete zmìnit své základní údaje.
Pro va¹i ochranu nejdøíve zadejte souèasné heslo.</p>

<p>
Pøihla¹ovací jméno (login) musí mít nejménì tøi znaky,
maximálnì 16 znakù a to pouze písmena A a¾ Z, èíslice,
pomlèku, teèku nebo podtr¾ítko.
Login vás jednoznaènì identifikuje v systému,
proto není mo¾né pou¾ívat hodnotu, které ji¾ pou¾il
nìkdo pøed vámi.
</p>

<p>Va¹e jméno musí být nejménì pìt znakù dlouhé. Email musí být platný.
Nebojte se, budeme jej chránit pøed spammery a my vám budeme
zasílat jen ty informace, které si sami objednáte.
</p>

<form action="${URL.noPrefix("/EditUser")}" method="POST">
 <table width="100%" border=0 cellpadding=5>
  <tr>
   <td class="required" width="60">Heslo</td>
   <td>
    <input type="password" name="PASSWORD" size="16" tabindex="1">
    <div class="error">${ERRORS.PASSWORD?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td class="required" width="60">Jméno</td>
   <td>
    <input type="text" name="name" value="${PARAMS.name?if_exists}" size="24" tabindex="2">
    <div class="error">${ERRORS.name?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td class="required" width="60">Login</td>
   <td>
    <input type="text" name="login" value="${PARAMS.login?if_exists}" size="24" tabindex="3">
    <div class="error">${ERRORS.login?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td class="required" width="60">Email</td>
   <td>
    <input type="text" name="email" value="${PARAMS.email?if_exists}" size="24" tabindex="4">
    <div class="error">${ERRORS.email?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td class="required" width="60">Pøezdívka</td>
   <td>
    <input type="text" name="nick" value="${PARAMS.nick?if_exists}" size="24" tabindex="5">
    <div class="error">${ERRORS.nick?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="60">&nbsp;</td>
   <td><input type="submit" value="Dokonèi" tabindex="6"></td>
  </tr>
 </table>
 <input type="hidden" name="action" value="editBasic2">
 <input type="hidden" name="uid" value="${MANAGED.id}">
</form>


<#include "../footer.ftl">
