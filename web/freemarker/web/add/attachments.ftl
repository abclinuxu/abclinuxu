<#include "../header.ftl">

<h1>Upload příloh</h1>

<@lib.showMessages/>

<@lib.addForm URL.make("/inset/"+RELATION.id), "", true>
    <@lib.addFile false, "attachment", "Příloha" />
    <@lib.addFile false, "attachment", "Příloha" />
    <@lib.addFile false, "attachment", "Příloha" />

    <@lib.addSubmit "Nahrát" />
    <@lib.addHidden "action", "addFile2" />
</@lib.addForm>

<#include "../footer.ftl">
