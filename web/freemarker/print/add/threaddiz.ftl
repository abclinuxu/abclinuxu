<#include "../header.ftl">

<@lib.showMessages/>

<h1>Úvod</h1>

<p>Chystáte se pøesunout vlákno z existující diskuse
a vytvoøit z nìj samostatnou diskusi. Zkontrolujte prosím
zaøazení nové diskuse a pøípadnì ji pøesuòte do lep¹ího fóra.
Zvlá¹tì dávejte pozor, pokud pøesouváte vlákno z diskuse
ke èlánku a ihned novou diskusi pøesuòte do diskusního fóra.
Také byste mìli opravit titulek/text generovaného komentáøe.
</p>

<form action="${URL.make("/EditDiscussion")}" method="POST">
 <input type="submit" value="Pøesuò">
 <input type="hidden" name="action" value="toQuestion2">
 <input type="hidden" name="rid" value="${RELATION.id}">
 <input type="hidden" name="dizId" value="${PARAMS.dizId}">
 <input type="hidden" name="threadId" value="${PARAMS.threadId}">
</form>

<#include "../footer.ftl">

