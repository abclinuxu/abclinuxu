<#include "../header.ftl">

<@lib.showMessages/>

<h1>Produkty z 64bit.cz</h1>

<p>
    Na této stránce zadejte seznam adres produktů z eshopu 64bit.cz, jejichž cena se má automaticky
    načítat do databáze portálu. Zkopírujte z internetového prohlížeče celou URL adresu detailu
    zvoleného produktu. Každou adresu dávejte na samostatný řádek.
</p>

<@lib.addForm URL.noPrefix("/EditAdvertisement")>
    <@lib.addTextArea true, "urls", "Adresy", 30/>
    <@lib.addSubmit "Dokonči", "finish" />
    <@lib.addHidden "action", "set64Bit2" />
</@lib.addForm>

