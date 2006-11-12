<#include "../header.ftl">

<@lib.showMessages/>

<h1>Hry</h1>

<p>
    Chcete se pobavit, zjistit �rove� sv�ch znalost� �i se z�bavnou formou
    nau�it n��emu nov�mu? Zahrajte si na�e hry. Nejde o ��dn� ceny �i �eb���ky,
    jen o legraci.
</p>

<#if USER?exists && USER.hasRole("games admin")>
    <a href="/EditTrivia?action=add">P�idat kv�z</a>
</#if>

<#list TRIVIA_GAMES as relation>
    <#assign trivia=relation.child, dif=TOOL.xpath(trivia, "/data/difficulty"),
             stats=TOOL.calculatePercentage(trivia.data,"/data/stats",100),
             tmp=TOOL.groupByType(trivia.children, "Item"),diz=TOOL.analyzeDiscussion(tmp.discussion[0])>
    <h2 class="st_nadpis"><a href="${relation.url}">${TOOL.childName(relation)}</a></h2>
    <p>${TOOL.xpath(trivia, "/data/description")}</p>
    <p class="cl_inforadek">
        �rove�: <#if dif=="simple">jednoduch�<#elseif dif=="normal">norm�ln�<#elseif dif=="hard">slo�it�<#else>guru</#if> |
        Hr�no: ${stats.count}&times; |
        Pr�m�rn� sk�re: ${stats.percent} |
        <@lib.showCommentsInListing diz, "SMART", "/hry" />
        <#if USER?exists && USER.hasRole("games admin")>
            <a href="/EditTrivia/${relation.id}?action=edit">Upravit</a>
        </#if>
    </p>
    <hr>
</#list>

<p>
    Pokud jste nalezli chybu nebo v�s napadly dal�� ot�zky pro n�jak� kv�z,
    vlo�te pros�m informace do <a rel="nofollow" href="http://bugzilla.abclinuxu.cz/show_bug.cgi?id=624">bugzilly</a>.
</p>

<#include "../footer.ftl">
