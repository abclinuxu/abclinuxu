<#include "../header.ftl">

<@lib.showMessages/>

 <@lib.showThread THREAD, 0, TOOL.createEmptyDiscussion(), false />

<p>Napi�te zde d�vod, pro� cenzurujete tento p��sp�vek.</p>

<form action="${URL.make("/EditDiscussion")}" method="POST">
 <table cellpadding="5">
  <tr>
   <td class="required">Koment��</td>
   <td>
    <textarea name="text" cols="60" rows="5">${PARAMS.text?if_exists?html}</textarea>
   </td>
  </tr>
  <tr>
   <td>&nbsp;</td>
   <td>
    <input type="submit" name="finish" value="Dokon�i">
   </td>
  </tr>
 </table>
 <input type="hidden" name="action" value="censore2">
 <input type="hidden" name="rid" value="${RELATION.id}">
 <input type="hidden" name="dizId" value="${PARAMS.dizId}">
 <input type="hidden" name="threadId" value="${PARAMS.threadId}">
</form>

<#include "../footer.ftl">
