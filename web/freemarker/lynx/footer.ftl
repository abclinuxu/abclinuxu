<!-- obsah konci zde -->

<hr width="100%">

<#if VARS.currentPoll?exists>
 <#global anketa = VARS.currentPoll>
 <#global total = anketa.totalVotes>
 <#if anketa.multiChoice>
  <#global type = "CHECKBOX">
  <#else>
  <#global type = "RADIO">
 </#if>

 <p>
  <b>Anketa</b><br>
  <form action="${URL.noPrefix("/EditPoll")}" method="POST">
  <i>${anketa.text}</i><br>
  <#list anketa.choices as choice>
   <#global procento = TOOL.percent(choice.count,total)>
   <input type=${type} name="voteId" value="${choice.id}">
   ${choice.text} (${procento}%) ${TOOL.percentBar(procento)}<br>
  </#list>

  <br>Celkem ${total} hlas�<br>
  <input type="submit" value="Hlasuj">
  </span>
  <input type="hidden" name="pollId" value="${anketa.id}">
  <input type="hidden" name="url" value="${URL.noPrefix("/Index")}">
  <input type="hidden" name="action" value="vote">
 </form>
 </p>
</#if>

<p>
 <b>Slu�by</b>
 <a href="${URL.make("/hardware/ViewCategory?relationId=1")}">Hardware
 (${VARS.counter.HARDWARE})</a>,
 <a href="${URL.make("/software/ViewCategory?relationId=317")}">Software
 (${VARS.counter.SOFTWARE})</a>,
 <a href="${URL.make("/drivers/ViewCategory?relationId=318")}">Ovlada�e
 (${VARS.counter.DRIVERS})</a>,
 <a href="${URL.make("/hardware/ViewCategory?relationId=3739")}">Diskuse
 (${VARS.counter.FORUM})</a>,
 <a href="${URL.make("/clanky/ViewCategory?relationId=3500")}">Po�adavky
 (${VARS.counter.REQUESTS})</a>
</p>
 
<p>
 <b>O serveru</b>
 <#list SORT.byName(ABCLINUXU) as rel>
  <a href="${URL.make("/clanky/ViewCategory?relationId="+rel.id)}">
  ${TOOL.childName(rel)}</a>,
 </#list>
 ISSN 1214-1267
</p>

<p>
 <b>Doporu�ujeme</b>
 <a href="http://www.linux.cz">Linux.cz</a>,
 <a href="http://www.root.cz">Root</a>,
 <a href="http://www.mandrake.cz">Mandrake</a>,
 <a href="http://www.broadnet.cz">Broadnet - hosting</a>
</p>

<p>
 <b>Rozcestn�k</b>
</p>

<#list TOOL.createServers([5,1,2,4,3,12]) as server>
<p>
 <b><a href="${server.url}">${server.name}</a></b><br>
 <#global LINKY = SORT.byDate(LINKS.get(server),"DESCENDING")>
 <#list LINKY as link>
  <a href="${link.url}">${link.text}</a><#if link_has_next>,</#if>
 </#list>
</#list>

</body>
</html>
