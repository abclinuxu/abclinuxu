<#include "../header.ftl">

<@lib.showMessages/>

<h1>Hry</h1>

<p></p>

<#if USER?exists && USER.hasRole("games admin")>
    <a href="/EditTrivia?action=add">Pøidat kvíz</a>
</#if>

<#global PLAYS = TOOL.getRelationCountersValue(TRIVIA_GAMES,"play")/>

<#list TRIVIA_GAMES as relation>
    <#assign trivia=relation.child, dif=TOOL.xpath(trivia, "/data/difficulty")>
    <h2>${TOOL.childName(relation)}</h2>
    <p>${TOOL.xpath(trivia, "/data/description")}</p>
    <p class="cl_inforadek">
        Úroveò: <#if dif=="simple">jednoduchá<#elseif dif=="normal">normální<#elseif dif=="hard">slo¾itá<#else>guru</#if>,
        hráno: <@lib.showCounter trivia, PLAYS, "play" />&times;,
        prùmìrné skóre: ${TOOL.calculatePercentage(trivia.data,"/data/stats",100).percent}
        <#if USER?exists && USER.hasRole("games admin")>
            <a href="/EditTrivia/${relation.id}?action=edit">Upravit</a>
        </#if>
    </p>
    <form action="${relation.url}" method="POST">
        <input type="submit" value="Hrát">
    </form>

    <hr>
</#list>

<#include "../footer.ftl">
