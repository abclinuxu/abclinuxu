<#assign plovouci_sloupec>
    <div class="s_sekce">
    <ul>
        <li><a href="${URL.noPrefix("/EditAdvertisement?action=set64Bit")}">Upravit seznam produktů</a></li>
    </ul>
    </div>
</#assign>
<#include "../header.ftl">

<@lib.showMessages/>

<h1>Produkty z 64bit.cz</h1>

<p>
    Na této stránce najdete seznam produktů z eshopu 64bit.cz, jejichž cena se automaticky a pravidelně
    načítá do databáze portálu. Vložte vybrané níže vygenerované makro do reklamní pozice a systém
    sám zobrazí aktuální cenu. Jméno produktu je jen informační a nikde jinde se nezobrazuje (je zde problém
    s kódováním českých znaků v databázi).
</p>

<table class="siroka">
    <tr>
        <th>Produkt</th>
        <th>Cena</th>
        <th>Makro</th>
    </tr>
<#list PRODUCTS as product>
    <tr>
        <td>
            <#if product.name??>
                <a href="${product.url}">${product.name}</a>
            <#else>
                <a href="${product.url}">${product.id}</a>
                Produkt nenalezen v databázi!
            </#if>
        </td>
        <td>${product.price!}</td>
        <td>TODO ${product.id}</td>
    </tr>
</#list>
</table>
