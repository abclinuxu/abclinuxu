<#include "/include/macros.ftl">
<#include "../header.ftl">

<#call showMessages>

<form action="${URL.make("/honorare/"+RELATION.id)}" method="POST">

 <table width=100 border=0 cellpadding=5>
  <tr>
   <td width="90" class="required">��slo autora</td>
   <td>
    <input type="text" name="authorId" value="${PARAMS.authorId?if_exists}" size=60 tabindex=1>
    <div class="error">${ERRORS.authorId?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="90" class="required">Datum publikov�n�</td>
   <td>
    <input type="text" name="published" value="${PARAMS.published?if_exists}" size=40 tabindex=2>
    <div class="error">${ERRORS.published?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="90">Datum zaplacen�</td>
   <td>
    <input type="text" name="paid" value="${PARAMS.paid?if_exists}" size=40 tabindex=3>
    <div class="error">${ERRORS.paid?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="90" class="required">��stka</td>
   <td>
    <input type="text" name="amount" value="${PARAMS.amount?if_exists}" size=10 tabindex=4>
    <div class="error">${ERRORS.amount?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="90">Pozn�mka</td>
   <td>
    <textarea name="note" cols="60" rows="4" tabindex="5">${PARAMS.note?if_exists}</textarea>
    <div class="error">${ERRORS.note?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="90">&nbsp;</td>
   <td><input type="submit" value="Pokra�uj" tabindex="8"></td>
  </tr>
 </table>
 <#if PARAMS.action=="add" || PARAMS.action="add2" >
  <input type="hidden" name="action" value="add2">
  <#else>
  <input type="hidden" name="action" value="edit2">
 </#if>
</form>


<#include "../footer.ftl">
