<#include "../header.ftl">

<@lib.showMessages/>

<p>
    Na této stránce můžete nahrát obrázek. Maximální velikost je omezena
    na půl megabajtu, podporovány jsou formáty JPG, PNG a GIF (pro obrázky
    desktopů je nejvhodnější formát PNG).
</p>

<p>
  <form action="${URL.make("/desktopy/edit")}" method="POST" enctype="multipart/form-data">
  <span class="required">Titulek</span><br />
    <input type="text" name="name" value="${PARAMS.name?if_exists}" size="40" tabindex="1">
    <div class="error">${ERRORS.name?if_exists}</div><br />
  <span class="required">Obrázek</span><br />
    <input type="file" name="screenshot" size="40" tabindex="2">
    <div class="error">${ERRORS.screenshot?if_exists}</div><br />
  Popis<br />
    <textarea name="desc" class="siroka" rows="5" tabindex="3">${PARAMS.desc?if_exists?html}</textarea>
    <div class="error">${ERRORS.desc?if_exists}</div><br />
    <input type="submit" name="submit" value="Dokonči" tabindex="4" class="button">
    <input type="hidden" name="action" value="add2">
</form>
</p>

<#include "../footer.ftl">