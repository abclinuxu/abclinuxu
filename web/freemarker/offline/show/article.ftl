<#include "../macros.ftl">
<#include "../header.ftl">

<#call showParents>

<#global autor=TOOL.createUser(TOOL.xpath(ITEM,"/data/author"))>

<h1>${TOOL.xpath(ITEM,"/data/name")}</h1>

<div class="barva">
${DATE.show(ITEM.created,"CZ_FULL")} |
<a href="http://www.abclinuxu.cz/Profile/${autor.id}">${autor.name}</a>
</div>

<br clear="all">

<p class="perex">${TOOL.xpath(ITEM,"/data/perex")}</p>

${TOOL.render(TOOL.xpath(CHILDREN.record[0].child,"/data/content"),USER?if_exists)}

<#if CHILDREN.discussion?exists && CHILDREN.discussion[0].child.children?size gt 0>
<h1>Diskuse k tomuto èlánku</h1>
 <#global DISCUSSION=CHILDREN.discussion[0].child>
 <#list TOOL.createDiscussionTree(DISCUSSION) as thread>
  <#call showThread(thread 0 DISCUSSION.id RELATION.id)>
 </#list>
</#if>

<#include "../footer.ftl">
