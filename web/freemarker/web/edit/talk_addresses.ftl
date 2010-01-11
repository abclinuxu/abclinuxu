<#include "../header.ftl">

<h2>Adresy účastníků rozhovoru</h2>

<p>Zadejte e-mailové adresy všech účastníků rozhovoru.
Vyplňte také adresu moderátora diskuse, bude odesílatem
všech e-mailů.</p>

<form action="${URL.make("/clanky/edit/"+RELATION.id)}" method="POST">
    Adresa moderátora: <input type="text" name="moderator" value="${PARAMS.moderator!}" size="40"><br>
    Adresy účastníků (jedna na řádek)<br>
    <textarea name="addresses" cols="80" rows="4">${PARAMS.addresses!?html}</textarea>
    <@lib.showError "moderator" />
    <@lib.showError "addresses" />
    <input type="hidden" name="action" value="talkEmails2">
    <input type="submit" value="Ulož">
</form>

<#include "../footer.ftl">
