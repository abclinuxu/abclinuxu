<#include "../header.ftl">

<@lib.showMessages/>

<@lib.addForm URL.make("/edit")>
    <@lib.addInput true, "name", "Název", 40 />
    <@lib.addTextArea true, "note", "Popis", 15>
        <@lib.addTextAreaEditor "note" />
    </@lib.addTextArea>
    <@lib.addTextArea true, "rules", "Pravidla", 15>
        <@lib.addTextAreaEditor "rules" />
    </@lib.addTextArea>
    <@lib.addSubmit "Dokonči" />
    <@lib.addHidden "action", "edit2" />
    <@lib.addHidden "rid", RELATION.id />
</@lib.addForm>

<#include "../footer.ftl">
