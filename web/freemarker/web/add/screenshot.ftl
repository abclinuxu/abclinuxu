<#include "../header.ftl">
<#import "../misc/hardware.ftl" as hwlib>

<@lib.showMessages/>

<p>
    Na této stránce mù¾ete nahrát obrázek. Maximální velikost je omezena
    na pùl megabajtu, podporovány jsou formáty JPG, PNG a GIF (pro obrázky
    programù je nejvhodnìj¹í formát PNG). Pokud obrázek
    pøesahuje ¹íøku èi délku 200 pixelù, bude zároveò vytvoøena a zobrazena
    jeho miniatura.
</p>

<form action="${URL.make("/inset/"+RELATION.id)}" method="POST" enctype="multipart/form-data">
    <p>
        <span class="required">Jméno souboru</span><br/>
        <input type="file" name="screenshot" size="40" tabindex="1">
        <div class="error">${ERRORS.screenshot?if_exists}</div>
        <input tabindex="2" type="submit" name="finish" value="Nahrát">
        <input type="hidden" name="action" value="addScreenshot2">
    </p>
</form>

<#include "../footer.ftl">
