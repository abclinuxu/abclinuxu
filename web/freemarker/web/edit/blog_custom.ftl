<#include "../header.ftl">

<@lib.showMessages/>

<h2>Úvod</h2>

<p>Tato stránka slouží k upravení vzhledu vašeho blogu. Můžete
zde nastavit titulek celé stránky (hodnota značky HTML&gt;HEAD&gt;TITLE)
titulek a popis blogu či počet zobrazovaných zápisů na jedné stránce
archivu. Titulek blogu není název blogu (používaný v URL), ten
nastavíte <a href="${URL.noPrefix("/blog/edit/"+REL_BLOG.id+"?action=rename")}">zde</a>.</p>

<p>Popis blogu můžete využít například ke krátké informaci o své osobě,
přidat obrázek, odkazy na své přátelé nebo blogy, které čtete.
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
   <td>Počet zápisů
       <a class="info" href="#">?<span class="tooltip">Zde nastavíte, kolik zápisů se má zobrazovat na stránce</span></a>
   </td>
   <td>
    <input type="text" name="pageSize" size="40" maxlength="70" value="${PARAMS.pageSize?if_exists}">
    <div class="error">${ERRORS.pageSize?if_exists}</div>
   </td>
  </tr>
  <tr>
   <td>&nbsp;</td>
   <td>
    <input type="submit" name="finish" value="Dokonči">
   </td>
  </tr>
 </table>
 <input type="hidden" name="action" value="custom2">
</form>


<#include "../footer.ftl">
