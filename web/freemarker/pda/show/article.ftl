<#include "../header.ftl">

<h1>${TOOL.xpath(ITEM,"/data/name")}</h1>

<div class="barva">
<#assign autor=TOOL.createUser(TOOL.xpath(ITEM,"/data/author"))>
${DATE.show(ITEM.created,"CZ_FULL")} | ${autor.name}
</div>

<#if ( PAGE?default(0) == 0) >
 <p class="perex">${TOOL.xpath(ITEM,"/data/perex")}</p>
</#if>

${TOOL.render(TEXT,USER?if_exists)}

<#if PAGES?exists>
 <div class="perex">
  <h1>Jednotlivé podstránky èlánku</h1>
  <ol>
  <#list PAGES as page><li>
   <#if page_index==PAGE>
    ${page}
   <#else>
    <a href="/clanky/show/${RELATION.id}?page=${page_index}">${page}</a>
   </#if>
  </#list>
  </ol>
 </div>
</#if>

<#if RELATED?exists || RESOURCES?exists>
 <div class="perex">
  <#if RELATED?exists>
   <h1>Související èlánky</h1>
   <div class="linky">
    <#list RELATED as link>
     <a href="${link.url}">${link.title}</a> ${link.description?if_exists}<br>
    </#list>
   </div>
  </#if>
  <#if RESOURCES?exists>
  <h1>Odkazy a zdroje</h1>
   <div class="linky">
    <#list RESOURCES as link>
     <a href="${link.url}">${link.title}</a> ${link.description?if_exists}<br>
    </#list>
   </div>
  </#if>
 </div>
</#if>

<#flush>

<#if CHILDREN.discussion?exists>
 <h1>Diskuse k tomuto èlánku</h1>
 <#assign DISCUSSION=CHILDREN.discussion[0].child, frozen=true>
 <#list TOOL.createDiscussionTree(DISCUSSION) as thread>
  <@lib.showThread thread, 0 />
 </#list>
</#if>

<#include "../footer.ftl">
