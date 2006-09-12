<#include "../header.ftl">

<div class="s_nadpis">Anketa</div>

<@lib.showMessages/>

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

<p>Celkem ${POLL.totalVoters} hlas�<br />
Vytvo�eno: ${DATE.show(POLL.created, "CZ_FULL")}</p>

<#if CHILDREN.discussion?exists>
    <#assign diz = TOOL.createDiscussionTree(CHILDREN.discussion[0].child,USER?if_exists,CHILDREN.discussion[0].id,true)>
    <#if diz.frozen>Diskuse byla administr�tory uzam�ena</#if>

<div class="ds_toolbox">
     <b>N�stroje:</b>

    <#if diz.hasUnreadComments>
        <a href="#${diz.firstUnread}" title="Sko�it na prvn� nep�e�ten� koment��">Prvn� nep�e�ten� koment��</a>,
    </#if>

    <#if diz.monitored>
        <#assign monitorState="P�esta� sledovat"><#else><#assign monitorState="Sleduj">
    </#if>
    <a href="${URL.make("/monitor/"+CHILDREN.discussion[0].id+"?action=toggle")}"
    title="AbcMonitor za�le emailem zpr�vu, dojde-li v diskusi ke zm�n�">${monitorState}</a>
    <span title="Po�et lid�, kte�� sleduj� tuto diskusi">(${diz.monitorSize})</span>

    <#if USER?exists && USER.hasRole("discussion admin")>
     <br />
     <b>Admin:</b>
        <a href="${URL.make("/EditDiscussion?action=freeze&amp;rid="+diz.relationId+"&amp;dizId="+diz.id)}">
        <#if diz.frozen>Rozmrazit<#else>Zmrazit</#if> diskusi</a>
    </#if>

</div>

    <p><a href="${URL.make("/EditDiscussion?action=add&amp;dizId="+diz.id+"&amp;threadId=0&amp;rid="+diz.relationId)}">
    Vlo�it dal�� koment��</a></p>

    <#list diz.threads as thread>
       <@lib.showThread thread, 0, diz, !diz.frozen />
    </#list>
<#else>
   <a href="${URL.make("/EditDiscussion?action=addDiz&amp;rid="+RELATION.id)}">Vlo�it prvn� koment��</a>
</#if>

<#include "../footer.ftl">
