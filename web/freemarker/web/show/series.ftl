<#include "../header.ftl">

<@lib.showMessages/>

<div class="serial-nadpis">
<#assign desc = TOOL.xpath(RELATION.child, "/data/description")!"UNDEFINED",
         icon = TOOL.xpath(RELATION.child, "/data/icon")!"UNDEFINED">
<#if icon != "UNDEFINED">
    <img src="${icon}" style="float: right" alt="${TOOL.childName(RELATION)}">
</#if>

<h1>Seriál: ${TOOL.childName(RELATION)}</h1>

<#if desc != "UNDEFINED">
    <p>${desc}</p>
</#if>
</div>

<#if USER?? && TOOL.permissionsFor(USER, RELATION).canModify()>
    <p>
        <a href="${URL.make("/serialy/edit/"+RELATION.id+"?action=edit")}">Uprav seriál</a>
        <a href="${URL.make("/serialy/edit/"+RELATION.id+"?action=addArticlesUrls")}">Přidej články</a>
        <a href="${URL.make("/serialy/edit/"+RELATION.id+"?action=rm"+TOOL.ticket(USER, false))}"
           onclick="return confirm('Opravdu chcete smazat tento seriál?')">Smaž seriál</a>
    </p>
</#if>

<#list ARTICLES.data as relation>
    <@lib.showArticle relation, "SMART_DMY" />
    <hr>
</#list>

<p>
    <#if (ARTICLES.currentPage.row > 0) >
        <#assign start=ARTICLES.currentPage.row-ARTICLES.pageSize><#if (start<0)><#assign start=0></#if>
        <a href="${RELATION.url}?from=${start}">Novější články</a> &#8226;
    </#if>
    <#assign start=ARTICLES.currentPage.row + ARTICLES.pageSize>
    <#if (start < ARTICLES.total) >
        <a href="${RELATION.url}?from=${start}">Starší články</a>
    </#if>
</p>

<@lib.showPageTools RELATION />

<#include "../footer.ftl">
