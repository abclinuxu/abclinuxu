<!-- obsah konci zde -->

<hr width="100%">

<#if VARS.currentPoll?exists>
 <#assign relAnketa = VARS.currentPoll, anketa = relAnketa.child, total = anketa.totalVotes>
 <#if anketa.multiChoice>
  <#assign type = "CHECKBOX">
  <#else>
  <#assign type = "RADIO">
 </#if>

 <p>
  <b>Anketa</b><br>
  <form action="${URL.noPrefix("/EditPoll")}" method="POST">
  <i>${anketa.text}</i><br>
  <#list anketa.choices as choice>
   <#assign procento = TOOL.percent(choice.count,total)>
   <input type=${type} name="voteId" value="${choice.id}">
   ${choice.text} (${procento}%) ${TOOL.percentBar(procento)}<br>
  </#list>

  <br>Celkem ${total} hlasù<br>
  <input type="submit" value="Hlasuj">
  </span>
  <input type="hidden" name="pollId" value="${anketa.id}">
  <input type="hidden" name="url" value="${URL.noPrefix("/Index")}">
  <input type="hidden" name="action" value="vote">
 </form>
  <#assign diz=TOOL.findComments(anketa)>
  <a href="/news/show/${relAnketa.id}">Komentáøù:</a> ${diz.responseCount}
 </p>
</#if>

<p>
 <b>Slu¾by</b>
 <a href="${URL.make("/clanky/dir/3500")}">Po¾adavky
 (${VARS.counter.REQUESTS})</a>
</p>

<h1>O serveru</h1>
<p>
 <a href="/clanky/show/44043">Pøehled zmìn</a>,
 <a href="/doc/portal/jine_pristupy">Titulky, PDA a RSS</a>,
 <a href="/clanky/show/44049">Tým AbcLinuxu</a>,
 <a href="/clanky/show/42393">Staòte se autorem</a>,
 ISSN 1214-1267
</p>

<p>
 <b>Doporuèujeme</b>
 <a href="http://www.linux.cz">Linux.cz</a>,
 <a href="http://www.root.cz">Root</a>,
 <a href="http://www.mandrake.cz">Mandrake</a>,
 <a href="http://www.broadnet.cz">Broadnet - hosting</a>
</p>

<p>
 <b>Rozcestník</b>
</p>

<#list TOOL.createServers([7,1,13,12,3,2,5,4]) as server>
<p>
 <b><a href="${server.url}">${server.name}</a></b><br>
 <#assign linky = TOOL.sublist(SORT.byDate(LINKS[server.name],"DESCENDING"),0,4)>
 <#list linky as link>
  <a href="${link.url}">${link.text}</a><#if link_has_next>,</#if>
 </#list>
</#list>

</body>
</html>
