<#include "../header.ftl">

<@lib.showMessages/>

<h1>Hry</h1>

<p>
    Chcete se pobavit, zjistit úroveò svých znalostí èi si zábavnou formou
    nauèit nìèemu novému? Zahrejte si na¹e hry. Nejde o ¾ádné ceny èi ¾ebøíèky,
    jen o legraci.
</p>

<#if USER?exists && USER.hasRole("games admin")>
    <a href="/EditTrivia?action=add">Pøidat kvíz</a>
</#if>

<#list TRIVIA_GAMES as relation>
    <#assign trivia=relation.child, dif=TOOL.xpath(trivia, "/data/difficulty"),
             stats=TOOL.calculatePercentage(trivia.data,"/data/stats",100)>
    <h2>${TOOL.childName(relation)}</h2>
    <p>${TOOL.xpath(trivia, "/data/description")}</p>
    <p class="cl_inforadek">
        Úroveò: <#if dif=="simple">jednoduchá<#elseif dif=="normal">normální<#elseif dif=="hard">slo¾itá<#else>guru</#if>,
        hráno: ${stats.count}&times;,
        prùmìrné skóre: ${stats.percent}
        <#if USER?exists && USER.hasRole("games admin")>
            <a href="/EditTrivia/${relation.id}?action=edit">Upravit</a>
        </#if>
    </p>
    <form action="${relation.url}" method="POST">
        <input type="submit" value="Hrát">
    </form>

    <hr>
</#list>

<p>
    Pokud jste nalezli chybu v kvízu, nebo vás napadly dal¹í otázky pro nìjaký kvíz,
    vlo¾te prosím informace do <a href="http://bugzilla.abclinuxu.cz/show_bug.cgi?id=624">bugzilly</a>.
</p>

<#include "../footer.ftl">
