<#include "../header.ftl">

<@lib.showMessages/>

<h1>Osamostatnění diskusního vlákna</h1>

<p>Chystáte se přesunout vlákno z existující diskuse
a vytvořit z něj samostatnou diskusi. Zkontrolujte prosím
zařazení nové diskuse a případně ji přesuňte do lepšího fóra.
Zvláště dávejte pozor, pokud přesouváte vlákno z diskuse
ke článku, a ihned novou diskusi přesuňte do diskusního fóra.
Také byste měli opravit titulek/text generovaného komentáře.</p>

<@lib.addForm URL.make("/EditDiscussion")>
    <@lib.addSubmit "Přesuň" />
    <@lib.addHidden "action", "toQuestion2" />
    <@lib.addHidden "rid", RELATION.id />
    <@lib.addHidden "dizId", PARAMS.dizId />
    <@lib.addHidden "threadId", PARAMS.threadId />
</@lib.addForm>

<#include "../footer.ftl">

