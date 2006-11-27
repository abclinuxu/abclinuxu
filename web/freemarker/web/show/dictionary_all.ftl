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
    <input type="submit" value="Hledej ve slovn�ku" class="button" tabindex="2">
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
