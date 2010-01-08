<#include "../header.ftl">

<@lib.showMessages/>

<p>
    Na této stránce si můžete změnit heslo. Pro vaši ochranu nejdříve zadejte současné
    heslo a pak dvakrát nové heslo. Heslo musí mít nejméně čtyři znaky.
</p>

<@lib.addForm URL.noPrefix("/EditUser")>
    <@lib.addPassword true, "PASSWORD", "Současné heslo", 16 />
    <@lib.addPassword true, "password", "Nové heslo", 16 />
    <@lib.addPassword true, "password2", "Zopakujte nové heslo", 16 />
    <@lib.addSubmit "Dokonči" />
    <@lib.addHidden "action", "changePassword2" />
    <@lib.addHidden "uid", MANAGED.id />
</@lib.addForm>

<#include "../footer.ftl">
