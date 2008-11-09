<#assign plovouci_sloupec>
    <div class="s_sekce">
    <ul>
        <li><a href="${URL.noPrefix("/EditAdvertisement?action=addPosition")}">Přidat pozici</a></li>
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
    <tr><th>Název</th><th>Identifikátor</th><th>Stav</th><th>Popis</th></tr>
    <#list POSITIONS as ad>
        <tr>
            <td><a href="${URL.noPrefix("/EditAdvertisement/"+ad.id+"?action=showPosition")}">${TOOL.childName(ad)}</a></td>
            <td>${ad.child.string1}</td>
            <td>
                <#if TOOL.xpath(ad.child, "/data/active")?default("yes")=="yes">
                    aktivní
                <#else>
                    <span style="color: red">neaktivní</span>
                </#if>
            </td>
            <td>${TOOL.xpath(ad.child, "/data/description")?if_exists}</td>
        </tr>
        <tr>
            <td colspan="4">
                Kódy: ${TOOL.xpathValue(ad.child.data, "count(//code)")?eval}<#--
                --><#assign codeid=0>
                <#macro code><#--
                    -->, <a href="${URL.noPrefix("/EditAdvertisement?action=showCode&amp;rid="+thisad.id+"&amp;code="+codeid)}">${.node.@name}</a> (${.node.variants?children?size})
                    <#assign codeid=codeid+1>
                </#macro>

                <#macro @element></#macro>
                <#assign thisad=ad>
                <#recurse TOOL.asNode(ad.child.data).data.codes>
            </td>
        </tr>
        <tr>
            <td colspan="4">
                <hr>
            </td>
        </tr>
    </#list>
</table>

<#include "../footer.ftl">
