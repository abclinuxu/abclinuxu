<#import "../macros.ftl" as lib>
<#include "../header.ftl">

<@lib.showParents />

<#assign autor=TOOL.createUser(TOOL.xpath(ITEM,"/data/author"))>

<h1>${TOOL.xpath(ITEM,"/data/name")}</h1>

<div class="barva">
${DATE.show(ITEM.created,"CZ_FULL")} |
<a href="http://www.abclinuxu.cz/Profile/${autor.id}">${autor.name}</a>
</div>

<br clear="all">

<p class="perex">${TOOL.xpath(ITEM,"/data/perex")}</p>

${TOOL.render(TOOL.xpath(CHILDREN.record[0].child,"/data/content"),USER?if_exists)}


<#if CHILDREN.discussion?exists>
 <h1>Diskuse k tomuto èlánku</h1>
 <#assign DISCUSSION=CHILDREN.discussion[0].child>
 <#assign diz = TOOL.createDiscussionTree(DISCUSSION,"no",true)>
 <#list diz.threads as thread>
  <@lib.showThread thread, 0, DISCUSSION.id, CHILDREN.discussion[0].id, false />
 </#list>
</#if>

<#include "../footer.ftl">
