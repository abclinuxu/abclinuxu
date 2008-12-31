<#include "../header.ftl">

<@lib.showMessages/>

<h2>Souhrn honorářů</h2>

<form action="${URL.make("/honorare/")}" method="GET">

 <table width="100%" border=0 cellpadding=5>
  <tr>
   <td width="200" class="required">Datum publikování od</td>
   <td>
    <input type="text" name="since" value="${PARAMS.since!}" size=40 tabindex=1>
    <div class="error">${ERRORS.since!}</div>
   </td>
  </tr>
  <tr>
   <td width="200" class="required">Datum publikování do</td>
   <td>
    <input type="text" name="until" value="${PARAMS.until!}" size=40 tabindex=2>
    <div class="error">${ERRORS.until!}</div>
   </td>
  </tr>
  <tr>
   <td width="200">&nbsp;</td>
   <td><input type="submit" value="Generuj sestavu" tabindex="3"></td>
  </tr>
 </table>
 <input type="hidden" name="action" value="report">
 <#if PARAMS.authorId??><input type="hidden" name="authorId" value="${PARAMS.authorId}"></#if>
</form>

<#include "../footer.ftl">
