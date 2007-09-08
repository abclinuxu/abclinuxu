<#include "../header.ftl">

<@lib.showMessages/>

<h1>Kniha návštěv</h1>

<p>Tento formulář slouží pro vkládání zápisů do knihy návštěv.
Zde můžete psát své pocity či vzkazy administrátorům tohoto
portálu. Pokud chcete nahlásit chybu, přejděte na
<a href="${URL.noPrefix("/pozadavky")}">tuto</a> stránku.
</p>

<p>Do formuláře zadejte své jméno a váš vzkaz. Ve vzkazu jsou
zakázány všechny HTML značky kromě odkazu. Odstavce oddělujte
prázdným řádkem.
</p>

<form action="${URL.noPrefix("/kniha_navstev/edit")}" method="POST">
  <p>
   <span class="required">Vaše jméno</span><br>
   <input tabindex="1" type="text" name="name" size="40" value="${PARAMS.name?if_exists}">
   <div class="error">${ERRORS.name?if_exists}</div>

   <span class="required">Vzkaz</span><br>
   <textarea tabindex="2" name="message" cols="60" rows="10" tabindex="2">${PARAMS.message?if_exists?html}</textarea>
   <div class="error">${ERRORS.message?if_exists}</div>
  </p>
   <p>
       <input tabindex="3" type="submit" value="Dokonči">
       <input type="hidden" name="action" value="add2">
   </p>
</form>


<#include "../footer.ftl">
