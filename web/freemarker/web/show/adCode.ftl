<#assign plovouci_sloupec>
    <div class="s_sekce">
        <ul>
            <li><a href="${URL.noPrefix("/EditAdvertisement/"+PARAMS.rid+"?code="+PARAMS.code+"&amp;action=addVariant")}">Přidat variantu</a></li>
            <li><a href="${URL.noPrefix("/EditAdvertisement/"+PARAMS.rid+"?code="+PARAMS.code+"&amp;action=editCode")}">Upravit kód</a></li>
            <li><a href="${URL.noPrefix("/EditAdvertisement/"+PARAMS.rid+"?code="+PARAMS.code+"&amp;action=rmCode"+TOOL.ticket(USER,false))}" onclick="return confirm('Opravdu smazat kód?')">Smazat kód</a></li>
        </ul>
    </div>
</#assign>

<#include "../header.ftl">
<#include "../ads-macro.ftl">

<@lib.showMessages/>

<h1>Reklamní kód</h1>

<p>Na této stránce je možné spravovat reklamní kód vybrané pozice a jeho varianty. Kód je platný pro všechny stránky s URL, které splňuje jeho kritéria definovaná pomocí regulárního výrazu. Kód může být platný například pro všechny stránky, pro jednu konkrétní stránku nebo pro stránky, jejichž URL začíná stejným výrazem.</p>

<p>Kód má jednu či více variant. Pokud jsou varianty stejné (neliší se štítky), pak jen jedna z nich může být aktivní. Varianty jsou kromě míření na štítky vhodné pro archivování starších reklamních kódů, které mohou být v budoucnu opět aktivovány.</p>

<h1>${CODE.name!}</h1>
<table>
<tr>
    <td>Popis:</td>
    <td>${CODE.description!}</td>
</tr>
<tr>
    <td>Regulární výraz:</td>
    <td><@niceAdvertisementRegexp CODE.regexp! /></code></td>
</tr>
</table>

<h2>Varianty</h2>

<#list VARIANTS as variant>
<form action="/EditAdvertisement" method="post">
    <table width="100%">
        <tr>
            <td>Popis:</td>
            <td>${variant.description!}</td>
        </tr>
        <tr>
            <td width="20%">Stav:</td>
            <td align="left">
                <#if variant.active?default("yes")=="yes"><#assign btnValue="vypnout">
                    aktivní
                <#else><#assign btnValue="zapnout">
                    <span style="color: red">neaktivní</span>
                </#if>
                &nbsp;&nbsp;
                <input type="submit" value="${btnValue}">
            </td>
        </tr>
        <tr>
            <td>Štítky:</td>
            <td>
                <#if !variant.tags?? || variant.tags?size==0>
                    (žádné)
                <#else>
                    <#list variant.tags as tag><#if tag_index gt 0>, </#if><a href="/stitky/${tag}">${tag}</a></#list>
                </#if>
            </td>
        </tr>
        <tr>
            <td>Náhled:</td>
            <td>
                <#if (variant.dynamic!"no") == "yes">
                    <#attempt>
                        <#assign inlineTemplate = variant.code?interpret>
                        <@inlineTemplate />
                    <#recover>
                        Dynamický reklamní kód obsahuje chybu! ${.error}
                    </#attempt>
                <#else>
                    ${variant.code}
                </#if>
            </td>
        </tr>
        <tr>
            <td>
                <a href="${URL.noPrefix("/EditAdvertisement/"+PARAMS.rid+"?code="+PARAMS.code+"&amp;action=editVariant&amp;variant="+variant_index)}">Upravit</a>
                <a href="${URL.noPrefix("/EditAdvertisement/"+PARAMS.rid+"?code="+PARAMS.code+"&amp;action=rmVariant&amp;variant="+variant_index+TOOL.ticket(USER,false))}" onclick="return confirm('Opravdu smazat tuto variantu?')">Smazat</a>
            </td>
        </tr>
    </table>
    <input type="hidden" name="variant" value="${variant_index}">
    <input type="hidden" name="rid" value="${PARAMS.rid}">
    <input type="hidden" name="code" value="${PARAMS.code}">
    <input type="hidden" name="action" value="toggleVariant">
</form>
    <hr />
</#list>

<#include "../footer.ftl">

