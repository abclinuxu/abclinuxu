<#include "../header.ftl">

<@lib.showMessages/>

<p>
    Na této stránce si můžete upravit své osobní údaje. Všechny údaje jsou volitelné a nemusíte je vyplňovat.
    Pro vaši ochranu nejdříve zadejte současné heslo.
</p>

<@lib.addForm URL.noPrefix("/EditUser")>
    <@lib.addPassword true, "PASSWORD", "Heslo" />
    <@lib.addSelect false, "sex", "Vaše pohlaví">
        <@lib.addOption "sex", "nezadávat", "undef" />
        <@lib.addOption "sex", "muž", "man" />
        <@lib.addOption "sex", "žena", "woman" />
    </@lib.addSelect>

    <@lib.addInput false, "birth", "Rok narození" />
    <@lib.addInput false, "city", "Bydliště" />
    <@lib.addInput false, "area", "Kraj" />
    <@lib.addInput false, "country", "Země" />

    <@lib.addSubmit "Dokonči" />
    <@lib.addHidden "action", "editPersonal2" />
    <@lib.addHidden "uid", MANAGED.id />
</@lib.addForm>

<#include "../footer.ftl">
