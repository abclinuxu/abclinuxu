<#macro showBazaarAd (AD who)>
    <h1>
        ${TOOL.xpath(AD.data, "/data/title")}
    </h1>

    <table>
        <tr>
            <td>Typ:</td>
            <td>
                <#if AD.subType=='sell'>prodej<#else>koup�</#if>
            </td>
        </tr>
        <#assign price = TOOL.xpath(AD.data, "/data/price")?default("UNDEFINED")>
        <#if price != "UNDEFINED">
            <tr>
                <td>Cena:</td>
                <td>
                    ${price}
                </td>
            </tr>
        </#if>
        <tr>
            <td>Autor:</td>
            <td>
                <a href="/Profile/${who.id}">${who.nick?default(who.name)}</a>
            </td>
        </tr>
        <tr>
            <td>Kontakt:</td>
            <td>
                <#assign contact=TOOL.xpath(AD,"/data/contact")?default("UNDEFINED")>
                <#if contact != "UNDEFINED">
                    ${contact}
                <#else>
                    <#if TOOL.xpath(who,"/data/communication/email[@valid='yes']")?exists>
                        <form action="${URL.noPrefix("/Profile")}">
                            <input type="hidden" name="action" value="sendEmail">
                            <input type="hidden" name="uid" value="${who.id}">
                            <input type="submit" value="Po�lete mi email" class="button">
                        </form>
                    <#else>
                        <p class="error">Administr�to�i ozna�ili email u�ivatele za neplatn�!</p>
                    </#if>
                </#if>
            </td>
        </tr>
        <tr>
            <td>Vlo�eno:</td>
            <td>
                ${DATE.show(AD.created,"SMART")}<#rt>
                <#lt><#if AD.created.time != AD.updated.time><#rt>
                    <#lt>, posledn� �prava ${DATE.show(AD.updated,"SMART")}
                </#if>
            </td>
        </tr>
        <tr>
            <td>P�e�teno:</td>
            <td>
                <#local reads = TOOL.getCounterValue(AD,"read")>${reads}&times;
            </td>
        </tr>
    </table>

    <div class="content" style="margin: 1em; padding: 1ex; border: ridge gray"> <!-- TODO: prevest do CSS -->
        ${TOOL.render(TOOL.xpath(AD.data,"/data/text"), USER?if_exists)}
    </div>
</#macro>