<#include "../header.ftl">

<div slass="s">
<div class="s_nad_h1"><div class="s_nad_pod_h1"><h1 class="st_nadpis">Anketa</h1></div></div>
</div>

<#if USER?exists && USER.hasRole("poll admin")>
 <p>
  <a href="${URL.make("/EditPoll/"+RELATION.id+"?action=edit&amp;pollId="+POLL.id)}">Upravit</a>
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
 <p>
   <a href="${URL.make("/EditDiscussion?action=add&amp;dizId="+DISCUSSION.id+"&amp;threadId=0&amp;rid="+CHILDREN.discussion[0].id)}">
     Vlo¾it dal¹í komentáø</a>
 </p>

  <#assign frozen=TOOL.xpath(DISCUSSION,"/data/frozen")?exists>
  <#if frozen>Diskuse byla administrátory uzamèena</#if>

   <#if USER?exists && USER.hasRole("discussion admin")>
	<a href="${URL.make("/EditDiscussion?action=freeze&amp;rid="+CHILDREN.discussion[0].id+"&amp;dizId="+DISCUSSION.id)}">
      <#if frozen>Rozmrazit<#else>Zmrazit</#if> diskusi</a>
   </#if>

  <#if USER?exists><#assign MAX_COMMENT=TOOL.getLastSeenComment(DISCUSSION,USER,true) in lib></#if>
  <#list TOOL.createDiscussionTree(DISCUSSION) as thread>
     <@lib.showThread thread, 0, DISCUSSION.id, CHILDREN.discussion[0].id, !frozen />
  </#list>
<#else>
   <a href="${URL.make("/EditDiscussion?action=addDiz&amp;rid="+RELATION.id)}">Vlo¾it první komentáø</a>
</#if>

<#include "../footer.ftl">
