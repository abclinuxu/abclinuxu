<#include "/include/macros.ftl">
<#include "../header.ftl">

<#call showParents>

<#global autor=TOOL.createUser(TOOL.xpath(ITEM,"/data/author"))>

<h1>${TOOL.xpath(ITEM,"/data/name")}</h1>

<div class="barva">
${DATE.show(ITEM.created,"CZ_FULL")} | <a href="/Profile?userId=${autor.id}">${autor.name}</a>
</div>

<#if USER?exists && USER.isAdmin()>
 <p>
  <a href="${URL.make("/EditItem?action=edit&relationId="+RELATION.id)}" title="Uprav">
  <img src="/images/actions/pencil.png" class="ikona22" alt="Uprav"></a>
  <a href="${URL.noPrefix("/SelectRelation?prefix=/clanky&url=/EditRelation&action=move&relationId="+RELATION.id)}" title="Pøesunout">
  <img src="/images/actions/cut.png" alt="Pøesunout" class="ikona"></a>
  <a href="${URL.noPrefix("/EditRelation?action=remove&prefix=/clanky&relationId="+RELATION.id)}" title="Sma¾">
  <img src="/images/actions/delete.png" alt="Sma¾" class="ikona"></a>
 </p>
</#if>

<p class="perex">${TOOL.xpath(ITEM,"/data/perex")}</p>

${TOOL.render(TOOL.getCompleteArticleText(ITEM))}

<#if PAGES?exists>
 <div class="perex">
  <h1>Jednotlivé podstránky èlánku</h1>
  <ol>
  <#list PAGES as page><li>
   <#if page_index==PAGE>
    ${page}
   <#else>
    <a href="/clanky/ViewRelation?relationId=${RELATION.id}&page=${page_index}&varianta=print">${page}</a>
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
     <a href="${link.url}">${link.title}</a><br>
    </#list>
   </div>
  </#if>
  <#if RESOURCES?exists>
  <h1>Odkazy a zdroje</h1>
   <div class="linky">
    <#list RESOURCES as link>
     <a href="${link.url}">${link.title}</a><br>
    </#list>
   </div>
  </#if>
 </div>
</#if>

<#flush>

<#if CHILDREN.discussion?exists>
 <h1>Diskuse k tomuto èlánku</h1>
 <#global DISCUSSION=CHILDREN.discussion[0].child>
 <p>
  <a href="${URL.make("/EditDiscussion?action=add&dizId="+DISCUSSION.id+"&threadId=0&relationId="+CHILDREN.discussion[0].id)}">
  Vlo¾it dal¹í komentáø</a>
 </p>
 <#list TOOL.createDiscussionTree(DISCUSSION) as thread>
  <#call showThread(thread 0 DISCUSSION.id CHILDREN.discussion[0].id)>
 </#list>
<#elseif ALLOW_DISCUSSIONS>
 <h1>Diskuse k tomuto èlánku</h1>
 <a href="${URL.make("/EditDiscussion?action=addDiz&relationId="+RELATION.id)}">Vlo¾it první komentáø</a>
</#if>

<#include "../footer.ftl">
