<#assign plovouci_sloupec>

    <div class="s_nadpis">
      <a class="info" href="#">?<span class="tooltip">Sbírka uživatelských desktopů. Pochlubte se, jak vypadá vaše pracovní prostředí.</span></a>
      <a href="/desktopy">Desktopy</a>
    </div>

    <div class="s_sekce">
        <ul>
            <li>
                <a class="bez-slovniku" href="${URL.make("/edit?action=add")}" rel="nofollow">Vložit</a>
            </li>
        </ul>
    </div>
</#assign>

<#include "../header.ftl">

<div class="desktopy">

<h1>Desktopy</h1>

<#global CITACE = TOOL.getRelationCountersValue(ITEMS.data,"read")/>
<#list ITEMS.data as relation>
    <#assign item = relation.child, reads = TOOL.getCounterValue(item,"read"), usedBy = item.getProperty("favourited_by")>
    <#assign tmp=TOOL.groupByType(item.children, "Item"), diz=TOOL.analyzeDiscussion(tmp.discussion[0]), autor=TOOL.createUser(item.owner)>

    <h2 class="st_nadpis">
        <a href="${relation.url}" title="${TOOL.xpath(item,"/data/title")}">${TOOL.xpath(item,"/data/title")}</a>
    </h2>

    <div class="thumb">
        <a href="${relation.url}" title="${TOOL.xpath(item,"/data/title")}">
            <img src="${TOOL.xpath(item,"/data/listingThumbnail")}" alt="${TOOL.xpath(item,"/data/title")}" border="0">
        </a>
    </div>

    <p class="meta-vypis">
        <@lib.showUser autor/> |
        ${DATE.show(item.created,"SMART_DMY")} |
        <a href="${diz.url}">Komentářů: ${diz.responseCount}<@lib.markNewComments diz/></a> |
        Zhlédnuto: <@lib.showCounter item, .globals["CITACE"]?if_exists, "read" />&times;
        <#if (usedBy?size > 0)>
            | <a href="${relation.url}?action=users" title="Seznam uživatelů abclinuxu, kterým se líbí tento desktop">Oblíbenost: ${usedBy?size}</a>
        </#if>
    </p>

    <hr />

</#list>

<p>
    <#if (ITEMS.currentPage.row > 0) >
        <#assign start=ITEMS.currentPage.row-ITEMS.pageSize><#if (start<0)><#assign start=0></#if>
        <a href="${RELATION.url}?from=${start}">Novější desktopy</a> &#8226;
    </#if>
    <#assign start=ITEMS.currentPage.row + ITEMS.pageSize>
    <#if (start < ITEMS.total) >
        <a href="${RELATION.url}?from=${start}">Starší desktopy</a>
    </#if>
</p>

</div>

<#include "../footer.ftl">
