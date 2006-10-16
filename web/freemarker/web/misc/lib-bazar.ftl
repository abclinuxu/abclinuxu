<#macro showBazaarAd (AD who)>
    <h1>
        ${TOOL.xpath(AD.data, "/data/title")}
    </h1>

    <table>
        <tr>
            <td><b>Typ:</b></td>
            <td>
                <#if AD.subType=='sell'>prodej<#else>koup�</#if>
            </td>
        </tr>
        <#assign price = TOOL.xpath(AD.data, "/data/price")?default("UNDEFINED")>
        <#if price != "UNDEFINED">
            <tr>
                <td><b>Cena:</b></td>
                <td>
                    ${price}
                </td>
            </tr>
        </#if>
        <tr>
            <td><b>Autor:</b></td>
            <td>
                <a href="/Profile/${who.id}">${who.nick?default(who.name)}</a>
            </td>
        </tr>
        <tr>
            <td><b>Kontakt:</b></td>
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
            <td><b>Vlo�eno:</b></td>
            <td>
                ${DATE.show(AD.created,"SMART")}<#rt>
                <#lt><#if AD.created.time != AD.updated.time><#rt>
                    <#lt>, posledn� �prava ${DATE.show(AD.updated,"SMART")}
                </#if>
            </td>
        </tr>
        <tr>
            <td><b>P�e�teno:</b></td>
            <td>
                <#local reads = TOOL.getCounterValue(AD,"read")>${reads}&times;
            </td>
        </tr>
    </table>

    <div class="bazar-ad">
        ${TOOL.render(TOOL.xpath(AD.data,"/data/text"), USER?if_exists)}
    </div>
</#macro>