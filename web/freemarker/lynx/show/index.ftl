<#include "/include/macros.ftl">
<#include "../header.ftl">

<#include "/include/zprava.txt">
<#call showMessages>

<#list ARTICLES as rel>
 <#call showArticle(rel)>
 <#if rel_has_next><#call separator><#else><#call doubleSeparator></#if>
</#list>

<p>
 <a href="/History?type=articles&from=${ARTICLES?size}&count=10" title="Dal¹í">Star¹í èlánky</a>
</p>

<#flush>

<p>
 <b>Základy Linuxu</b><br>
 <a href="/clanky/ViewRelation?relationId=26394">Co je to Linux?</a>,
 <a href="/clanky/ViewRelation?relationId=12707">Je opravdu zdarma?</a>,
 <a href="/clanky/ViewRelation?relationId=9503">Co jsou to distribuce?</a>,
 <a href="/clanky/ViewRelation?relationId=14665">Èím nahradím aplikaci X?</a>,
 <a href="/clanky/ViewRelation?relationId=20310">Rozcestník na¹ich seriálù</a>
</p>

<p>
 <b>Ovladaèe</b><br>
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
 <b>Aktuální jádra</b><br>
 <#include "/include/kernel.txt">
</p>

<#flush>

<p>
 <#global diskuse=TOOL.sublist(SORT.byDate(TOOL.analyzeDiscussions(FORUM.content),"DESCENDING"),0,15)>
 <b>Diskusní fórum:</b> výbìr ${diskuse?size} aktivních diskusí ze ${FORUM.content?size}<br>
 <#list diskuse as diz>
  ${DATE.show(diz.lastUpdate,"CZ_SHORT")}, ${diz.responseCount} odp. :
   <a href="/hardware/ViewRelation?relationId=${diz.relationId}">
   ${TOOL.limit(TOOL.xpath(diz.discussion,"data/title"),60," ..")}</a><br>
 </#list>
</p>

<ul>
 <li><a href="${URL.make("/hardware/EditDiscussion?action=addQuez&relationId=3739")}">Polo¾it nový dotaz</a>
 <li><a href="/History?type=discussions&from=0}&count=20">Listovat dotazy podle èasu vytvoøení</a>
 <li><a href="/hardware/ViewRelation?relationId=3739">Celé diskusní fórum</a>
</ul>

<#include "../footer.ftl">
