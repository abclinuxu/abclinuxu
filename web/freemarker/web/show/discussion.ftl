<#assign DIZ = TOOL.createDiscussionTree(ITEM,USER?if_exists,RELATION.id,true)>
<#assign is_question=TOOL.xpath(ITEM,"data/title")?exists>
<#if DIZ.monitored>
    <#assign monitorState="Pøestaò sledovat"><#else><#assign monitorState="Sleduj">
</#if>

<#assign plovouci_sloupec>
    <div class="s_nad_h1"><div class="s_nad_pod_h1">
        <h1>Funkce</h1>
    </div></div>

   <div class="s_sekce">
     <ul>
       <#if DIZ.hasUnreadComments>
           <li><a href="#${DIZ.firstUnread}" title="Skoèit na první nepøeètený komentáø">První nepøeètený komentáø</a></li>
       </#if>
       <li><a href="${URL.prefix}/show/${DIZ.relationId}?varianta=print">Tisk diskuse</a></li>
       <li><a href="${URL.make("/EditDiscussion?action=monitor&amp;rid="+RELATION.id)}">${monitorState}</a>
       <span title="Poèet lidí, kteøí sledují tuto diskusi">(${DIZ.monitorSize})</span>
       <a class="info" href="#">?<span class="tooltip">Za¹le ka¾dý nový komentáø emailem na va¹i adresu</span></a></li>
       <#if is_question>
           <li>
              <a href="${URL.make("/EditDiscussion?action=solved&amp;rid="+RELATION.id+"&amp;solved=true")}">Otázka byla vyøe¹ena</a>
              (${TOOL.xpath(ITEM,"//solved/@yes")?default("0")})
              <a class="info" href="#">?<span class="tooltip">Pou¾ijte, pokud problém polo¾ený v otázce byl vyøe¹en.</span></a>
           </li>
           <li>
              <a href="${URL.make("/EditDiscussion?action=solved&amp;rid="+RELATION.id+"&amp;solved=false")}">Otázka nebyla vyøe¹ena</a>
              (${TOOL.xpath(ITEM,"//solved/@no")?default("0")})
              <a class="info" href="#">?<span class="tooltip">Pou¾ijte, pokud problém polo¾ený v otázce nebyl vyøe¹en.</span></a>
           </li>
       </#if>
      <#if USER?exists && (USER.hasRole("discussion admin") || USER.hasRole("move relation"))>
          <li><a href="/SelectRelation?prefix=/hardware&amp;url=/EditRelation&amp;action=move&amp;rid=${RELATION.id}">Pøesunout</a></li>
      </#if>
      <#if USER?exists && USER.hasRole("discussion admin")>
          <li><a href="${URL.noPrefix("/EditRelation?action=remove&amp;rid="+RELATION.id+"&amp;prefix="+URL.prefix)}">Sma¾ diskusi</a></li>
          <li><a href="${URL.make("/EditDiscussion?action=freeze&amp;rid="+RELATION.id+"&amp;dizId="+ITEM.id)}"><#if DIZ.frozen>Rozmrazit<#else>Zmrazit</#if> diskusi</a></li>
      </#if>
     </ul>
   </div>
</#assign>

<#include "../header.ftl">

<@lib.showMessages/>

<#if is_question>
 <h1>Otázka</h1>
 <@lib.showThread TOOL.createComment(ITEM), 0, DIZ, !DIZ.frozen />

    <p class="questionToFaq">
        U¾ jste tuto otázku vidìli? Ptají se na ni ètenáøi èasto? Pak by asi bylo vhodné
        ulo¾it vzorovou odpovìï do <a href="/faq">Èasto kladených otázek (FAQ)</a>.
    </p>

 <#if DIZ.size==0>
    <p>Na otázku zatím nikdo bohu¾el neodpovìdìl.</p>
 <#else>
     <h2>Odpovìdi</h2>
 </#if>
<#elseif !DIZ.frozen>
 <a href="${URL.make("/EditDiscussion?action=add&amp;threadId=0&amp;dizId="+ITEM.id+"&amp;rid="+RELATION.id)}">
 Vlo¾it dal¹í komentáø</a>
</#if>

<#if DIZ.frozen><p class="error">Diskuse byla administrátory uzamèena</p></#if>

<#list DIZ.threads as thread>
   <@lib.showThread thread, 0, DIZ, !DIZ.frozen />
</#list>

<#if (!DIZ.frozen && DIZ.size>3)>
 <p><a href="${URL.make("/EditDiscussion?action=add&amp;threadId=0&amp;dizId="+ITEM.id+"&amp;rid="+RELATION.id)}">
 Zalo¾it nové vlákno</a></p>
</#if>

<#include "../footer.ftl">
