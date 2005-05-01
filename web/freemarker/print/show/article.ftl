<#include "../header.ftl">

<#assign autor=TOOL.createUser(TOOL.xpath(ITEM,"/data/author"))>
<#assign forbidDiscussion=TOOL.xpath(ITEM, "//forbid_discussions")?default("UNDEF")>

<h1>${TOOL.xpath(ITEM,"/data/name")}</h1>

<div class="barva">
${DATE.show(ITEM.created,"CZ_FULL")} | <a href="/Profile/${autor.id}">${autor.name}</a>
</div>

<@lib.showParents PARENTS />

<#if USER?exists && USER.hasRole("article admin")>
 <p>
  <a href="${URL.make("/edit?action=edit&rid="+RELATION.id)}">Upravit</a>
  <a href="${URL.noPrefix("/SelectRelation?prefix=/clanky&url=/EditRelation&action=move&rid="+RELATION.id)}">Pøesunout</a>
  <a href="${URL.noPrefix("/EditRelation?action=remove&prefix=/clanky&rid="+RELATION.id)}">Smazat</a>
  <a href="${URL.make("/honorare/"+RELATION.id+"?action=add")}">Vlo¾it honoráø</a>
  <#if CHILDREN.royalties?exists>
   <#list CHILDREN.royalties as honorar>
    <a href="${URL.make("/honorare/"+honorar.id+"?action=edit")}">Upravit honoráø</a>
   </#list>
  </#if>
  <a href="${URL.make("/"+RELATION.id+".docb")}">Docbook</a>
 </p>
</#if>

<p class="perex">${TOOL.xpath(ITEM,"/data/perex")}</p>

${TOOL.render(TOOL.getCompleteArticleText(ITEM),USER?if_exists)}

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
  <h1 class="st_nadpis">Dal¹í èlánky z této rubriky</h1>
  <div class="st_linky">
    <#list SAME_SECTION_ARTICLES as relation>
      <a href="${relation.url?default("/clanky/show/"+relation.id)}">${TOOL.xpath(relation.child,"data/name")}</a><br>
    </#list>
  </div>
</div>

<#flush>

<#if ! PARAMS.noDiz?exists>
 <#if CHILDREN.discussion?exists>
  <h1>Diskuse k tomuto èlánku</h1>
  <#assign DISCUSSION=CHILDREN.discussion[0].child>
  <p>
   <a href="${URL.make("/EditDiscussion?action=add&dizId="+DISCUSSION.id+"&threadId=0&rid="+CHILDREN.discussion[0].id)}">
   Vlo¾it dal¹í komentáø</a>
  </p>
  <#assign frozen=TOOL.xpath(DISCUSSION,"/data/frozen")?exists>
  <#if USER?exists && USER.hasRole("discussion admin")>
   <a href="${URL.make("/EditDiscussion?action=freeze&amp;rid="+CHILDREN.discussion[0].id+"&amp;dizId="+DISCUSSION.id)}">
   <#if frozen>Rozmrazit<#else>Zmrazit</#if> diskusi</a>
  </#if>

  <#list TOOL.createDiscussionTree(DISCUSSION) as thread>
   <@lib.showThread thread, 0, DISCUSSION.id, CHILDREN.discussion[0].id, !frozen />
  </#list>
 <#elseif forbidDiscussion!="yes">
  <h1>Diskuse k tomuto èlánku</h1>
  <a href="${URL.make("/EditDiscussion?action=addDiz&rid="+RELATION.id)}">Vlo¾it první komentáø</a>
 </#if>
</#if>

<#include "../footer.ftl">
