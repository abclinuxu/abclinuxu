<#include "../header.ftl">

<@lib.showMessages/>

<h1 class="st_nadpis">Úvod</h1>

<p>Tato stránka slou¾í k upravení vzhledu va¹eho blogu. Mù¾ete
zde nastavit titulek celé stránky (hodnota znaèky HTML-HEAD-TITLE)
titulek a popis blogu èi poèet zobrazovaných zápisù na jedné stránce
archivu. Titulek blogu není název blogu (pou¾ívaný v URL), ten
nastavíte <a href="${URL.noPrefix("/blog/edit/"+REL_BLOG.id+"?action=rename")}">zde</a>.
</p>

<p>Popis blogu mù¾ete vyu¾ít napøíklad ke krátké informaci o své osobì,
pøidat obrázek, odkazy na své pøátelé nebo blogy, které ètete.
</p>

<form action="${URL.make("/blog/edit/"+REL_BLOG.id)}" method="POST">
 <table cellpadding="5">
  <tr>
   <td class="required">Titulek stránky
       <a class="info" href="#">?<span class="tooltip">Zde nastavíte titulek celé stránky</span></a>
   </td>
   <td>
    <input type="text" name="htitle" size="40" maxlength="70" value="${PARAMS.htitle?if_exists?html}">
    <div class="error">${ERRORS.htitle?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td>Titulek blogu
       <a class="info" href="#">?<span class="tooltip">Zde nastavíte titulek celé stránky</span></a>
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
   <td>Poèet zápisù
       <a class="info" href="#">?<span class="tooltip">Zde nastavíte, kolik zápisù se má zobrazovat na stránce</span></a>
   </td>
   <td>
    <input type="text" name="pageSize" size="40" maxlength="70" value="${PARAMS.pageSize?if_exists}">
    <div class="error">${ERRORS.pageSize?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td>&nbsp;</td>
   <td>
    <input type="submit" name="finish" value="Dokonèi">
   </td>
  </tr>
 </table>
 <input type="hidden" name="action" value="custom2">
</form>


<#include "../footer.ftl">
