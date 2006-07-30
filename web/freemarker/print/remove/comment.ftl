<#include "../header.ftl">

<@lib.showMessages/>

<p>Chystáte se smazat ní¾e zobrazenı komentáø. Jedná se o naprosto
ojedinìlou akci, kterou byste mìl pou¾ívat jen ve vıjimeènıch pøípadech.
V drtivé vìt¹inì pøípadù byste mìli pou¾ít cenzuru. Pou¾ít jej mù¾ete
napøíklad na komentáøe nìjakého ¹ílence (je¹tírci, paranoici),
zakázanou reklamu a spamy a podobnì.</p>

<p>Tato funkce je rekurzivní! Sma¾e tedy kompletní vlákno
vèetnì v¹ech potomkù!</p>

<form action="${URL.make("/EditDiscussion")}" method="POST">
 <input type="submit" value="Smazat">
 <input type="hidden" name="action" value="rm2">
 <input type="hidden" name="rid" value="${RELATION.id}">
 <input type="hidden" name="dizId" value="${PARAMS.dizId}">
 <input type="hidden" name="threadId" value="${PARAMS.threadId}">
</form>

<#if THREAD?exists>
 <h1>Náhled pøíspìvku</h1>
 <@lib.showThread THREAD, 0, TOOL.createEmptyDiscussion(), false />
</#if>


<#include "../footer.ftl">
