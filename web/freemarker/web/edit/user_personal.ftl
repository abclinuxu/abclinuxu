<#include "../header.ftl">

<@lib.showMessages/>

<p>Na t�to str�nce si m��ete upravit sv� osobn� �daje.
V�echny �daje jsou voliteln� a nemus�te je vypl�ovat.
Tyto informace budou zve�ejn�ny ve va�em profilu,
podpis pak pod va�imi p��sp�vky v diskus�ch.
Do pati�ky m��ete vlo�it nap��klad sv�j obl�ben� cit�t,
nebo obecn� vzkaz lidem, kte�� budou ��st �i odpov�dat
na v�s p��sp�vek.
</p>

<p>
Pro va�i ochranu nejd��ve zadejte sou�asn� heslo.
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
   <td class="required" width="60">Va�e pohlav�</td>
   <td>
    <select name="sex" tabindex="2">
     <option value="man" <#if PARAMS.sex=="man">SELECTED</#if>>mu�</option>
     <option value="woman"<#if PARAMS.sex=="woman">SELECTED</#if>>�ena</option>
    </select>
    <div class="error">${ERRORS.sex?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="60">Rok narozen�</td>
   <td>
    <input type="text" name="birth" value="${PARAMS.birth?if_exists}" size="24" tabindex="3">
    <div class="error">${ERRORS.birth?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="60">M�sto</td>
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
   <td width="60">St�t</td>
   <td>
    <input type="text" name="country" value="${PARAMS.country?if_exists}" size="24" tabindex="6">
   </td>
  </tr>
  <tr>
   <td width="60">Pati�ka</td>
   <td>
    <textarea name="signature" rows="4" cols="60" tabindex="7">${PARAMS.signature?if_exists?html}</textarea>
    <div class="error">${ERRORS.signature?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="60">&nbsp;</td>
   <td><input type="submit" value="Dokon�i" tabindex="8"></td>
  </tr>
 </table>
 <input type="hidden" name="action" value="editPersonal2">
 <input type="hidden" name="uid" value="${MANAGED.id}">
</form>


<#include "../footer.ftl">
