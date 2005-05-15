<#include "../header.ftl">

<@lib.showMessages/>

<h1 class="st_nadpis">�vod</h1>

<p>Tato str�nka je ur�ena v�hradn� administr�tor�m. Jej�m ��elem
nen� prov�d�t cenzuru (na to jsou jin� n�stroje) nebo
zasahovat do smyslu koment��e, n�br� opravovat chyby u�ivatel�.
Nap��klad �patn� zvolen� titulek u dotazu, titulek poru�uj�c�
z�sady (ps�n velk�mi p�smeny apod.), nevhodn� HTML zna�ky ..</p>

<#if PREVIEW?exists>
 <h1 class="st_nadpis">N�hled p��sp�vku</h1>
 <@lib.showThread PREVIEW, 0, 0, 0, false />
</#if>

<h1 class="st_nadpis">Zde m��ete prov�st sv� �pravy</h1>

<form action="${URL.make("/EditDiscussion")}" method="POST">
 <table cellpadding="5">
  <tr>
   <td class="required">Titulek</td>
   <td>
    <input type="text" name="title" size="40" maxlength="70" value="${PARAMS.title?if_exists}">
    <div class="error">${ERRORS.title?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td class="required">Koment��</td>
   <td>
    <textarea name="text" cols="60" rows="20">${PARAMS.text?if_exists?html}</textarea>
    <div>Sm�te pou��vat z�kladn� HTML zna�ky. Pokud je nepou�ijete,
    pr�zdn� ��dky budou nahrazeny nov�m odstavcem.</div>
    <div class="error">${ERRORS.text?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td>&nbsp;</td>
   <td>
    <input type="submit" name="preview" value="Zopakuj n�hled">
    <input type="submit" name="finish" value="Dokon�i">
   </td>
  </tr>
 </table>
 <input type="hidden" name="action" value="edit2">
 <input type="hidden" name="rid" value="${RELATION.id}">
 <input type="hidden" name="dizId" value="${PARAMS.dizId}">
 <input type="hidden" name="threadId" value="${PARAMS.threadId}">
</form>


<#include "../footer.ftl">
