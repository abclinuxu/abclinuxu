<#assign DIZ = TOOL.createDiscussionTree(ITEM,USER?if_exists,RELATION.id,true)>
<#assign is_question=TOOL.xpath(ITEM,"data/title")?exists>
<#if DIZ.monitored>
    <#assign monitorState="P�esta� sledovat"><#else><#assign monitorState="Sleduj">
</#if>

<#assign plovouci_sloupec>
    <div class="s_nad_h1"><div class="s_nad_pod_h1">
        <h1>Funkce</h1>
    </div></div>

   <div class="s_sekce">
     <ul>
       <#if DIZ.hasUnreadComments>
           <li><a href="#${DIZ.firstUnread}" title="Sko�it na prvn� nep�e�ten� koment��">Prvn� nep�e�ten� koment��</a></li>
       </#if>
       <li><a href="${URL.prefix}/show/${DIZ.relationId}?varianta=print">Tisk diskuse</a></li>
       <li><a href="${URL.make("/EditDiscussion?action=monitor&amp;rid="+RELATION.id)}">${monitorState}</a>
       <span title="Po�et lid�, kte�� sleduj� tuto diskusi">(${DIZ.monitorSize})</span>
       <a class="info" href="#">?<span class="tooltip">Za�le ka�d� nov� koment�� emailem na va�i adresu</span></a></li>
       <#if is_question>
           <li>
              <a href="${URL.make("/EditDiscussion?action=solved&amp;rid="+RELATION.id+"&amp;solved=true")}">Ot�zka byla vy�e�ena</a>
              (${TOOL.xpath(ITEM,"//solved/@yes")?default("0")})
              <a class="info" href="#">?<span class="tooltip">Pou�ijte, pokud probl�m polo�en� v ot�zce byl vy�e�en.</span></a>
           </li>
           <li>
              <a href="${URL.make("/EditDiscussion?action=solved&amp;rid="+RELATION.id+"&amp;solved=false")}">Ot�zka nebyla vy�e�ena</a>
              (${TOOL.xpath(ITEM,"//solved/@no")?default("0")})
              <a class="info" href="#">?<span class="tooltip">Pou�ijte, pokud probl�m polo�en� v ot�zce nebyl vy�e�en.</span></a>
           </li>
       </#if>
      <#if USER?exists && (USER.hasRole("discussion admin") || USER.hasRole("move relation"))>
          <li><a href="/SelectRelation?prefix=/hardware&amp;url=/EditRelation&amp;action=move&amp;rid=${RELATION.id}">P�esunout</a></li>
      </#if>
      <#if USER?exists && USER.hasRole("discussion admin")>
          <li><a href="${URL.noPrefix("/EditRelation?action=remove&amp;rid="+RELATION.id+"&amp;prefix="+URL.prefix)}">Sma� diskusi</a></li>
          <li><a href="${URL.make("/EditDiscussion?action=freeze&amp;rid="+RELATION.id+"&amp;dizId="+ITEM.id)}"><#if DIZ.frozen>Rozmrazit<#else>Zmrazit</#if> diskusi</a></li>
      </#if>
     </ul>
   </div>
</#assign>

<#include "../header.ftl">

<@lib.showMessages/>

<#if is_question>
 <h1>Ot�zka</h1>
 <@lib.showThread TOOL.createComment(ITEM), 0, DIZ, !DIZ.frozen />

    <p class="questionToFaq">
        U� jste tuto ot�zku vid�li? Ptaj� se na ni �ten��i �asto? Pak by asi bylo vhodn�
        ulo�it vzorovou odpov�� do <a href="/faq">�asto kladen�ch ot�zek (FAQ)</a>.
    </p>

 <#if DIZ.size==0>
    <p>Na ot�zku zat�m nikdo bohu�el neodpov�d�l.</p>
 <#else>
     <h2>Odpov�di</h2>
 </#if>
<#elseif !DIZ.frozen>
 <a href="${URL.make("/EditDiscussion?action=add&amp;threadId=0&amp;dizId="+ITEM.id+"&amp;rid="+RELATION.id)}">
 Vlo�it dal�� koment��</a>
</#if>

<#if DIZ.frozen><p class="error">Diskuse byla administr�tory uzam�ena</p></#if>

<#list DIZ.threads as thread>
   <@lib.showThread thread, 0, DIZ, !DIZ.frozen />
</#list>

<#if (!DIZ.frozen && DIZ.size>3)>
 <p><a href="${URL.make("/EditDiscussion?action=add&amp;threadId=0&amp;dizId="+ITEM.id+"&amp;rid="+RELATION.id)}">
 Zalo�it nov� vl�kno</a></p>
</#if>

<#include "../footer.ftl">
