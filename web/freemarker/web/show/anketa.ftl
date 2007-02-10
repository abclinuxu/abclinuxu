<#include "../header.ftl">

<@lib.showMessages/>

<@lib.advertisement id="arbo-sq" />

<#if USER?exists && USER.hasRole("poll admin")>
 <p>
  <a href="${URL.noPrefix("/EditPoll/"+RELATION.id+"?action=edit&amp;pollId="+POLL.id)}">Upravit</a>
 </p>
</#if>


<p><b>${POLL.text}</b></p>

<table class="ank" border="0" cellpadding="3">
 <#list POLL.choices as choice>
  <#assign procento=TOOL.percent(choice.count,POLL.totalVoters)>
  <tr>
   <td>${choice.text}</td>
   <td>
     <div style="width: ${procento}px" class="ank-sloup-okraj">
       <div class="ank-sloup"></div>
     </div>
   </td>
   <td>${procento}% (${choice.count})</td>
  </tr>
 </#list>
</table>

<p>Celkem ${POLL.totalVoters} hlasů<br />
Vytvořeno: ${DATE.show(POLL.created, "CZ_FULL")}</p>

<#if CHILDREN.discussion?exists>
    <#assign diz = TOOL.createDiscussionTree(CHILDREN.discussion[0].child,USER?if_exists,CHILDREN.discussion[0].id,true)>
    <#if diz.frozen>Diskuse byla administrátory uzamčena</#if>

<div class="ds_toolbox">
     <b>Nástroje:</b>

    <#if diz.hasUnreadComments>
        <a href="#${diz.firstUnread}" title="Skočit na první nepřečtený komentář">První nepřečtený komentář</a>, 
    </#if>

    <#if diz.monitored>
        <#assign monitorState="Přestaň sledovat"><#else><#assign monitorState="Sleduj">
    </#if>
    <a href="${URL.make("/EditDiscussion?action=monitor&amp;rid="+CHILDREN.discussion[0].id)}"
    title="AbcMonitor zašle emailem zprávu, dojde-li v diskusi ke změně">${monitorState}</a>
    <span title="Počet lidí, kteří sledují tuto diskusi">(${diz.monitorSize})</span>

    <#if USER?exists && USER.hasRole("discussion admin")>
     <br />
     <b>Admin:</b>
        <a href="${URL.make("/EditDiscussion?action=freeze&amp;rid="+diz.relationId+"&amp;dizId="+diz.id)}">
        <#if diz.frozen>Rozmrazit<#else>Zmrazit</#if> diskusi</a>
    </#if>

</div>

    <p><a href="${URL.make("/EditDiscussion?action=add&amp;dizId="+diz.id+"&amp;threadId=0&amp;rid="+diz.relationId)}">
    Vložit další komentář</a></p>

    <#list diz.threads as thread>
       <@lib.showThread thread, 0, diz, !diz.frozen />
    </#list>
<#else>
   <a href="${URL.make("/EditDiscussion?action=addDiz&amp;rid="+RELATION.id)}">Vložit první komentář</a>
</#if>

<#include "../footer.ftl">
