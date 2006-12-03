<#include "../header.ftl">

<@lib.showMessages/>

<h1>Sestava</h1>

<table border="1" cellspacing="1" cellpadding="4">
<#list RESULT as relation>
 <tr>
 <#assign honorar=relation.child, clanek=TOOL.sync(relation.parent), autor=TOOL.createRelation(honorar.owner)>
  <td><a href="${autor.url}">${TOOL.childName(autor)}</a></td>
  <td align="right">${TOOL.xpath(honorar,"/data/amount")}</td>
  <td align="right">${DATE.show(honorar.created, "CZ_DMY")}</td>
  <td>${TOOL.xpath(clanek,"/data/name")}</td>
 </tr>
</#list>
</table>
<#include "../footer.ftl">
