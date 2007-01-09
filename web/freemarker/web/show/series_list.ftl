<#include "../header.ftl">

<@lib.showMessages/>

<h1>Seznam seri�l�</h1>

<#if USER?exists && USER.hasRole("article admin")>
    <p>
        <a href="${URL.make("/serialy/edit?action=add")}">P�idej seri�l</a>
    </p>
</#if>

<#list SERIES as series>
    <h3><a href="${series.url}">${TOOL.childName(series)}</a></h3>
</#list>


<#include "../footer.ftl">
