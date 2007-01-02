<#include "../header.ftl">

<@lib.showMessages/>

<#list RESULT as found>
    <h3><a href="${found.author.url}">${TOOL.childName(found.author)}</a></h3>
    <table border="1" cellspacing="1" cellpadding="4">
        <tr>
            <th>Honoráø</th>
            <th>Vytvoøen</th>
            <th>Èlánek</th>
        </tr>
        <#list found.royalties as relation>
            <#assign honorar=relation.child, clanek=TOOL.sync(relation.parent),
                     relaceClanku = TOOL.createRelation(relation.upper)>
            <tr>
                <td align="right">${TOOL.xpath(honorar,"/data/amount")}</td>
                <td align="right">${DATE.show(honorar.created, "CZ_DMY")}</td>
                <td><a href="${relaceClanku.url}">${TOOL.xpath(clanek,"/data/name")}</a></td>
            </tr>
        </#list>
        <tr>
            <td><b>${found.sum}</b></td>
            <td colspan="2">${TOOL.xpath(found.author.child, "/data/accountNumber")?default("bankovní úèet není zadán")}</td>
        </tr>
    </table>
</#list>
<#include "../footer.ftl">
