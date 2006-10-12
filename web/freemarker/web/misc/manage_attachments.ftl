<#include "../header.ftl">

<h1>Spr�va p��loh</h1>

<@lib.showMessages/>

<form action="${URL.make("/inset/"+RELATION.id)}" method="POST" name="form">
    <#list XML.data.inset.images.image as node>
        <label>
            <input type="checkbox" name="attachment" value="${node}">
            obr�zek
        </label>
        <#if node.@thumbnail[0]?exists>
            <#assign imageSrc = node.@thumbnail>
        <#else>
            <#assign imageSrc = node>
        </#if>
        <a href="${node}"><img src="${imageSrc}" border="0"></a>
        <#-- p��loha <a href="${node}">${node}</a> -->
        <br>
    </#list>
    <input type="hidden" name="action" value="remove">
    <input type="submit" value="Smazat">
</form>
