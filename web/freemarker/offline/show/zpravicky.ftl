<#import "../macros.ftl" as lib>
<#include "../header.ftl">

<@lib.showParents />

<#list RESULT.data as relation>
    <#assign item=TOOL.sync(relation.child), autor=TOOL.createUser(item.owner),
      diz=TOOL.findComments(item), url=relation.url?default("/zpravicky/show/"+relation.id)>
    <h3 class="st_nadpis">${TOOL.xpath(item, "/data/title")?default("Zprávièka")}</h3>
    <p>
        ${DATE.show(item.created,"CZ_FULL")}
        | ${NEWS_CATEGORIES[item.subType].name}
        | <a href="http://www.abclinuxu.cz/Profile/${autor.id}">${autor.name}</a>
        | Komentáøù: ${diz.responseCount}
        | <a href="../../${DUMP.getFile(relation.id)}">Detail</a>
    </p>
    <p>${TOOL.xpath(item,"data/content")}</p>
    <hr>
</#list>

<#if (RESULT.pageCount>0)><@lib.listPages RESULT, RELATION.id /></#if>

<#include "../footer.ftl">
