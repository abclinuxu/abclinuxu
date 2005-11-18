<#include "../header.ftl">

<h1>V�kladov� slovn�k port�lu www.abclinuxu.cz</h1>

<p>V�kladov� slovn�k na�eho port�lu je projekt, jen� se sna�� �esk�m
a slovensk�m u�ivatel�m p�ibl�it Linux. M�lokter� nov��ek se za�ne
s t�mto opera�n�m syst�mem seznamovat tak, �e si o n�m nejd��ve p�e�te
<a href="/clanky/dir/2">n�jakou kn�ku</a>. Pokud nem� ��dn� zku�enosti
s jin�mi opera�n�mi syst�my odvozen�mi od Unixu, velmi rychle si p�ipad� ztracen�,
proto�e se v�ude pou��vaj� pojmy a slova, kter� nech�pe.</p>

<p>V�kladov� slovn�k je pokus jak tento probl�m zmen�it. Jak je na�im dobr�m zvykem,
jedn� se o otev�en� komunitn� projekt, do n�ho� m��e p�isp�t ka�d�. Jeho c�lem je popsat
v�echny z�kladn� pojmy, kter� se v Linuxu b�n� objevuj�. Pokud se chcete zapojit
do tvorby t�to datab�ze, m��ete 
<a class="bez-slovniku" href="${URL.make("/slovnik/edit?action=add")}">vysv�tlit</a>
nov� pojem.</p>

<p>Pokud nenajdete n�kter� pojem v na�em slovn�ku a rozum�te anglicky, ur�it�
jej najdete ve <a href="http://en.wikipedia.org/wiki/Category:Computing">Wikipedii</a>
nebo <a href="http://www.acronymdictionary.co.uk">seznamu akronym�</a>. K dispozici
existuje i <a href="http://www.ucc.ie/cgi-bin/acronym">hled�n�</a> akronym�.
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
  <a href="/History?type=dictionary&amp;from=${FOUND.pageSize+1}&amp;count=25">Star�� pojmy</a>
 </p>
</#if>

<#include "../footer.ftl">
