<#include "../header.ftl">

<@lib.showMessages/>

<h2>Úvod</h2>

<p>Tato stránka je určena výhradně administrátorům. Jejím účelem
není provádět cenzuru (na to jsou jiné nástroje) nebo
zasahovat do smyslu komentáře, nýbrž opravovat chyby uživatelů.
Například špatně zvolený titulek u dotazu, titulek porušující
zásady (psán velkými písmeny apod.), nevhodné HTML značky, ...</p>

<#if PREVIEW??>
 <h2>Náhled příspěvku</h2>
 <@lib.showThread PREVIEW, 0, TOOL.createEmptyDiscussion(), false />
</#if>

<h2>Zde můžete provést své úpravy</h2>

<form action="${URL.make("/EditDiscussion")}" name="editForm" method="POST">
 <table cellpadding="5" class="siroka">
  <tr>
   <td class="required">Titulek</td>
   <td>
    <input type="text" name="title" size="40" maxlength="70" value="${PARAMS.title!}">
    <div class="error">${ERRORS.title!}</div>
   </td>
  </tr>
    <tr>
        <td>Autor (id)</td>
        <td>
            <input type="text" name="author_id" size="5" maxlength="5" value="${PARAMS.author_id!}">
            <br>Jen ve výjimečných případech (sloučení dvou kont)!
            <div class="error">${ERRORS.author_id!}</div>
        </td>
    </tr>
    <tr>
        <td>Autor</td>
        <td>
            <input type="text" name="author" size="30" maxlength="50" value="${PARAMS.author!}">
            <br>Jen ve výjimečných případech!
            <div class="error">${ERRORS.author!}</div>
        </td>
    </tr>
  <tr>
   <td class="required">Komentář</td>
   <td>
     <div class="form-edit">
        <a href="javascript:insertAtCursor(document.editForm.text, '&lt;b&gt;', '&lt;/b&gt;');" id="serif" title="Vložit značku tučně"><b>B</b></a>
        <a href="javascript:insertAtCursor(document.editForm.text, '&lt;i&gt;', '&lt;/i&gt;');" id="serif" title="Vložit značku kurzíva"><i>I</i></a>
        <a href="javascript:insertAtCursor(document.editForm.text, '&lt;a href=&quot;&quot;&gt;', '&lt;/a&gt;');" id="mono" title="Vložit značku odkazu">&lt;a&gt;</a>
        <a href="javascript:insertAtCursor(document.editForm.text, '&lt;blockquote&gt;', '&lt;/blockquote&gt;');" id="mono" title="Vložit značku citace">BQ</a>
        <a href="javascript:insertAtCursor(document.editForm.text, '&lt;p&gt;', '&lt;/p&gt;');" id="mono" title="Vložit značku odstavce">&lt;p&gt;</a>
        <a href="javascript:insertAtCursor(document.editForm.text, '&lt;pre&gt;', '&lt;/pre&gt;');" id="mono" title="Vložit značku formátovaného textu. Vhodné pro konfigurační soubory či výpisy.">&lt;pre&gt;</a>
	    <a href="javascript:insertAtCursor(document.editForm.text, '&lt;code&gt;', '&lt;/code&gt;');" id="mono" title="Vložit značku pro písmo s pevnou šířkou">&lt;code&gt;</a>
	    <a href="javascript:insertAtCursor(document.editForm.text, '&amp;lt;', '');" id="mono" title="Vložit písmeno &lt;">&lt;</a>
	    <a href="javascript:insertAtCursor(document.editForm.text, '&amp;gt;', '');" id="mono" title="Vložit písmeno &gt;">&gt;</a>
        <#if THREAD??>
            <a href="javascript:cituj(document.editForm.text);" id="mono" title="Vloží komentovaný příspěvek jako citaci">Citace</a>
        </#if>
     </div>
     <textarea name="text" class="siroka" rows="20">${PARAMS.text!?html}</textarea>
     <div>Smíte používat základní HTML značky. Pokud je nepoužijete,
      prázdné řádky budou nahrazeny novým odstavcem.</div>
     <div class="error">${ERRORS.text!}</div>
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
