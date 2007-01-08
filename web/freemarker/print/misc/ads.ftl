<#include "../header.ftl">

<@lib.showMessages/>

<h1>Správa reklamních pozic</h1>

<p>
    Nacházíte se na stránce, kde mù¾ete zakládat nové reklamní pozice,
    zapínat èi vypínat jednotlivé existující pozice dle potøeby
    nebo pøidávat k pozicím nové reklamní kódy. Reklamní pozice je pøesnì
    definovaný prostor na stránkách, kde se má zobrazit reklama. Typickým
    pøíkladem je napøíklad banner na vr¹ku stránky. Ke ka¾dé pozici je
    tøeba nadefinovat jeden èi více reklamních kódu, které mají za úkol
    zobrazit vlastní reklamu. V pøípadì více kódu pro jednu pozici
    je tøeba kódy rozli¹it podle URL adresy.
</p>

<form action="${URL.noPrefix("/EditAdvertisement")}" method="POST" name="form">
    <#if POSITIONS?exists>
        <table border="0">
		<tr>
			<td><b>&nbsp;</b></td>
			<td><b>název</b></td>
			<td><b>identifikátor</b></td>
			<td><b>stav</b></td>
		</tr>
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
    <input type="submit" name="addPosition" value="Vlo¾it novou pozici">
</form>

<#include "../footer.ftl">
