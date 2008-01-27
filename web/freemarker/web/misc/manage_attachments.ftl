<#include "../header.ftl">

<h1>Správa příloh</h1>

<@lib.showMessages/>

<form action="${URL.make("/inset/"+RELATION.id)}" method="POST" name="form">
    <#assign empty = true>
    <#list ATTACHMENTS?if_exists as rel>
        <#assign empty = false, item = rel.child, xml = TOOL.asNode(item.data), node = xml.data.object>
        <input type="checkbox" name="attachment" value="${rel.id}">
        <#if item.type == 1>
            <#if node.thumbnail[0]?exists>
                <#assign imageSrc = node.thumbnail[0].@path>
            <#else>
                <#assign imageSrc = node.@path>
            </#if>
            <a href="${node.@path}"><img src="${imageSrc}" border="0"></a>
        <#else>
            <a href="${node.@path}">${node.@path}</a>
        </#if>

        <#if item.subtype?exists>mime typ: ${item.subtype}</#if>
        <#if node.@originalFilename[0]?exists>původní název souboru: ${node.@originalFilename[0]}</#if>
        <#if node.@size[0]?exists>velikost: ${node.@size[0]}</#if>
        <br>
    </#list>
    <input type="hidden" name="action" value="remove">
    <#if ! empty>
        <input type="submit" value="Smazat">
    <#else>
        Není co spravovat - k dokumentu nebyly přidány žádné přílohy.
    </#if>
</form>
