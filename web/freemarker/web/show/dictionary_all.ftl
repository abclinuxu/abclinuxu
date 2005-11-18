<#include "../header.ftl">

<h1>Výkladový slovník portálu www.abclinuxu.cz</h1>

<p>Výkladový slovník na¹eho portálu je projekt, jen¾ se sna¾í èeským
a slovenským u¾ivatelùm pøiblí¾it Linux. Málokterý nováèek se zaène
s tímto operaèním systémem seznamovat tak, ¾e si o nìm nejdøíve pøeète
<a href="/clanky/dir/2">nìjakou kní¾ku</a>. Pokud nemá ¾ádné zku¹enosti
s jinými operaèními systémy odvozenými od Unixu, velmi rychle si pøipadá ztracený,
proto¾e se v¹ude pou¾ívají pojmy a slova, která nechápe.</p>

<p>Výkladový slovník je pokus jak tento problém zmen¹it. Jak je na¹im dobrým zvykem,
jedná se o otevøený komunitní projekt, do nìho¾ mù¾e pøispìt ka¾dý. Jeho cílem je popsat
v¹echny základní pojmy, které se v Linuxu bì¾nì objevují. Pokud se chcete zapojit
do tvorby této databáze, mù¾ete 
<a class="bez-slovniku" href="${URL.make("/slovnik/edit?action=add")}">vysvìtlit</a>
nový pojem.</p>

<p>Pokud nenajdete nìkterý pojem v na¹em slovníku a rozumíte anglicky, urèitì
jej najdete ve <a href="http://en.wikipedia.org/wiki/Category:Computing">Wikipedii</a>
nebo <a href="http://www.acronymdictionary.co.uk">seznamu akronymù</a>. K dispozici
existuje i <a href="http://www.ucc.ie/cgi-bin/acronym">hledání</a> akronymù.
</p>

<br>

<table class="bez-slovniku" border="0" class="siroka">
 <#list FOUND.data as rel>
  <#if rel_index % 3 == 0><tr></#if>
   <td><a href="/slovnik/${rel.child.subType}">${TOOL.xpath(rel.child,"data/name")}</a></td>
  <#if rel_index % 3 == 2></tr></#if>
 </#list>
</table>

<#if FOUND.nextPage?exists>
 <p>
  <a href="/History?type=dictionary&amp;from=${FOUND.pageSize+1}&amp;count=25">Star¹í pojmy</a>
 </p>
</#if>

<#include "../footer.ftl">
