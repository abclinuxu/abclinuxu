<#include "../header.ftl">

<@lib.showMessages/>

<h1>Sestava</h1>

<table border="1" cellspacing="1" cellpadding="4">
<#list RESULT as relation>
 <tr>
 <#assign honorar=relation.child, clanek=TOOL.sync(relation.parent), autor=TOOL.createUser(honorar.owner)>
  <td><a href="/Profile/${autor.id}">${autor.name}</a></td>
  <td align="right">${TOOL.xpath(honorar,"/data/amount")}</td>
  <td align="right">${DATE.show(honorar.created, "CZ_DATE")}</td>
  <td>${TOOL.xpath(clanek,"/data/name")}</td>
 </tr>
</#list>
</table>
<#include "../footer.ftl">
