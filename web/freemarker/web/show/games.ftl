<#include "../header.ftl">

<@lib.showMessages/>

<h1>Hry</h1>

<p></p>

<#if USER?exists && USER.hasRole("games admin")>
    <a href="/EditTrivia?action=add">P�idat kv�z</a>
</#if>

<#global PLAYS = TOOL.getRelationCountersValue(TRIVIA_GAMES,"play")/>

<#list TRIVIA_GAMES as relation>
    <#assign trivia=relation.child, dif=TOOL.xpath(trivia, "/data/difficulty")>
    <h2>${TOOL.childName(relation)}</h2>
    <p>${TOOL.xpath(trivia, "/data/description")}</p>
    <p class="cl_inforadek">
        �rove�: <#if dif=="simple">jednoduch�<#elseif dif=="normal">norm�ln�<#elseif dif=="hard">slo�it�<#else>guru</#if>,
        hr�no: <@lib.showCounter trivia, PLAYS, "play" />&times;,
        pr�m�rn� sk�re: ${TOOL.calculatePercentage(trivia.data,"/data/stats",100).percent}
        <#if USER?exists && USER.hasRole("games admin")>
            <a href="/EditTrivia/${relation.id}?action=edit">Upravit</a>
        </#if>
    </p>
    <form action="${relation.url}" method="POST">
        <input type="submit" value="Hr�t">
    </form>

    <hr>
</#list>

<#include "../footer.ftl">
