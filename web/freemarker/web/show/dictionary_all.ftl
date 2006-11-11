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
    <input type="submit" value="Hledej ve slovníku" tabindex="2">
    <input type="hidden" name="type" value="pojem">
</form>

<p class="prefixes">
    <a href="/slovnik?prefix=a"<@highlight 'a'/>>a</a>
    <a href="/slovnik?prefix=b"<@highlight 'b'/>>b</a>
    <a href="/slovnik?prefix=c"<@highlight 'c'/>>c</a>
    <a href="/slovnik?prefix=d"<@highlight 'd'/>>d</a>
    <a href="/slovnik?prefix=e"<@highlight 'e'/>>e</a>
    <a href="/slovnik?prefix=f"<@highlight 'f'/>>f</a>
    <a href="/slovnik?prefix=g"<@highlight 'g'/>>g</a>
    <a href="/slovnik?prefix=h"<@highlight 'h'/>>h</a>
    <a href="/slovnik?prefix=i"<@highlight 'i'/>>i</a>
    <a href="/slovnik?prefix=j"<@highlight 'j'/>>j</a>
    <a href="/slovnik?prefix=k"<@highlight 'k'/>>k</a>
    <a href="/slovnik?prefix=l"<@highlight 'l'/>>l</a>
    <a href="/slovnik?prefix=m"<@highlight 'm'/>>m</a>
    <a href="/slovnik?prefix=n"<@highlight 'n'/>>n</a>
    <a href="/slovnik?prefix=o"<@highlight 'o'/>>o</a>
    <a href="/slovnik?prefix=p"<@highlight 'p'/>>p</a>
    <a href="/slovnik?prefix=q"<@highlight 'q'/>>q</a>
    <a href="/slovnik?prefix=r"<@highlight 'r'/>>r</a>
    <a href="/slovnik?prefix=s"<@highlight 's'/>>s</a>
    <a href="/slovnik?prefix=t"<@highlight 't'/>>t</a>
    <a href="/slovnik?prefix=u"<@highlight 'u'/>>u</a>
    <a href="/slovnik?prefix=v"<@highlight 'v'/>>v</a>
    <a href="/slovnik?prefix=w"<@highlight 'w'/>>w</a>
    <a href="/slovnik?prefix=x"<@highlight 'x'/>>x</a>
    <a href="/slovnik?prefix=y"<@highlight 'y'/>>y</a>
    <a href="/slovnik?prefix=z"<@highlight 'z'/>>z</a>
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
