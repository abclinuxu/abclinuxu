<#include "../header.ftl">

<h1>Bazar</h1>

<p>
    Bazar je bezplatná slu¾ba portálu www.abclinuxu.cz, která umo¾òuje ètenáøùm
    prodat, darovat èi koupit hardware, software, odbornou literaturu
    nebo pøedmìty s FOSS tématikou (napøíklad ply¹oví tuèòáci, trièka). Bazar
    není urèen pro výdìleènou èinnost, komerèní inzeráty (OSVÈ, firmy) jsou
    bez pøedchozího schválení provozovatelem zakázány.
</p>

<table class="bazar_inzeraty">
    <tr>
        <th>Titulek</th>
        <th>Typ</th>
        <th>Vlo¾eno</th>
    </tr>
    <#if ADS.total == 0>
        <tr>
            <td colspan="3">Nenalezeny ¾ádné inzeráty</td>
        </tr>
    </#if>
    <#list ADS.data as ad>
        <tr>
            <td>
                <a href="/bazar/show/${ad.id}">${TOOL.xpath(ad.child, "/data/title")}</a>
            </td>
            <td>
                <#if ad.child.subType=='buy'>
                    prodej
                <#elseif ad.child.subType=='sell'>
                    koupì
                <#else>
                    darování
                </#if>
            </td>
            <td>${DATE.show(ad.child.created, "SMART")}</td>
        </tr>
    </#list>
</table>

<ul>
    <li>
        <a href="${URL.make("/bazar/edit/"+RELATION.id+"?action=add")}">Vlo¾it nový inzerát</a>
    </li>
    <#if (ADS.currentPage.row > 0) >
        <#assign start=ADS.currentPage.row-ADS.pageSize><#if (start<0)><#assign start=0></#if>
        <li>
            <a href="/bazar?from=${start}&amp;count=${ADS.pageSize}">Novìj¹í inzeráty</a>
        </li>
    </#if>
    <#assign start=ADS.currentPage.row + ADS.pageSize>
    <#if (start < ADS.total) >
        <li>
            <a href="/bazar?from=${start}&amp;count=${ADS.pageSize}">Star¹í inzeráty</a>
        </li>
    </#if>
</ul>

<#include "../footer.ftl">
