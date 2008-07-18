<#macro showHardware(hardware)>
    <h1>${hardware.title?if_exists}</h1>

    <table class="hwdetail">
        <#if TOOL.xpath(hardware,"/data/support")?exists>
            <tr>
                <td><b>Podpora:</b></td>
                <td>
                    <#switch TOOL.xpath(hardware,"data/support")>
                        <#case "complete">kompletní<#break>
                        <#case "partial">částečná<#break>
                        <#case "none">žádná<#break>
                    </#switch>
                </td>
            </tr>
        </#if>

        <#if TOOL.xpath(hardware,"/data/driver")?exists>
            <tr>
                <td><b>Ovladač:</b></td>
                <td>
                    <#switch TOOL.xpath(hardware,"data/driver")>
                        <#case "kernel">v jádře<#break>
                        <#case "xfree">v X Window serveru<#break>
                        <#case "maker">dodává výrobce<#break>
                        <#case "other">dodává někdo jiný<#break>
                        <#case "none">neexistuje<#break>
                    </#switch>
                </td>
            </tr>
        </#if>

        <#assign hwurl = TOOL.xpath(hardware,"data/driver_url")?default("UNDEFINED")>
        <#if (hwurl!="UNDEFINED")>
            <tr>
                <td><b>Adresa ovladače:</b></td>
                <td>
                    <a href="${hwurl}" rel="nofollow">${TOOL.limit(hwurl,50,"..")}</a>
                </td>
            <tr>
        </#if>

        <#if TOOL.xpath(hardware,"data/outdated")?exists>
            <tr>
                <td><b>Zastaralý:</b></td>
                <td>ano</td>
            </tr>
        </#if>
    </table>

    <#if TOOL.xpath(hardware,"data/params")?exists>
        <h2>Technické parametry</h2>
        <div>
            ${TOOL.render(TOOL.element(hardware.data,"data/params"),USER?if_exists)}
        </div>
    </#if>

    <#if TOOL.xpath(hardware,"data/identification")?exists>
        <h2>Identifikace pod Linuxem</h2>
        <div>
            ${TOOL.render(TOOL.element(hardware.data,"data/identification"),USER?if_exists)}
        </div>
    </#if>

    <#if TOOL.xpath(hardware,"data/setup")?exists>
        <h2>Postup zprovoznění pod Linuxem</h2>
        <div>
            ${TOOL.render(TOOL.element(hardware.data,"data/setup"),USER?if_exists)}
        </div>
    </#if>

    <#if TOOL.xpath(hardware,"data/note")?exists>
        <h2>Poznámka</h2>
        <div>
            ${TOOL.render(TOOL.element(hardware.data,"data/note"),USER?if_exists)}
        </div>
    </#if>
</#macro>
