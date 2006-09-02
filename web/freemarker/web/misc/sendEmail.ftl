<#include "../header.ftl">

<@lib.showMessages/>

<p>Chr�n�me na�e u�ivatele p�ed roboty spammer�, proto z�sadn�
nezve�ej�ujeme jejich emailov� adresy. Pokud chcete n�kter�ho
na�eho u�ivatele kontaktovat emailem, tento formul�� v�m to umo�n�.
Z�rove� m��ete zadat adresu, kam se m� poslat (slep�) kopie emailu.
</p>

<p>Jako mal� prevence p�ed zneu��v�n�m slou�� kontroln�
k�d. Ve va�em p��pad� m� hodnotu ${KOD}. Vypl�te jej pros�m
n�e do formul��e. Do pol��ka pro email zad�vajte maxim�ln� jednu
adresu.</p>

<form action="${URL.noPrefix("/Mail")}" method="POST">

 <table class="siroka" border="0" cellpadding="5">
  <tr>
   <td class="required">Kontroln� k�d</td>
   <td><input type="text" name="KOD" value="${PARAMS.KOD?if_exists}" size="4">
   <#if ERRORS.KOD?exists><span class="error">${ERRORS.KOD}</span></#if>
   </td>
  </tr>
  <tr>
   <td class="required">V� email</td>
   <td><input type="text" name="sender" size="30" value="${PARAMS.sender?if_exists}">
   <#if ERRORS.sender?exists><span class="error">${ERRORS.sender}</span></#if>
   </td>
  </tr>
  <tr>
   <td>Kopie</td>
   <td><input type="text" name="cc" size="30" value="${PARAMS.cc?if_exists}">
   <#if ERRORS.cc?exists><span class="error">${ERRORS.cc}</span></#if>
   </td>
  </tr>
  <tr>
   <td>Slep� kopie</td>
   <td><input type="text" name="bcc" size="30" value="${PARAMS.bcc?if_exists}">
   <#if ERRORS.bcc?exists><span class="error">${ERRORS.bcc}</span></#if>
   </td>
  </tr>
  <tr>
   <td class="required">P�edm�t zpr�vy</td>
   <td><input type="text" name="subject" size="30" value="${PARAMS.subject?if_exists}">
   <#if ERRORS.subject?exists><span class="error">${ERRORS.subject}</span></#if>
   </td>
  </tr>
  <tr>
   <td colspan="2">Text zpr�vy<br>
   <textarea name="message" class="siroka" rows="20">${PARAMS.message?if_exists}</textarea>
   <#if ERRORS.message?exists><br><span class="error">${ERRORS.message}</span></#if>
   </td>
  </tr>
  <tr>
   <td colspan="2">
    <input type="submit" name="finish" value="Odeslat">
    <#if PARAMS.url?exists><input type="hidden" name="url" value="${PARAMS.url}"></#if>
   </td>
  </tr>
 </table>

 <input type="hidden" name="action" value="finish">
</form>

<#include "../footer.ftl">
