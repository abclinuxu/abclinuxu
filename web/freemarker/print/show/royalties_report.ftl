<#include "../header.ftl">

<@lib.showMessages/>

<table style="font-size: small" cellspacing="0" border="1">
<#list RESULT as found>
        <tr>
           <td colspan="3" style="background:lightgray">
	      <b><a href="${found.author.url}">${TOOL.childName(found.author)}</a><code>&#09;&#09;${found.sum}</b>&#09;${TOOL.xpath(found.author.child, "/data/accountNumber")?default("bankovní úèet není zadán")}</code>
	   </td>
        <#list found.royalties as relation>
            <#assign honorar=relation.child, clanek=TOOL.sync(relation.parent),
                     relaceClanku = TOOL.createRelation(relation.upper)>
            <tr>
                <td align="right">${TOOL.xpath(honorar,"/data/amount")}</td>
                <td align="right">${DATE.show(honorar.created, "CZ_DMY")}</td>
                <td><a href="${relaceClanku.url}">${TOOL.xpath(clanek,"/data/name")}</a></td>
            </tr>
        </#list>
</#list>
</table>

<#include "../footer.ftl">
