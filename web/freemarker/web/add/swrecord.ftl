<#include "../header.ftl">

<@lib.showMessages/>

<h1 class="st_nadpis">N�pov�da</h1>

<p>Vypl�te �daje z�znamu. Sna�te se p�itom zapsat co nejv�ce podrobnost�.
Mnoh� detaily, kter� v�m mohou p�ipadat samoz�ejm�, jsou pro za��te�n�ky
no�n� m�rou.</p>

<h1 class="st_nadpis">Form�tov�n�</h1>

<p>Sm�te pou��vat z�kladn� HTML zna�ky. Pokud je nepou�ijete,
pr�zdn� ��dky budou nahrazeny nov�m odstavcem. Nedoporu�ujeme
pou��vat zna�ku ${"<pre>"?html}, v�t�ina prohl�e�� pak nespr�vn�
rozt�hne v� z�znam p�es celou str�nku, tak�e je nutn� scrollovat.
</p>

<form action="${URL.make("/edit")}" method="POST">

 <table width=100 border=0 cellpadding=5>

  <tr>
   <td>Verze softwaru</td>
   <td>
    <input type="text" name="version" size="20" value="${PARAMS.version?if_exists}">
   </td>
  </tr>

  <tr>
   <td>URL softwaru</td>
   <td>
    <input type="text" name="url" size="40" value="${PARAMS.url?if_exists}">
   </td>
  </tr>

  <tr>
   <td class="required">N�vod �i pozn�mka</td>
   <td>
    <textarea name="text" cols="60" rows="20">${PARAMS.text?if_exists?html}</textarea>
    <div class="error">${ERRORS.text?if_exists}</div>
   </td>
  </tr>

  <tr>
   <td width="120">&nbsp;</td>
   <td><input type="submit" value="Dokon�i" tabindex="4"></td>
  </tr>

 </table>

 <#assign action=PARAMS.action?if_exists>
 <#if action.startsWith("addItem")>
  <input type="hidden" name="action" value="addItem3">
  <input type="hidden" name="name" value="${PARAMS.name?if_exists}">
  <input type="hidden" name="icon" value="${PARAMS.icon?if_exists}">
 <#else>
  <input type="hidden" name="action" value="${action}">
 </#if>

 <input type="hidden" name="rid" value="${PARAMS.rid}">
 <#if PARAMS.recordId?exists>
  <input type="hidden" name="recordId" value="${PARAMS.recordId}">
 </#if>

</form>


<#include "../footer.ftl">
