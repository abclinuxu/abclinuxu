<#include "../header.ftl">

<@lib.showMessages/>

<p>Na t�to str�nce si m��ete zm�nit heslo.
Pro va�i ochranu nejd��ve zadejte sou�asn�
heslo a pak dvakr�t nov� heslo. Heslo mus�
m�t nejm�n� �ty�i znaky.</p>

<form action="${URL.noPrefix("/EditUser")}" method="POST">
 <table width="100%" border=0 cellpadding=5>
  <tr>
   <td class="required" width="160">Sou�asn� heslo</td>
   <td>
    <input type="password" name="PASSWORD" size="16" tabindex="1">
    <div class="error">${ERRORS.PASSWORD?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td class="required" width="160">Nov� heslo</td>
   <td>
    <input type="password" name="password" size="16" maxlength="12" tabindex="2">
    <div class="error">${ERRORS.password?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td class="required" width="160">Zopakujte nov� heslo</td>
   <td>
    <input type="password" name="password2" size="16" tabindex="3">
   </td>
  </tr>
  <tr>
   <td width="160">&nbsp;</td>
   <td><input type="submit" value="Dokon�i" tabindex="4"></td>
  </tr>
 </table>
 <input type="hidden" name="action" value="changePassword2">
 <input type="hidden" name="uid" value="${MANAGED.id}">
</form>


<#include "../footer.ftl">
