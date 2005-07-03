<#include "../header.ftl">

<div slass="s">
<div class="s_nad_h1"><div class="s_nad_pod_h1"><h1 class="st_nadpis">Anketa</h1></div></div>
</div>
<@lib.showMessages/>

<#if USER?exists && USER.hasRole("poll admin")>
 <p>
  <a href="${URL.noPrefix("/EditPoll/"+RELATION.id+"?action=edit&amp;pollId="+POLL.id)}">Upravit</a>
 </p>
</#if>

<table border="0" cellpadding="3">
<thead>
 <tr>
  <td colspan=3 class="hlavicka_ankety">${POLL.text}</td>
 </tr>
</thead>
<tbody>
 <#list POLL.choices as choice>
  <#assign procento=TOOL.percent(choice.count,POLL.totalVotes)>
  <tr>
   <td class="volby_ankety">${choice.text}</td>
   <td><img src="/images/site2/anketa.gif" height=11 width=${procento} alt="${TOOL.percentBar(procento)}"></td>
   <td class="volby_ankety">${procento}% (${choice.count})</td>
  </tr>
 </#list>
 <tr>
  <td colspan=3 class="vysledek_ankety">Celkem ${POLL.totalVotes} hlasù</td>
 </tr>
</tbody>
<tfoot>
 <tr>
  <td colspan="3">Vytvoøeno: ${DATE.show(POLL.created, "CZ_FULL")}</td>
 </tr>
</tfoot>
</table>

<#if CHILDREN.discussion?exists>
    <#assign DISCUSSION=CHILDREN.discussion[0].child>
    <#assign diz = TOOL.createDiscussionTree(DISCUSSION,USER?if_exists,true)>

    <#assign frozen=TOOL.xpath(DISCUSSION,"/data/frozen")?exists>
    <#if frozen>Diskuse byla administrátory uzamèena</#if>
    <#if USER?exists && USER.hasRole("discussion admin")>
        <a href="${URL.make("/EditDiscussion?action=freeze&amp;rid="+CHILDREN.discussion[0].id+"&amp;dizId="+DISCUSSION.id)}">
        <#if frozen>Rozmrazit<#else>Zmrazit</#if> diskusi</a>
    </#if>

    <p>
    <#if diz.hasUnreadComments>
        <a href="#${diz.firstUnread}" title="Skoèit na první nepøeètený komentáø">První nepøeètený komentáø</a>
    </#if>

    <a href="${URL.make("/EditDiscussion?action=add&amp;dizId="+DISCUSSION.id+"&amp;threadId=0&amp;rid="+CHILDREN.discussion[0].id)}">
    Vlo¾it dal¹í komentáø</a>

    <#if USER?exists && TOOL.xpath(DISCUSSION,"//monitor/id[text()='"+USER.id+"']")?exists>
        <#assign monitorState="Pøestaò sledovat"><#else><#assign monitorState="Sleduj">
    </#if>
    <a href="${URL.make("/EditDiscussion?action=monitor&amp;rid="+CHILDREN.discussion[0].id)}"
    title="AbcMonitor za¹le emailem zprávu, dojde-li v diskusi ke zmìnì">${monitorState}</a>
    <span title="Poèet lidí, kteøí sledují tuto diskusi">(${TOOL.getMonitorCount(DISCUSSION.data)})</span>
    </p>

    <#list diz.threads as thread>
       <@lib.showThread thread, 0, DISCUSSION.id, CHILDREN.discussion[0].id, !frozen />
    </#list>
<#else>
   <a href="${URL.make("/EditDiscussion?action=addDiz&amp;rid="+RELATION.id)}">Vlo¾it první komentáø</a>
</#if>

<#include "../footer.ftl">
