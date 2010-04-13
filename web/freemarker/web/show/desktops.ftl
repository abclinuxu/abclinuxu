<#import "../macros.ftl" as lib>

<#assign plovouci_sloupec>

    <@lib.advertisement id="hypertext2nahore" />

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

    <div class="s_nadpis">Nejoblíbenější nové desktopy</div>
    <div class="s_sekce" align="center">
        <#list TOOL.sublist(VARS.recentMostPopularDesktops.keySet(), 0, 3) as rel>
            <@lib.showTopDesktop rel />
            <#if rel_index == 0>
                 <@lib.advertisement id="square" />
            </#if>
        </#list>
    </div>
    <div class="s_nadpis">Nejprohlíženější nové desktopy</div>
    <div class="s_sekce" align="center">
        <#list TOOL.sublist(VARS.recentMostSeenDesktops.keySet(), 0, 3) as rel>
            <@lib.showTopDesktop rel />
        </#list>
    </div>
    <div class="s_nadpis">Nejkomentovanější nové desktopy</div>
    <div class="s_sekce" align="center">
        <#list TOOL.sublist(VARS.recentMostCommentedDesktops.keySet(), 0, 3) as rel>
            <@lib.showTopDesktop rel />
        </#list>
    </div>

    &nbsp;<a href="/nej">další&nbsp;&raquo;</a>

    <@lib.advertisement id="hypertext2dole" />

</#assign>

<#include "../header.ftl">

<@lib.showMessages/>

<div class="desktopy">

<h1>Desktopy</h1>

<#list ITEMS.data as relation>
    <#assign desktop = TOOL.createScreenshot(relation), item = relation.child, autor=TOOL.createUser(item.owner)>
    <#assign usedBy = item.getProperty("favourited_by")>
    <#assign tmp = TOOL.groupByType(item.children, "Item"), diz=TOOL.analyzeDiscussion(tmp.discussion[0])>

    <h2 class="st_nadpis"><a href="${desktop.url}" title="${desktop.title}">${desktop.title}</a></h2>

    <div>
        <a href="${desktop.url}" title="${desktop.title}" class="thumb">
            <img src="${desktop.thumbnailListingUrl}" alt="${desktop.title}" border="0">
        </a>
    </div>

    <p class="meta-vypis">
        <@lib.showUser autor/> |
        ${DATE.show(item.created,"SMART_DMY")} |
        <a href="${diz.url}">Komentářů: ${diz.responseCount}<@lib.markNewComments diz/></a> |
        Zhlédnuto: <@lib.showCounter item, "read" />&times;
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
