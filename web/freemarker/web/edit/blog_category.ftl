<#include "../header.ftl">

<@lib.showMessages/>

<p>Na této stránce můžete přejmenovat zvolenou kategorii.</p>

<@lib.addForm URL.make("/blog/edit/"+REL_BLOG.id), "name='form'">
    <@lib.addInput true, "category", "Název", 20 />
    <@lib.addSubmit "Dokonči", "finish" />
    <@lib.addHidden "action", "editCategory2" />
    <@lib.addHidden "cid", PARAMS.cid />
</@lib.addForm>

<#include "../footer.ftl">
