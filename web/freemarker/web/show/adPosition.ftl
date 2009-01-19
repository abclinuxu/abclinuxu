<#include "../header.ftl">
<#include "../ads-macro.ftl">

<@lib.showMessages/>

<h1>Reklamní pozice: ${POSITION.title}</h1>

<#assign desc = TOOL.xpath(POSITION, "/data/description")!"UNDEF", id = POSITION.string1>

<form action="${URL.noPrefix("/EditAdvertisement")}" method="POST" name="form">
    <table class="siroka" border="0">
        <tr>
            <td width="90">Identifikátor:</td>
            <td>${id}</td>
        </tr>
        <tr>
            <td width="90">Stav:</td>
            <td>
                <#if TOOL.xpath(POSITION, "/data/active")?default("yes")=="yes">
                    <span class="ad_active">aktivní</span>
                    <input type="submit" name="deactivatePosition" value="Vypnout">
                <#else>
                    <span class="ad_inactive">neaktivní</span>
                    <input type="submit" name="activatePosition" value="Zapnout">
                </#if>
            </td>
        </tr>
        <#if desc != "UNDEF">
            <tr>
                <td width="90">Popis:</td>
                <td>${desc}</td>
            </tr>
        </#if>
    </table>

    <br>
    <input type="submit" name="editPosition" value="Upravit">
    <input type="submit" name="addCode" value="Přidat kód">
    <input type="hidden" name="rid" value="${PARAMS.rid}">
    <input type="hidden" name="action" value="dummy">
    <input type="submit" name="rmPosition" value="Smazat" onclick="return confirm('Opravdu chcete smazat tuto pozici?')">
    <input type="submit" name="nothing" value="Zpět">

    <h2>Kódy</h2>
    <table>
        <tr><th>Název</th><th>Varianty</th><th>Popis</th></tr>
        <#assign codeid=0>
        <#macro code>
            <tr>
                <td><a href="${URL.noPrefix("/EditAdvertisement/"+RELATION.id+"?action=showCode&amp;code="+codeid)}">${.node.@name}</a></td>
                <td>${.node.variants?size}</td>
                <td><#if .node.@description[0]??>${.node.@description}</#if></td>
            </tr>
            <#assign codeid=codeid+1>
        </#macro>

        <#macro @element></#macro>
        <#recurse TOOL.asNode(POSITION.data).data.codes>
    </table>
</form>

<#include "../footer.ftl">
