<#include "../header.ftl">

<@lib.showMessages/>

<div class="serial-nadpis">
<#assign desc = TOOL.xpath(RELATION.child, "/data/description")?default("UNDEFINED"),
         icon = TOOL.xpath(RELATION.child, "/data/icon")?default("UNDEFINED")>
<#if icon != "UNDEFINED">
    <img src="${icon}" style="float: right" alt="${TOOL.childName(RELATION)}">
</#if>

<h1>Seriál: ${TOOL.childName(RELATION)}</h1>

<#if desc != "UNDEFINED">
    <p>${desc}</p>
</#if>
</div>

<#if USER?exists && USER.hasRole("article admin")>
    <p>
        <a href="${URL.make("/serialy/edit/"+RELATION.id+"?action=edit")}">Uprav seriál</a>
        <a href="${URL.make("/serialy/edit/"+RELATION.id+"?action=addArticlesUrls")}">Pøidej èlánky</a>
        <a href="${URL.make("/serialy/edit/"+RELATION.id+"?action=rm")}" onclick="return confirm('Opravdu chcete smazat tento seriál?')">Sma¾ seriál</a>
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
        <a href="${RELATION.url}?from=${start}">Novìj¹í èlánky</a>
    </#if>
    <#assign start=ARTICLES.currentPage.row + ARTICLES.pageSize>
    <#if (start < ARTICLES.total) >
        <a href="${RELATION.url}?from=${start}">Star¹í èlánky</a>
    </#if>
</p>


<#include "../footer.ftl">
