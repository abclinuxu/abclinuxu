<#include "/include/macros.ftl">
<#include "../header.ftl">

<#call showMessages>

<p>Tento formul�� slou�� pro zneplatn�n� emailu u�ivatel�m.
Pokud se n�m vr�t� n�kter� email jako nedoru�iteln�,
zde uve�te ��slo u�ivatele. Jedno ��slo na jeden ��dek.
Po odesl�n� bude email t�chto u�ivatel� ozna�en jako
neplatn� a p��t� jim nebude vygenerov�n ��dn� dal��
email, dokud si sami nezm�n� adresu.</p>

<form action="${URL.make("/EditUser")}" method="POST">

 <table width=100 border=0 cellpadding=5>
  <tr>
   <td width="120">��sla u�ivatel�</td>
   <td>
    <textarea name="users" cols="20" rows="6" tabindex="1">${PARAMS.users?if_exists}</textarea>
   </td>
  </tr>
  <tr>
   <td width="120">&nbsp;</td>
   <td><input type="submit" value="Pokra�uj" tabindex="1"></td>
  </tr>
 </table>

 <input type="hidden" name="action" value="invalidateEmail2">
</form>


<#include "../footer.ftl">
