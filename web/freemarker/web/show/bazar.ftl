<#include "../header.ftl">

<div class="bazar">

<h1>Bazar</h1>

<p>
    Bazar je bezplatná služba portálu www.abclinuxu.cz, která umožňuje čtenářům
    prodat, darovat či koupit <a href="/hardware">hardware</a>, spotřební
    elektroniku, <a href="/software">software</a>, odbornou literaturu
    nebo předměty s FOSS tématikou (například plyšoví tučňáci, trička). Bazar
    není určen pro výdělečnou činnost; komerční inzeráty (OSVČ, firmy) jsou
    bez předchozího schválení provozovatelem zakázány.
</p>

<table class="bazar-polozky">
  <thead>
    <tr>
        <td class="td01">Titulek inzerátu</td>
        <td class="td02">Typ</td>
        <td class="td03">Přečteno</td>
        <td class="td04">Reakcí</td>
        <td class="td05">Vloženo</td>
    </tr>
  </thead>
  <tbody>
    <#if ADS.total == 0>
        <tr>
            <td colspan="4" align="center">Nenalezeny žádné inzeráty</td>
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
                <#if ad.child.subType=='sell'>
                    <span class="prodej">prodej</span>
                <#else>
                    <span class="koupe">koupě</span>
                </#if>
            </td>
            <td class="td03">
                <@lib.showCounter ad.child, reads, "read" />&times;
            </td>
            <td class="td04">
                <#assign diz=TOOL.findComments(ad.child)>
                ${diz.responseCount}<#if diz.responseCount gt 0><@lib.markNewComments diz/></#if>
            </td>
            <td class="td05">${DATE.show(ad.child.created, "SMART")}</td>
        </tr>
    </#list>
  </tbody>
</table>
</div> <!-- bazar -->

<ul>
    <li>
        <a href="${URL.make("/bazar/edit/"+RELATION.id+"?action=add")}">Vložit nový inzerát</a>
    </li>
    <#if (ADS.currentPage.row > 0) >
        <#assign start=ADS.currentPage.row-ADS.pageSize><#if (start<0)><#assign start=0></#if>
        <li>
            <a href="/bazar?from=${start}&amp;count=${ADS.pageSize}">Novější inzeráty</a>
        </li>
    </#if>
    <#assign start=ADS.currentPage.row + ADS.pageSize>
    <#if (start < ADS.total) >
        <li>
            <a href="/bazar?from=${start}&amp;count=${ADS.pageSize}">Starší inzeráty</a>
        </li>
    </#if>
</ul>


<#include "../footer.ftl">
