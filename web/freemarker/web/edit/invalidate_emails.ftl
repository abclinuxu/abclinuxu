<#include "../header.ftl">

<@lib.showMessages/>

<h2>Invalidace e-mailových adres</h2>

<p>Tento formulář slouží pro zneplatnění e-mailu uživatelům.
Pokud se nám vrátí některý e-mail jako nedoručitelný,
zde uveďte číslo uživatele. Jedno číslo na jeden řádek.
Po odeslání bude e-mail těchto uživatelů označen jako
neplatný a příště jim nebude vygenerován žádný další
e-mail, dokud si sami nezmění adresu.</p>

<@lib.addForm URL.make("/EditUser")>
    <@lib.addTextArea true, "users", "Čísla uživatelů", 6 />
    <@lib.addSubmit "Pokračuj" />
    <@lib.addHidden "action", "invalidateEmail2" />
</@lib.addForm>

<a href="${URL.make("/SelectUser?sAction=form&amp;url=/EditUser&amp;action=invalidateEmail2"+TOOL.ticket(USER, false))}">Najdi uživatele</a>

<#include "../footer.ftl">
