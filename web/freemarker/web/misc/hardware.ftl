<#macro showHardware(hardware)>
    <h1 style="margin-bottom: 10px;">${TOOL.xpath(hardware,"/data/name")?if_exists}</h1>

    <table border="0" class="hwdetail">
        <#if TOOL.xpath(hardware,"/data/support")?exists>
            <tr>
                <td><span class="hardware caption">Podpora:</span></td>
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
                <td><span class="hardware caption">Ovlada�:</span></td>
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
                <td><span class="hardware caption">Adresa ovlada�e:</span></td>
                <td>
                    <a href="${hwurl}">${TOOL.limit(hwurl,50,"..")}</a>
                </td>
            <tr>
        </#if>

        <#if TOOL.xpath(hardware,"data/outdated")?exists>
            <tr>
                <td><span class="hardware caption">Zastaral�:</span></td>
                <td>ano</td>
            </tr>
        </#if>

        <#if TOOL.xpath(hardware,"data/price")?exists>
            <tr>
                <td><span class="hardware caption">Cena:</span></td>
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
        <h3 class="hardware caption">Technick� parametry</h3>
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
        <h3 class="hardware caption">Postup zprovozn�n� pod Linuxem</h3>
        <div>
            ${TOOL.render(TOOL.element(hardware.data,"data/setup"),USER?if_exists)}
        </div>
    </#if>

    <#if TOOL.xpath(hardware,"data/note")?exists>
        <h3 class="hardware caption">Pozn�mka</h3>
        <div>
            ${TOOL.render(TOOL.element(hardware.data,"data/note"),USER?if_exists)}
        </div>
    </#if>
</#macro>
