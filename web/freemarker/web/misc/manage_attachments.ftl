<#include "../header.ftl">

<h1>Správa pøíloh</h1>

<@lib.showMessages/>

<form action="${URL.make("/inset/"+RELATION.id)}" method="POST" name="form">
    <#list XML.data.inset.images.image as node>
        <label>
            <input type="checkbox" name="attachment" value="${node}">
            obrázek
        </label>
        <#if node.@thumbnail[0]?exists>
            <#assign imageSrc = node.@thumbnail>
        <#else>
            <#assign imageSrc = node>
        </#if>
        <a href="${node}"><img src="${imageSrc}" border="0"></a>
        <#-- pøíloha <a href="${node}">${node}</a> -->
        <br>
    </#list>
    <input type="hidden" name="action" value="remove">
    <input type="submit" value="Smazat">
</form>
