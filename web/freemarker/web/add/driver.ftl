<#include "../header.ftl">

<@lib.showMessages/>

<h1>�vod</h1>

<p>Pr�v� se chyst�te vlo�it do datab�ze nov� ovlada�. Pokud jste si
jisti, �e v datab�zi nen� ulo�ena jeho star�� verze, pokra�ujte
a vypl�te tento formul��. Pokud jste se ale ned�vali, pros�m
vra�te se zp�t a zkontrolujte, zda ji� v datab�zi nen� p��tomen.
Pokud jej najdete, otev�ete jej a zvolte odkaz <i>Vlo� novou verzi</i>.
</p>

<p>Polo�ka ovlada�e slou�� pro shroma��ov�n� informac� ohledn� ovlada��,
kter� nejsou standardn� sou��st� j�dra. Typicky jde bu� o Open Source
projekty, kdy se nad�enci sna�� vytvo�it podporu pro dan� hardware
(typicky ovlada�e scanner�) nebo v�robce odm�t� uvolnit specifikaci
sv�ch produkt� komunit� a m�sto toho vyr�b� vlastn� ovlada�e (nap��klad
nVidia).
</p>

<p>Vytvo�en� polo�ky ovlada�e je snadn�. Nejd��ve vypl�te jm�no
ovlada�e, p��padn� hardwaru. Pak vlo�te verzi ovlada�e a adresu,
kde je mo�n� st�hnout ovlada�. Ned�vejte zde z�kladn� adresu produktu,
n�br� adresu, kde je mo�n� st�hnout jeho ovlada�. Posledn�
polo�kou je pozn�mka, kam pat�� informace o tom, co ovlada� um�,
respektive jak� zm�ny tato verze p�inesla.
</p>

<#if PARAMS.preview?exists>
 <h1>N�hled p��sp�vku</h1>

 <table cellspacing=0 border=1 cellpadding=5 align="center">
  <tr>
    <td>Jm�no ovlada�e</td><td>${PARAMS.name?if_exists}</td>
  </tr>
  <tr>
    <td>Verze ovlada�e</td><td>${PARAMS.version?if_exists}</td>
  </tr>
  <tr>
    <td>URL ovlada�e</td>
    <td>
      <a href="${PARAMS.url?if_exists}">${TOOL.limit(PARAMS.url?if_exists,50," ..")}</a>
    </td>
  </tr>
  <tr>
    <td valign="top">Pozn�mka</td><td>${TOOL.render(PARAMS.note?if_exists,USER?if_exists)}</td>
  </tr>
 </table>
</#if>

<h1>Nov� ovlada�</h1>

<form action="${URL.make("/edit")}" method="POST">
 <table cellpadding="0" border="0" width="100%">
  <tr>
   <td class="required">Jm�no ovlada�e</td>
   <td>
    <input type="text" name="name" value="${PARAMS.name?if_exists}" size="30" maxlength="30" tabindex="1">
    <div class="error">${ERRORS.name?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td class="required">Verze ovlada�e</td>
   <td>
    <input type="text" name="version" value="${PARAMS.version?if_exists}" size="30" tabindex="2">
    <div class="error">${ERRORS.version?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td class="required">URL ovlada�e</td>
   <td>
    <input type="text" name="url" value="${PARAMS.url?default("http://")}" size="50" tabindex="3">
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
   <td>&nbsp;</td>
   <td>
    <input type="submit" name="preview" value="N�hled">
    <input type="submit" name="submit" value="Dokon�i">
   </td>
  </tr>
 </table>
 <input type="hidden" name="action" value="add2">
</form>


<#include "../footer.ftl">
