<#include "/include/macros.ftl">
<#include "../header.ftl">

<#call showMessages>

<form action="${URL.make("/EditItem")}" method="POST">

 <table width=100 border=0 cellpadding=5>
  <tr>
   <td width="90" class="required">Titulek</td>
   <td>
    <input type="text" name="title" value="${PARAMS.title?if_exists}" size=60 tabindex=1>
    <div class="error">${ERRORS.title?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="90" class="required">Autor</td>
   <td>
    <select name="authorId" size="1" tabindex=2>
     <#list AUTHORS as author>
      <option value="${author.id}"<#if PARAMS.authorId==author.id > selected</#if>>${author.name}</option>
     </#list>
    </select>
    <div class="error">${ERRORS.authorId?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="90" class="required">Zveøejni dne</td>
   <td>
    <input type="text" name="published" value="${PARAMS.published?if_exists}" size=60 tabindex=3>
    <div class="error">${ERRORS.published?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="90" class="required">Perex</td>
   <td>
    <textarea name="perex" cols="80" rows="5" tabindex="4">${PARAMS.perex?if_exists?html}</textarea>
    <div class="error">${ERRORS.perex?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="90" class="required">Obsah èlánku</td>
   <td>
    <textarea name="content" cols="80" rows="30" tabindex="5">${PARAMS.content?if_exists?html}</textarea>
    <div class="error">${ERRORS.content?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="90">&nbsp;</td>
   <td><input type="submit" VALUE="Pokraèuj" TABINDEX="6"></td>
  </tr>
 </table>

 <#if PARAMS.action=="add" >
  <input type="hidden" name="action" value="add2">
  <#else>
  <input type="hidden" name="action" value="edit2">
 </#if>
 <input type="hidden" name="relationId" value="${PARAMS.relationId}">
</form>


<#include "../footer.ftl">
