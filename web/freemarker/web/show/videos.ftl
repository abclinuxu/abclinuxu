<#if USER?exists && TOOL.permissionsFor(USER, RELATION).canModify()>
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

<#list ITEMS.data as video>
    <#assign item=video.child, tmp=TOOL.groupByType(item.children, "Item")>
    <#if tmp.discussion?exists><#assign diz=TOOL.analyzeDiscussion(tmp.discussion[0])><#else><#assign diz=null></#if>

    <h1 class="st_nadpis"><a href="${video.url?default("/videa/show/"+video.id)}">${TOOL.childName(video)}</a></h1>
    <p>${TOOL.xpath(item,"//description")?default("")}</p>
    <p class="meta-vypis">
        <@lib.showUser TOOL.createUser(item.owner)/> | ${DATE.show(item.created, "SMART")}
        <#if diz?exists>| <@lib.showCommentsInListing diz, "CZ_SHORT", "/videa" /></#if>
    </p>
    <hr />
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
