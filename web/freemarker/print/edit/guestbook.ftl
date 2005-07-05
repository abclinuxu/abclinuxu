<#include "../header.ftl">

<@lib.showMessages/>

<h1>Kniha náv¹tìv</h1>

<p>Mo¾nost editace existujícího vzkazu v knize náv¹tìv. Pou¾ívat
jen ve vyjímeèných pøípadech.
</p>

<form action="${URL.noPrefix("/kniha_navstev/edit/"+RELATION.id)}" method="POST">
  <p>
   <span class="required">Jméno</span><br>
   <input tabindex="1" type="text" name="name" size="40" value="${PARAMS.name?if_exists}">
   <div class="error">${ERRORS.name?if_exists}</div>

   <span class="required">Vzkaz</span><br>
   <textarea tabindex="2" name="message" cols="60" rows="10" tabindex="2">${PARAMS.message?if_exists?html}</textarea>
   <div class="error">${ERRORS.message?if_exists}</div>
  </p>
   <p>
       <input tabindex="3" type="submit" value="Dokonèi">
       <input type="hidden" name="action" value="edit2">
   </p>
</form>


<#include "../footer.ftl">
