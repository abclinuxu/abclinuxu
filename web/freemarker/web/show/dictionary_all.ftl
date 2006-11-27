<#include "../header.ftl">

<h1>Výkladový slovník</h1>

<p>
    Výkladový slovník na¹eho portálu je projekt, jen¾ se sna¾í èeským
    a slovenským u¾ivatelùm pøiblí¾it Linux. Málokterý nováèek se zaène
    s tímto operaèním systémem seznamovat tak, ¾e si o nìm nejdøíve pøeète
    <a href="/Search?query=recenze+kniha+cena&advancedMode=true&type=clanek">nìjakou knihu</a>
    nebo <a href="/ucebnice">uèebnici</a>. Pokud nemá ¾ádné zku¹enosti
    s jinými operaèními systémy odvozenými od Unixu, velmi rychle si pøipadá ztracený,
    proto¾e se v¹ude pou¾ívají pojmy a slova, která nechápe.
</p>

<p>
    Výkladový slovník je pokus, jak tento problém zmen¹it. Jak je na¹im dobrým zvykem,
    jedná se o otevøený komunitní projekt, do nìho¾ mù¾e pøispìt ka¾dý. Jeho cílem je popsat
    v¹echny základní pojmy, které se v Linuxu bì¾nì objevují. Pokud se chcete zapojit
    do tvorby této databáze, mù¾ete
    <a class="bez-slovniku" href="${URL.make("/slovnik/edit?action=add")}">vysvìtlit
    nový pojem</a>.
</p>

<p>
    Pokud nenajdete nìkterý pojem v na¹em slovníku a rozumíte anglicky, zkuste
    <a href="http://en.wikipedia.org/wiki/Category:Computing">Wikipedii</a>
    nebo <a href="http://www.acronymdictionary.co.uk">seznam akronymù</a>. K dispozici
    existuje i <a href="http://www.ucc.ie/cgi-bin/acronym">hledání</a> akronymù.
</p>


<form action="/Search" method="GET">
    <input type="text" name="query" size="30" tabindex="1">
    <input type="submit" value="Hledej ve slovníku" class="button" tabindex="2">
    <input type="hidden" name="type" value="pojem">
</form>

<p class="dict-abc">
    Filtr:
    <a href="/slovnik?prefix=a"<@highlight 'a'/>>A</a>
    <a href="/slovnik?prefix=b"<@highlight 'b'/>>B</a>
    <a href="/slovnik?prefix=c"<@highlight 'c'/>>C</a>
    <a href="/slovnik?prefix=d"<@highlight 'd'/>>D</a>
    <a href="/slovnik?prefix=e"<@highlight 'e'/>>E</a>
    <a href="/slovnik?prefix=f"<@highlight 'f'/>>F</a>
    <a href="/slovnik?prefix=g"<@highlight 'g'/>>G</a>
    <a href="/slovnik?prefix=h"<@highlight 'h'/>>H</a>
    <a href="/slovnik?prefix=i"<@highlight 'i'/>>I</a>
    <a href="/slovnik?prefix=j"<@highlight 'j'/>>J</a>
    <a href="/slovnik?prefix=k"<@highlight 'k'/>>K</a>
    <a href="/slovnik?prefix=l"<@highlight 'l'/>>L</a>
    <a href="/slovnik?prefix=m"<@highlight 'm'/>>M</a>
    <a href="/slovnik?prefix=n"<@highlight 'n'/>>N</a>
    <a href="/slovnik?prefix=o"<@highlight 'o'/>>O</a>
    <a href="/slovnik?prefix=p"<@highlight 'p'/>>P</a>
    <a href="/slovnik?prefix=q"<@highlight 'q'/>>Q</a>
    <a href="/slovnik?prefix=r"<@highlight 'r'/>>R</a>
    <a href="/slovnik?prefix=s"<@highlight 's'/>>S</a>
    <a href="/slovnik?prefix=t"<@highlight 't'/>>T</a>
    <a href="/slovnik?prefix=u"<@highlight 'u'/>>U</a>
    <a href="/slovnik?prefix=v"<@highlight 'v'/>>V</a>
    <a href="/slovnik?prefix=w"<@highlight 'w'/>>W</a>
    <a href="/slovnik?prefix=x"<@highlight 'x'/>>X</a>
    <a href="/slovnik?prefix=y"<@highlight 'y'/>>Y</a>
    <a href="/slovnik?prefix=z"<@highlight 'z'/>>Z</a>
</p>

<table border="0" class="siroka bez-slovniku">
 <#list FOUND.data as rel>
  <#if rel_index % 3 == 0><tr></#if>
   <td><a href="${rel.url}">${TOOL.xpath(rel.child,"data/name")}</a></td>
  <#if rel_index % 3 == 2></tr></#if>
 </#list>
</table>

<#macro highlight(letter)>
    <#if (CURRENT_PREFIX?default('a')==letter)> class="selected"</#if><#rt>
</#macro>

<#include "../footer.ftl">
