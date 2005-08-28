<#import "../macros.ftl" as lib>
<#include "../header.ftl">

<@lib.showParents />

<#list RESULT.data as relation>
    <@lib.showArticle relation />
    <hr>
</#list>

<#if (RESULT.pageCount>0)><@lib.listPages RESULT, RELATION.id /></#if>

<#include "../footer.ftl">
