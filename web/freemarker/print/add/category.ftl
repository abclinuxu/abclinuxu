<#include "/include/macros.ftl">
<#include "../header.ftl">

<#call showMessages>

<#if PARAMS.icon?exists>
 <form action="${URL.make("/EditCategory")}" method="POST">
 <input type="hidden" name="icon" value="${PARAMS.icon}">
<#else>
 <form action="${URL.noPrefix("/SelectIcon")}" method="POST">
 <input type="hidden" name="dir" value="${TOOL.substring(URL.prefix,1)}">
 <input type="hidden" name="url" value="${URL.make("/EditCategory")}">
</#if>

 <table width="100%" border=0 cellpadding=5>
  <tr>
   <td width="120" class="required">Jméno kategorie</td>
   <td>
    <input type="text" name="name" value="${PARAMS.name?if_exists}" size="40" tabindex="1">
    <div class="error">${ERRORS.name?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="120" class="required">Kdo smí pøidávat polo¾ky</td>
   <td>
    <select name="open" tabindex="2">
     <option value="yes"<#if PARAMS.open?if_exists=="yes"> SELECTED</#if>>Kdokoliv</option>
     <option value="no"<#if PARAMS.open?if_exists!="yes"> SELECTED</#if>>Jen administrátor</option>
    </select>
   </td>
  </tr>
  <tr>
   <td width="120">Poznámka</td>
   <td>
    <textarea name="note" cols="60" rows="7" tabindex="3">${PARAMS.note?if_exists?html}</textarea>
    <div class="error">${ERRORS.note?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="120">&nbsp;</td>
   <td><input type="submit" VALUE="Pokraèuj" tabindex="4"></td>
  </tr>
 </table>

 <input type="hidden" name="action" value="add2">
 <input type="hidden" name="rid" value="${PARAMS.rid}">
 <input type="hidden" name="categoryId" value="${PARAMS.categoryId}">
</form>


<#include "../footer.ftl">
