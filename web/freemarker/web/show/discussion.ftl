<#include "../header.ftl">

<@lib.showParents PARENTS />

<@lib.showMessages/>

<#if USER?exists && USER.hasRole("discussion admin")>
 <a href="/SelectRelation?prefix=/hardware&url=/EditRelation&action=move&rid=${RELATION.id}">Pøesunout</a>
 <a href="${URL.noPrefix("/EditRelation?action=remove&rid="+RELATION.id+"&prefix="+URL.prefix)}">Sma¾ diskusi</a>
</#if>

<p class="monitor"><b>AbcMonitor</b> vám emailem za¹le upozornìní pøi jakékoliv zmìnì v diskusi.
 <#if USER?exists && TOOL.xpath(ITEM,"//monitor/id[text()='"+USER.id+"'")?exists>
  <#assign monitorState="Vypni">
 <#else>
  <#assign monitorState="Zapni">
 </#if>
 <a href="${URL.make("/EditDiscussion?action=monitor&rid="+RELATION.id)}">${monitorState}</a>
 (${TOOL.getMonitorCount(ITEM.data)})
</p>

<#assign frozen=TOOL.xpath(ITEM,"/data/frozen")?exists>

<#if TOOL.xpath(ITEM,"data/title")?exists>
 <h1>Otázka</h1>
 <@lib.showComment TOOL.createComment(ITEM), ITEM.id, RELATION.id, !frozen />

 <p class="wrongForum">
 Tato otázka je v diskusním fóru <a href="/forum/dir/${RELATION.upper}">${TOOL.childName(RELATION.upper)}</a>.
 Pokud ji tazatel zaøadil ¹patnì,
 <a href="${URL.noPrefix("/clanky/EditRequest?action=chooseRightForum&amp;rid="+RELATION.id)}">informujte</a>
 prosím administrátory. Dìkujeme.
 </p>

 <#include "/include/linuxplus.txt">
 <h1>Odpovìdi</h1>
<#elseif !frozen>
 <a href="${URL.make("/EditDiscussion?action=add&threadId=0&dizId="+ITEM.id+"&rid="+RELATION.id)}">
 Vlo¾it dal¹í komentáø</a>
</#if>

<#if frozen><p>Diskuse byla administrátory uzamèena</p></#if>

<p><b>Nástroje</b>: <a href="/forum/show/${RELATION.id}?varianta=print">Tisk</a></p>

<#if USER?exists && USER.hasRole("discussion admin")>
 <a href="${URL.make("/EditDiscussion?action=freeze&amp;rid="+RELATION.id+"&amp;dizId="+ITEM.id)}">
 <#if frozen>Rozmrazit<#else>Zmrazit</#if> diskusi</a>
</#if>

<#list TOOL.createDiscussionTree(ITEM) as thread>
 <@lib.showThread thread, 0, ITEM.id, RELATION.id, !frozen />
</#list>

<#include "../footer.ftl">
