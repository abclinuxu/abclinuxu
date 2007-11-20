<#include "../header.ftl">
<#import "../misc/hardware.ftl" as hwlib>

<@lib.showMessages/>

<p>
    Na této stránce můžete nahrát obrázek. Maximální velikost je omezena
    na 750&nbsp;kB, podporovány jsou formáty JPG, PNG a GIF (pro obrázky
    programů a desktopů je nejvhodnější formát PNG). Pokud obrázek
    přesahuje šířku či délku 200 pixelů, bude zároveň vytvořena a zobrazena
    jeho miniatura.
</p>

<form action="${URL.make("/inset/"+RELATION.id)}" method="POST" enctype="multipart/form-data">
    <p>
        <span class="required">Jméno souboru</span><br/>
        <input type="file" name="screenshot" size="40" tabindex="1">
        <div class="error">${ERRORS.screenshot?if_exists}</div>
        <input tabindex="2" type="submit" name="finish" value="Nahrát" class="button">
        <input type="hidden" name="action" value="addScreenshot2">
    </p>
</form>

<#include "../footer.ftl">
