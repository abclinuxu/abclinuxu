<#include "/include/macros.ftl">
<#include "../header.ftl">

<#call showMessages>

<form action="${URL.noPrefix("/EditUser")}" method="POST">
 <table width="100%" border=0 cellpadding=5>
  <tr>
   <td class="required" width="100">Va�e heslo</td>
   <td>
    <input type="password" name="PASSWORD" size="16" tabindex="1">
    <div class="error">${ERRORS.PASSWORD?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td class="required" width="100">��slo u�ivatele</td>
   <td>
    <input type="text" name="userId" value="${PARAMS.userId?if_exists}" size="4" tabindex="2">
    <div class="error">${ERRORS.userId?if_exists}</div>
   </td>
  </tr>
  <#macro isDefined(name)>
   <#if ! PARAMS.roles?exists><#return></#if>
   <#if PARAMS.roles.contains(name)>selected</#if>
  </#macro>
  <tr>
   <td class="required" width="100">Jeho role</td>
   <td>
     <select name="roles" tabindex="3" size="10" multiple="multiple">
       <option value="root" <#call isDefined("root")>>Root</option>
       <option value="user admin" <#call isDefined("user admin")>>Administr�tor u�ivatel�</option>
       <option value="category admin" <#call isDefined("category admin")>>Administr�tor sekc�</option>
       <option value="discussion admin" <#call isDefined("discussion admin")>>Administr�tor diskus�</option>
       <option value="news admin" <#call isDefined("news admin")>>Administr�tor zpr�vi�ek</option>
       <option value="article admin" <#call isDefined("article admin")>>Administr�tor �l�nk�</option>
       <option value="survey admin" <#call isDefined("survey admin")>>Administr�tor anket</option>
       <option value="poll admin" <#call isDefined("poll admin")>>Administr�tor mal�ch anket</option>
       <option value="move relation" <#call isDefined("move relation")>>Sm� p�esunout relaci</option>
       <option value="remove relation" <#call isDefined("remove relation")>>Sm� smazat relaci a objekt</option>
     </select>
   </td>
  </tr>
  <tr>
   <td width="100">&nbsp;</td>
   <td><input type="submit" value="Dokon�i" tabindex="4"></td>
  </tr>
 </table>
 <input type="hidden" name="action" value="grant2">
</form>


<#include "../footer.ftl">
