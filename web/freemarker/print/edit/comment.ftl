<#include "../header.ftl">

<@lib.showMessages/>

<h1>Úvod</h1>

<p>Tato stránka je urèena výhradnì administrátorùm. Jejím úèelem
není provádìt cenzuru (na to jsou jiné nástroje) nebo
zasahovat do smyslu komentáøe, nýbr¾ opravovat chyby u¾ivatelù.
Napøíklad ¹patnì zvolený titulek u dotazu, titulek poru¹ující
zásady (psán velkými písmeny apod.), nevhodné HTML znaèky ..</p>

<#if PREVIEW?exists>
 <h1>Náhled pøíspìvku</h1>
 <@lib.showComment PREVIEW, 0, 0, false />
</#if>

<h1>Zde mù¾ete provést své úpravy</h1>

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
    <br>Jen ve výjimeèných pøípadech (slouèení dvou kont)!
    <div class="error">${ERRORS.author_id?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td>Autor</td>
   <td>
    <input type="text" name="author" size="30" maxlength="50" value="${PARAMS.author?if_exists}">
    <br>Jen ve výjimeèných pøípadech!
    <div class="error">${ERRORS.author?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td class="required">Komentáø</td>
   <td>
    <div class="form-edit">
        <a href="javascript:insertAtCursor(document.form.text, '<p>', '</p>');" id="mono" title="Vlo¾it znaèku odstavce">&lt;p&gt;</a>
        <a href="javascript:insertAtCursor(document.form.text, '<pre>', '</pre>');" id="mono" title="Vlo¾it formátovaný text. Vhodné pouze pro konfiguraèní soubory èi výpisy.">&lt;pre&gt;</a>
        <a href="javascript:insertAtCursor(document.form.text, '<b>', '</b>');" id="serif" title="Vlo¾it znaèku tuènì"><b>B</b></a>
        <a href="javascript:insertAtCursor(document.form.text, '<i>', '</i>');" id="serif" title="Vlo¾it znaèku kurzíva"><i>I</i></a>
        <a href="javascript:insertAtCursor(document.form.text, '<a href=&quot;&quot;>', '</a>');" id="mono" title="Vlo¾it znaèku odkazu">&lt;a&gt;</a>
        <a href="javascript:insertAtCursor(document.form.text, '<code>', '</code>');" id="mono" title="Vlo¾it znaèku pro písmo s pevnou ¹íøkou">&lt;code&gt;</a>
    </div>
    <textarea name="text" cols="60" rows="20">${PARAMS.text?if_exists?html}</textarea>
    <div>Smíte pou¾ívat základní HTML znaèky. Pokud je nepou¾ijete,
    prázdné øádky budou nahrazeny novým odstavcem.</div>
    <div class="error">${ERRORS.text?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td>&nbsp;</td>
   <td>
    <input type="submit" name="preview" value="Zopakuj náhled">
    <input type="submit" name="finish" value="Dokonèi">
   </td>
  </tr>
 </table>
 <input type="hidden" name="action" value="edit2">
 <input type="hidden" name="rid" value="${RELATION.id}">
 <input type="hidden" name="dizId" value="${PARAMS.dizId}">
 <input type="hidden" name="threadId" value="${PARAMS.threadId}">
</form>


<#include "../footer.ftl">
