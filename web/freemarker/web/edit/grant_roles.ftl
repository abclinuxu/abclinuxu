<#include "../header.ftl">

<@lib.showMessages/>

<h2>Přiřazení práv uživateli</h2>

<@lib.addForm URL.noPrefix("/EditUser")>
    <@lib.addPassword true, "PASSWORD", "Vaše heslo", 16 />
    <@lib.addFormField true, "Uživatel">
       ${MANAGED.name}
    </@lib.addFormField>
    <@lib.addFormField true, "Jeho role">
        <@lib.showOption "roles", "root", "Root", "checkbox" /><br>
       <@lib.showOption "roles", "poll admin", "Administrátor anket", "checkbox" /><br>
       <@lib.showOption "roles", "survey admin", "Administrátor anket/průzkumů", "checkbox" /><br>
       <@lib.showOption "roles", "blog digest admin", "Administrátor blog digestu", "checkbox" /><br>
       <@lib.showOption "roles", "discussion admin", "Administrátor diskusí", "checkbox" /><br>
       <@lib.showOption "roles", "tag admin", "Administrátor štítků", "checkbox" /><br>
       <@lib.showOption "roles", "tip admin", "Administrátor tipů", "checkbox" /><br>
       <@lib.showOption "roles", "user admin", "Administrátor uživatelů", "checkbox" /><br>
       <@lib.showOption "roles", "news admin", "Administrátor zpráviček", "checkbox" /><br>
       <@lib.showOption "roles", "advertisement admin", "Administrátor reklamních pozic", "checkbox" /><br>
       <@lib.showOption "roles", "email invalidator", "Smí invalidovat emaily", "checkbox" /><br>
    </@lib.addFormField>

    <@lib.addSubmit "Dokonči" />
    <@lib.addHidden "uid", PARAMS.uid />
    <@lib.addHidden "action", "grant3" />
</@lib.addForm>

<#include "../footer.ftl">
