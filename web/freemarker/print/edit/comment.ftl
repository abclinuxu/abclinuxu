<#include "../header.ftl">

<@lib.showMessages/>

<h2>Úvod</h2>

<p>Tato stránka je určena výhradně administrátorům. Jejím účelem
není provádět cenzuru (na to jsou jiné nástroje) nebo
zasahovat do smyslu komentáře, nýbrž opravovat chyby uživatelů.
Například špatně zvolený titulek u dotazu, titulek porušující
zásady (psán velkými písmeny apod.), nevhodné HTML značky, ...</p>

<#if PREVIEW?exists>
 <h2>Náhled příspěvku</h2>
 <@lib.showThread PREVIEW, 0, TOOL.createEmptyDiscussion(), false />
</#if>

<h2>Zde můžete provést své úpravy</h2>

<form action="${URL.make("/EditDiscussion")}" method="POST" name="form">
 <table cellpadding="5" class="siroka">
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
    <br>Jen ve výjimečných případech (sloučení dvou kont)!
    <div class="error">${ERRORS.author_id?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td>Autor</td>
   <td>
    <input type="text" name="author" size="30" maxlength="50" value="${PARAMS.author?if_exists}">
    <br>Jen ve výjimečných případech!
    <div class="error">${ERRORS.author?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td class="required">Komentář</td>
   <td>
    <div class="form-edit">
        <a href="javascript:insertAtCursor(document.form.text, '<p>', '</p>');" id="mono" title="Vložit značku odstavce">&lt;p&gt;</a>
        <a href="javascript:insertAtCursor(document.form.text, '<pre>', '</pre>');" id="mono" title="Vložit formátovaný text. Vhodné pouze pro konfigurační soubory či výpisy.">&lt;pre&gt;</a>
        <a href="javascript:insertAtCursor(document.form.text, '<b>', '</b>');" id="serif" title="Vložit značku tučně"><b>B</b></a>
        <a href="javascript:insertAtCursor(document.form.text, '<i>', '</i>');" id="serif" title="Vložit značku kurzíva"><i>I</i></a>
        <a href="javascript:insertAtCursor(document.form.text, '<a href=&quot;&quot;>', '</a>');" id="mono" title="Vložit značku odkazu">&lt;a&gt;</a>
        <a href="javascript:insertAtCursor(document.form.text, '<code>', '</code>');" id="mono" title="Vložit značku pro písmo s pevnou šířkou">&lt;code&gt;</a>
    </div>
    <textarea name="text" class="siroka" rows="20">${PARAMS.text?if_exists?html}</textarea>
    <div>Smíte používat základní HTML značky. Pokud je nepoužijete,
    prázdné řádky budou nahrazeny novým odstavcem.</div>
    <div class="error">${ERRORS.text?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td>&nbsp;</td>
   <td>
    <input type="submit" name="preview" value="Zopakuj náhled">
    <input type="submit" name="finish" value="Dokonči">
   </td>
  </tr>
 </table>
 <input type="hidden" name="action" value="edit2">
 <input type="hidden" name="rid" value="${RELATION.id}">
 <input type="hidden" name="dizId" value="${PARAMS.dizId}">
 <input type="hidden" name="threadId" value="${PARAMS.threadId}">
</form>


<#include "../footer.ftl">
