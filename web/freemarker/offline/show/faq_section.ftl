<#import "../macros.ftl" as lib>
<#include "../header.ftl">

<@lib.showParents/>

<#assign CATEGORY=RELATION.child>

<h1>${TOOL.xpath(CATEGORY,"data/name")} - �asto kladen� ot�zky</h1>

<#if TOOL.xpath(CATEGORY,"data/note")?exists>
    ${TOOL.render(TOOL.element(CATEGORY.data,"data/note"),USER?if_exists)}
</#if>

<#if RESULT.total==0>
    <p>
        V t�to sekci nejsou zat�m ��dn� ot�zky.
    </p>
<#else>
    <ul>
    <#list RESULT.data as relation>
        <li><a href="../../${DUMP.getFile(relation.id)}">${TOOL.childName(relation)}</a></li>
    </#list>
    </ul>
</#if>

<#include "../footer.ftl">
