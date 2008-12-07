<#assign plovouci_sloupec>
    <div class="s_sekce">
    <ul>
        <li><a href="${URL.noPrefix("/EditAdvertisement?action=addPosition")}">Přidat pozici</a></li>
        <#if PARAMS.mode?default("active")=="active">
            <li><a href="${URL.noPrefix("/EditAdvertisement?mode=inactive")}">Zobrazit neaktivní pozice</a></li>
        <#else>
            <li><a href="${URL.noPrefix("/EditAdvertisement?mode=active")}">Zobrazit aktivní pozice</a></li>
        </#if>
        <li><a href="${URL.noPrefix("/EditAdvertisement?action=show64Bit")}">Zobrazit produkty z 64bit.cz</a></li>
    </ul>
    </div>
</#assign>

<#include "../header.ftl">
<#include "../ads-macro.ftl">

<@lib.showMessages/>

<h1>Správa reklamních pozic</h1>

<p>
    Nacházíte se na stránce, kde můžete zakládat nové reklamní pozice,
    zapínat či vypínat jednotlivé existující pozice dle potřeby
    nebo přidávat k pozicím nové reklamní kódy. Reklamní pozice je přesně
    definovaný prostor na stránkách, kde se má zobrazit reklama. Typickým
    příkladem je například banner na vršku stránky. Ke každé pozici je
    třeba nadefinovat jeden či více reklamních kódů, které mají za úkol
    zobrazit vlastní reklamu. V případě více kódu pro jednu pozici
    je třeba kódy rozlišit podle URL adresy.
</p>

<table class="siroka">
    <tr>
        <th>Název</th>
        <th>Identifikátor</th>
        <th>Stav</th>
        <th>Popis</th>
    </tr>
    <#list POSITIONS as ad>
        <tr>
            <td><a href="${URL.noPrefix("/EditAdvertisement/"+ad.id+"?action=showPosition")}">${TOOL.childName(ad)}</a></td>
            <td><tt>${ad.child.string1}</tt></td>
            <td>
                <#if TOOL.xpath(ad.child, "/data/active")?default("yes")=="yes">
                    <span style="color: green">aktivní</span> &nbsp; &nbsp;
                <#else>
                    <span style="color: red">neaktivní</span>
                </#if>
            </td>
            <td>${TOOL.xpath(ad.child, "/data/description")?if_exists}</td>
        </tr>
        <tr style="font-size:small;">
            <td colspan="3">&nbsp;</td>
            <td>
                <#--Kódy: ${TOOL.xpathValue(ad.child.data, "count(//code)")?eval}-->
                <#assign codeid=0>
                <#macro code>
                    <li><a href="${URL.noPrefix("/EditAdvertisement?action=showCode&amp;rid="+thisad.id+"&amp;code="+codeid)}">${.node.@name}</a> (${.node.variants?children?size})
                    <#assign codeid=codeid+1></li>
                </#macro>
                <#macro @element></#macro>
                <#assign thisad=ad>
                <ul>
                    <#recurse TOOL.asNode(ad.child.data).data.codes>
                </ul>
            </td>
        </tr>
        <tr>
            <td colspan="4">
                <hr />
            </td>
        </tr>
    </#list>
</table>

<#include "../footer.ftl">
