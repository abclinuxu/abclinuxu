<#include "../macros.ftl">
<#include "../header.ftl">

<@showParents>

<#if TOOL.xpath(ITEM,"data/title")?exists>
 <h1>Ot�zka</h1>
 <@showComment(ITEM ITEM.id RELATION.id true)>
 <@doubleSeparator()>
 <h1>Odpov�di</h1>
</#if>

<#list TOOL.createDiscussionTree(ITEM) as thread>
 <@showThread(thread 0 ITEM.id RELATION.id)>
</#list>

<#include "../footer.ftl">
