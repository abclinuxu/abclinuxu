<#include "../header.ftl">

<@lib.showMessages/>

<h1>Osamostatnění diskusního vlákna</h1>

<p>Chystáte se přesunout vlákno z existující diskuse
a vytvořit z něj samostatnou diskusi. Zkontrolujte prosím
zařazení nové diskuse a případně ji přesuňte do lepšího fóra.
Zvláště dávejte pozor, pokud přesouváte vlákno z diskuse
ke článku, a ihned novou diskusi přesuňte do diskusního fóra.
Také byste měli opravit titulek/text generovaného komentáře.</p>

<form action="${URL.make("/EditDiscussion")}" method="POST">
 <input type="submit" value="Přesuň">
 <input type="hidden" name="action" value="toQuestion2">
 <input type="hidden" name="rid" value="${RELATION.id}">
 <input type="hidden" name="dizId" value="${PARAMS.dizId}">
 <input type="hidden" name="threadId" value="${PARAMS.threadId}">
</form>

<#include "../footer.ftl">

