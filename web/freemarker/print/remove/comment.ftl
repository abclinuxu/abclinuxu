<#include "/include/macros.ftl">
<#include "../header.ftl">

<#call showMessages>

<p>Chyst�te se smazat n��e zobrazen� koment��. Jedn� se o naprosto
ojedin�lou akci, kterou byste m�l pou��vat jen ve v�jime�n�ch p��padech.
V drtiv� v�t�in� p��pad� byste m�li pou��t cenzuru. Pou��t jej m��ete
nap��klad na koment��e n�jak�ho ��lence (je�t�rci, paranoici),
zak�zanou reklamu a spamy a podobn�.</p>

<p>Tato funkce nen� rekurzivn�, tak�e ne� m��ete smazat n�kter� koment��,
mus�te smazat v�echny reakce, kter� na n�j odkazuj�.</p>


<#if THREAD?exists>
 <h1>N�hled p��sp�vku</h1>
 <#call showComment(THREAD 0 0 false)>
</#if>

<form action="${URL.make("/EditDiscussion")}" method="POST">
 <input type="submit" value="Smazat">
 <input type="hidden" name="action" value="rm2">
 <input type="hidden" name="rid" value="${PARAMS.rid}">
 <input type="hidden" name="dizId" value="${PARAMS.dizId}">
 <input type="hidden" name="threadId" value="${PARAMS.threadId}">
</form>


<#include "../footer.ftl">
