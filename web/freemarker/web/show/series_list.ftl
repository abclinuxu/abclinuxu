<#include "../header.ftl">

<@lib.showMessages/>

<h1>Seznam seri�l�</h1>

<#if USER?exists && USER.hasRole("article admin")>
    <p><a href="${URL.make("/serialy/edit?action=add")}">P�idej seri�l</a></p>
</#if>

<ul>
<#list SERIES as series>
    <#assign desc = TOOL.xpath(series.child, "/data/description")?default("UNDEFINED"),
             total = TOOL.xpathValue(series.child.data, "count(//article)")>
    <li><a href="${series.url}">${TOOL.childName(series)}</a> (d�l�:&nbsp;${total})
    <#if desc != "UNDEFINED">
        <div class="serialy-vypis">${desc}</div>
    </#if></li>
</#list>
</ul>

<#include "../footer.ftl">
