<#include "../header.ftl">

<#macro showArticle(relation)>
 <#local clanek=relation.child>
 <#local autor=TOOL.createUser(TOOL.xpath(clanek,"/data/author"))>
 <p>
  <a href="/clanky/ViewRelation?relationId=${relation.id}" target="_content">
   ${TOOL.xpath(clanek,"data/name")}
  </a><br>
  ${DATE.show(clanek.created, "CZ_FULL")}<br>
  <a href="/Profile?userId=${autor.id}">${autor.name}</a><br>
 </p>
</#macro>

<div align="center">
<a href="/Index?src=sidebar" title="AbcLinuxu.cz" target="_content">
<img src="/images/site/logo_small.gif" width=77 height=97 border=0 alt="AbcLinuxu.cz"></a>
</div>

<br>

<div align="center">
<form action="/Search" method="POST">
<input type="text" name="query" size="12"><br>
<input type="submit" value="Hledej">
</form>
</div>

<a href="/clanky/ViewRelation?relationId=5&src=sidebar" class="nadpis">Èlánky</a>

<#list ARTICLES as rel>
 <#call showArticle(rel)>
</#list>

<#include "../footer.ftl">
