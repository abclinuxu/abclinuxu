<#include "/include/macros.ftl">
<#include "../header.ftl">

<#call showMessages>

<form action="${URL.noPrefix("/EditUser")}" method="POST">
 <table width="100%" border=0 cellpadding=5>
  <tr>
   <td class="required" width="100">Èíslo u¾ivatele</td>
   <td>
    <input type="text" name="userId" value="${PARAMS.userId?if_exists}" size="4" tabindex="1">
    <div class="error">${ERRORS.userId?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td width="100">&nbsp;</td>
   <td><input type="submit" value="Zobraz" tabindex="2"></td>
  </tr>
 </table>
 <input type="hidden" name="action" value="grant2">
</form>

<p>U¾ivatelé s definovanými rolemi:</p>

<ol>
 <#list USERS as id>
  <#global user=TOOL.createUser(id)>
  <li><a href="${URL.noPrefix("/EditUser?action=grant2&userId="+id)}">${user.name}</a>
 </#list>
</ol>

<#include "../footer.ftl">
