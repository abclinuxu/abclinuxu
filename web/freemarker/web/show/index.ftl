<#assign plovouci_sloupec>
    <div class="ps_nad_reklama">
    reklama
        <div class="ps_reklama">
        <#include "/include/hucek.txt">
        </div>
    </div>

    <!-- ZAKLADY LINUXU -->
    <div class="s_nad_h1"><div class="s_nad_pod_h1">
        <a class="info" href="#">?<span class="tooltip">Co je to Linux a jak ho pou��t.</span></a>
        <h1>Z�klady Linuxu</h1>
    </div></div>
    <div class="s_sekce">
        <ul>
            <li><a href="/clanky/show/26394">Co je to Linux?</a></li>
            <li><a href="/clanky/show/12707">Je opravdu zdarma?</a></li>
            <li><a href="/clanky/show/9503">Co jsou to distribuce?</a></li>
            <li><a href="/clanky/show/14665">N�hrady Windows aplikac�</a></li>
            <li><a href="/clanky/show/20310">Rozcestn�k na�ich seri�l�</a>
        </ul>
    </div>

    <div class="s_nad_h1"><div class="s_nad_pod_h1">
        <a class="info" href="#">?<span class="tooltip">Datab�ze ovlada�� pro v� hardware</span></a>
        <h1><a href="/drivers/dir/318">Ovlada�e</a></h1>
    </div></div>

    <div class="s_sekce">
        <ul>
        <#list VARS.newDrivers as rel>
            <li><a href="/drivers/show/${rel.id}">${TOOL.xpath(rel.child,"data/name")}</a></li>
        </#list>
        </ul>
    </div>

    <div class="s_nad_h1"><div class="s_nad_pod_h1">
        <a class="info" href="#">?<span class="tooltip">Jestli nev�te, co znamen� n�kter� slovo, pod�vejte se do na�eho slovn�ku</span></a>
        <h1><a href="/slovnik">Slovn�k</a></h1>
    </div></div>
    <div class="s_sekce">
        <ul>
        <#list DICTIONARY as rel>
            <li><a href="/slovnik/${rel.child.subType}">${TOOL.xpath(rel.child,"data/name")}</a></li>
        </#list>
        </ul>
    </div>

    <!-- prace.abclinuxu.cz -->
    <div class="s_nad_h1"><div class="s_nad_pod_h1">
        <a class="info" href="#">?<span class="tooltip">Prvn� server s nab�dkami pr�ce (nejen) pro tu���ky. Spojujeme lidi s prac� v IT.</span></a>
        <h1><a href="http://prace.abclinuxu.cz">Prace.abclinuxu.cz</a></h1>
    </div></div>
    <div class="s_sekce">
        <#include "/include/prace_main.txt">
    </div>

    <div class="s_nad_h1"><div class="s_nad_pod_h1">
        <a class="info" href="#">?<span class="tooltip">Obrovsk� datab�ze znalost� o hardwaru</span></a>
        <h1><a href="/hardware/dir/1">Hardware</a></h1>
    </div></div>
    <div class="s_sekce">
        <ul>
        <#list VARS.newHardware as rel>
             <li><a href="/hardware/show/${rel.id}">${TOOL.xpath(rel.child,"data/name")}</a></li>
        </#list>
        </ul>
    </div>

    <div class="s_nad_h1"><div class="s_nad_pod_h1">
        <h1>Aktu�ln� j�dra</h1>
    </div></div>
    <div class="s_sekce">
        <#include "/include/kernel.txt">
    </div>

    <!-- unixshop -->
    <div class="s_nad_h1"><div class="s_nad_pod_h1">
        <a class="info" href="#">?<span class="tooltip">Kvalitn� �elezo pro va�e serverovny za dostupn� ceny</span></a>
        <h1><a href="http://www.unixshop.cz">unixshop.cz</a></h1>
    </div></div>
    <div class="s_sekce">
        <#include "/include/unixshop.txt">
    </div>
</#assign>

<#include "../header.ftl">

<#include "/include/zprava.txt">
<@lib.showMessages/>

<#list ARTICLES as rel>
    <@lib.showArticle rel, "CZ_SHORT"/>
    <hr>
</#list>

<div class="st_uprostred">
    <a href="/History?type=articles&amp;from=${ARTICLES?size}&amp;count=10">Star�� �l�nky</a>
</div>

<#flush>

<#if FORUM?exists>
    <div class="ds">
        <h1 class="st_nadpis"><a href="/diskuse.jsp" title="Cel� diskusn� f�rum">Diskusn� f�rum</a></h1>

        <table>
        <thead>
            <tr>
                <td class="td01">Dotaz</td>
                <td class="td02">Reakc�</td>
                <td class="td03">Posledn�</td>
            </tr>
        </thead>
        <tbody>
        <#list FORUM.data as diz>
            <tr onmouseover="javascript:style.backgroundColor='#F7F7F7'" onmouseout="javascript:style.backgroundColor='#FFFFFF'">
                <td class="td01"><a href="/forum/show/${diz.relationId}">${TOOL.limit(TOOL.xpath(diz.discussion,"data/title"),60," ..")}</a></td>
                <td class="td02">${diz.responseCount}</td>
                <td class="td03">${DATE.show(diz.updated,"CZ_SHORT")}</td>
            </tr>
        </#list>
        </tbody>
        </table>
    </div>
    <ul>
        <li><a href="/diskuse.jsp">Polo�it dotaz</a>
        <li><a href="/History?type=discussions&amp;from=${FORUM.nextPage.row}&amp;count=20">Star�� dotazy</a>
    </ul>
</#if>

<#if IS_INDEX?exists>

<div class="st_nad_rozc"><div class="st_rozc">
    <h1 class="st_nadpis">Rozcestn�k</h1>
	<div class="s"><div class="s_sekce"><div class="rozc">
    <table>
    <#list TOOL.createServers([1,13,12,3,2,5]) as server>
        <#if server_index % 3 = 0><tr><#assign open=true></#if>
        <td>
        <a class="server" href="${server.url}">${server.name}</a>
            <ul>
            <#assign linky = TOOL.sublist(SORT.byDate(LINKS[server.name],"DESCENDING"),0,3)>
            <#list linky as link>
                <li><a href="${link.url}">${link.text}</a></li>
            </#list>
            </ul>
        </td>
        <#if server_index % 3 = 2></tr><#assign open=false></#if>
    </#list>
    <#if open></tr></#if>
    </table>
	</div></div></div>
</div></div>

</#if>

<#include "../footer.ftl">
