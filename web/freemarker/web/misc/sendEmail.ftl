<#include "../header.ftl">

<@lib.showMessages/>

<p>Chráníme na¹e u¾ivatele pøed roboty spammerù, proto zásadnì
nezveøejòujeme jejich emailové adresy. Pokud chcete nìkterého
na¹eho u¾ivatele kontaktovat emailem, tento formuláø vám to umo¾ní.
Zároveò mù¾ete zadat adresu, kam se má poslat (slepá) kopie emailu.
</p>

<p>Jako malá prevence pøed zneu¾íváním slou¾í kontrolní
kód. Ve va¹em pøípadì má hodnotu ${KOD}. Vyplòte jej prosím
ní¾e do formuláøe. Do políèka pro email zadávajte maximálnì jednu
adresu.</p>

<form action="${URL.noPrefix("/Mail")}" method="POST">

 <table class="siroka" border="0" cellpadding="5">
  <tr>
   <td class="required">Kontrolní kód</td>
   <td><input type="text" name="KOD" value="${PARAMS.KOD?if_exists}" size="4">
   <#if ERRORS.KOD?exists><span class="error">${ERRORS.KOD}</span></#if>
   </td>
  </tr>
  <tr>
   <td class="required">Vá¹ email</td>
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
   <td>Slepá kopie</td>
   <td><input type="text" name="bcc" size="30" value="${PARAMS.bcc?if_exists}">
   <#if ERRORS.bcc?exists><span class="error">${ERRORS.bcc}</span></#if>
   </td>
  </tr>
  <tr>
   <td class="required">Pøedmìt zprávy</td>
   <td><input type="text" name="subject" size="30" value="${PARAMS.subject?if_exists}">
   <#if ERRORS.subject?exists><span class="error">${ERRORS.subject}</span></#if>
   </td>
  </tr>
  <tr>
   <td colspan="2">Text zprávy<br>
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
