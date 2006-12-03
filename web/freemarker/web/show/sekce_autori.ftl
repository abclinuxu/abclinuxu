<#include "../header.ftl">

<@lib.showMessages/>

<h1>Auto�i</h1>
<#if USER?exists && USER.hasRole("article admin")>
    <a href="${URL.noPrefix("/autori/edit?action=add")}">P�idat</a>
</#if>

<table>
    <tr>
        <td>Jm�no</td>
        <td>Po�et �l�nk�</td>
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
