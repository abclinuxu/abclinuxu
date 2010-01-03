<#include "../header.ftl">

<@lib.showMessages/>

<p>
    Na této stránce si můžete změnit heslo. Pro vaši ochranu zadejte 
    dvakrát nové heslo. Heslo musí mít nejméně čtyři znaky.
</p>

<@lib.addForm URL.noPrefix("/EditUser")>
    <@lib.addPassword true, "password", "Nové heslo", 16 />
    <@lib.addPassword true, "password2", "Zopakujte nové heslo", 16 />
    <@lib.addSubmit "Dokonči" />
    <@lib.addHidden "token", PARAMS.token />
    <@lib.addHidden "action", "changeForgottenPassword2" />
    <@lib.addHidden "uid", MANAGED.id />
</@lib.addForm>

<#include "../footer.ftl">
