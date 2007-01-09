<#include "../header.ftl">

<@lib.showMessages/>

<h1>Seri�l ${TOOL.childName(RELATION)}</h1>

<#if USER?exists && USER.hasRole("article admin")>
    <p>
        <a href="${URL.make("/serialy/edit/"+RELATION.id+"?action=edit")}">Uprav seri�l</a>
        <a href="${URL.make("/serialy/edit/"+RELATION.id+"?action=addArticlesUrls")}">P�idej �l�nky</a>
        <a href="${URL.make("/serialy/edit/"+RELATION.id+"?action=rm")}" onclick="return confirm('Opravdu chcete smazat tento seri�l?')">Sma� seri�l</a>
    </p>
</#if>

<#global CITACE = TOOL.getRelationCountersValue(ARTICLES.data,"read")/>
<#list ARTICLES.data as relation>
    <@lib.showArticle relation, "SMART_DMY" />
    <hr>
</#list>

<p>
    <#if (ARTICLES.currentPage.row > 0) >
        <#assign start=ARTICLES.currentPage.row-ARTICLES.pageSize><#if (start<0)><#assign start=0></#if>
        <a href="${RELATION.url}?from=${start}">Nov�j�� �l�nky</a>
    </#if>
    <#assign start=ARTICLES.currentPage.row + ARTICLES.pageSize>
    <#if (start < ARTICLES.total) >
        <a href="${RELATION.url}?from=${start}">Star�� �l�nky</a>
    </#if>
</p>


<#include "../footer.ftl">
