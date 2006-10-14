<#include "../header.ftl">

<div class="bazar">

<h1>Bazar</h1>

<p>
    Bazar je bezplatn� slu�ba port�lu www.abclinuxu.cz, kter� umo��uje �ten���m
    prodat, darovat �i koupit hardware, spot�ebn� elektroniku, software, odbornou literaturu
    nebo p�edm�ty s FOSS t�matikou (nap��klad ply�ov� tu���ci, tri�ka). Bazar
    nen� ur�en pro v�d�le�nou �innost, komer�n� inzer�ty (OSV�, firmy) jsou
    bez p�edchoz�ho schv�len� provozovatelem zak�z�ny.
</p>

<table class="bazar-polozky">
  <thead>
    <tr>
        <td class="td01">Titulek inzer�tu</td>
        <td class="td02">Typ</td>
        <td class="td03">Koment��e</td>
        <td class="td04">Vlo�eno</td>
        <td class="td05">P�e�teno</td>
    </tr>
  </thead>
  <tbody>
    <#if ADS.total == 0>
        <tr>
            <td colspan="4" align="center">Nenalezeny ��dn� inzer�ty</td>
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
                <#if ad.child.subType=='sell'>prodej<#else>koup�</#if>
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
        <a href="${URL.make("/bazar/edit/"+RELATION.id+"?action=add")}">Vlo�it nov� inzer�t</a>
    </li>
    <#if (ADS.currentPage.row > 0) >
        <#assign start=ADS.currentPage.row-ADS.pageSize><#if (start<0)><#assign start=0></#if>
        <li>
            <a href="/bazar?from=${start}&amp;count=${ADS.pageSize}">Nov�j�� inzer�ty</a>
        </li>
    </#if>
    <#assign start=ADS.currentPage.row + ADS.pageSize>
    <#if (start < ADS.total) >
        <li>
            <a href="/bazar?from=${start}&amp;count=${ADS.pageSize}">Star�� inzer�ty</a>
        </li>
    </#if>
</ul>

</div> <!-- bazar -->

<#include "../footer.ftl">
