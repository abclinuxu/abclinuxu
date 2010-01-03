<#include "../header.ftl">

<@lib.showMessages/>

<p>
    Zde můžete upravit související dokument. Povinným políčkem je adresa dokumentu.
    Políčko jméno musíte vyplnit pouze tehdy, vkládáte-li dokument, který se nenachází
    na tomto portále, nebo když chcete změnit jeho jméno. Popis nemusíte vůbec zadávat,
    používejte jej jen tehdy, urychlí-li to navigaci uživatele.
</p>

<@lib.addForm URL.make("/EditRelated/"+RELATION.id)>
    <@lib.addInput true, "url", "Adresa", 60 />
    <@lib.addInput false, "title", "Jméno", 40 />
    <@lib.addTextArea false, "description", "Popis", 4, "cols='40'" />
    <@lib.addSubmit "Dokonči" />
    <@lib.addHidden "document", PARAMS.document />
    <@lib.addHidden "action", "edit2" />
</@lib.addForm>

<#include "../footer.ftl">
