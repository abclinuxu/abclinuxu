<#include "/include/macros.ftl">
<#include "../header.ftl">

<#include "/include/zprava.txt">
<#call showMessages>

Zkratka na <a href="#zpravicky">zprávièky</a>, <a href="#diskuse">diskusní fórum</a>

<#list ARTICLES as rel>
 <#call showArticle(rel "CZ_SHORT")>
 <#if rel_has_next><#call separator><#else><#call doubleSeparator></#if>
</#list>

<p>
 <a href="/History?type=articles&from=${ARTICLES?size}&count=10" title="Dal¹í">Star¹í èlánky</a>
</p>

<#flush>

<p>
 <b>Základy Linuxu</b><br>
 <a href="/clanky/ViewRelation?rid=26394">Co je to Linux?</a>,
 <a href="/clanky/ViewRelation?rid=12707">Je opravdu zdarma?</a>,
 <a href="/clanky/ViewRelation?rid=9503">Co jsou to distribuce?</a>,
 <a href="/clanky/ViewRelation?rid=14665">Èím nahradím aplikaci X?</a>,
 <a href="/clanky/ViewRelation?rid=20310">Rozcestník na¹ich seriálù</a>
</p>

<p>
 <b>Ovladaèe</b><br>
 <#list VARS.newDrivers as rel>
  <a href="/drivers/ViewRelation?rid=${rel.id}">
  ${TOOL.xpath(rel.child,"data/name")}</a><#if rel_has_next>,</#if>
 </#list>
 <a href="/drivers/ViewCategory?rid=318">&gt;&gt;</a>
</p>

<p>
 <b>Hardware</b><br>
 <#list VARS.newHardware as rel>
  <a href="/hardware/ViewRelation?rid=${rel.id}">
  ${TOOL.xpath(rel.parent,"data/name")}</a><#if rel_has_next>,</#if>
 </#list>
 <a href="/History?type=hardware&from=0&count=25">&gt;&gt;</a><br>
 <#list SORT.byName(HARDWARE) as rel>
  <a href="/hardware/ViewCategory?rid=${rel.id}">
  ${TOOL.childName(rel)}</a><#if rel_has_next>,</#if>
 </#list>
</p>

<p>
 <b>Software</b><br>
 <#list VARS.newSoftware as rel>
  <a href="/software/ViewRelation?rid=${rel.id}">
  ${TOOL.xpath(rel.parent,"data/name")}</a><#if rel_has_next>,</#if>
 </#list>
 <a href="/History?type=software&from=0&count=25">&gt;&gt;</a><br>
 <#list SORT.byName(SOFTWARE) as rel>
  <a href="/software/ViewCategory?rid=${rel.id}">
  ${TOOL.childName(rel)}</a><#if rel_has_next>,</#if>
 </#list>
</p>

<p>
 <b>Aktuální jádra</b><br>
 <#include "/include/kernel.txt">
</p>

<#global NEWS=VARS.getFreshNews(user?if_exists)>
<a name="zpravicky"><h1>Zprávièky</h1></a>
<#list NEWS as rel>
 <#call showNews(rel)>
 <#if rel_has_next><#call separator></#if>
</#list>
<p>
 <a href="/History?type=news&from=${NEWS?size}&count=15" title="Dal¹í">Star¹í zprávièky</a>
 <a href="${URL.make("/news/EditItem?action=add")}">Vytvoøit zprávièku</a>
</p>

<#flush>

<#if FORUM?exists>
<a name="diskuse"><h1>Diskusní fórum</h1></a>
<p>
 <#list FORUM.data as diz>
  ${DATE.show(diz.lastUpdate,"CZ_SHORT")}, ${diz.responseCount} odp. :
   <a href="/hardware/ViewRelation?rid=${diz.rid}">
   ${TOOL.limit(TOOL.xpath(diz.discussion,"data/title"),60," ..")}</a><br>
 </#list>
</p>

 <ul>
  <li><a href="${URL.make("/hardware/EditDiscussion?action=addQuez&rid=3739")}">Polo¾it nový dotaz</a>
  <li><a href="/diskuse.jsp">Zobrazit celé diskusní fórum</a> (${FORUM.total} dotazù)
  <li><a href="/History?type=discussions&from=${FORUM.nextPage?if_exists.row?if_exists}&count=20">
   Listovat dotazy podle èasu vytvoøení</a>
 </ul>
</#if>

<#include "../footer.ftl">
