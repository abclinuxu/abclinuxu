<#include "../header.ftl">

<#include "/include/zprava.txt">
<@lib.showMessages/>

Zkratka na <a href="#zpravicky">zpr�vi�ky</a>, <a href="#diskuse">diskusn� f�rum</a>

<#list ARTICLES as rel>
 <@lib.showArticle rel, "CZ_SHORT" />
 <@lib.separator double=!rel_has_next />
</#list>

<p>
 <a href="/History?type=articles&from=${ARTICLES?size}&count=10" title="Dal��">Star�� �l�nky</a>
</p>

<#flush>

<p>
 <b>Z�klady Linuxu</b><br>
 <a href="/clanky/show/26394">Co je to Linux?</a>,
 <a href="/clanky/show/12707">Je opravdu zdarma?</a>,
 <a href="/clanky/show/9503">Co jsou to distribuce?</a>,
 <a href="/clanky/show/14665">��m nahrad�m aplikaci X?</a>,
 <a href="/clanky/show/20310">Rozcestn�k na�ich seri�l�</a>
</p>

<p>
 <b>Ovlada�e</b><br>
 <#list VARS.newDrivers as rel>
  <a href="/drivers/show/${rel.id}">
  ${TOOL.xpath(rel.child,"data/name")}</a><#if rel_has_next>,</#if>
 </#list>
 <a href="/drivers/dir/318">&gt;&gt;</a>
</p>

<p>
 <b>Hardware</b><br>
 <#list VARS.newHardware as rel>
  <a href="/hardware/show/${rel.id}">
  ${TOOL.xpath(rel.parent,"data/name")}</a><#if rel_has_next>,</#if>
 </#list>
 <a href="/History?type=hardware&from=0&count=25">&gt;&gt;</a><br>
 <#list SORT.byName(HARDWARE) as rel>
  <a href="/hardware/dir/${rel.id}">
  ${TOOL.childName(rel)}</a><#if rel_has_next>,</#if>
 </#list>
</p>

<p>
 <b>Software</b><br>
 <#list VARS.newSoftware as rel>
  <a href="/software/show/${rel.id}">
  ${TOOL.xpath(rel.parent,"data/name")}</a><#if rel_has_next>,</#if>
 </#list>
 <a href="/History?type=software&from=0&count=25">&gt;&gt;</a><br>
 <#list SORT.byName(SOFTWARE) as rel>
  <a href="/software/dir/${rel.id}">
  ${TOOL.childName(rel)}</a><#if rel_has_next>,</#if>
 </#list>
</p>

<p>
 <b>Aktu�ln� j�dra</b><br>
 <#include "/include/kernel.txt">
</p>

<#assign NEWS=VARS.getFreshNews(user?if_exists)>
<a name="zpravicky"><h1>Zpr�vi�ky</h1></a>
<#list NEWS as rel>
 <@lib.showNews rel/>
 <#if rel_has_next><@lib.separator /></#if>
</#list>
<p>
 <a href="/History?type=news&from=${NEWS?size}&count=15" title="Dal��">Star�� zpr�vi�ky</a>
 <a href="${URL.make("/news/EditItem?action=add")}">Vytvo�it zpr�vi�ku</a>
</p>

<#flush>

<#if FORUM?exists>
 <a name="diskuse"><h1>Diskusn� f�rum</h1></a>
 <p>
 <#list FORUM.data as diz>
  ${DATE.show(diz.updated,"CZ_SHORT")}, ${diz.responseCount} odp. :
   <a href="/hardware/show/${diz.relationId}">
   ${TOOL.limit(TOOL.xpath(diz.discussion,"data/title"),60," ..")}</a><br>
 </#list>
 </p>

 <ul>
  <li><a href="/diskuse.jsp">Zobrazit diskusn� f�rum (polo�it dotaz)</a>
  <li><a href="/History?type=discussions&from=${FORUM.nextPage.row}&count=20">Zobrazit star�� dotazy</a>
 </ul>
</#if>

<#include "../footer.ftl">
