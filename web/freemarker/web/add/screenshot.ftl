<#include "../header.ftl">
<#import "../misc/hardware.ftl" as hwlib>

<@lib.showMessages/>

<p>
    Na této stránce můžete nahrát obrázek. Maximální velikost je omezena
    na 1&nbsp;MB, podporovány jsou formáty JPG, PNG a GIF (pro obrázky
    programů a desktopů je nejvhodnější formát PNG). Pokud obrázek
    přesahuje šířku či délku 200 pixelů, bude zároveň vytvořena a zobrazena
    jeho miniatura.
</p>

<@lib.addForm URL.make("/inset/"+RELATION.id), "", true>
    <@lib.addFile true, "screenshot", "Jméno souboru" />
    <@lib.addSubmit "Nahrát", "finish" />
    <@lib.addHidden "action", "addScreenshot2" />
</@lib.addForm>

<#include "../footer.ftl">
