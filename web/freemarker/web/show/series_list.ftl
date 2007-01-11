<#include "../header.ftl">

<@lib.showMessages/>

<h1>Seznam seriálù</h1>

<#if USER?exists && USER.hasRole("article admin")>
    <p>
        <a href="${URL.make("/serialy/edit?action=add")}">Pøidej seriál</a>
    </p>
</#if>

<#list SERIES as series>
    <#assign desc = TOOL.xpath(series.child, "/data/description")?default("UNDEFINED"),
             total = TOOL.xpathValue(series.child.data, "count(//article)")>
    <h3>
        <a href="${series.url}">${TOOL.childName(series)}</a>
         (dílù: ${total})
    </h3>
    <#if desc != "UNDEFINED">
        <div>${desc}</div>
    </#if>
</#list>


<#include "../footer.ftl">
