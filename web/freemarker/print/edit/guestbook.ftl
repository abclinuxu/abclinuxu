<#include "../header.ftl">

<@lib.showMessages/>

<h1>Kniha n�v�t�v</h1>

<p>Mo�nost editace existuj�c�ho vzkazu v knize n�v�t�v. Pou��vat
jen ve vyj�me�n�ch p��padech.
</p>

<form action="${URL.noPrefix("/kniha_navstev/edit/"+RELATION.id)}" method="POST">
  <p>
   <span class="required">Jm�no</span><br>
   <input tabindex="1" type="text" name="name" size="40" value="${PARAMS.name?if_exists}">
   <div class="error">${ERRORS.name?if_exists}</div>

   <span class="required">Vzkaz</span><br>
   <textarea tabindex="2" name="message" cols="60" rows="10" tabindex="2">${PARAMS.message?if_exists?html}</textarea>
   <div class="error">${ERRORS.message?if_exists}</div>
  </p>
   <p>
       <input tabindex="3" type="submit" value="Dokon�i">
       <input type="hidden" name="action" value="edit2">
   </p>
</form>


<#include "../footer.ftl">
