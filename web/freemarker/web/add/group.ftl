<#include "../header.ftl">

<@lib.showMessages/>

<h2>Přidání skupiny</h2>

<form action="${URL.noPrefix("/Group")}" method="POST">
 <table width="100%" border=0 cellpadding=5>
  <tr>
   <td width="120" class="required">Jméno skupiny</td>
   <td>
    <input type="text" name="name" value="${PARAMS.name!}" size="20" tabindex="1">
    <div class="error">${ERRORS.name!}</div>
   </td>
  </tr>
  <tr>
   <td width="120" class="required">Popis skupiny</td>
   <td>
    <textarea name="desc" cols="60" rows="7" tabindex="2">${PARAMS.desc!?html}</textarea>
    <div class="error">${ERRORS.desc!}</div>
   </td>
  </tr>
  <tr>
   <td width="120">&nbsp;</td>
   <td><input type="submit" value="Pokračuj" tabindex="3"></td>
  </tr>
 </table>

 <#if PARAMS.action=="add">
  <input type="hidden" name="action" value="add2">
 <#else>
  <input type="hidden" name="action" value="edit2">
 </#if>
 <input type="hidden" name="gid" value="${PARAMS.gid!}">
</form>


<#include "../footer.ftl">
