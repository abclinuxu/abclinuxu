<#include "../header.ftl">

<@lib.showMessages/>

<h1>�prava ovlada�e</h1>

<p>Sekce Ovlada�� slou�� pro shroma��ov�n� informac� ohledn� ovlada��,
kter� nejsou standardn� sou��st� j�dra. Jedn� se bu� o Open Source
projekty vytv��en� a udr�ovan� komunitou, nebo o propriet�rn� ovlada�e
s uzav�en�m k�dem, vyv�jen� obvykle v�robcem.</p>

<p>�prava polo�ky ovlada�e je snadn� a d�ky historii verz� i bezpe�n�.
Nejd��ve m��ete zm�nit n�zev ovlada�e, je-li nevhodn�. Pak zadejte aktu�ln�
verzi ovlada�e a p��padn� upravte adresu, odkud je mo�n� st�hnout tento ovlada�.
Do pozn�mky vlo�te informace o novink�ch t�to verze a jin�ch zm�n�ch.
Nebojte se celou pozn�mku aktualizovat, p�edchoz� text bude dostupn� v historii
ovlada�e.</p>

<#if PARAMS.preview?exists>
 <h2>N�hled p��sp�vku</h2>

 <table cellspacing=0 border=1 cellpadding=5 align="center">
  <tr>
    <td>Jm�no</td><td>${PARAMS.name?if_exists}</td>
  </tr>
  <tr>
    <td>Verze</td><td>${PARAMS.version?if_exists}</td>
  </tr>
  <tr>
    <td>Adresa</td>
    <td>
      <a href="${PARAMS.url?if_exists}">${TOOL.limit(PARAMS.url?if_exists,50," ..")}</a>
    </td>
  </tr>
  <tr>
    <td valign="top">Pozn�mka</td><td>${TOOL.render(PARAMS.note?if_exists,USER?if_exists)}</td>
  </tr>
 </table>
</#if>

<h2>Zde zadejte sv� �pravy</h2>

<form action="${URL.make("/edit")}" method="POST">
 <table cellpadding="0" border="0" style="margin-top: 1em;">
  <tr>
   <td class="required">Jm�no</td>
   <td>
    <input type="text" name="name" value="${PARAMS.name?if_exists}" size="30" maxlength="30" tabindex="1">
    <div class="error">${ERRORS.name?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td class="required">Verze</td>
   <td>
    <input type="text" name="version" value="${PARAMS.version?if_exists}" size="30" tabindex="2">
    <div class="error">${ERRORS.version?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td class="required">URL</td>
   <td>
    <input type="text" name="url" value="${PARAMS.url?default("http://")}" size="70" tabindex="3">
    <div class="error">${ERRORS.url?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td colspan="2" class="required">Pozn�mka</td>
  </tr>
  <tr>
   <td colspan="2">
    <textarea name="note" cols="70" rows="15" tabindex="4">${PARAMS.note?if_exists?html}</textarea>
    <div class="error">${ERRORS.note?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td colspan="2" align="center">
    <input type="submit" name="preview" value="N�hled">
    <input type="submit" name="submit" value="Dokon�i">
   </td>
  </tr>
 </table>
 <input type="hidden" name="action" value="edit2">
 <input type="hidden" name="rid" value="${RELATION.id}">
</form>


<#include "../footer.ftl">
