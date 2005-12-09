<#macro showHardware(hardware)>
    <h1 style="margin-bottom: 10px;">${TOOL.xpath(hardware,"/data/name")?if_exists}</h1>

    <table border="0" class="hwdetail">
        <#if TOOL.xpath(hardware,"/data/support")?exists>
            <tr>
                <td><span class="hardware caption">Podpora:</span></td>
                <td>
                    <#switch TOOL.xpath(hardware,"data/support")>
                        <#case "complete">kompletní<#break>
                        <#case "partial">èásteèná<#break>
                        <#case "none">¾ádná<#break>
                    </#switch>
                </td>
            </tr>
        </#if>

        <#if TOOL.xpath(hardware,"/data/driver")?exists>
            <tr>
                <td><span class="hardware caption">Ovladaè:</span></td>
                <td>
                    <#switch TOOL.xpath(hardware,"data/driver")>
                        <#case "kernel">v jádøe<#break>
                        <#case "xfree">v X Window serveru<#break>
                        <#case "maker">dodává výrobce<#break>
                        <#case "other">dodává nìkdo jiný<#break>
                        <#case "none">neexistuje<#break>
                    </#switch>
                </td>
            </tr>
        </#if>

        <#assign hwurl = TOOL.xpath(hardware,"data/driver_url")?default("UNDEFINED")>
        <#if (hwurl!="UNDEFINED")>
            <tr>
                <td><span class="hardware caption">Adresa ovladaèe:</span></td>
                <td>
                    <a href="${hwurl}">${TOOL.limit(hwurl,50,"..")}</a>
                </td>
            <tr>
        </#if>

        <#if TOOL.xpath(hardware,"data/outdated")?exists>
            <tr>
                <td><span class="hardware caption">Zastaralý:</span></td>
                <td>ano</td>
            </tr>
        </#if>

        <#if TOOL.xpath(hardware,"data/price")?exists>
            <tr>
                <td><span class="hardware caption">Cena:</span></td>
                <td>
                    <#switch TOOL.xpath(hardware,"data/price")>
                        <#case "verylow">velmi nízká<#break>
                        <#case "low">nízká<#break>
                        <#case "good">pøimìøená<#break>
                        <#case "high">vysoká<#break>
                        <#case "toohigh">pøemr¹tìná<#break>
                    </#switch>
                </td>
            </tr>
        </#if>
    </table>

    <#if TOOL.xpath(hardware,"data/params")?exists>
        <h3 class="hardware caption">Technické parametry</h3>
        <div>
            ${TOOL.render(TOOL.element(hardware.data,"data/params"),USER?if_exists)}
        </div>
    </#if>

    <#if TOOL.xpath(hardware,"data/identification")?exists>
        <h3 class="hardware caption">Identifikace pod Linuxem</h3>
        <div>
            ${TOOL.render(TOOL.element(hardware.data,"data/identification"),USER?if_exists)}
        </div>
    </#if>

    <#if TOOL.xpath(hardware,"data/setup")?exists>
        <h3 class="hardware caption">Postup zprovoznìní pod Linuxem</h3>
        <div>
            ${TOOL.render(TOOL.element(hardware.data,"data/setup"),USER?if_exists)}
        </div>
    </#if>

    <#if TOOL.xpath(hardware,"data/note")?exists>
        <h3 class="hardware caption">Poznámka</h3>
        <div>
            ${TOOL.render(TOOL.element(hardware.data,"data/note"),USER?if_exists)}
        </div>
    </#if>
</#macro>
