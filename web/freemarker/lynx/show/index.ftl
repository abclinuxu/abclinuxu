<#include "/include/macros.ftl">
<#include "../header.ftl">

<#include "/include/zprava.txt">
<#call showMessages>

<#list ARTICLES as rel>
 <#call showArticle(rel)>
 <#if rel_has_next><#call separator><#else><#call doubleSeparator></#if>
</#list>

<p>
 <a href="/History?type=articles&from=${ARTICLES?size}&count=10" title="Dal��">Star�� �l�nky</a>
</p>

<#flush>

<p>
 <b>Z�klady Linuxu</b><br>
 <a href="/clanky/ViewRelation?relationId=26394">Co je to Linux?</a>,
 <a href="/clanky/ViewRelation?relationId=12707">Je opravdu zdarma?</a>,
 <a href="/clanky/ViewRelation?relationId=9503">Co jsou to distribuce?</a>,
 <a href="/clanky/ViewRelation?relationId=14665">��m nahrad�m aplikaci X?</a>,
 <a href="/clanky/ViewRelation?relationId=20310">Rozcestn�k na�ich seri�l�</a>
</p>

<p>
 <b>Ovlada�e</b><br>
 <#list VARS.newDrivers as rel>
  <a href="/drivers/ViewRelation?relationId=${rel.id}">
  ${TOOL.xpath(rel.child,"data/name")}</a><#if rel_has_next>,</#if>
 </#list>
 <a href="/drivers/ViewCategory?relationId=318">&gt;&gt;</a>
</p>

<p>
 <b>Hardware</b><br>
 <#list VARS.newHardware as rel>
  <a href="/hardware/ViewRelation?relationId=${rel.id}">
  ${TOOL.xpath(rel.parent,"data/name")}</a><#if rel_has_next>,</#if>
 </#list>
 <a href="/History?type=hardware&from=0&count=25">&gt;&gt;</a><br>
 <#list SORT.byName(HARDWARE) as rel>
  <a href="/hardware/ViewCategory?relationId=${rel.id}">
  ${TOOL.childName(rel)}</a><#if rel_has_next>,</#if>
 </#list>
</p>

<p>
 <b>Software</b><br>
 <#list VARS.newSoftware as rel>
  <a href="/software/ViewRelation?relationId=${rel.id}">
  ${TOOL.xpath(rel.parent,"data/name")}</a><#if rel_has_next>,</#if>
 </#list>
 <a href="/History?type=software&from=0&count=25">&gt;&gt;</a><br>
 <#list SORT.byName(SOFTWARE) as rel>
  <a href="/software/ViewCategory?relationId=${rel.id}">
  ${TOOL.childName(rel)}</a><#if rel_has_next>,</#if>
 </#list>
</p>

<p>
 <b>Aktu�ln� j�dra</b><br>
 <#include "/include/kernel.txt">
</p>

<#if NEWS?exists>
 <h1>Zpr�vi�ky</h1>
 <#list NEWS as rel>
  <#call showNews(rel)>
  <#if rel_has_next><#call separator></#if>
 </#list>
 <p>
  <a href="/History?type=news&from=${NEWS?size}&count=15" title="Dal��">Star�� zpr�vi�ky</a>
  <a href="${URL.make("/news/EditItem?action=add")}">Vytvo�it zpr�vi�ku</a>
 </p>
</#if>

<#flush>

<#if FORUM?exists>
<p>
 <b>Diskusn� f�rum:</b> v�b�r ${FORUM.currentPage.size} aktivn�ch diskus� ze ${FORUM.total}<br>
 <#list FORUM.data as diz>
  ${DATE.show(diz.lastUpdate,"CZ_SHORT")}, ${diz.responseCount} odp. :
   <a href="/hardware/ViewRelation?relationId=${diz.relationId}">
   ${TOOL.limit(TOOL.xpath(diz.discussion,"data/title"),60," ..")}</a><br>
 </#list>
</p>

 <ul>
  <li><a href="${URL.make("/hardware/EditDiscussion?action=addQuez&relationId=3739")}">Polo�it nov� dotaz</a>
  <li><a href="/diskuse.jsp">Zobrazit cel� diskusn� f�rum</a> (${FORUM.total} dotaz�)
  <li><a href="/History?type=discussions&from=${FORUM.nextPage?if_exists.row?if_exists}&count=20">
   Listovat dotazy podle �asu vytvo�en�</a>
 </ul>
</#if>

<#include "../footer.ftl">
