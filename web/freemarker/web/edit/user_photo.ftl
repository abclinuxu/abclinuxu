<#include "../header.ftl">

<@lib.showMessages/>

<p>Tento formuláø slou¾í pro nahrání va¹í fotografie.
Pro va¹i ochranu nejdøíve zadejte souèasné heslo.
Pak vyberte soubor s va¹í fotografií. Soubor musí být
typu PNG, GIF nebo JPEG. Pro fotografie je optimální
JPEG, velikost souboru sni¾te vhodnou volbou komprese.
Doporuèené rozmìry obrázku jsou 175 pixelù na ¹íøku
a 200 pixelù na vý¹ku. Maximální velikost souboru je
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
   <td><input type="submit" value="Dokonèi" tabindex="3"></td>
  </tr>
 </table>
</form>


<#include "../footer.ftl">
