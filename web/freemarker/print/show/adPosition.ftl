<#include "../header.ftl">

<@lib.showMessages/>

<h1>Pozice: ${TOOL.xpath(POSITION, "name/text()")}</h1>

<#assign desc = TOOL.xpath(POSITION, "description/text()")?default("UNDEF"), id = TOOL.xpath(POSITION,"@id")>

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
        <#if desc != "UNDEFINED">
            <tr>
                <td>Popis:</td>
                <td>${desc}</td>
            </tr>
        </#if>
        <tr>
            <td>Defaultní kód:</td>
            <td>
                <textarea disabled rows="5" class="siroka">${DEFAULT_CODE.getText()?html}</textarea>
            </td>
        </tr>
    </table>

    <#if (CODES?size > 0)>
        <h2>Reklamní kódy pro urèité URL adresy</h2>
        <p>
            Tyto kódy se porovnají s aktuální URL adresou v tomto poøadí, pøièem¾ první shoda
            urèí kód, který bude pou¾it. Nebude-li nalezena ¾ádná shoda, pou¾ije se hlavní kód
            pozice.
        </p>
        <#list CODES as code>
            <#assign desc = TOOL.xpath(code,"@description")?default("UNDEFINED")>
            <div style="border: solid 1px black">
                <table border="0">
                    <tr>
                        <td>Regulární výraz:</td>
                        <td>${TOOL.xpath(code,"@regexp")}</td>
                    </tr>
                    <#if desc != "UNDEFINED">
                        <tr>
                            <td>Popis:</td>
                            <td>${desc}</td>
                        </tr>
                    </#if>
                    <tr>
                        <td>Reklamní kód:</td>
                        <td>
                            <textarea disabled rows="5" class="siroka">${code.getText()?html}</textarea>
                        </td>
                    </tr>
                </table>
                <input type="submit" name="editCode${code_index}" value="Upravit">
                <input type="submit" name="rmCode${code_index}" value="Smazat" onclick="return confirm('Opravdu chcete smazat tento kód?')">
            </div>
        </#list>
    </#if>

    <br>
    <input type="submit" name="editPosition" value="Upravit">
    <input type="submit" name="addCode" value="Pøidat kód">
    <input type="submit" name="rmPosition" value="Smazat" onclick="return confirm('Opravdu chcete smazat tuto pozici?')">
    <input type="submit" name="nothing" value="Zpìt">
    <input type="hidden" name="identifier" value="${id}">
</form>

<#include "../footer.ftl">
