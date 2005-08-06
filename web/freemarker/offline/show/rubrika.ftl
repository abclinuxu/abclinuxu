<#import "../macros.ftl" as lib>
<#include "../header.ftl">

<#list RESULT.data as relation>
    <@lib.showArticle relation />
    <hr>
</#list>

<#if (RESULT.pageCount>0)>
    <p>
        Stránky:
        <#list 0..RESULT.pageCount as page>
            <#if page!=RESULT.pageIndex><a href="../../${DUMP.getFile(RELATION.id, page)}"></#if>
            ${page}
            <#if page!=RESULT.pageIndex></a></#if>
        </#list>
    </p>
</#if>

<#include "../footer.ftl">
