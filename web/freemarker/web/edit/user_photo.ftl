<#include "../header.ftl">

<@lib.showMessages/>

<p>Tento formul�� slou�� pro nahr�n� va�� fotografie.
Pro va�i ochranu nejd��ve zadejte sou�asn� heslo.
Pak vyberte soubor s va�� fotografi�. Soubor mus� b�t
typu PNG, GIF nebo JPEG. Pro fotografie je optim�ln�
JPEG, velikost souboru sni�te vhodnou volbou komprese.
Doporu�en� rozm�ry obr�zku jsou 175 pixel� na ���ku
a 200 pixel� na v��ku. Maxim�ln� velikost souboru je
50 KB.
</p>

<form action="${URL.noPrefix("/EditUser")}" method="POST"
 enctype="multipart/form-data">
 <input type="hidden" name="action" value="uploadPhoto2">
 <input type="hidden" name="uid" value="${MANAGED.id}">
 <table width="100%" border=0 cellpadding=5>
  <tr>
   <td class="required" width="120">Heslo</td>
   <td>
    <input type="password" name="PASSWORD" size="20" tabindex="1">
    <div class="error">${ERRORS.PASSWORD?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="120">Fotografie</td>
   <td>
    <input type="file" name="photo" size="20" tabindex="2">
    <div class="error">${ERRORS.photo?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="120">&nbsp;</td>
   <td><input type="submit" value="Dokon�i" tabindex="3"></td>
  </tr>
 </table>
</form>


<#include "../footer.ftl">
