<#include "../header.ftl">

<@lib.showMessages/>

<p>Na této stránce si mù¾ete upravit své osobní údaje.
V¹echny údaje jsou volitelné a nemusíte je vyplòovat.
Tyto informace budou zveøejnìny ve va¹em profilu,
podpis pak pod va¹imi pøíspìvky v diskusích.
Do patièky mù¾ete vlo¾it napøíklad svùj oblíbený citát,
nebo obecnì vzkaz lidem, kteøí budou èíst èi odpovídat
na vás pøíspìvek.
</p>

<p>
Pro va¹i ochranu nejdøíve zadejte souèasné heslo.
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
   <td class="required" width="60">Va¹e pohlaví</td>
   <td>
    <select name="sex" tabindex="2">
     <option value="man" <#if PARAMS.sex=="man">SELECTED</#if>>mu¾</option>
     <option value="woman"<#if PARAMS.sex=="woman">SELECTED</#if>>¾ena</option>
    </select>
    <div class="error">${ERRORS.sex?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="60">Rok narození</td>
   <td>
    <input type="text" name="birth" value="${PARAMS.birth?if_exists}" size="24" tabindex="3">
    <div class="error">${ERRORS.birth?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="60">Mìsto</td>
   <td>
    <input type="text" name="city" value="${PARAMS.city?if_exists}" size="24" tabindex="4">
   </td>
  </tr>
  <tr>
   <td width="60">Kraj</td>
   <td>
    <input type="text" name="area" value="${PARAMS.area?if_exists}" size="24" tabindex="5">
   </td>
  </tr>
  <tr>
   <td width="60">Stát</td>
   <td>
    <input type="text" name="country" value="${PARAMS.country?if_exists}" size="24" tabindex="6">
   </td>
  </tr>
  <tr>
   <td width="60">Patièka</td>
   <td>
    <textarea name="signature" rows="4" cols="60" tabindex="7">${PARAMS.signature?if_exists?html}</textarea>
    <div class="error">${ERRORS.signature?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="60">&nbsp;</td>
   <td><input type="submit" value="Dokonèi" tabindex="8"></td>
  </tr>
 </table>
 <input type="hidden" name="action" value="editPersonal2">
 <input type="hidden" name="uid" value="${MANAGED.id}">
</form>


<#include "../footer.ftl">
