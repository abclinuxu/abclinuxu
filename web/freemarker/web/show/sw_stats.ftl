<#include "../header.ftl">

<h1>Softwarové žebříčky</h1>

<h2>Deset nejnavštěvovanějších aplikací</h2>

<table class="sw-polozky">
    <tr>
        <th>Jméno</th>
        <th class="td-meta">Počet návštěv</th>
    </tr>
    <#assign visits = TOOL.getRelationCountersValue(TOP_VISITED, "visit")>
    <#list TOP_VISITED as sw>
        <tr>
            <td>
                <a href="${sw.url}">${TOOL.xpath(sw.child, "/data/name")}</a>
            </td>
            <td class="td-meta">
                <@lib.showCounter sw.child, visits, "visit" />&times;
            </td>
        </tr>
    </#list>
</table>

<h2>Deset nejpoužívanějších aplikací</h2>

<table class="sw-polozky">
    <tr>
        <th>Jméno</th>
        <th class="td-meta">Počet uživatelů</th>
    </tr>
    <#list TOP_USED as sw>
        <tr>
            <td>
                <a href="${sw.url}">${TOOL.xpath(sw.child, "/data/name")}</a>
            </td>
            <td class="td-meta">
                <a href="${sw.url}?action=users">${sw.child.getProperty("used_by")?size}</a>
            </td>
        </tr>
    </#list>
</table>

<h2>Deset nejnověji přidaných aplikací</h2>

<table class="sw-polozky">
    <tr>
        <th>Jméno</th>
        <th>Datum</th>
    </tr>
    <#list LAST_ADDED as sw>
        <tr>
            <td>
                <a href="${sw.url}">${TOOL.xpath(sw.child, "/data/name")}</a>
            </td>
            <td class="td-datum">
                ${DATE.show(sw.child.created, "SMART")}
            </td>
        </tr>
    </#list>
</table>

<h2>Deset naposledy upravených aplikací</h2>

<table class="sw-polozky">
    <tr>
        <th>Jméno</th>
        <th class="td-datum">Datum</th>
    </tr>
    <#list LAST_UPDATED as sw>
        <tr>
            <td>
                <a href="${sw.url}">${TOOL.xpath(sw.child, "/data/name")}</a>
            </td>
            <td class="td-datum">
                ${DATE.show(sw.child.updated, "SMART")}
            </td>
        </tr>
    </#list>
</table>

<#include "../footer.ftl">
