<#include "/include/macros.ftl">
<#include "../header.ftl">

<#call showMessages>

<form action="${URL.noPrefix("/Group")}" method="POST">
 <table width="100%" border=0 cellpadding=5>
  <tr>
   <td width="120" class="required">Jméno skupiny</td>
   <td>
    <input type="text" name="name" value="${PARAMS.name?if_exists}" size="20" tabindex="1">
    <div class="error">${ERRORS.name?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="120" class="required">Popis skupiny</td>
   <td>
    <textarea name="desc" cols="60" rows="7" tabindex="2">${PARAMS.desc?if_exists?html}</textarea>
    <div class="error">${ERRORS.desc?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="120">&nbsp;</td>
   <td><input type="submit" value="Pokraèuj" tabindex="3"></td>
  </tr>
 </table>

 <#if PARAMS.action=="add">
  <input type="hidden" name="action" value="add2">
 <#else>
  <input type="hidden" name="action" value="edit2">
 </#if>
 <input type="hidden" name="gid" value="${PARAMS.gid?if_exists}">
</form>


<#include "../footer.ftl">
