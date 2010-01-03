<#include "../header.ftl">

<@lib.showMessages/>

<p>
    Na této stránce si můžete změnit své základní údaje.
    Pro vaši ochranu nejdříve zadejte současné heslo.
</p>

<p>
    Vaše jméno musí být nejméně pět znaků dlouhé. Email musí být platný.
    Nebojte se, budeme jej chránit před spammery a my vám budeme
    zasílat jen ty informace, které si sami objednáte.
</p>

<@lib.addForm URL.noPrefix("/EditUser")>
    <@lib.addPassword true, "PASSWORD", "Heslo" />
    <@lib.addInput true, "name", "Jméno", 24 />
    <@lib.addInput true, "nick", "Přezdívka", 24, "onChange=\"new Ajax.Updater('nickError', '/ajax/checkNick', {parameters: { value : $F('nick')}})\"" />
    <@lib.addInput false, "email", "E-mail", 24 />
    <@lib.addSubmit "Dokonči" />
    <@lib.addHidden "action", "editBasic2" />
    <@lib.addHidden "uid", MANAGED.id />
</@lib.addForm>

<#include "../footer.ftl">
