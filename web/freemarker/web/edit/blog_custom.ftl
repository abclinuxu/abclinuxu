<#include "../header.ftl">

<@lib.showMessages/>

<h1 class="st_nadpis">�vod</h1>

<p>Tato str�nka slou�� k upraven� vzhledu va�eho blogu. M��ete
zde nastavit titulek cel� str�nky (hodnota zna�ky HTML-HEAD-TITLE)
a d�le obsah prav�ho sloupce. V tomto sloupci m��ete nastavit
titulek, popis blogu a po�et zobrazovan�ch z�pis� na jedn� str�nce
archivu.
</p>

<p>Popis blogu m��ete vyu��t nap��klad ke kr�tk� informaci o sv� osob�,
p�idat odkazy na sv� p��tel� nebo blogy, kter� �tete.
</p>

<form action="${URL.make("/blog/edit/"+REL_BLOG.id)}" method="POST">
 <table cellpadding="5">
  <tr>
   <td class="required">Titulek str�nky
       <a class="info" href="#">?<span class="tooltip">Zde nastav�te titulek cel� str�nky</span></a>
   </td>
   <td>
    <input type="text" name="htitle" size="40" maxlength="70" value="${PARAMS.htitle?if_exists?html}">
    <div class="error">${ERRORS.htitle?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td>Titulek blogu
       <a class="info" href="#">?<span class="tooltip">Zde nastav�te titulek cel� str�nky</span></a>
   </td>
   <td>
    <input type="text" name="title" size="40" maxlength="70" value="${PARAMS.title?if_exists?html}">
    <div class="error">${ERRORS.title?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td>Popis blogu</td>
   <td>
    <textarea name="intro" cols="60" rows="20">${PARAMS.intro?if_exists?html}</textarea>
    <div class="error">${ERRORS.intro?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td>Po�et z�pis�
       <a class="info" href="#">?<span class="tooltip">Zde nastav�te, kolik z�pis� se m� zobrazovat na str�nce</span></a>
   </td>
   <td>
    <input type="text" name="pageSize" size="40" maxlength="70" value="${PARAMS.pageSize?if_exists}">
    <div class="error">${ERRORS.pageSize?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td>&nbsp;</td>
   <td>
    <input type="submit" name="finish" value="Dokon�i">
   </td>
  </tr>
 </table>
 <input type="hidden" name="action" value="custom2">
</form>


<#include "../footer.ftl">
