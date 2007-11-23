<#if USER?exists && TOOL.xpath(CATEGORY,"//monitor/id[text()='"+USER.id+"']")?exists>
    <#assign monitorState="Přestaň sledovat"><#else><#assign monitorState="Sleduj sekci">
</#if>
<#assign plovouci_sloupec>
    <div class="s_sekce">
        <ul>
            <li>
                <a href="${URL.make("/edit?action=add")}">Vložit nový ovladač</a>
            </li>
            <li>
                <a href="${URL.make("/EditMonitor/"+RELATION.id+"?action=toggle"+TOOL.ticket(USER?if_exists, false))}">${monitorState}</a>
                <span title="Počet lidí, kteří sledují tuto sekci">(${TOOL.getMonitorCount(CATEGORY.data)})</span>
                <a class="info" href="#">?<span class="tooltip">Zašle upozornění na váš email při nové položce v této a v podřazených sekcích.</span></a>
            </li>
        </ul>
    </div>
</#assign>

<#include "../header.ftl">
<@lib.showMessages/>

<h1>Ovladače</h1>

<p>
    Databáze Ovladačů slouží pro shromažďování informací ohledně ovladačů,
    které nejsou standardní součástí jádra. Typicky jde buď o Open Source
    projekty, v rámci kterých se nadšenci snaží vytvořit podporu pro daný
    hardware nebo obvykle proprietární binární ovladače přímo od výrobce.
</p>

<#assign sorted=SORT.byName(CHILDREN,"ASCENDING")>
<#list CATEGORIES as category>
    <#assign wrote_hdr=false>
    <#list sorted as relation>
        <#if relation.child.getSubType()?default("NONE") == category.key>
            <#if !wrote_hdr>
                <h3>${category.name}</h3>
                <table class="siroka">
                    <tr>
                        <th width="40%" align="left">Název</th>
                        <th width="40%" align="left">Verze</th>
                        <th width="20%" align="right">Poslední úprava</th>
                    </tr>
                <#assign wrote_hdr=true>
            </#if>
            <tr>
                <td>
                    <a href="${relation.url}">${TOOL.xpath(relation.child,"data/name")}</a>
                </td>
                <td>${TOOL.xpath(relation.child,"data/version")}</td>
                <td align="right">${DATE.show(relation.child.updated, "SMART")}</td>
            </tr>
        </#if>
    </#list>
    <#if wrote_hdr></table></#if>
</#list>

<#include "../footer.ftl">
