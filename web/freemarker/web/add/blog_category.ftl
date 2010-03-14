<#include "../header.ftl">

<@lib.showMessages/>

<p>Opravdu si přejete vytvořit následující kategorii?</p>

<@lib.addForm URL.make("/blog/edit/"+REL_BLOG.id), "name='form'">
    <@lib.addInput true, "category", "Kategorie", 20 />
    <@lib.addSubmit "Dokonči", "finish" />
    <@lib.addHidden "action", "addCategory2" />
</@lib.addForm>

<#include "../footer.ftl">
