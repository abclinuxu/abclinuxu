<#if USER?? && TOOL.permissionsFor(USER, RELATION).canCreate()>
    <#assign plovouci_sloupec>
        <div class="s_sekce">
        <ul>
            <li><a href="${URL.make("/edit/"+RELATION.id+"?action=add")}">Přidat video</a></li>
        </ul>
        </div>
    </#assign>
</#if>

<#include "../header.ftl">

<@lib.showMessages/>

<#global READS = TOOL.getRelationCountersValue(ITEMS.data,"read")/>
<#list ITEMS.data as video>
    <#assign item=video.child, tmp=TOOL.groupByType(item.children, "Item"),
        icon=TOOL.xpath(item,"/data/thumbnail")?default("UNDEF")>
    <#if tmp.discussion??><#assign diz=TOOL.analyzeDiscussion(tmp.discussion[0])><#else><#assign diz=null></#if>

    <h1 class="st_nadpis"><a href="${video.url?default("/videa/show/"+video.id)}">${TOOL.childName(video)}</a></h1>
    <#if icon!="UNDEF">
        <div style="padding: 5px">
            <a href="${video.url?default("/videa/show/"+video.id)}"><img src="${icon}" alt="${TOOL.childName(video)}" /></a>
        </div>
    </#if>
    <p>${TOOL.xpath(item,"//description")?default("")}</p>
    <p class="meta-vypis">
        ${DATE.show(item.created, "SMART")} | <@lib.showUser TOOL.createUser(item.owner)/>
        | Zhlédnuto: <@lib.showCounter item, .globals["READS"]!, "read" />&times;
        <#if diz??>| <@lib.showCommentsInListing diz, "CZ_SHORT", "/videa" /></#if>
    </p>
    <hr style="clear:right" />
</#list>

<#if (ITEMS.currentPage.row > 0) >
    <#assign start=ITEMS.currentPage.row-ITEMS.pageSize><#if (start<0)><#assign start=0></#if>
    <li>
        <a href="/videa?from=${start}&amp;count=${ITEMS.pageSize}">Novější videa</a>
    </li>
</#if>
<#assign start=ITEMS.currentPage.row + ITEMS.pageSize>
<#if (start < ITEMS.total) >
    <li>
        <a href="/videa?from=${start}&amp;count=${ITEMS.pageSize}">Starší videa</a>
    </li>
</#if>

<#include "../footer.ftl">
