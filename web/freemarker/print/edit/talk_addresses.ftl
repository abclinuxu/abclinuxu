<#include "../header.ftl">

<p>Zadejte emailové adresy v¹ech úèastníkù rozhovoru.
Vyplòte také adresu moderátora diskuse, bude odesílatem
v¹ech emailù.</p>

<form action="${URL.make("/edit/"+RELATION.id)}" method="POST">
    Adresa moderátora: <input type="text" name="moderator" value="${PARAMS.moderator?if_exists}" size=40 tabindex=1><br>
    Adresy úèastníkù (jedna na øádek)<br>
    <textarea name="addresses" cols="80" rows="4" tabindex="2">${PARAMS.addresses?if_exists?html}</textarea>
    <div class="error">${ERRORS.moderator?if_exists}${ERRORS.addresses?if_exists}</div>
    <input type="hidden" name="action" value="talkEmails2">
    <input type="submit" value="Ulo¾" tabindex="3">
</form>

<#include "../footer.ftl">
