<#import "../macros.ftl" as lib>
<#include "../header.ftl">

<@lib.showParents/>

<#assign CATEGORY=RELATION.child>

<h1>${TOOL.xpath(CATEGORY,"data/name")} - často kladené otázky</h1>

<#if TOOL.xpath(CATEGORY,"data/note")??>
    ${TOOL.render(TOOL.element(CATEGORY.data,"data/note"),USER!)}
</#if>

<#if RESULT.total==0>
    <p>
        V této sekci nejsou zatím žádné otázky.
    </p>
<#else>
    <ul>
    <#list RESULT.data as relation>
        <li><a href="../../${DUMP.getFile(relation.id)}">${TOOL.childName(relation)}</a></li>
    </#list>
    </ul>
</#if>

<#include "../footer.ftl">
