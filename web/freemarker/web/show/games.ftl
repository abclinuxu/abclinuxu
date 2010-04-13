<#import "../macros.ftl" as lib>
<#assign plovouci_sloupec>

    <@lib.advertisement id="hypertext2nahore" />

    <div class="s_nadpis">Hry</div>
    <div class="s_sekce">
        <p>Chcete se pobavit, zjistit úroveň svých znalostí či se zábavnou formou naučit něčemu novému? Zahrajte si naše hry. Nejde o žádné ceny či žebříčky, jen o legraci.</p>

        <p>Pokud jste nalezli v některém kvízu chybu, vložte prosím informace do diskuze. Chcete-li se podílet na přípravě dalších kvízů, pošlete e-mail na <a href="mailto:redakce@abclinuxu.cz">redakce@abclinuxu.cz</a>. Za každý kvíz, který redakce přijme, získáte finanční odměnu.</p>
    </div>

    <#if USER?? && TOOL.permissionsFor(USER, RELATION).canCreate()>
        <div class="s_nadpis">Nástroje</div>
        <div class="s_sekce">
            <ul>
                <li><a href="/EditTrivia?action=add">Přidat kvíz</a></li>
            </ul>
        </div>
    </#if>

    <@lib.advertisement id="square" />
    <@lib.advertisement id="hypertext2dole" />

</#assign>

<#include "../header.ftl">

<@lib.showMessages/>


<h1>Hry</h1>

<#list TRIVIA_GAMES as relation>
    <#assign trivia=relation.child, dif=TOOL.xpath(trivia, "/data/difficulty"),
             stats=TOOL.calculatePercentage(trivia.data,"/data/stats",100),
             tmp=TOOL.groupByType(trivia.children, "Item"),diz=TOOL.analyzeDiscussion(tmp.discussion[0])>
    <h2 class="st_nadpis"><a href="${relation.url}">${TOOL.childName(relation)}</a></h2>
    <p>${TOOL.xpath(trivia, "/data/description")}</p>
    <p class="meta-vypis">
        Úroveň: <#if dif=="simple">jednoduchá
                <#elseif dif=="normal">normální
                <#elseif dif=="hard">složitá
                <#else>guru</#if> |
        Hráno: ${stats.count}&times; |
        Průměrné skóre: ${stats.percent} |
        <@lib.showCommentsInListing diz, "SMART", "/hry" />
        <#if USER?? && TOOL.permissionsFor(USER, relation).canModify()>
            <a href="/EditTrivia/${relation.id}?action=edit">Upravit</a>
        </#if>
    </p>
    <hr />
</#list>

<#include "../footer.ftl">
