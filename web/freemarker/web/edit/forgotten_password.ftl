<#include "../header.ftl">

<@lib.showMessages/>

<h1>Zapomenuté heslo</h1>

<p>
Zadejte uživatelské jméno, které jste na portálu používal.
Pokud existuje a má přiřazenou e-mailovou adresu, bude vám
zaslán odkaz, přes který si můžete nastavit heslo nové.
</p>

<@lib.addForm URL.noPrefix("/EditUser")>
    <@lib.addInput true, "login", "Login", 8 />
    <@lib.addSubmit "Odeslat" />
    <@lib.addHidden "action", "forgottenPassword2" />
</@lib.addForm>

<#include "../footer.ftl">

