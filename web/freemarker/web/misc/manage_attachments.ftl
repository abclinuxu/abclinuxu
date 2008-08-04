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
        <#if node.@hidden[0]?default("false")=="true">skrytá</#if>
        <br>
    </#list>
    <input type="hidden" name="action" value="manage2">
    <#if PARAMS.threadId?exists>
        <input type="hidden" name="threadId" value="${PARAMS.threadId}">
    </#if>
    <#if ! empty>
        <input type="submit" name="remove" value="Smazat">
        <input type="submit" name="setVisible" value="Nastavit jako viditelné">
        <input type="submit" name="setHidden" value="Nastavit jako skryté">
    <#else>
        Není co spravovat - k dokumentu nebyly přidány žádné přílohy.
    </#if>
</form>
