<#import "../macros.ftl" as lib>
<#assign plovouci_sloupec>

    <@lib.advertisement id="hypertext2nahore" />

    <div class="s_nadpis">Nástroje</div>
    <div class="s_sekce">
        <ul>
            <li>
                <a href="${URL.make("/edit?action=add")}">Vložit nový ovladač</a>
            </li>
            <li>
                <@lib.showMonitor RELATION />
            </li>
        </ul>
    </div>

    <@lib.advertisement id="square" />
    <@lib.advertisement id="hypertext2dole" />

</#assign>

<#include "../header.ftl">
<@lib.showMessages/>

<h1>Ovladače</h1>

<p>Databáze Ovladačů slouží pro shromažďování informací ohledně ovladačů, které nejsou standardní součástí jádra. Typicky jde buď o Open Source projekty, v rámci kterých se nadšenci snaží vytvořit podporu pro daný hardware nebo obvykle proprietární binární ovladače přímo od výrobce.</p>

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
                    <a href="${relation.url}">${relation.child.title}</a>
                </td>
                <td>${TOOL.xpath(relation.child,"data/version")}</td>
                <td align="right">${DATE.show(relation.child.updated, "SMART")}</td>
            </tr>
        </#if>
    </#list>
    <#if wrote_hdr></table></#if>
</#list>

<#include "../footer.ftl">
