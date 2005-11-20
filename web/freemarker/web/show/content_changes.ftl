<#include "../header.ftl">

<h1>P�ehled str�nek</h1>

<p>Na t�to str�nce najdete p�ehledn� seznam dokument�,
jejich adres a datumu posledn� �pravy. </p>

<#if ! DATA?has_content>
    <p>
        Pod touto str�nkou nejsou ��dn� podstr�nky.
    </p>
<#else>
    <table width="100%">
        <tr>
            <th align="center"><a href="<@sortUrl column="url"/>">URL</a></th>
            <th align="right"><a href="<@sortUrl column="date"/>">Posledn� zm�na</a></th>
            <th align="right"><a href="<@sortUrl column="size"/>">Znak�</a></th>
            <th align="left"><a href="<@sortUrl column="user"/>">Autor</th>
        </tr>
        <#list DATA as content>
            <tr>
                <td>
                    <a href="${content.url}" title="${content.url}">${TOOL.reverseLimit(content.url,50,"..")}</a>
                </td>
                <td align="right">
                    ${DATE.show(content.updated, "CZ_FULL")}
                </td>
                <td align="right">
                    ${content.size}
                </td>
                <td>
                    <a href="/Profile/${content.user.id}">${content.userName}</a>
                </td>
            </tr>
        </#list>
    </table>
</#if>

<#macro sortUrl column>
    <#assign url = "/zmeny/"+RELATION.id+"?sortBy="+column+"&amp;order=">
    <#if column==COLUMN>
        <#if ORDER_DESC><#assign url = url + "asc"><#else><#assign url = url + "desc"></#if>
    <#else>
        <#assign url = url + "asc">
    </#if>
    ${URL.make(url)}
</#macro>

<#include "../footer.ftl">
