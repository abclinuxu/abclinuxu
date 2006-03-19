<!-- obsah konci zde -->

<hr width="100%">

<#if VARS.currentPoll?exists>
 <#assign relAnketa = VARS.currentPoll, anketa = relAnketa.child, total = anketa.totalVoters,
  url=relAnketa.url?default("/ankety/show/"+relAnketa.id)>
 <#if anketa.multiChoice>
  <#assign type = "CHECKBOX">
  <#else>
  <#assign type = "RADIO">
 </#if>

 <p>
  <b>Anketa</b><br>
  <form action="${URL.noPrefix("/EditPoll/"+relAnketa.id)}" method="POST">
  <i>${anketa.text}</i><br>
  <#list anketa.choices as choice>
   <#assign procento = TOOL.percent(choice.count,total)>
   <input type=${type} name="voteId" value="${choice.id}">
   ${choice.text} (${procento}%) ${TOOL.percentBar(procento)}<br>
  </#list>

  <br>Celkem ${total} hlasù
  <#assign diz=TOOL.findComments(anketa)>
  <a href="${url}">Komentáøù: ${diz.responseCount}</a><br>
  <input type="submit" value="Hlasuj">
  </span>
  <input type="hidden" name="url" value="/clanky/show/${relAnketa.id}">
  <input type="hidden" name="action" value="vote">
 </form>
 </p>
</#if>

<h3>Slu¾by</h3>
<p>
<a href="/diskuse.jsp" class="za_mn_odkaz">Diskuse</a>
<a href="/faq" class="za_mn_odkaz">FAQ</a>
<a href="/hardware" class="za_mn_odkaz">Hardware</a>
<a href="/clanky" class="za_mn_odkaz">Èlánky</a>
<a href="/ucebnice" class="za_mn_odkaz">Uèebnice</a>
<a href="/blog" class="za_mn_odkaz">Blogy</a>
<a href="/download/abicko.jsp" class="za_mn_odkaz">PDF</a>
<a href="/slovnik" class="za_mn_odkaz">Slovník</a>
<a href="/ankety" class="za_mn_odkaz">Ankety</a>
<a href="/ovladace" class="za_mn_odkaz">Ovladaèe</a>
<a href="/hosting" class="za_mn_odkaz">Hosting</a>
<a href="http://www.praceabc.cz" class="za_mn_odkaz">Práce</a>
</p>

<h3>O serveru</h3>
<p>
 <a href="${URL.make("/clanky/dir/3500")}">Po¾adavky</a>
 <a href="http://abicko.stickfish.cz/bugzilla/">Bugzilla</a>
 <a href="/doc/portal/rss-a-jine-pristupy">RSS a PDA</a>
 <a href="/doc/propagace">Propagace</a>
 <a href="/clanky/show/44049">Tým AbcLinuxu</a>
 <a href="/clanky/novinky/pojdte-psat-pro-abclinuxu.cz">Pi¹te pro abclinuxu</a>
 ISSN 1214-1267
</p>

</body>
</html>
