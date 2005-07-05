<#include "../header.ftl">

<@lib.showMessages/>

<h1>Kniha náv¹tìv</h1>

<p>Tento formuláø slou¾í pro vkládání zápisù do knihy náv¹tìv.
Zde mù¾ete psát své pocity èi vzkazy administrátorùm tohoto
portálu. Pokud chcete nahlásit chybu, pøejdìte na
<a href="${URL.noPrefix("/hardware/dir/3500")}">tuto</a> stránku.
</p>

<p>Do formuláøe zadejte své jméno a vá¹ vzkaz. Ve vzkazu jsou
zakázány v¹echny HTML znaèky kromì odkazu. Odstavce oddìlujte
prázdným øádkem.
</p>

<form action="${URL.noPrefix("/kniha_navstev/edit")}" method="POST">
  <p>
   <span class="required">Va¹e jméno</span><br>
   <input tabindex="1" type="text" name="name" size="40" value="${PARAMS.name?if_exists}">
   <div class="error">${ERRORS.name?if_exists}</div>

   <span class="required">Vzkaz</span><br>
   <textarea tabindex="2" name="message" cols="60" rows="10" tabindex="2">${PARAMS.message?if_exists?html}</textarea>
   <div class="error">${ERRORS.message?if_exists}</div>
  </p>
   <p>
       <input tabindex="3" type="submit" value="Dokonèi">
       <input type="hidden" name="action" value="add2">
   </p>
</form>


<#include "../footer.ftl">
