<#include "../header.ftl">

<h1 class="st_nadpis">Zprávièka</h1>

<p>
 <#assign autor=TOOL.createUser(ITEM.owner)>
 <b>Autor:</b> <a href="/Profile/${autor.id}">${autor.name}</a><br>
 <#if CATEGORY?exists>
  <b>Kategorie:</b> ${CATEGORY.name}<br>
 </#if>
 <b>Datum:</b> ${DATE.show(ITEM.created,"CZ_FULL")}<br>
 <#if RELATION.upper=37672>
  <b>Stav:</b> èeká na schválení
  <#if USER?exists && USER.id=RELATION.child.owner>
   - <a href="${URL.make("/edit?action=edit&amp;rid="+RELATION.id)}">Upravit</a>
  </#if>
  <br>
 </#if>
 <#if USER?exists && USER.hasRole("news admin")>
  <#if TOOL.xpath(ITEM, "//locked_by")?exists>
   <#assign admin=TOOL.createUser(TOOL.xpath(ITEM, "//locked_by"))>
   Uzamknul <a href="/Profile/${admin.id}">${admin.name}</a> -
   <a href="${URL.make("/edit?action=unlock&amp;rid="+RELATION.id)}">odemknout</a>
  <#else>
   <a href="${URL.make("/edit?action=edit&amp;rid="+RELATION.id)}">Upravit</a>
   <#if RELATION.upper=37672><a href="${URL.make("/edit?action=approve&amp;rid="+RELATION.id)}">Schválit</a></#if>
   <a href="${URL.make("/edit?action=remove&amp;rid="+RELATION.id)}">Smazat</a>
   <a href="${URL.make("/edit?action=mail&amp;rid="+RELATION.id)}">Poslat email autorovi</a>
   <a href="${URL.make("/edit?action=lock&amp;rid="+RELATION.id)}">Zamknout</a>
  </#if>
  <br>
 </#if>
</p>

<p class="zpravicka">${TOOL.xpath(ITEM,"data/content")}</p>

<p><b>Nástroje</b>: <a href="/forum/show/${RELATION.id}?varianta=print">Tisk</a></p>

<h2>Komentáøe</h2>
<#if CHILDREN.discussion?exists>
 <#assign DISCUSSION=CHILDREN.discussion[0].child>

<p class="monitor"><b>AbcMonitor</b> vám emailem za¹le upozornìní pøi zmìnì.
 <#if USER?exists && TOOL.xpath(DISCUSSION,"//monitor/id[text()='"+USER.id+"'")?exists>
  <#assign monitorState="Vypni">
 <#else>
  <#assign monitorState="Zapni">
 </#if>
 <a href="${URL.make("/EditDiscussion?action=monitor&amp;rid="+CHILDREN.discussion[0].id)}">${monitorState}</a>
 (${TOOL.getMonitorCount(DISCUSSION.data)})
</p>

 <p>
  <a href="${URL.make("/EditDiscussion?action=add&amp;dizId="+DISCUSSION.id+"&amp;threadId=0&amp;rid="+CHILDREN.discussion[0].id)}">
  Vlo¾it dal¹í komentáø</a>
 </p>

 <#assign frozen=TOOL.xpath(DISCUSSION,"/data/frozen")?exists>
 <#if frozen>Diskuse byla administrátory uzamèena</#if>

 <#if USER?exists && USER.hasRole("discussion admin")>
  <a href="${URL.make("/EditDiscussion?action=freeze&amp;rid="+CHILDREN.discussion[0].id+"&amp;dizId="+DISCUSSION.id)}">
  <#if frozen>Rozmrazit<#else>Zmrazit</#if> diskusi</a>
 </#if>

 <#if USER?exists><#assign MAX_COMMENT=TOOL.getLastSeenComment(DISCUSSION,USER,true) in lib></#if>
 <#list TOOL.createDiscussionTree(DISCUSSION) as thread>
  <@lib.showThread thread, 0, DISCUSSION.id, CHILDREN.discussion[0].id, !frozen />
 </#list>
<#else>
 <a href="${URL.make("/EditDiscussion?action=addDiz&amp;rid="+RELATION.id)}">Vlo¾it první komentáø</a>
</#if>

<#include "../footer.ftl">
