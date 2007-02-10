<#include "../header.ftl">

<h1 class="st_nadpis">Sekce ${TOOL.xpath(CATEGORY,"/data/name")}</h1>

<@lib.showMessages/>

<p><a href="${URL.noPrefix("/editContent/66948?action=add")}">Vytvoř dokument</a></p>

<#if TOOL.xpath(CATEGORY,"data/note")?exists>
 ${TOOL.render(TOOL.element(CATEGORY.data,"data/note"),USER?if_exists)}
</#if>

<#assign map=TOOL.groupByType(CHILDREN)>

<#if map.documents?exists>
    <table border="0">
        <#list map.documents as relation>
            <tr>
                <td>${relation.url}</td>
                <td><a href="${relation.url}">${TOOL.childName(relation)}</a></td>
            </tr>
        </#list>
    </table>
</#if>
<#include "../footer.ftl">
