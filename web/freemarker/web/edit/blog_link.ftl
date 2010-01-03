<#include "../header.ftl">

<@lib.showMessages/>

<#if PARAMS.url??>
    <h3>Upravit odkaz</h3>

    <@lib.addForm URL.make("/blog/edit/"+REL_BLOG.id)>
        <@lib.addInput true, "url", "URL" />
        <@lib.addInput true, "title", "Popis" />
        <@lib.addSubmit "Uložit", "finish" />
        <@lib.addHidden "position", PARAMS.position />
        <@lib.addHidden "action", "editLink2" />
    </@lib.addForm>
</#if>

<#include "../footer.ftl">
