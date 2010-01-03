<#include "../header.ftl">

<@lib.showMessages/>

<h1>Registrace na akci</h1>

<p>
Registrací dáváte najevo svou účast na této akci.
Pokud jste přihlášen(a), formulář byl vyplněn údaji z vašeho profilu.
Na zadanou e-mailovou adresu vám v předstihu přijde upozornění, že se
blíží datum konání akce.
</p>

<@lib.addForm "/akce/edit">
    <@lib.addFormField false, "Akce">
        <a href="${RELATION.url!"/akce/"+RELATION.id}">${TOOL.childName(RELATION)}</a>
    </@lib.addFormField>

    <@lib.addInput true, "name", "Vaše jméno" />
    <@lib.addInput true, "email", "E-mail" />
    <@lib.addSubmit "Dokonči" />
    <@lib.addHidden "action", "register2" />
    <@lib.addHidden "rid", RELATION.id />
</@lib.addForm>

<#include "../footer.ftl">
