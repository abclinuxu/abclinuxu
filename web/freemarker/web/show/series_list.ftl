<#include "../header.ftl">

<@lib.showMessages/>

<h1>Seznam seri�l�</h1>

<#if USER?exists && USER.hasRole("article admin")>
    <p>
        <a href="${URL.make("/serialy/edit?action=add")}">P�idej seri�l</a>
    </p>
</#if>

<#list SERIES as series>
    <#assign desc = TOOL.xpath(series.child, "/data/description")?default("UNDEFINED"),
             total = TOOL.xpathValue(series.child.data, "count(//article)")>
    <h3>
        <a href="${series.url}">${TOOL.childName(series)}</a>
         (d�l�: ${total})
    </h3>
    <#if desc != "UNDEFINED">
        <div>${desc}</div>
    </#if>
</#list>


<#include "../footer.ftl">
