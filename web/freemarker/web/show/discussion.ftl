<#assign frozen=TOOL.xpath(ITEM,"/data/frozen")?exists>
<#assign is_question=TOOL.xpath(ITEM,"data/title")?exists>
<#if USER?exists && TOOL.xpath(ITEM,"//monitor/id[text()='"+USER.id+"'")?exists>
    <#assign monitorState="P�esta� sledovat">
<#else>
    <#assign monitorState="Sleduj diskusi">
</#if>

<#assign plovouci_sloupec>

    <div class="s_nad_h1"><div class="s_nad_pod_h1">
        <h1>Funkce</h1>
    </div></div>

   <div class="s_sekce">
     <ul>
       <li><a href="/forum/show/${RELATION.id}?varianta=print">Tisk diskuse</a></li>
       <li><a href="${URL.make("/EditDiscussion?action=monitor&amp;rid="+RELATION.id)}">${monitorState}</a>
       (${TOOL.getMonitorCount(ITEM.data)})
       <a class="info" href="#">?<span class="tooltip">Za�le ka�d� nov� koment�� emailem na va�i adresu</span></a></li>
       <#if is_question>
           <li>
              <a href="${URL.make("/EditDiscussion?action=solved&amp;rid="+RELATION.id+"&amp;solved=true")}">Ot�zka byla vy�e�ena</a>
              (${TOOL.xpath(ITEM,"//solved/@yes")?default("0")})
              <a class="info" href="#">?<span class="tooltip">Pou�ijte, pokud probl�m polo�en� v ot�zce byl vy�e�en. Sm�te hlasovat jen jednou.</span></a></li>
           </li>
           <li>
              <a href="${URL.make("/EditDiscussion?action=solved&amp;rid="+RELATION.id+"&amp;solved=false")}">Ot�zka nebyla vy�e�ena</a>
              (${TOOL.xpath(ITEM,"//solved/@no")?default("0")})
              <a class="info" href="#">?<span class="tooltip">Pou�ijte, pokud probl�m polo�en� v ot�zce nebyl vy�e�en. Sm�te hlasovat jen jednou.</span></a></li>
           </li>
       </#if>
       <li><a href="/slovnik">Slovn�k pojm�</a></li>

      <#if USER?exists && (USER.hasRole("discussion admin") || USER.hasRole("move relation"))>
          <li><a href="/SelectRelation?prefix=/hardware&amp;url=/EditRelation&amp;action=move&amp;rid=${RELATION.id}">P�esunout</a></li>
      </#if>
      <#if USER?exists && USER.hasRole("discussion admin")>
          <li><a href="${URL.noPrefix("/EditRelation?action=remove&amp;rid="+RELATION.id+"&amp;prefix="+URL.prefix)}">Sma� diskusi</a></li>
          <li><a href="${URL.make("/EditDiscussion?action=freeze&amp;rid="+RELATION.id+"&amp;dizId="+ITEM.id)}"><#if frozen>Rozmrazit<#else>Zmrazit</#if> diskusi</a></li>
      </#if>
     </ul>
   </div>
</#assign>

<#include "../header.ftl">

<@lib.showMessages/>

<#assign diz = TOOL.createDiscussionTree(ITEM)>

<#if is_question>
 <h1 class="st_nadpis">Ot�zka</h1>
 <@lib.showComment TOOL.createComment(ITEM), ITEM.id, RELATION.id, !frozen />

 <p class="wrongForum">
 Tato ot�zka je v diskusn�m f�ru <a href="/forum/dir/${RELATION.upper}">${TOOL.childName(RELATION.upper)}</a>.
 Pokud ji tazatel za�adil �patn�,
 <a href="${URL.noPrefix("/clanky/EditRequest?action=chooseRightForum&amp;rid="+RELATION.id)}">informujte</a>
 pros�m administr�tory. D�kujeme.
 </p>

 <#if diz?size==0>
    <p>Na ot�zku zat�m nikdo bohu�el neodpov�d�l.</p>
 <#else>
     <h1 class="st_nadpis">Odpov�di</h1>
 </#if>
<#elseif !frozen>
 <a href="${URL.make("/EditDiscussion?action=add&amp;threadId=0&amp;dizId="+ITEM.id+"&amp;rid="+RELATION.id)}">
 Vlo�it dal�� koment��</a>
</#if>

<#if frozen><p class="error">Diskuse byla administr�tory uzam�ena</p></#if>

<#if USER?exists><#assign MAX_COMMENT=TOOL.getLastSeenComment(ITEM,USER,true) in lib></#if>
<#list diz as thread>
 <@lib.showThread thread, 0, ITEM.id, RELATION.id, !frozen />
</#list>

<#include "../footer.ftl">
