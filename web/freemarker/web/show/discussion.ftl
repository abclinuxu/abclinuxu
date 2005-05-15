<#assign frozen=TOOL.xpath(ITEM,"/data/frozen")?exists>
<#assign is_question=TOOL.xpath(ITEM,"data/title")?exists>
<#if USER?exists && TOOL.xpath(ITEM,"//monitor/id[text()='"+USER.id+"']")?exists>
    <#assign monitorState="Pøestaò sledovat">
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
       <a class="info" href="#">?<span class="tooltip">Za¹le ka¾dý nový komentáø emailem na va¹i adresu</span></a></li>
       <#if is_question>
           <li>
              <a href="${URL.make("/EditDiscussion?action=solved&amp;rid="+RELATION.id+"&amp;solved=true")}">Otázka byla vyøe¹ena</a>
              (${TOOL.xpath(ITEM,"//solved/@yes")?default("0")})
              <a class="info" href="#">?<span class="tooltip">Pou¾ijte, pokud problém polo¾ený v otázce byl vyøe¹en. Smíte hlasovat jen jednou.</span></a></li>
           </li>
           <li>
              <a href="${URL.make("/EditDiscussion?action=solved&amp;rid="+RELATION.id+"&amp;solved=false")}">Otázka nebyla vyøe¹ena</a>
              (${TOOL.xpath(ITEM,"//solved/@no")?default("0")})
              <a class="info" href="#">?<span class="tooltip">Pou¾ijte, pokud problém polo¾ený v otázce nebyl vyøe¹en. Smíte hlasovat jen jednou.</span></a></li>
           </li>
       </#if>
       <li><a href="/slovnik">Slovník pojmù</a></li>

      <#if USER?exists && (USER.hasRole("discussion admin") || USER.hasRole("move relation"))>
          <li><a href="/SelectRelation?prefix=/hardware&amp;url=/EditRelation&amp;action=move&amp;rid=${RELATION.id}">Pøesunout</a></li>
      </#if>
      <#if USER?exists && USER.hasRole("discussion admin")>
          <li><a href="${URL.noPrefix("/EditRelation?action=remove&amp;rid="+RELATION.id+"&amp;prefix="+URL.prefix)}">Sma¾ diskusi</a></li>
          <li><a href="${URL.make("/EditDiscussion?action=freeze&amp;rid="+RELATION.id+"&amp;dizId="+ITEM.id)}"><#if frozen>Rozmrazit<#else>Zmrazit</#if> diskusi</a></li>
      </#if>
     </ul>
   </div>
</#assign>

<#include "../header.ftl">

<@lib.showMessages/>

<#assign diz = TOOL.createDiscussionTree(ITEM)>

<#if is_question>
 <h1 class="st_nadpis">Otázka</h1>
 <@lib.showThread TOOL.createComment(ITEM), ITEM.id, RELATION.id, 0, !frozen />

 <p class="wrongForum">
 Tato otázka je v diskusním fóru <a href="/forum/dir/${RELATION.upper}">${TOOL.childName(RELATION.upper)}</a>.
 Pokud ji tazatel zaøadil ¹patnì,
 <a href="${URL.noPrefix("/clanky/EditRequest?action=chooseRightForum&amp;rid="+RELATION.id)}">informujte</a>
 prosím administrátory. Dìkujeme.
 </p>

 <#if diz?size==0>
    <p>Na otázku zatím nikdo bohu¾el neodpovìdìl.</p>
 <#else>
     <h1 class="st_nadpis">Odpovìdi</h1>
 </#if>
<#elseif !frozen>
 <a href="${URL.make("/EditDiscussion?action=add&amp;threadId=0&amp;dizId="+ITEM.id+"&amp;rid="+RELATION.id)}">
 Vlo¾it dal¹í komentáø</a>
</#if>

<#if frozen><p class="error">Diskuse byla administrátory uzamèena</p></#if>

<#if USER?exists><#assign MAX_COMMENT=TOOL.getLastSeenComment(ITEM,USER,true) in lib></#if>
<#list diz as thread>
 <@lib.showThread thread, 0, ITEM.id, RELATION.id, !frozen />
</#list>

<#include "../footer.ftl">
