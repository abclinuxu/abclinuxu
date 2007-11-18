<#assign plovouci_sloupec>
    <div class="s_sekce">
        <ul>
            <li>
                <a class="bez-slovniku" href="${URL.make("/edit?action=add")}" rel="nofollow">Vlozit</a>
            </li>
        </ul>
    </div>
</#assign>

<#include "../header.ftl">

<h1>Desktopy</h1>

<#global CITACE = TOOL.getRelationCountersValue(ITEMS.data,"read")/>
<#list ITEMS.data as relation>
    <#assign item = relation.child, reads = TOOL.getCounterValue(item,"read"), usedBy = item.getProperty("favourited_by")>
    <#assign tmp=TOOL.groupByType(item.children, "Item"), diz=TOOL.analyzeDiscussion(tmp.discussion[0]), autor=TOOL.createUser(item.owner)>

    <h2>
        <a href="${relation.url}">${TOOL.xpath(item,"/data/title")}</a>
    </h2>
    <table class="swdetail">
        <tr>
            <td>Autor</td>
            <td><@lib.showUser autor/></td>
            <td rowspan="5">
                <a href="${relation.url}">
                    <img src="${TOOL.xpath(item,"/data/listingThumbnail")}" alt="${TOOL.xpath(item,"/data/title")}" border="0">
                </a>
            </td>
        </tr>
        <tr>
            <td>Datum</td>
            <td>${DATE.show(item.created,"SMART_DMY")}</td>
        </tr>
        <tr>
            <td>Komentaru</td>
            <td>
                <a href="${diz.url}">${diz.responseCount}<@lib.markNewComments diz/></a>
            </td>
        </tr>
        <tr>
            <td>Oblibenost</td>
            <td>
                <a href="?action=users" title="Seznam uživatelů abclinuxu, kterym se libi tento desktop">${usedBy?size}</a>
            </td>
        </tr>
        <tr>
            <td>Shlednuto:</td>
            <td>
                <@lib.showCounter item, .globals["CITACE"]?if_exists, "read" />&times;
            </td>
        </tr>
    </table>

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


<#include "../footer.ftl">
