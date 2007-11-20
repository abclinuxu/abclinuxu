<#include "../header.ftl">

<@lib.showMessages/>

<h1>Seznam seriálů</h1>

<#if USER?exists && USER.hasRole("article admin")>
    <p><a href="${URL.make("/serialy/edit?action=add")}">Přidej seriál</a></p>
</#if>

<ul class="serialy">
<#list SERIES as series>
    <#assign desc = TOOL.xpath(series.child, "/data/description")?default("UNDEFINED"),
             total = TOOL.xpathValue(series.child.data, "count(//article)")>
    <li>
        <a href="${series.url}">${TOOL.childName(series)}</a> (${total?number})
        <#if desc != "UNDEFINED">
            <div class="meta-vypis">${desc}</div>
        </#if>
    </li>
</#list>
</ul>

<#include "../footer.ftl">
