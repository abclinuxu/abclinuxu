<#include "../header.ftl">

<@lib.showMessages/>

<h2>Role uživatelů</h2>

<form action="${URL.noPrefix("/EditUser")}" method="POST">
 <table width="100%" border=0 cellpadding=5>
  <tr>
   <td class="required" width="100">Číslo uživatele</td>
   <td>
    <input type="text" name="uid" value="${PARAMS.uid!}" size="6" tabindex="1">
    <div class="error">${ERRORS.uid!}</div>
   </td>
  </tr>
  <tr>
   <td width="100">&nbsp;</td>
   <td><input type="submit" value="Zobraz" tabindex="2"></td>
  </tr>
 </table>
 <input type="hidden" name="action" value="grant2">
</form>

<p>Uživatelé s definovanými rolemi:</p>

<ol>
 <#list USERS as id>
  <#assign user=TOOL.createUser(id)>
  <li><a href="${URL.noPrefix("/EditUser?action=grant2&amp;uid="+id)}">${user.name}</a>
 </#list>
</ol>

<#include "../footer.ftl">
