<#include "../header.ftl">

<div id="anketa">

<h1 style="text-align: center">Anketa</h1>

<#if USER?exists && USER.hasRole("poll admin")>
 <p>
  <a href="${URL.make("/EditPoll?action=edit&amp;rid="+RELATION.id+"&amp;pollId="+POLL.id)}">
  <img src="/images/actions/pencil.png" class="ikona22" alt="Uprav"></a>
 </p>
</#if>

<table border="0" cellpadding="3">
 <tr>
  <td colspan=3 class="hlavicka_ankety">${POLL.text}</td>
 </tr>
 <#list POLL.choices as choice>
  <#assign procento=TOOL.percent(choice.count,POLL.totalVotes)>
  <tr>
   <td class="volby_ankety">${choice.text}</td>
   <td><img src="/images/site/graf.gif" height=11 width=${procento} alt="${TOOL.percentBar(procento)}"></td>
   <td class="volby_ankety">${procento}% (${choice.count})</td>
  </tr>
 </#list>
 <tr>
  <td colspan=3 class="vysledek_ankety">Celkem ${POLL.totalVotes} hlasù</td>
 </tr>
 <tr>
  <td colspan="3">Vytvoøeno: ${DATE.show(POLL.created, "CZ_FULL")}</td>
 </tr>
</table>

</div>

<#include "../footer.ftl">
