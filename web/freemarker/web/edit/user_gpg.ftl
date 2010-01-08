<#include "../header.ftl">

<@lib.showMessages/>

<p>
    Zde můžete nastavit svůj veřejný klíč GPG, takže lidé mohou
    ověřovat zprávy, které jim zašlete, a mohou vám posílat
    šifrované e-maily.
</p>

<@lib.addForm URL.noPrefix("/EditUser"), "", true>
    <@lib.addPassword true, "PASSWORD", "Heslo" />
    <@lib.addTextArea true, "key", "Veřejný klíč", 20, "cols='50'" />
    <@lib.addSubmit "Nastav GPG klíč" />
    <@lib.addHidden "action", "editGPG2" />
    <@lib.addHidden "uid", MANAGED.id />
</@lib.addForm>

<#include "../footer.ftl">
