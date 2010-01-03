<#include "../header.ftl">

<@lib.showMessages/>

<h2>Role uživatelů</h2>

<@lib.addForm URL.noPrefix("/EditUser")>
    <@lib.addInput true, "uid", "Číslo uživatele", 6 />
    <@lib.addSubmit "Zobraz" />
    <@lib.addHidden "action", "grant2" />
</@lib.addForm>

<p>Uživatelé s definovanými rolemi:</p>

<ol>
 <#list USERS as id>
  <#assign user=TOOL.createUser(id)>
  <li><a href="${URL.noPrefix("/EditUser?action=grant2&amp;uid="+id)}">${user.name}</a>
 </#list>
</ol>

<#include "../footer.ftl">
