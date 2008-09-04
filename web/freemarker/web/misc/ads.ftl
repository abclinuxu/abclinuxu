<#include "../header.ftl">

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

<form action="${URL.noPrefix("/EditAdvertisement")}" method="POST" name="form">
    <#if POSITIONS?exists>
        <table border="0">
            <thead>
                <tr>
                    <td>&nbsp;</td>
                    <td>název</td>
                    <td>identifikátor</td>
                    <td>stav</td>
                </tr>
            </thead>
            <#list POSITIONS as position>
                <#assign id = TOOL.xpath(position,"@id")>
                <tr>
                    <td>
                        <input type="checkbox" name="identifier" value="${id}">
                    </td>
                    <td>
                        <a href="${URL.noPrefix("/EditAdvertisement?action=showPosition&amp;identifier="+id)}">
                            ${TOOL.xpath(position, "name/text()")}
                        </a>
                    </td>
                    <td>
			            <code>${id}</code>
                    </td>
                    <td>
                        <#if TOOL.xpath(position, "@active")=="yes">
                            <span class="ad_active">aktivní</span>
                        <#else>
                            <span class="ad_inactive">neaktivní</span>
                        </#if>
                    </td>
                </tr>
            </#list>
            <tr>
                <td>
                    <!-- select none/all checkbox -->
                </td>
                <td colspan="2">
                    <input type="submit" name="activatePosition" value="Zapnout">
                    <input type="submit" name="deactivatePosition" value="Vypnout">
                    <input type="submit" name="rmPosition" value="Smazat" onclick="return confirm('Opravdu chcete smazat zvolené pozice?')">
                    vybrané pozice
                </td>
            </tr>
        </table>
    </#if>
    <input type="submit" name="addPosition" value="Vložit novou pozici">
</form>

<#include "../footer.ftl">
