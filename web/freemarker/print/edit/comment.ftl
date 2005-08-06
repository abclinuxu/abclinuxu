<#include "../header.ftl">

<@lib.showMessages/>

<h1>�vod</h1>

<p>Tato str�nka je ur�ena v�hradn� administr�tor�m. Jej�m ��elem
nen� prov�d�t cenzuru (na to jsou jin� n�stroje) nebo
zasahovat do smyslu koment��e, n�br� opravovat chyby u�ivatel�.
Nap��klad �patn� zvolen� titulek u dotazu, titulek poru�uj�c�
z�sady (ps�n velk�mi p�smeny apod.), nevhodn� HTML zna�ky ..</p>

<#if PREVIEW?exists>
 <h1>N�hled p��sp�vku</h1>
 <@lib.showComment PREVIEW, 0, 0, false />
</#if>

<h1>Zde m��ete prov�st sv� �pravy</h1>

<form action="${URL.make("/EditDiscussion")}" method="POST" name="form">
 <table cellpadding="5">
  <tr>
   <td class="required">Titulek</td>
   <td>
    <input type="text" name="title" size="40" maxlength="70" value="${PARAMS.title?if_exists}">
    <div class="error">${ERRORS.title?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td>Autor (id)</td>
   <td>
    <input type="text" name="author_id" size="5" maxlength="5" value="${PARAMS.author_id?if_exists}">
    <br>Jen ve v�jime�n�ch p��padech (slou�en� dvou kont)!
    <div class="error">${ERRORS.author_id?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td>Autor</td>
   <td>
    <input type="text" name="author" size="30" maxlength="50" value="${PARAMS.author?if_exists}">
    <br>Jen ve v�jime�n�ch p��padech!
    <div class="error">${ERRORS.author?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td class="required">Koment��</td>
   <td>
    <div class="form-edit">
        <a href="javascript:insertAtCursor(document.form.text, '<p>', '</p>');" id="mono" title="Vlo�it zna�ku odstavce">&lt;p&gt;</a>
        <a href="javascript:insertAtCursor(document.form.text, '<pre>', '</pre>');" id="mono" title="Vlo�it form�tovan� text. Vhodn� pouze pro konfigura�n� soubory �i v�pisy.">&lt;pre&gt;</a>
        <a href="javascript:insertAtCursor(document.form.text, '<b>', '</b>');" id="serif" title="Vlo�it zna�ku tu�n�"><b>B</b></a>
        <a href="javascript:insertAtCursor(document.form.text, '<i>', '</i>');" id="serif" title="Vlo�it zna�ku kurz�va"><i>I</i></a>
        <a href="javascript:insertAtCursor(document.form.text, '<a href=&quot;&quot;>', '</a>');" id="mono" title="Vlo�it zna�ku odkazu">&lt;a&gt;</a>
        <a href="javascript:insertAtCursor(document.form.text, '<code>', '</code>');" id="mono" title="Vlo�it zna�ku pro p�smo s pevnou ���kou">&lt;code&gt;</a>
    </div>
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
