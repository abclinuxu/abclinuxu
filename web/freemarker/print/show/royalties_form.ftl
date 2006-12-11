<#include "../header.ftl">

<@lib.showMessages/>

<form action="${URL.make("/honorare/")}" method="GET">

 <table width="100%" border=0 cellpadding=5>
  <tr>
   <td width="200" class="required">Datum publikování od</td>
   <td>
    <input type="text" name="since" value="${PARAMS.since?if_exists}" size=40 tabindex=1>
    <div class="error">${ERRORS.since?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="200" class="required">Datum publikování do</td>
   <td>
    <input type="text" name="until" value="${PARAMS.until?if_exists}" size=40 tabindex=2>
    <div class="error">${ERRORS.until?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="200">&nbsp;</td>
   <td><input type="submit" value="Generuj sestavu" tabindex="3"></td>
  </tr>
 </table>
 <input type="hidden" name="action" value="report">
 <#if PARAMS.authorId?exists><input type="hidden" name="authorId" value="${PARAMS.authorId}"></#if>
</form>

<#include "../footer.ftl">
