<#assign DIZ = TOOL.createDiscussionTree(ITEM,USER?if_exists,RELATION.id,true)>
<#assign is_question=TOOL.xpath(ITEM,"data/title")?exists>
<#if DIZ.monitored>
    <#assign monitorState="Pøestaò sledovat"><#else><#assign monitorState="Sleduj">
</#if>

<#include "../header.ftl">

<#--<@lib.advertisement id="arbo-sq" />
<#if !is_question>
 <@lib.advertisement id="arbo-sky" />
</#if>-->

<@lib.showMessages/>

<div class="ds_toolbox">
 <b>Nástroje:</b>
   <#if DIZ.hasUnreadComments>
     <a href="#${DIZ.firstUnread}" title="Skoèit na první nepøeètený komentáø">První nepøeètený komentáø</a>,
   </#if>
   <a href="${URL.make("/EditMonitor/"+RELATION.id+"?action=toggle")}">${monitorState}</a>
      <span title="Poèet lidí, kteøí sledují tuto diskusi">(${DIZ.monitorSize})</span>
      <a class="info" href="#">?<span class="tooltip">Za¹le ka¾dý nový komentáø emailem na va¹i adresu</span></a>,
   <#if is_question>
     Otázka <a href="${URL.make("/EditDiscussion?action=solved&amp;rid="+RELATION.id+"&amp;solved=true")}">byla</a>
        (${TOOL.xpath(ITEM,"//solved/@yes")?default("0")}) /
     <a href="${URL.make("/EditDiscussion?action=solved&amp;rid="+RELATION.id+"&amp;solved=false")}">nebyla</a>
        (${TOOL.xpath(ITEM,"//solved/@no")?default("0")}) vyøe¹ena
        <a class="info" href="#">?<span class="tooltip">Kliknutím na pøíslu¹ný odkaz zvolte, jestli otázka <i>byla</i> nebo <i>nebyla</i> vyøe¹ena.</span></a>,
   </#if>
   <a href="${URL.prefix}/show/${DIZ.relationId}?varianta=print">Tisk</a>
   <#if USER?exists && (USER.hasRole("discussion admin") || USER.hasRole("move relation"))>
     <br />
     <b>Admin:</b>
     <a href="/SelectRelation?prefix=/hardware&amp;url=/EditRelation&amp;action=move&amp;rid=${RELATION.id}">Pøesunout</a>,
   </#if>
   <#if USER?exists && USER.hasRole("discussion admin")>
     <a href="${URL.noPrefix("/EditRelation?action=remove&amp;rid="+RELATION.id+"&amp;prefix="+URL.prefix)}">Smazat</a>,
     <a href="${URL.make("/EditDiscussion?action=freeze&amp;rid="+RELATION.id+"&amp;dizId="+ITEM.id)}"><#if DIZ.frozen>Rozmrazit<#else>Zmrazit</#if></a>
   </#if>
</div>

<#if is_question>
 <h1>Otázka</h1>
 <@lib.showThread TOOL.createComment(ITEM), 0, DIZ, !DIZ.frozen />

    <p class="questionToFaq">
        U¾ jste tuto otázku vidìli? Ptají se na ni ètenáøi èasto? Pak by asi bylo vhodné
        ulo¾it vzorovou odpovìï do <a href="/faq">Èasto kladených otázek (FAQ)</a>.
    </p>

 <@lib.advertisement id="sun-box" />
<#--<@lib.advertisement id="arbo-sky" />
 <@lib.advertisement id="arbo-full" />-->

 <#if DIZ.size==0>
    <p>Na otázku zatím nikdo bohu¾el neodpovìdìl.</p>
 <#else>
     <h2>Odpovìdi</h2>
 </#if>
<#elseif !DIZ.frozen>
 <br />
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
