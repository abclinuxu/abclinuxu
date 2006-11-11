<#include "../header.ftl">

<h1>V�kladov� slovn�k</h1>

<p>
    V�kladov� slovn�k na�eho port�lu je projekt, jen� se sna�� �esk�m
    a slovensk�m u�ivatel�m p�ibl�it Linux. M�lokter� nov��ek se za�ne
    s t�mto opera�n�m syst�mem seznamovat tak, �e si o n�m nejd��ve p�e�te
    <a href="/Search?query=recenze+kniha+cena&advancedMode=true&type=clanek">n�jakou knihu</a>
    nebo <a href="/ucebnice">u�ebnici</a>. Pokud nem� ��dn� zku�enosti
    s jin�mi opera�n�mi syst�my odvozen�mi od Unixu, velmi rychle si p�ipad� ztracen�,
    proto�e se v�ude pou��vaj� pojmy a slova, kter� nech�pe.
</p>

<p>
    V�kladov� slovn�k je pokus, jak tento probl�m zmen�it. Jak je na�im dobr�m zvykem,
    jedn� se o otev�en� komunitn� projekt, do n�ho� m��e p�isp�t ka�d�. Jeho c�lem je popsat
    v�echny z�kladn� pojmy, kter� se v Linuxu b�n� objevuj�. Pokud se chcete zapojit
    do tvorby t�to datab�ze, m��ete
    <a class="bez-slovniku" href="${URL.make("/slovnik/edit?action=add")}">vysv�tlit
    nov� pojem</a>.
</p>

<p>
    Pokud nenajdete n�kter� pojem v na�em slovn�ku a rozum�te anglicky, zkuste
    <a href="http://en.wikipedia.org/wiki/Category:Computing">Wikipedii</a>
    nebo <a href="http://www.acronymdictionary.co.uk">seznam akronym�</a>. K dispozici
    existuje i <a href="http://www.ucc.ie/cgi-bin/acronym">hled�n�</a> akronym�.
</p>


<form action="/Search" method="GET">
    <input type="text" name="query" size="30" tabindex="1">
    <input type="submit" value="Hledej ve slovn�ku" tabindex="2">
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
