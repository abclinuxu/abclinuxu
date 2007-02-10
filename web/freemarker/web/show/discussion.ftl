<#assign DIZ = TOOL.createDiscussionTree(ITEM,USER?if_exists,RELATION.id,true)>
<#assign is_question=TOOL.xpath(ITEM,"data/title")?exists>
<#if DIZ.monitored>
    <#assign monitorState="Přestaň sledovat"><#else><#assign monitorState="Sleduj">
</#if>

<#include "../header.ftl">

<#--<@lib.advertisement id="arbo-sq" />-->
<#if !is_question>
 <@lib.advertisement id="arbo-sky" />
 <@lib.advertisement id="gg-ds-half" />
</#if>

<@lib.showMessages/>

<div class="ds_toolbox">
 <b>Nástroje:</b>
   <#if DIZ.hasUnreadComments>
     <a href="#${DIZ.firstUnread}" title="Skočit na první nepřečtený komentář">První nepřečtený komentář</a>,
   </#if>
   <a href="${URL.make("/EditMonitor/"+RELATION.id+"?action=toggle")}">${monitorState}</a>
      <span title="Počet lidí, kteří sledují tuto diskusi">(${DIZ.monitorSize})</span>
      <a class="info" href="#">?<span class="tooltip">Zašle každý nový komentář emailem na vaši adresu</span></a>,
   <#if is_question>
     Otázka <a href="${URL.make("/EditDiscussion?action=solved&amp;rid="+RELATION.id+"&amp;solved=true")}">byla</a>
        (${TOOL.xpath(ITEM,"//solved/@yes")?default("0")}) /
     <a href="${URL.make("/EditDiscussion?action=solved&amp;rid="+RELATION.id+"&amp;solved=false")}">nebyla</a>
        (${TOOL.xpath(ITEM,"//solved/@no")?default("0")}) vyřešena
        <a class="info" href="#">?<span class="tooltip">Kliknutím na příslušný odkaz zvolte, jestli otázka <i>byla</i> nebo <i>nebyla</i> vyřešena.</span></a>,
   </#if>
   <a href="${URL.prefix}/show/${DIZ.relationId}?varianta=print">Tisk</a>
   <#if USER?exists && (USER.hasRole("discussion admin") || USER.hasRole("move relation"))>
     <br />
     <b>Admin:</b>
     <a href="/SelectRelation?prefix=/hardware&amp;url=/EditRelation&amp;action=move&amp;rid=${RELATION.id}">Přesunout</a>,
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
        Už jste tuto otázku viděli? Ptají se na ni čtenáři často? Pak by asi bylo vhodné
        uložit vzorovou odpověď do <a href="/faq">Často kladených otázek (FAQ)</a>.
    </p>

 <@lib.advertisement id="sun-box" />
 <@lib.advertisement id="arbo-sky" />
 <@lib.advertisement id="gg-ds-full" />

 <#if DIZ.size==0>
    <p>Na otázku zatím nikdo bohužel neodpověděl.</p>
 <#else>
     <h2>Odpovědi</h2>
 </#if>
<#elseif !DIZ.frozen>
 <br />
 <a href="${URL.make("/EditDiscussion?action=add&amp;threadId=0&amp;dizId="+ITEM.id+"&amp;rid="+RELATION.id)}">
 Vložit další komentář</a>
</#if>

<#if DIZ.frozen><p class="error">Diskuse byla administrátory uzamčena</p></#if>

<#list DIZ.threads as thread>
   <@lib.showThread thread, 0, DIZ, !DIZ.frozen />
</#list>

<#if (!DIZ.frozen && DIZ.size>3)>
 <p><a href="${URL.make("/EditDiscussion?action=add&amp;threadId=0&amp;dizId="+ITEM.id+"&amp;rid="+RELATION.id)}">
 Založit nové vlákno</a></p>
</#if>

<@lib.advertisement id="arbo-full" />

<#include "../footer.ftl">
