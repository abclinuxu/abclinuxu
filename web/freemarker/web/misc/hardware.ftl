<#macro showHardware(hardware)>
    <h1>${TOOL.xpath(hardware,"/data/name")?if_exists}</h1>

    <table class="hwdetail">
        <#if TOOL.xpath(hardware,"/data/support")?exists>
            <tr>
                <td><b>Podpora:</b></td>
                <td>
                    <#switch TOOL.xpath(hardware,"data/support")>
                        <#case "complete">kompletn�<#break>
                        <#case "partial">��ste�n�<#break>
                        <#case "none">��dn�<#break>
                    </#switch>
                </td>
            </tr>
        </#if>

        <#if TOOL.xpath(hardware,"/data/driver")?exists>
            <tr>
                <td><b>Ovlada�:</b></td>
                <td>
                    <#switch TOOL.xpath(hardware,"data/driver")>
                        <#case "kernel">v j�d�e<#break>
                        <#case "xfree">v X Window serveru<#break>
                        <#case "maker">dod�v� v�robce<#break>
                        <#case "other">dod�v� n�kdo jin�<#break>
                        <#case "none">neexistuje<#break>
                    </#switch>
                </td>
            </tr>
        </#if>

        <#assign hwurl = TOOL.xpath(hardware,"data/driver_url")?default("UNDEFINED")>
        <#if (hwurl!="UNDEFINED")>
            <tr>
                <td><b>Adresa ovlada�e:</b></td>
                <td>
                    <a href="${hwurl}" rel="nofollow">${TOOL.limit(hwurl,50,"..")}</a>
                </td>
            <tr>
        </#if>

        <#if TOOL.xpath(hardware,"data/outdated")?exists>
            <tr>
                <td><b>Zastaral�:</b></td>
                <td>ano</td>
            </tr>
        </#if>

        <#if TOOL.xpath(hardware,"data/price")?exists>
            <tr>
                <td><b>Cena:</b></td>
                <td>
                    <#switch TOOL.xpath(hardware,"data/price")>
                        <#case "verylow">velmi n�zk�<#break>
                        <#case "low">n�zk�<#break>
                        <#case "good">p�im��en�<#break>
                        <#case "high">vysok�<#break>
                        <#case "toohigh">p�emr�t�n�<#break>
                    </#switch>
                </td>
            </tr>
        </#if>
    </table>

    <#if TOOL.xpath(hardware,"data/params")?exists>
        <h2>Technick� parametry</h2>
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
        <h2>Postup zprovozn�n� pod Linuxem</h2>
        <div>
            ${TOOL.render(TOOL.element(hardware.data,"data/setup"),USER?if_exists)}
        </div>
    </#if>

    <#if TOOL.xpath(hardware,"data/note")?exists>
        <h2>Pozn�mka</h2>
        <div>
            ${TOOL.render(TOOL.element(hardware.data,"data/note"),USER?if_exists)}
        </div>
    </#if>
</#macro>
