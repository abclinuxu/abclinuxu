<#include "../header.ftl">

<div class="bazar">

<@lib.advertisement id="square" />

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
        <td class="td-nazev">Titulek inzerátu</td>
        <td class="td-meta">Typ</td>
        <td class="td-meta">Přečteno</td>
        <td class="td-meta">Reakcí</td>
        <td class="td-datum">Vloženo</td>
    </tr>
  </thead>
  <tbody>
    <#if ADS.total == 0>
        <tr>
            <td colspan="4" align="center">Nenalezeny žádné inzeráty</td>
        </tr>
    </#if>
    <#list ADS.data as ad>
        <tr>
            <td>
                <a href="/bazar/show/${ad.id}">${ad.child.title}</a>
            </td>
            <td class="td-meta">
                <#if ad.child.subType=='sell'>
                    <span class="prodej">prodej</span>
                <#else>
                    <span class="koupe">koupě</span>
                </#if>
            </td>
            <td class="td-meta">
                <@lib.showCounter ad.child, "read" />&times;
            </td>
            <td class="td-meta">
                <#assign diz=TOOL.findComments(ad.child)>
                ${diz.responseCount}<#if diz.responseCount gt 0><@lib.markNewComments diz/></#if>
            </td>
            <td class="td-datum">${DATE.show(ad.child.created, "SMART")}</td>
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
