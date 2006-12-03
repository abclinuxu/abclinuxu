<#include "../header.ftl">

<@lib.showMessages/>

<h1>Autoøi</h1>
<#if USER?exists && USER.hasRole("article admin")>
    <a href="${URL.noPrefix("/autori/edit?action=add")}">Pøidat</a>
</#if>

<table>
    <tr>
        <td>Jméno</td>
        <td>Poèet èlánkù</td>
    </tr>
    <#list SORT.byName(AUTHORS) as author>
        <tr>
            <td>
                <a href="${author.url}">${TOOL.childName(author)}</a>
            </td>
            <td align="right">
                <a href="${author.url}">${COUNTS.get(author.id)?default(0)}</a>
            </td>
        </tr>
    </#list>
</table>


<#include "../footer.ftl">
