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
   <td width="120" class="required">Jméno sekce</td>
   <td>
    <input type="text" name="name" value="${PARAMS.name?if_exists}" size="40" tabindex="1">
    <div class="error">${ERRORS.name?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="120" class="required">Typ</td>
   <td>
    <select name="type" tabindex="2">
        <option value="nic"<#if ! PARAMS.type?exists> SELECTED</#if>>Sekce</option>
        <option value="software"<#if PARAMS.type?if_exists=="software"> SELECTED</#if>>Sekce Software</option>
        <option value="hardware"<#if PARAMS.type?if_exists=="hardware"> SELECTED</#if>>Sekce hardware</option>
        <option value="faq"<#if PARAMS.type?if_exists=="faq"> SELECTED</#if>>Sekce FAQ</option>
        <option value="forum"<#if PARAMS.type?if_exists=="forum"> SELECTED</#if>>Diskusní fórum</option>
        <option value="section"<#if PARAMS.type?if_exists=="section"> SELECTED</#if>>Rubrika pro články</option>
        <option value="blog"<#if PARAMS.type?if_exists=="blog"> SELECTED</#if>>Blog</option>
    </select>
   </td>
  </tr>
    <tr>
        <td width="120">Otevřená</td>
        <td>
            <input type="checkbox" name="open" value="true"<#if PARAMS.open?default("false")=="true"> checked</#if> tabindex="3">
        </td>
    </tr>
  <tr>
   <td width="120">Poznámka</td>
   <td>
    <textarea name="note" cols="60" rows="7" tabindex="4">${PARAMS.note?if_exists?html}</textarea>
    <div class="error">${ERRORS.note?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="120">&nbsp;</td>
   <td><input type="submit" VALUE="Pokračuj" tabindex="5"></td>
  </tr>
 </table>

 <input type="hidden" name="action" value="add2">
 <input type="hidden" name="rid" value="${PARAMS.rid}">
 <input type="hidden" name="categoryId" value="${PARAMS.categoryId}">
</form>


<#include "../footer.ftl">
