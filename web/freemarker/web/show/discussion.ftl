<#include "../header.ftl">

<@lib.showMessages/>

<#if USER?exists && USER.hasRole("discussion admin")>
 <a href="/SelectRelation?prefix=/hardware&amp;url=/EditRelation&amp;action=move&amp;rid=${RELATION.id}">P�esunout</a>
 <a href="${URL.noPrefix("/EditRelation?action=remove&amp;rid="+RELATION.id+"&amp;prefix="+URL.prefix)}">Sma� diskusi</a>
</#if>

<p class="monitor"><b>AbcMonitor</b> v�m emailem za�le upozorn�n� p�i jak�koliv zm�n� v diskusi.
 <#if USER?exists && TOOL.xpath(ITEM,"//monitor/id[text()='"+USER.id+"'")?exists>
  <#assign monitorState="Vypni">
 <#else>
  <#assign monitorState="Zapni">
 </#if>
 <a href="${URL.make("/EditDiscussion?action=monitor&amp;rid="+RELATION.id)}">${monitorState}</a>
 (${TOOL.getMonitorCount(ITEM.data)})
</p>

<#assign frozen=TOOL.xpath(ITEM,"/data/frozen")?exists>

<#if TOOL.xpath(ITEM,"data/title")?exists>
 <h1 class="st_nadpis">Ot�zka</h1>
 <@lib.showComment TOOL.createComment(ITEM), ITEM.id, RELATION.id, !frozen />

 <p class="wrongForum">
 Tato ot�zka je v diskusn�m f�ru <a href="/forum/dir/${RELATION.upper}">${TOOL.childName(RELATION.upper)}</a>.
 Pokud ji tazatel za�adil �patn�,
 <a href="${URL.noPrefix("/clanky/EditRequest?action=chooseRightForum&amp;rid="+RELATION.id)}">informujte</a>
 pros�m administr�tory. D�kujeme.
 </p>

 <h1 class="st_nadpis">Odpov�di</h1>
<#elseif !frozen>
 <a href="${URL.make("/EditDiscussion?action=add&amp;threadId=0&amp;dizId="+ITEM.id+"&amp;rid="+RELATION.id)}">
 Vlo�it dal�� koment��</a>
</#if>

<#if frozen><p class="error">Diskuse byla administr�tory uzam�ena</p></#if>

<p><b>N�stroje</b>: <a href="/forum/show/${RELATION.id}?varianta=print">Tisk</a></p>

<#if USER?exists && USER.hasRole("discussion admin")>
 <a href="${URL.make("/EditDiscussion?action=freeze&amp;rid="+RELATION.id+"&amp;dizId="+ITEM.id)}">
 <#if frozen>Rozmrazit<#else>Zmrazit</#if> diskusi</a>
</#if>

<#if USER?exists><#assign MAX_COMMENT=TOOL.getLastSeenComment(ITEM,USER,true) in lib></#if>
<#list TOOL.createDiscussionTree(ITEM) as thread>
 <@lib.showThread thread, 0, ITEM.id, RELATION.id, !frozen />
</#list>

<#include "../footer.ftl">
