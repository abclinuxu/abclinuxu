<#include "../header.ftl">

<@lib.showMessages/>

<h1>�vod</h1>

<p>Chyst�te se p�esunout vl�kno z existuj�c� diskuse
a vytvo�it z n�j samostatnou diskusi. Zkontrolujte pros�m
za�azen� nov� diskuse a p��padn� ji p�esu�te do lep��ho f�ra.
Zvl�t� d�vejte pozor, pokud p�esouv�te vl�kno z diskuse
ke �l�nku a ihned novou diskusi p�esu�te do diskusn�ho f�ra.
Tak� byste m�li opravit titulek/text generovan�ho koment��e.
</p>

<form action="${URL.make("/EditDiscussion")}" method="POST">
 <input type="submit" value="P�esu�">
 <input type="hidden" name="action" value="toQuestion2">
 <input type="hidden" name="rid" value="${RELATION.id}">
 <input type="hidden" name="dizId" value="${PARAMS.dizId}">
 <input type="hidden" name="threadId" value="${PARAMS.threadId}">
</form>

<#include "../footer.ftl">

