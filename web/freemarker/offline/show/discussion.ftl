<#include "/offline/macros.ftl">
<#call showParents>

<#if TOOL.xpath(ITEM,"data/title")?exists>
 <h1>Otázka</h1>
 <#call showComment(ITEM ITEM.id RELATION.id true)>
 <#call doubleSeparator()>
 <h1>Odpovìdi</h1>
</#if>

<#list TOOL.createDiscussionTree(ITEM) as thread>
 <#call showThread(thread 0 ITEM.id RELATION.id)>
</#list>
