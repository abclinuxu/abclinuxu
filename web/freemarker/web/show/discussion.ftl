<#assign frozen=TOOL.xpath(ITEM,"/data/frozen")?exists>
<#if USER?exists && TOOL.xpath(ITEM,"//monitor/id[text()='"+USER.id+"'")?exists>
    <#assign monitorState="vypni">
<#else>
    <#assign monitorState="zapni">
</#if>

<#assign plovouci_sloupec>

    <div class="s_nad_h1"><div class="s_nad_pod_h1">
        <h1>Funkce</h1>
    </div></div>

   <div class="s_sekce">
    <a href="/forum/show/${RELATION.id}?varianta=print">Tisk</a><br>
    Sledování <a href="${URL.make("/EditDiscussion?action=monitor&amp;rid="+RELATION.id)}">${monitorState}</a>
    (${TOOL.getMonitorCount(ITEM.data)})
    <a class="info" href="#">?<span class="tooltip">Za¹le ka¾dý nový komentáø emailem na va¹i adresu</span></a><br>

    <#if USER?exists && USER.hasRole("discussion admin")>
        <a href="/SelectRelation?prefix=/hardware&amp;url=/EditRelation&amp;action=move&amp;rid=${RELATION.id}">Pøesunout</a><br>
        <a href="${URL.noPrefix("/EditRelation?action=remove&amp;rid="+RELATION.id+"&amp;prefix="+URL.prefix)}">Sma¾ diskusi</a><br>
        <a href="${URL.make("/EditDiscussion?action=freeze&amp;rid="+RELATION.id+"&amp;dizId="+ITEM.id)}">
        <#if frozen>Rozmrazit<#else>Zmrazit</#if> diskusi</a><br>
    </#if>
   </div>
</#assign>

<#include "../header.ftl">

<@lib.showMessages/>


<#if TOOL.xpath(ITEM,"data/title")?exists>
 <h1 class="st_nadpis">Otázka</h1>
 <@lib.showComment TOOL.createComment(ITEM), ITEM.id, RELATION.id, !frozen />

 <p class="wrongForum">
 Tato otázka je v diskusním fóru <a href="/forum/dir/${RELATION.upper}">${TOOL.childName(RELATION.upper)}</a>.
 Pokud ji tazatel zaøadil ¹patnì,
 <a href="${URL.noPrefix("/clanky/EditRequest?action=chooseRightForum&amp;rid="+RELATION.id)}">informujte</a>
 prosím administrátory. Dìkujeme.
 </p>

 <h1 class="st_nadpis">Odpovìdi</h1>
<#elseif !frozen>
 <a href="${URL.make("/EditDiscussion?action=add&amp;threadId=0&amp;dizId="+ITEM.id+"&amp;rid="+RELATION.id)}">
 Vlo¾it dal¹í komentáø</a>
</#if>

<#if frozen><p class="error">Diskuse byla administrátory uzamèena</p></#if>

<#if USER?exists><#assign MAX_COMMENT=TOOL.getLastSeenComment(ITEM,USER,true) in lib></#if>
<#list TOOL.createDiscussionTree(ITEM) as thread>
 <@lib.showThread thread, 0, ITEM.id, RELATION.id, !frozen />
</#list>

<#include "../footer.ftl">
