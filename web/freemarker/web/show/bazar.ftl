<#include "../header.ftl">

<div class="bazar">

<h1>Bazar</h1>

<p>
    Bazar je bezplatná slu¾ba portálu www.abclinuxu.cz, která umo¾òuje ètenáøùm
    prodat, darovat èi koupit hardware, spotøební elektroniku, software, odbornou literaturu
    nebo pøedmìty s FOSS tématikou (napøíklad ply¹oví tuèòáci, trièka). Bazar
    není urèen pro výdìleènou èinnost, komerèní inzeráty (OSVÈ, firmy) jsou
    bez pøedchozího schválení provozovatelem zakázány.
</p>

<table class="bazar-polozky">
  <thead>
    <tr>
        <td class="td01">Titulek inzerátu</td>
        <td class="td02">Typ</td>
        <td class="td03">Komentáøe</td>
        <td class="td04">Vlo¾eno</td>
        <td class="td05">Pøeèteno</td>
    </tr>
  </thead>
  <tbody>
    <#if ADS.total == 0>
        <tr>
            <td colspan="4" align="center">Nenalezeny ¾ádné inzeráty</td>
        </tr>
    <#else>
        <#assign reads=TOOL.getRelationCountersValue(ADS.data,"read")>
    </#if>
    <#list ADS.data as ad>
        <tr>
            <td class="td01">
                <a href="/bazar/show/${ad.id}">${TOOL.xpath(ad.child, "/data/title")}</a>
            </td>
            <td class="td02">
                <#if ad.child.subType=='sell'>prodej<#else>koupì</#if>
            </td>
            <td class="td03">
                <#assign diz=TOOL.findComments(ad.child)>
                ${diz.responseCount}<#if diz.responseCount gt 0><@lib.markNewComments diz/></#if>
            </td>
            <td class="td04">${DATE.show(ad.child.created, "SMART")}</td>
            <td class="td05">
                <@lib.showCounter ad.child, reads, "read" />&times;
            </td>
        </tr>
    </#list>
  </tbody>
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

</div> <!-- bazar -->

<#include "../footer.ftl">
