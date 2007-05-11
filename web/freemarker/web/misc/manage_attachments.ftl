<#include "../header.ftl">

<h1>Správa příloh</h1>

<@lib.showMessages/>

<form action="${URL.make("/inset/"+RELATION.id)}" method="POST" name="form">
    <#assign empty = true>
    <#list XML.data.inset.images.image as node>
        <#assign empty = false>
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
        <#-- příloha <a href="${node}">${node}</a> -->
        <br>
    </#list>
    <input type="hidden" name="action" value="remove">
    <#if ! empty>
        <input type="submit" value="Smazat">
    <#else>
        Není co spravovat - k dokumentu nebyly přidány žádné přílohy.
    </#if>
</form>
