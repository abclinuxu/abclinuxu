<#include "../header.ftl">

<div id="articleHead">

	<form name="print" method="post" action="/clanky/show/${RELATION.id}?varianta=print&noDiz">
	  <input type="submit" name="Submit" value="Tisk bez diskuse" class="buton">
	</form>

	<form name="print" method="post" action="/clanky/show/${RELATION.id}?varianta=print">
	  <input type="submit" name="Submit" value="Tisk" class="buton">
	</form>

<#if CHILDREN.discussion?exists>
 <#assign DISCUSSION=CHILDREN.discussion[0].child>

  <#if USER?exists && TOOL.xpath(DISCUSSION,"//monitor/id[text()='"+USER.id+"'")?exists>
   <#assign monitorState="Nesledovat">
  <#else>
   <#assign monitorState="Sledovat">
  </#if>

	<form name="monitor" method="post" action="/EditDiscussion">
	  <input type="submit" name="Submit" value="${monitorState + " (" + TOOL.getMonitorCount(DISCUSSION.data)})" class="buton">
	  <input name="action" type="hidden" value="monitor">
	  <input name="rid" type="hidden" value="${CHILDREN.discussion[0].id}">
	</form>
</#if>

</div>

<h1 class="uvod">${TOOL.xpath(ITEM,"/data/name")}</h1>

<span class="uvod">
<#assign autor=TOOL.createUser(TOOL.xpath(ITEM,"/data/author"))>
${DATE.show(ITEM.created,"CZ_FULL")} | <a href="/Profile/${autor.id}">${autor.name}</a>
</span>

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
   <br><br>
   </div>
  </#if>
 </div>
</#if>

<#macro starLink rid rvalue rtype value>
<a href="${URL.make("/rating/"+rid+"?action=rate&amp;rtype="+rtype+"&amp;rvalue="+rvalue)}" target="rating" title="Va¹e hodnocení: <#list 1..rvalue as x>*</#list>"><@lib.star value /></a>
</#macro>
<#assign rating=TOOL.ratingFor(ITEM.data,"article")?default(0)>
<p class="rating">Hodnocení:
 <a href="${URL.make("/rating/"+RELATION.id+"?action=rate&amp;rtype=article&amp;rvalue=0")}" target="rating" title="Va¹e hodnocení: bída"><img src="/images/site2/rating/star_ble.gif" alt=":-(" class="star" align="absmiddle"></a>
 <@starLink RELATION.id, 1, "article", rating />
 <@starLink RELATION.id, 2, "article", rating-1 />
 <@starLink RELATION.id, 3, "article", rating-2 />
 (Stav: <#if rating!=0>${rating?string["#0.00"]} Poèet hlasù: ${TOOL.xpath(ITEM,"//rating[type/text()='article']/count")}
 <#else>bez hodnocení</#if>)
 <iframe name="rating" width="300" frameborder="0" height="15" scrolling="no" class="rating"></iframe>
</p>

<#flush>

<#if CHILDREN.discussion?exists>
 <h1>Diskuse k tomuto èlánku</h1>
 <#assign DISCUSSION=CHILDREN.discussion[0].child>

 <p>
  <a href="${URL.make("/EditDiscussion?action=add&dizId="+DISCUSSION.id+"&threadId=0&rid="+CHILDREN.discussion[0].id)}">
  Vlo¾it dal¹í komentáø</a>
 </p>

 <#assign frozen=TOOL.xpath(DISCUSSION,"/data/frozen")?exists>
 <#if frozen>Diskuse byla administrátory uzamèena</#if>

 <#if USER?exists && USER.hasRole("discussion admin")>
  <a href="${URL.make("/EditDiscussion?action=freeze&amp;rid="+CHILDREN.discussion[0].id+"&amp;dizId="+DISCUSSION.id)}">
  <#if frozen>Rozmrazit<#else>Zmrazit</#if> diskusi</a>
 </#if>

 <#list TOOL.createDiscussionTree(DISCUSSION) as thread>
  <@lib.showThread thread, 0, DISCUSSION.id, CHILDREN.discussion[0].id, !frozen />
 </#list>
<#elseif ALLOW_DISCUSSIONS>
 <h1>Diskuse k tomuto èlánku</h1>
 <a href="${URL.make("/EditDiscussion?action=addDiz&rid="+RELATION.id)}">Vlo¾it první komentáø</a>
</#if>

<#include "../footer.ftl">
