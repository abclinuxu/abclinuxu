<#include "../header.ftl">

<@lib.showMessages/>

<p>Na t�to str�nce si m��ete upravit sv�j profil.
Profil slou�� jako va�e ve�ejn� domovsk� str�nka,
na kter� m�te mo�nost zve�ejnit informace o sv�
osob�. O tom, kdo jste, odkud jste, co m�te r�d,
jak� je va�e kr�do. Fantazii se meze nekladou.
</p>

<p>
Pro va�i ochranu nejd��ve zadejte sou�asn� heslo.
Pokud m�te na internetu svou domovskou str�nku,
vypl�te jej� URL. Dal�� polo�kou je rok, kdy jste
za�al pou��vat Linux. N�sleduje mo�nost ulo�it
a� p�t distribuc�, kter� v sou�asnosti pou��v�te.
Posledn�m pol��kem je text <i>O mn�</i>. Do n�j
m��ete napsat informace o sob�, kter� chcete sd�lit
�ten���m. M��e to b�t jen p�r slov, ale i del��
pov�d�n�. Sm�te pou��vat pro form�tov�n� z�kladn�
html zna�ky, pokud je nevyu�ijete, pr�zdn� ��dek
bude odd�lovat odstavce.
</p>

<form action="${URL.noPrefix("/EditUser")}" method="POST">
 <table width="100%" border=0 cellpadding=5>
  <tr>
   <td class="required" width="120">Heslo</td>
   <td>
    <input type="password" name="PASSWORD" size="20" tabindex="1" class="pole">
    <div class="error">${ERRORS.PASSWORD?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="120">Domovsk� str�nka</td>
   <td>
    <input type="text" name="www" value="${PARAMS.www?if_exists}" size="40" tabindex="2" class="pole">
    <div class="error">${ERRORS.www?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="120">Linux pou��v�m<br>od roku</td>
   <td>
    <input type="text" name="linuxFrom" value="${PARAMS.linuxFrom?if_exists}" size="40" tabindex="2" class="pole">
   </td>
  </tr>
  <tr>
   <td width="120" valign="middle">Pou��v�m tyto distribuce</td>
   <td>
    <#assign distros=PARAMS.distribution?if_exists>
    <#if distros?size gte 1 >
     <input type="text" name="distribution" value="${distros[0]}" size="40" tabindex="3" class="pole"><br>
    <#else>
     <input type="text" name="distribution" size="16" tabindex="3" class="pole"><br>
    </#if>
    <#if distros?size gte 2 >
     <input type="text" name="distribution" value="${distros[1]}" size="40" tabindex="4" class="pole"><br>
    <#else>
     <input type="text" name="distribution" size="16" tabindex="4" class="pole"><br>
    </#if>
    <#if distros?size gte 3 >
     <input type="text" name="distribution" value="${distros[2]}" size="40" tabindex="5" class="pole"><br>
    <#else>
     <input type="text" name="distribution" size="16" tabindex="5" class="pole"><br>
    </#if>
    <#if distros?size gte 4 >
     <input type="text" name="distribution" value="${distros[3]}" size="40" tabindex="6" class="pole"><br>
    <#else>
     <input type="text" name="distribution" size="16" tabindex="6" class="pole"><br>
    </#if>
    <#if distros?size gte 5 >
     <input type="text" name="distribution" value="${distros[4]}" size="40" tabindex="7" class="pole"><br>
    <#else>
     <input type="text" name="distribution" size="16" tabindex="7" class="pole"><br>
    </#if>
   </td>
  </tr>
  <tr>
   <td colspan="2">O mn�</td>
  </tr>
  <tr>
   <td colspan="2">
    <textarea name="about" rows="25" cols="70" class="pole">${PARAMS.about?if_exists?html}</textarea>
    <div class="error">${ERRORS.about?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="120">&nbsp;</td>
   <td><input type="submit" value="Dokon�i" tabindex="8" class="buton"></td>
  </tr>
 </table>
 <input type="hidden" name="action" value="editProfile2">
 <input type="hidden" name="uid" value="${MANAGED.id}">
</form>


<#include "../footer.ftl">
