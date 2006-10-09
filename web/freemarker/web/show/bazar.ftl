<#include "../header.ftl">

<h1>Bazar</h1>

<p>
    Bazar je bezplatn� slu�ba port�lu www.abclinuxu.cz, kter� umo��uje �ten���m
    prodat, darovat �i koupit hardware, software, odbornou literaturu
    nebo p�edm�ty s FOSS t�matikou (nap��klad ply�ov� tu���ci, tri�ka). Bazar
    nen� ur�en pro v�d�le�nou �innost, komer�n� inzer�ty (OSV�, firmy) jsou
    bez p�edchoz�ho schv�len� provozovatelem zak�z�ny.
</p>

<table class="bazar_inzeraty">
    <tr>
        <th>Titulek</th>
        <th>Typ</th>
        <th>Vlo�eno</th>
    </tr>
    <#if ADS.total == 0>
        <tr>
            <td colspan="3">Nenalezeny ��dn� inzer�ty</td>
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
                    koup�
                <#else>
                    darov�n�
                </#if>
            </td>
            <td>${DATE.show(ad.child.created, "SMART")}</td>
        </tr>
    </#list>
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

<#include "../footer.ftl">
