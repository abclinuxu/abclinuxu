<#include "/include/macros.ftl">
<#include "../header.ftl">

<#call showMessages>

<h1>Sestava</h1>

<table border="1" cellspacing="1" cellpadding="4">
<#list RESULT as relation>
 <tr>
 <#global honorar=relation.child>
 <#global clanek=TOOL.sync(relation.parent)>
 <#global autor=TOOL.createUser(honorar.owner)>
  <td><a href="/Profile/${autor.id}">${autor.name}</a></td>
  <td align="right">${TOOL.xpath(honorar,"/data/amount")}</td>
  <td align="right">${DATE.show(honorar.created, "CZ_DATE")}</td>
  <td>${TOOL.xpath(clanek,"/data/name")}</td>
 </tr>
</#list>
</table>
<#include "../footer.ftl">
