<#include "../header.ftl">

<p>Zadejte emailov� adresy v�ech ��astn�k� rozhovoru.
Vypl�te tak� adresu moder�tora diskuse, bude odes�latem
v�ech email�.</p>

<form action="${URL.make("/edit/"+RELATION.id)}" method="POST">
    Adresa moder�tora: <input type="text" name="moderator" value="${PARAMS.moderator?if_exists}" size=40 tabindex=1><br>
    Adresy ��astn�k� (jedna na ��dek)<br>
    <textarea name="addresses" cols="80" rows="4" tabindex="2">${PARAMS.addresses?if_exists?html}</textarea>
    <div class="error">${ERRORS.moderator?if_exists}${ERRORS.addresses?if_exists}</div>
    <input type="hidden" name="action" value="talkEmails2">
    <input type="submit" value="Ulo�" tabindex="3">
</form>

<#include "../footer.ftl">
