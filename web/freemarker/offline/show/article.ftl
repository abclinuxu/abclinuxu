<#import "../macros.ftl" as lib>
<#include "../header.ftl">

<#assign autor=TOOL.createUser(TOOL.xpath(ITEM,"/data/author"))>

<h1>${TOOL.xpath(ITEM,"/data/name")}</h1>

<p class="cl_inforadek">
${DATE.show(ITEM.created,"CZ_FULL")} |
<a href="http://www.abclinuxu.cz/Profile/${autor.id}">${autor.name}</a></p>

<p class="cl_perex">${TOOL.xpath(ITEM,"/data/perex")}</p>

${TOOL.render(TOOL.getCompleteArticleText(ITEM),USER?if_exists)}

<div class="cl_perex">
  <#if RELATED?exists>
   <h3>Související èlánky</h3>
   <div class="s_sekce">
    <#list RELATED as link>
     <a href="${link.url}">${link.title}</a> ${link.description?if_exists}<br>
    </#list>
   </div>
  </#if>
  <#if RESOURCES?exists>
   <h3>Odkazy a zdroje</h3>
   <div class="s_sekce">
    <#list RESOURCES as link>
     <a href="${link.url}">${link.title}</a> ${link.description?if_exists}<br>
    </#list>
   </div>
  </#if>
</div>

<#if CHILDREN.discussion?exists>
    <#assign DISCUSSION=CHILDREN.discussion[0].child>
    <#assign diz = TOOL.createDiscussionTree(DISCUSSION,"no",true)>
    <#if (diz.threads?size>0) >
        <h2>Diskuse</h2>
        <#list diz.threads as thread>
            <@lib.showThread thread, 0, DISCUSSION.id, CHILDREN.discussion[0].id, false />
        </#list>
    </#if>
</#if>

<#include "../footer.ftl">
