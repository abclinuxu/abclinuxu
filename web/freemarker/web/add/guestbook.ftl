<#include "../header.ftl">

<@lib.showMessages/>

<h1>Kniha n�v�t�v</h1>

<p>Tento formul�� slou�� pro vkl�d�n� z�pis� do knihy n�v�t�v.
Zde m��ete ps�t sv� pocity �i vzkazy administr�tor�m tohoto
port�lu. Pokud chcete nahl�sit chybu, p�ejd�te na
<a href="${URL.noPrefix("/hardware/dir/3500")}">tuto</a> str�nku.
</p>

<p>Do formul��e zadejte sv� jm�no a v� vzkaz. Ve vzkazu jsou
zak�z�ny v�echny HTML zna�ky krom� odkazu. Odstavce odd�lujte
pr�zdn�m ��dkem.
</p>

<form action="${URL.noPrefix("/kniha_navstev/edit")}" method="POST">
  <p>
   <span class="required">Va�e jm�no</span><br>
   <input tabindex="1" type="text" name="name" size="40" value="${PARAMS.name?if_exists}">
   <div class="error">${ERRORS.name?if_exists}</div>

   <span class="required">Vzkaz</span><br>
   <textarea tabindex="2" name="message" cols="60" rows="10" tabindex="2">${PARAMS.message?if_exists?html}</textarea>
   <div class="error">${ERRORS.message?if_exists}</div>
  </p>
   <p>
       <input tabindex="3" type="submit" value="Dokon�i">
       <input type="hidden" name="action" value="add2">
   </p>
</form>


<#include "../footer.ftl">
