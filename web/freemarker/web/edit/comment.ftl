<#include "/include/macros.ftl">
<#include "../header.ftl">

<#call showMessages>

<h1>Úvod</h1>

<p>Tato stránka je urèena výhradnì administrátorùm. Jejím úèelem
není provádìt cenzuru (na to jsou jiné nástroje) nebo
zasahovat do smyslu komentáøe, nýbr¾ opravovat chyby u¾ivatelù.
Napøíklad ¹patnì zvolený titulek u dotazu, titulek poru¹ující
zásady (psán velkými písmeny apod.), nevhodné HTML znaèky ..</p>

<#if PREVIEW?exists>
 <h1>Náhled pøíspìvku</h1>
 <#call showComment(PREVIEW 0 0 false)>
</#if>

<h1>Zde mù¾ete provést své úpravy</h1>

<form action="${URL.make("/EditDiscussion")}" method="POST">
 <table cellpadding="5">
  <tr>
   <td class="required">Titulek</td>
   <td>
    <input type="text" name="title" size="40" maxlength="70" value="${PARAMS.title?if_exists}" class="pole">
    <div class="error">${ERRORS.title?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td class="required">Komentáø</td>
   <td>
    <textarea name="text" cols="60" rows="20" class="pole">${PARAMS.text?if_exists?html}</textarea>
    <div>Smíte pou¾ívat základní HTML znaèky. Pokud je nepou¾ijete,
    prázdé øádky budou nahrazeny novým odstavcem.</div>
    <div class="error">${ERRORS.text?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td>&nbsp;</td>
   <td>
    <input type="submit" name="preview" value="Zopakuj náhled" class="buton">
    <input type="submit" name="finish" value="Dokonèi" class="buton">
   </td>
  </tr>
 </table>
 <input type="hidden" name="action" value="edit2">
 <input type="hidden" name="rid" value="${RELATION.id}">
 <input type="hidden" name="dizId" value="${PARAMS.dizId}">
 <input type="hidden" name="threadId" value="${PARAMS.threadId}">
</form>


<#include "../footer.ftl">
