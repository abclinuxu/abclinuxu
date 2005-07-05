<#include "../header.ftl">

<@lib.showMessages/>

<#if PARAMS.icon?exists>
 <form action="${URL.make("/EditCategory")}" method="POST">
 <input type="hidden" name="icon" value="${PARAMS.icon}">
<#else>
 <form action="${URL.noPrefix("/SelectIcon")}" method="POST">
 <#if URL.prefix=='/hardware'>
    <#assign iconDir="hardware">
 <#elseif URL.prefix=='/software'>
    <#assign iconDir="hardware">
 <#else>
    <#assign iconDir="software">
 </#if>
 <input type="hidden" name="dir" value="${iconDir}">
 <input type="hidden" name="url" value="${URL.make("/EditCategory")}">
</#if>

 <table width="100%" border=0 cellpadding=5>
  <tr>
   <td width="120" class="required">Jm�no kategorie</td>
   <td>
    <input type="text" name="name" value="${PARAMS.name?if_exists}" size="40" tabindex="1">
    <div class="error">${ERRORS.name?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="120" class="required">Kdo sm� p�id�vat polo�ky</td>
   <td>
    <select name="open" tabindex="2">
     <option value="yes"<#if PARAMS.open?default("yes")=="yes"> SELECTED</#if>>Kdokoliv</option>
     <option value="no"<#if PARAMS.open?default("yes")!="yes"> SELECTED</#if>>Jen administr�tor</option>
    </select>
   </td>
  </tr>
  <tr>
   <td width="120">Pozn�mka</td>
   <td>
    <textarea name="note" cols="60" rows="7" tabindex="3">${PARAMS.note?if_exists?html}</textarea>
    <div class="error">${ERRORS.note?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="120">&nbsp;</td>
   <td><input type="submit" VALUE="Pokra�uj" tabindex="4"></td>
  </tr>
 </table>

 <input type="hidden" name="action" value="add2">
 <input type="hidden" name="rid" value="${PARAMS.rid}">
 <input type="hidden" name="categoryId" value="${PARAMS.categoryId}">
</form>


<#include "../footer.ftl">
