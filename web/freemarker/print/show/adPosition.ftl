<#include "../header.ftl">

<@lib.showMessages/>

<h1>Pozice: ${TOOL.xpath(POSITION, "name/text()")}</h1>

<#assign desc = TOOL.xpath(POSITION, "description/text()")?default("UNDEF"), id = TOOL.xpath(POSITION,"@id")>
<#if desc != "UNDEF">
    <p>${desc}</p>
</#if>

<form action="${URL.noPrefix("/EditAdvertisement")}" method="POST" name="form">
    <table border="0">
        <tr>
            <td>Identifikátor:</td>
            <td>${id}</td>
        </tr>
        <tr>
            <td>Stav:</td>
            <td>
                <#if TOOL.xpath(POSITION, "@active")=="yes">
                    <span class="ad_active">aktivní</span>
                    <input type="submit" name="deactivatePosition" value="Vypnout">
                <#else>
                    <span class="ad_inactive">neaktivní</span>
                    <input type="submit" name="activatePosition" value="Zapnout">
                </#if>
            </td>
        </tr>
        <tr>
            <td>Defaultní kód:</td>
            <td>
                <textarea disabled rows="5" class="siroka">${TOOL.xpath(POSITION, "code[string-length(@id)=0]")?html}</textarea>
            </td>
        </tr>
    </table>
    <input type="submit" name="editPosition" value="Upravit">
    <input type="submit" name="rmPosition" value="Smazat">
    <input type="submit" name="nothing" value="Zpìt">
    <input type="hidden" name="identifier" value="${id}">
</form>

<#include "../footer.ftl">
