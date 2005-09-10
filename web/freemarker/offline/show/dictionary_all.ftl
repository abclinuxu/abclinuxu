<#import "../macros.ftl" as lib>
<#include "../header.ftl">

<h1>V�kladov� slovn�k port�lu www.abclinuxu.cz</h1>

<p>V�kladov� slovn�k na�eho port�lu je projekt, jen� se sna�� �esk�m
a slovensk�m u�ivatel�m p�ibl�it Linux. M�lokter� nov��ek se za�ne
s t�mto opera�n�m syst�mem seznamovat tak, �e si o n�m nejd��ve p�e�te
kn�ku. Pokud nem� ��dn� zku�enosti s jin�mi opera�n�mi syst�my odvozen�mi
od Unixu, velmi rychle si p�ipad� ztracen�, proto�e se v�ude pou��vaj� pojmy
a slova, kter� nech�pe.</p>

<p>V�kladov� slovn�k je pokus jak tento probl�m zmen�it. Jak je na�im dobr�m zvykem,
jedn� se o otev�en� komunitn� projekt, do n�ho� m��e p�isp�t ka�d�. Jeho c�lem je popsat
v�echny z�kladn� pojmy, kter� se v Linuxu b�n� objevuj�.</p>

<p>Pokud n�kter� pojem nenajdete v na�em slovn�ku a rozum�te anglicky, ur�it�
jej najdete ve <a href="http://en.wikipedia.org/wiki/Category:Computing">Wikipedii</a>
nebo <a href="http://www.acronymdictionary.co.uk">seznamu akronym�</a>. K dispozici
existuje i <a href="http://www.ucc.ie/cgi-bin/acronym">hled�n�</a> akronym�.
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
