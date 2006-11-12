<#include "../header.ftl">

<@lib.showMessages/>

<h1>Hry</h1>

<p>
    Chcete se pobavit, zjistit úroveò svých znalostí èi se zábavnou formou
    nauèit nìèemu novému? Zahrajte si na¹e hry. Nejde o ¾ádné ceny èi ¾ebøíèky,
    jen o legraci.
</p>

<#if USER?exists && USER.hasRole("games admin")>
    <a href="/EditTrivia?action=add">Pøidat kvíz</a>
</#if>

<#list TRIVIA_GAMES as relation>
    <#assign trivia=relation.child, dif=TOOL.xpath(trivia, "/data/difficulty"),
             stats=TOOL.calculatePercentage(trivia.data,"/data/stats",100),
             tmp=TOOL.groupByType(trivia.children, "Item"),diz=TOOL.analyzeDiscussion(tmp.discussion[0])>
    <h2 class="st_nadpis"><a href="${relation.url}">${TOOL.childName(relation)}</a></h2>
    <p>${TOOL.xpath(trivia, "/data/description")}</p>
    <p class="cl_inforadek">
        Úroveò: <#if dif=="simple">jednoduchá<#elseif dif=="normal">normální<#elseif dif=="hard">slo¾itá<#else>guru</#if> |
        Hráno: ${stats.count}&times; |
        Prùmìrné skóre: ${stats.percent} |
        <@lib.showCommentsInListing diz, "SMART", "/hry" />
        <#if USER?exists && USER.hasRole("games admin")>
            <a href="/EditTrivia/${relation.id}?action=edit">Upravit</a>
        </#if>
    </p>
    <hr>
</#list>

<p>
    Pokud jste nalezli chybu nebo vás napadly dal¹í otázky pro nìjaký kvíz,
    vlo¾te prosím informace do <a rel="nofollow" href="http://bugzilla.abclinuxu.cz/show_bug.cgi?id=624">bugzilly</a>.
</p>

<#include "../footer.ftl">
