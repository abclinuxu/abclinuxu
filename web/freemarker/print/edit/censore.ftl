<#include "/include/macros.ftl">
<#include "../header.ftl">

<#call showMessages>

<#call showComment(THREAD 0 0 false)>

<p>Napi¹te zde dùvod, proè cenzurujete tento pøíspìvek.</p>

<form action="${URL.make("/EditDiscussion")}" method="POST">
 <table cellpadding="5">
  <tr>
   <td class="required">Komentáø</td>
   <td>
    <textarea name="text" cols="60" rows="5">${PARAMS.text?if_exists?html}</textarea>
   </td>
  </tr>
  <tr>
   <td>&nbsp;</td>
   <td>
    <input type="submit" name="finish" value="Dokonèi">
   </td>
  </tr>
 </table>
 <input type="hidden" name="action" value="censore2">
 <input type="hidden" name="rid" value="${PARAMS.rid}">
 <input type="hidden" name="dizId" value="${PARAMS.dizId}">
 <input type="hidden" name="threadId" value="${PARAMS.threadId}">
</form>

<#include "../footer.ftl">
