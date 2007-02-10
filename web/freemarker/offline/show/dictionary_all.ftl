<#import "../macros.ftl" as lib>
<#include "../header.ftl">

<h1>Výkladový slovník portálu www.abclinuxu.cz</h1>

<p>Výkladový slovník našeho portálu je projekt, jenž se snaží českým
a slovenským uživatelům přiblížit Linux. Málokterý nováček se začne
s tímto operačním systémem seznamovat tak, že si o něm nejdříve přečte
knížku. Pokud nemá žádné zkušenosti s jinými operačními systémy odvozenými
od Unixu, velmi rychle si připadá ztracený, protože se všude používají pojmy
a slova, která nechápe.</p>

<p>Výkladový slovník je pokus jak tento problém zmenšit. Jak je našim dobrým zvykem,
jedná se o otevřený komunitní projekt, do něhož může přispět každý. Jeho cílem je popsat
všechny základní pojmy, které se v Linuxu běžně objevují.</p>

<p>Pokud některý pojem nenajdete v našem slovníku a rozumíte anglicky, určitě
jej najdete ve <a href="http://en.wikipedia.org/wiki/Category:Computing">Wikipedii</a>
nebo <a href="http://www.acronymdictionary.co.uk">seznamu akronymů</a>. K dispozici
existuje i <a href="http://www.ucc.ie/cgi-bin/acronym">hledání</a> akronymů.
</p>

<br>

<table border="0" class="siroka">
 <#list RESULT.data as rel>
  <#if rel_index % 4 == 0><tr></#if>
   <td><a href="../../${DUMP.getFile(rel.id)}">${TOOL.xpath(rel.child,"data/name")}</a></td>
  <#if rel_index % 4 == 3></tr></#if>
 </#list>
</table>

<#include "../footer.ftl">
